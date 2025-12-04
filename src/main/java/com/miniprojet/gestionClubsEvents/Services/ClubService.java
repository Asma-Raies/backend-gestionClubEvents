package com.miniprojet.gestionClubsEvents.Services;

import com.miniprojet.gestionClubsEvents.DTO.ClubDTO;
import com.miniprojet.gestionClubsEvents.DTO.EvenementDTO;
import com.miniprojet.gestionClubsEvents.DTO.UserDTO;
import com.miniprojet.gestionClubsEvents.Model.Admin;
import com.miniprojet.gestionClubsEvents.Model.Club;
import com.miniprojet.gestionClubsEvents.Model.EtatEvenement;
import com.miniprojet.gestionClubsEvents.Model.Moderateur;
import com.miniprojet.gestionClubsEvents.Model.User;
import com.miniprojet.gestionClubsEvents.Repository.ClubRepository;
import com.miniprojet.gestionClubsEvents.Repository.ModerateurRepository;
import com.miniprojet.gestionClubsEvents.Repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClubService {

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private ModerateurRepository moderateurRepository;

    @Autowired
    private UserRepository userRepository;

    // Mapper Entity → DTO
    private ClubDTO toDTO(Club club) {
        ClubDTO dto = new ClubDTO();
        dto.setId(club.getId());
        dto.setNom(club.getNom());
        dto.setDescription(club.getDescription());
        dto.setPathUrl(club.getLogoPath());
        dto.setIsActive(club.getIsActive());
        dto.setFoundedDate(club.getFoundedDate());
        dto.setCategory(club.getCategory());
        if (club.getModerateur() != null) {
            dto.setModerateurId(club.getModerateur().getId());
            dto.setModerateurNom(club.getModerateur().getNom()); // getNom() vient de User
        }
        return dto;
    }

    // Mapper DTO → Entity
    public  Club toEntity(ClubDTO dto) {
        Club club = new Club();
        club.setId(dto.getId());
        club.setNom(dto.getNom());
        club.setDescription(dto.getDescription());
        club.setLogoPath(dto.getPathUrl());
        club.setCategory(dto.getCategory());
        club.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        club.setFoundedDate(dto.getFoundedDate());
        
        if (dto.getModerateurId() != null) {
            Moderateur moderateur = moderateurRepository.findById(dto.getModerateurId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Modérateur non trouvé"));
            club.setModerateur(moderateur);
        }
        return club;
    }

    // === CRUD ===

    public List<ClubDTO> getAllClubs() {
        return clubRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ClubDTO getClubById(Long id) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Club non trouvé"));
        return toDTO(club);
    }


    public ClubDTO addClub(ClubDTO clubDTO) {
    	
    	
    	
        Club club = toEntity(clubDTO);

        if (clubDTO.getModerateurId() != null) {
            User user = userRepository.findById(clubDTO.getModerateurId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

            if (!(user instanceof Moderateur)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'utilisateur sélectionné n'est pas un modérateur");
            }

            Moderateur moderateur = (Moderateur) user;

            // Vérifier qu’il n’a pas déjà un club
            if (moderateur.getClub() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ce modérateur est déjà responsable d'un autre club");
            }

            club.setModerateur(moderateur);
        }

        // 4. Sauvegarder le club
        club = clubRepository.save(club);

        // 5. Retourner le DTO
        return toDTO(club);
    }


    public ClubDTO updateClub(Long id, ClubDTO clubDTO) {
        Club existing = clubRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Club non trouvé"));

        // Mise à jour des champs simples
        existing.setNom(clubDTO.getNom().trim());
        existing.setDescription(clubDTO.getDescription());
        existing.setCategory(clubDTO.getCategory());
        existing.setIsActive(clubDTO.getIsActive() != null ? clubDTO.getIsActive() : true);

       
        // Logo (peut être mis à jour ou rester l'ancien)
        existing.setLogoPath(clubDTO.getPathUrl());

        // Gestion du modérateur
        if (clubDTO.getModerateurId() != null) {
            User user = userRepository.findById(clubDTO.getModerateurId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Modérateur introuvable"));

            if (!(user instanceof Moderateur)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cet utilisateur n'est pas un modérateur");
            }

            Moderateur moderateur = (Moderateur) user;

            // Si le modérateur avait déjà un club → on le libère
            if (moderateur.getClub() != null && !moderateur.getClub().getId().equals(id)) {
                moderateur.setClub(null);
                userRepository.save(moderateur);
            }

            existing.setModerateur(moderateur);
        }
        // Si moderateurId == null → on ne touche pas au modérateur actuel

        existing = clubRepository.save(existing);
        return toDTO(existing);
    }
    public List<Club> getClubsByModerateur(User user) {
        if (user == null) {
            throw new RuntimeException("Authenticated user not found");
        }

        boolean isAdmin = user instanceof Admin;

        if (isAdmin) {
            return clubRepository.findAll();
        }
        return clubRepository.findByModerateurId(user.getId());
    }

    public ClubDTO getClubDetails(Long id) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        ClubDTO dto = new ClubDTO();
        dto.setId(club.getId());
        dto.setNom(club.getNom());
        dto.setDescription(club.getDescription());
        dto.setCategory(club.getCategory());
        dto.setPathUrl(club.getLogoPath());
        dto.setFoundedDate(club.getFoundedDate());
        dto.setIsActive(club.getIsActive());

        if (club.getModerateur() != null) {
            dto.setModerateurId(club.getModerateur().getId());
            dto.setModerateurNom(club.getModerateur().getNom());
            dto.setModerateurPrenom(club.getModerateur().getPrenom());
            dto.setModerateurEmail(club.getModerateur().getEmail());
        }

        // Statistiques
        dto.setMembresCount(club.getMembers().size());
        dto.setEvenementsCount(club.getEvenements().size());
        dto.setEvenementsAVenirCount((int) club.getEvenements().stream()
            .filter(e -> e.getEtat() == EtatEvenement.A_VENIR)
            .count());

        // Liste des membres (triés par nom)
        dto.setMembres(club.getMembers().stream()
            .map(u -> new UserDTO(u.getId(), u.getNom(), u.getPrenom(), u.getEmail()))
            .sorted((a, b) -> (a.getNom() + a.getPrenom()).compareToIgnoreCase(b.getNom() + b.getPrenom()))
            .toList());

        // Liste des événements (triés par date)
        dto.setEvenements(club.getEvenements().stream()
            .map(e -> new EvenementDTO(
                e.getId(),
                e.getTitre(),
                e.getDateEvenement(),
                e.getHeure(),
                e.getEtat().name(),
                e.getInscrits().size()
            ))
            .sorted((a, b) -> b.getDateEvenement().compareTo(a.getDateEvenement())) // plus récent en haut
            .toList());

        return dto;
    }
    public void deleteClub(Long id) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Club non trouvé"));

       
        if (club.getModerateur() != null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Impossible de supprimer le club \"" + club.getNom() + "\" car un modérateur y est assigné."
            );
        }

        // Vérifie s'il y a des membres
        if (club.getMembers() != null && !club.getMembers().isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Impossible de supprimer le club \"" + club.getNom() + "\" car il contient " 
                + club.getMembers().size() + " membre(s)."
            );
        }

        // Si aucun problème → on supprime
        clubRepository.delete(club);
    }
 // Pour autoriser le modérateur à modifier son propre club
    public boolean isModeratorOfClub(Long clubId, String email) {
        return clubRepository.findById(clubId)
                .map(club -> {
                    Moderateur mod = club.getModerateur();
                    return mod != null && mod.getEmail().equals(email);
                })
                .orElse(false);
    }
    // === Assignation Modérateur ===
    @Transactional
    public ClubDTO assignModerateur(Long clubId, Long moderateurId) {
        Club club = clubRepository.findById(clubId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Club non trouvé"));

        // On récupère l'utilisateur complet (pas juste Moderateur)
        User user = userRepository.findById(moderateurId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));

        // On vérifie que c’est bien une instance de Moderateur
        if (!(user instanceof Moderateur)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Cet utilisateur n'est pas un modérateur");
        }

        club.setModerateur((Moderateur) user);
        club = clubRepository.save(club);
        return toDTO(club);
    }
}