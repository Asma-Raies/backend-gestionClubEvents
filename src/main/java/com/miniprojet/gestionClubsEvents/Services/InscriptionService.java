package com.miniprojet.gestionClubsEvents.Services;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.miniprojet.gestionClubsEvents.DTO.DemandeInscriptionClubDTO;
import com.miniprojet.gestionClubsEvents.DTO.PendingAccountDTO;
import com.miniprojet.gestionClubsEvents.Model.Club;
import com.miniprojet.gestionClubsEvents.Model.DemandeInscription;
import com.miniprojet.gestionClubsEvents.Model.Etudiant;
import com.miniprojet.gestionClubsEvents.Model.StatutDemande;
import com.miniprojet.gestionClubsEvents.Model.User;
import com.miniprojet.gestionClubsEvents.Repository.ClubRepository;
import com.miniprojet.gestionClubsEvents.Repository.DemandeInscriptionRepository;
import com.miniprojet.gestionClubsEvents.Repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class InscriptionService {

    private final DemandeInscriptionRepository demandeRepo;
    private final ClubRepository clubRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService; 

    public void saveDemandeClub(DemandeInscriptionClubDTO dto) {

        // Vérifier si l'utilisateur existe déjà
        Optional<User> existingUser = userRepo.findByEmail(dto.getEmail());

        Club club = clubRepo.findById(dto.getClubId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Club introuvable"));

        // Vérifier si cette personne a déjà fait une demande pour ce club
        if (demandeRepo.findByEmailAndClubId(dto.getEmail(), dto.getClubId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vous avez déjà fait une demande à ce club.");
        }

        DemandeInscription demande = new DemandeInscription();
        demande.setNom(dto.getNom());
        demande.setPrenom(dto.getPrenom());
        demande.setEmail(dto.getEmail());
        demande.setTelephone(dto.getTelephone());
        demande.setNiveau(dto.getNiveau());
        demande.setMotivation(dto.getMotivation());
        demande.setClub(club);
        demande.setStatut(StatutDemande.EN_ATTENTE);

        // Si utilisateur existe déjà → pas de création de compte plus tard
        if (existingUser.isPresent()) {
            demande.setCreatedUserId(existingUser.get().getId());
        }

        demandeRepo.save(demande);
    }


    // Approuver une demande → crée un compte Étudiant
    /*public void approuverDemande(Long demandeId, LocalDateTime dateEntretien) {

        DemandeInscription demande = demandeRepo.findById(demandeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Demande introuvable"));

        // Vérifier si déjà approuvée
        if (demande.getStatut() == StatutDemande.APPROUVEE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La demande est déjà approuvée");
        }

        // Vérifier la date d'entretien
        if (dateEntretien == null || dateEntretien.isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date d'entretien invalide");
        }

        // Mettre à jour la demande
        demande.setStatut(StatutDemande.APPROUVEE);
        demande.setDateEntretien(dateEntretien); // ⚠ tu dois ajouter ce champ dans ton entity
        demandeRepo.save(demande);

        // Email pour le visiteur
        String messageVisiteur = String.format(
                "Bonjour %s %s,\n\n" +
                "Félicitations ! Votre demande d'inscription au club '%s' a été approuvée.\n\n" +
                "Votre entretien est fixé pour : %s\n\n" +
                "Merci et à bientôt !",
                demande.getPrenom(),
                demande.getNom(),
                demande.getClub().getNom(),
                dateEntretien.toString()
        );

        emailService.sendSimpleEmail(
                demande.getEmail(),
                "Votre demande d'inscription est approuvée",
                messageVisiteur
        );

    

        }*/
    public void approuverDemande(Long demandeId, LocalDateTime dateEntretien) {

        DemandeInscription demande = demandeRepo.findById(demandeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Demande introuvable"));

        if (demande.getStatut() == StatutDemande.APPROUVEE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Déjà approuvée");
        }

        demande.setStatut(StatutDemande.APPROUVEE);
        demande.setDateEntretien(dateEntretien);

        User existing = null;

        // Vérifier si un utilisateur existe déjà
        if (demande.getCreatedUserId() != null) {
            existing = userRepo.findById(demande.getCreatedUserId()).orElse(null);
        }

        // Si aucun utilisateur → créer un compte
        if (existing == null) {
            String rawPassword = generateRandomPassword(10);

            Etudiant etudiant = new Etudiant();
            etudiant.setNom(demande.getNom());
            etudiant.setPrenom(demande.getPrenom());
            etudiant.setEmail(demande.getEmail());
            etudiant.setCreatedAt(LocalDateTime.now());
            etudiant.setPassword(passwordEncoder.encode(rawPassword));
            etudiant.setEnabled(false);

            etudiant = (Etudiant) userRepo.save(etudiant);

            demande.setCreatedUserId(etudiant.getId());

            // Envoyer mail avec mot de passe
            sendCreatedAccountEmail(demande, etudiant, rawPassword);

        } else {
            // CAS IMPORTANT : utilisateur existe déjà
            // Ici tu envoies seulement un email d'approbation SANS mot de passe
            sendExistingUserEmail(demande);
        }

        demandeRepo.save(demande);
    }
    private void sendExistingUserEmail(DemandeInscription demande) {
        String formattedDate = demande.getDateEntretien()
                .format(DateTimeFormatter.ofPattern("dd MMMM yyyy 'à' HH:mm", Locale.FRENCH));

        String body = String.format(
                "Bonjour %s %s,\n\n" +
                "Votre demande pour rejoindre le club '%s' a été approuvée.\n\n" +
                "Date d'entretien : %s\n\n" +
                "Votre compte existe déjà — vous pourrez accéder aux fonctionnalités du club\n" +
                "si votre entretien est validé.\n\n" +
                "Cordialement,\nL'équipe.",
                demande.getPrenom(),
                demande.getNom(),
                demande.getClub().getNom(),
                formattedDate
        );

        emailService.sendSimpleEmail(demande.getEmail(),
                "Approbation de votre demande - Entretien prévu",
                body);
    }
    private void sendCreatedAccountEmail(DemandeInscription demande, Etudiant etudiant, String rawPassword) {
        String formattedDate = demande.getDateEntretien()
                .format(DateTimeFormatter.ofPattern("dd MMMM yyyy 'à' HH:mm", Locale.FRENCH));

        String body = String.format(
                "Bonjour %s %s,\n\n" +
                "Votre demande pour rejoindre le club '%s' a été approuvée.\n\n" +
                "Date d'entretien prévue : %s\n\n" +
                "Un compte a été créé pour vous :\n" +
                "Email : %s\n" +
                "Mot de passe : %s\n\n" +
                "IMPORTANT : Vous ne pourrez pas vous connecter avant validation de votre entretien.\n\n" +
                "Cordialement,\nL'équipe.",
                demande.getPrenom(),
                demande.getNom(),
                demande.getClub().getNom(),
                formattedDate,
                etudiant.getEmail(),
                rawPassword
        );

        emailService.sendSimpleEmail(
                etudiant.getEmail(),
                "Votre compte a été créé - Entretien prévu",
                body
        );
    }

   /* public void approuverDemande(Long demandeId, LocalDateTime dateEntretien) {
        DemandeInscription demande = demandeRepo.findById(demandeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Demande introuvable"));

        if (demande.getStatut() == StatutDemande.APPROUVEE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La demande est déjà approuvée");
        }
        if (dateEntretien == null || dateEntretien.isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date d'entretien invalide");
        }

        // Mettre à jour la demande
        demande.setStatut(StatutDemande.APPROUVEE);
        demande.setDateEntretien(dateEntretien);
        demandeRepo.save(demande);

        // Créer le compte Etudiant (bloqué)
        String rawPassword = generateRandomPassword(10);
        Etudiant etudiant = new Etudiant();
        etudiant.setNom(demande.getNom());
        etudiant.setPrenom(demande.getPrenom());
        etudiant.setEmail(demande.getEmail());
        etudiant.setCreatedAt(LocalDateTime.now());
        etudiant.setPassword(passwordEncoder.encode(rawPassword));
        etudiant.setEnabled(false); 
        etudiant = (Etudiant) userRepo.save(etudiant);

       
        demande.setCreatedUserId(etudiant.getId()); 
        demandeRepo.save(demande);

   
        String formattedDate = dateEntretien.format(DateTimeFormatter.ofPattern("dd MMMM yyyy 'à' HH:mm", Locale.FRENCH));
        String emailBody = String.format(
            "Bonjour %s %s,\n\n" +
            "Votre demande pour rejoindre le club '%s' a été approuvée.\n\n" +
            "Date d'entretien prévue : %s\n\n" +
            "Un compte a été créé pour vous avec les identifiants suivants :\n" +
            "Adresse e-mail : %s\n" +
            "Mot de passe : %s\n\n" +
            "IMPORTANT : Vous **ne pourrez pas** vous connecter avant que le modérateur n'accepte votre compte après l'entretien.\n\n" +
            "Cordialement,\nL'équipe",
            demande.getPrenom(),
            demande.getNom(),
            demande.getClub().getNom(),
            formattedDate,
            etudiant.getEmail(),
            rawPassword
        );

        emailService.sendSimpleEmail(demande.getEmail(), "Approbation de votre demande - entretien & compte créé", emailBody);
    }*/
    public List<Etudiant> getPendingCreatedAccountsForModerator(User moderateur) {
        Club club = clubRepo.findByModerateurId(moderateur.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vous n'avez pas de club"));
        // Trouver les demandes approuvées pour ce club et avec createdUserId non null et user.enabled = false
        List<DemandeInscription> demandes = demandeRepo.findByClubIdAndStatut(club.getId(), StatutDemande.APPROUVEE);
        // Map to users
        return demandes.stream()
                .map(d -> {
                    if (d.getCreatedUserId() == null) return null;
                    return (Etudiant) userRepo.findById(d.getCreatedUserId()).orElse(null);
                })
                .filter(Objects::nonNull)
                .filter(u -> !u.isEnabled()) // en attente d'activation
                .collect(Collectors.toList());
    }

    public void moderatorAcceptAccount(Long userId, User moderator) {
        Club club = clubRepo.findByModerateurId(moderator.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vous n'avez pas de club"));
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        if (!(user instanceof Etudiant)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Utilisateur n'est pas un étudiant");
        }
        Etudiant etudiant = (Etudiant) user;
        etudiant.setEnabled(true);
        etudiant.getClubs().add(club);
        userRepo.save(etudiant);

        Optional<DemandeInscription> opt = demandeRepo.findByEmailAndClubId(user.getEmail(), club.getId());
        opt.ifPresent(d -> {
            d.setStatut(StatutDemande.ACCEPTEE_FINAL);
            demandeRepo.save(d);
        });

        String body = String.format(
            "Bonjour %s,\n\nVotre compte a été activé par le modérateur du club '%s'. Vous pouvez maintenant vous connecter.\n\nCordialement.",
            etudiant.getPrenom(), club.getNom());
        emailService.sendSimpleEmail(etudiant.getEmail(), "Compte activé - vous pouvez maintenant vous connecter", body);
    }


    public void moderatorRejectAccount(Long userId, Long clubId, User moderator) {
        Club club = clubRepo.findById(clubId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Club introuvable"));
        if (!club.getModerateur().getId().equals(moderator.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ce n'est pas votre club");
        }
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        if (!(user instanceof Etudiant)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Utilisateur n'est pas un étudiant");
        }
   
        userRepo.delete(user);

      
        Optional<DemandeInscription> opt = demandeRepo.findByEmailAndClubId(user.getEmail(), clubId);
        opt.ifPresent(d -> {
            d.setStatut(StatutDemande.REFUSE_FINAL);
            demandeRepo.save(d);
        });

        // Notifier candidat si tu veux (email)
        emailService.sendSimpleEmail(user.getEmail(), "Décision finale – demande refusée", 
            "Bonjour,\n\nAprès entretien, votre demande a été refusée. Merci pour votre intérêt.\n\nCordialement.");
    }
    public List<DemandeInscription> getDemandesEnAttente() {
        return demandeRepo.findByStatut(StatutDemande.EN_ATTENTE);
    }


    public List<DemandeInscription> getDemandesEnAttentePourMonClub(User moderateur) {
        // Récupère le club du modérateur
        Club club = clubRepo.findByModerateurId(moderateur.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vous n'avez pas de club"));

        return demandeRepo.findByClubIdAndStatut(club.getId(), StatutDemande.EN_ATTENTE);
    }
    public void approuverDemandeMonClub(Long demandeId, LocalDateTime dateEntretien, User moderator) {
        DemandeInscription demande = demandeRepo.findById(demandeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // Vérifie que c'est bien son club
        if (!demande.getClub().getModerateur().getId().equals(moderator.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ce n'est pas votre club");
        }

        if (demande.getStatut() == StatutDemande.APPROUVEE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Déjà approuvée");
        }

        if (dateEntretien == null || dateEntretien.isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date invalide");
        }

        demande.setStatut(StatutDemande.APPROUVEE);
        demande.setDateEntretien(dateEntretien);
        demandeRepo.save(demande);

        // Email au candidat
        String message = String.format(
            "Bonjour %s %s,\n\n" +
            "Félicitations ! Votre demande pour rejoindre le club '%s' a été approuvée par le modérateur.\n\n" +
            "Votre entretien est prévu le : %s\n\n" +
            "À très bientôt !",
            demande.getPrenom(),
            demande.getNom(),
            demande.getClub().getNom(),
            dateEntretien.format(DateTimeFormatter.ofPattern("dd MMMM yyyy à HH:mm", Locale.FRENCH))
        );

        emailService.sendSimpleEmail(demande.getEmail(), "Demande approuvée !", message);
    }
    private String generateRandomPassword(int length) {
        // SecureRandom + caractères alphanumériques simples
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%&*!";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
 // InscriptionService.java → AJOUTE CETTE MÉTHODE
    public List<PendingAccountDTO> getPendingAccountsForMyClub(User moderator) {
        Club club = clubRepo.findByModerateurId(moderator.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Votre club n'existe pas"));

        return demandeRepo.findByClubIdAndStatut(club.getId(), StatutDemande.APPROUVEE)
                .stream()
                .filter(d -> d.getCreatedUserId() != null)
                .map(d -> {
                    User user = userRepo.findById(d.getCreatedUserId()).orElse(null);
                    if (user instanceof Etudiant etudiant && !etudiant.isEnabled()) {
                        return new PendingAccountDTO(
                            etudiant.getId(),
                            etudiant.getPrenom(),
                            etudiant.getNom(),
                            etudiant.getEmail(),
                            etudiant.getTelephone(),
                            etudiant.getNiveau(),
                            d.getDateEntretien(),
                            d.getId()
                        );
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
   /* private String generateRandomPassword() {
        return RandomStringUtils.randomAlphanumeric(10);
    }*/
}