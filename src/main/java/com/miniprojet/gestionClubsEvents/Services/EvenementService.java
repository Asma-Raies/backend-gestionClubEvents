package com.miniprojet.gestionClubsEvents.Services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.miniprojet.gestionClubsEvents.DTO.ClubDTO;
import com.miniprojet.gestionClubsEvents.DTO.EvenementDTO;
import com.miniprojet.gestionClubsEvents.DTO.UserDTO;
import com.miniprojet.gestionClubsEvents.Model.Evenement;
import com.miniprojet.gestionClubsEvents.Model.User;
import com.miniprojet.gestionClubsEvents.Model.Admin;
import com.miniprojet.gestionClubsEvents.Model.Club;

import com.miniprojet.gestionClubsEvents.Repository.ClubRepository;
import com.miniprojet.gestionClubsEvents.Repository.EvenementRepository;
import com.miniprojet.gestionClubsEvents.Repository.UserRepository;

import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

@Service
@Transactional
public class EvenementService {

    @Autowired
    private EvenementRepository evenementRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private UserRepository userRepository;

    public Evenement create(EvenementDTO dto, String username) {

        User user = userRepository.findByEmail(username);
        if (user == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé");

        Club club = clubRepository.findById(dto.getClubId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Club non trouvé"));

        // Only moderator or admin can create
        boolean isAdmin = user instanceof Admin;
        if (club.getModerateur() == null ||
                (!club.getModerateur().getId().equals(user.getId()) && !isAdmin )) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous n'êtes pas autorisé à créer des événements pour ce club");
        }

        Evenement ev = new Evenement();
        ev.setTitre(dto.getTitre());
        ev.setDescription(dto.getDescription());
        ev.setDateEvenement(dto.getDateEvenement());
        ev.setHeure(dto.getHeure());
        ev.setLieu(dto.getLieu());
        ev.setEstPublic(dto.isEstPublic());
        ev.setEtat(dto.getEtat());
        ev.setClub(club);

        return evenementRepository.save(ev);
    }

    public EvenementDTO getEvenementById(Long id) {
        Evenement event = evenementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "evenement non trouvé"));
        return convertToDTO(event);
    }

    public Evenement update(Long id, EvenementDTO dto, String username) {

        Evenement ev = evenementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Événement non trouvé"));

        User user = userRepository.findByEmail(username);
        if (user == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé");

        boolean isAdmin = user instanceof Admin;
        boolean isModerateur = ev.getClub().getModerateur() != null
                               && ev.getClub().getModerateur().getId().equals(user.getId());

        if (!isAdmin && !isModerateur) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé");
        }

        // 🔥 FIX : permettre le changement de club
        if (dto.getClubId() != null) {
            Club club = clubRepository.findById(dto.getClubId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Club non trouvé"));
            ev.setClub(club);
        }

        // Mise à jour des champs
        ev.setTitre(dto.getTitre());
        ev.setDescription(dto.getDescription());
        ev.setDateEvenement(dto.getDateEvenement());
        ev.setHeure(dto.getHeure());
        ev.setLieu(dto.getLieu());
        ev.setEstPublic(dto.isEstPublic());
        ev.setEtat(dto.getEtat());

        return evenementRepository.save(ev);
    }

    // ---------------- DELETE ----------------
    public void delete(Long id, String username) {

        Evenement ev = evenementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Événement non trouvé"));

        User user = userRepository.findByEmail(username);

        boolean isAdmin = user instanceof Admin;
        boolean isModerateur = ev.getClub().getModerateur() != null
                               && ev.getClub().getModerateur().getId().equals(user.getId());

        if (!isAdmin && !isModerateur) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé");
        }


        evenementRepository.delete(ev);
    }

    // ---------------- GET ALL ----------------
    public List<EvenementDTO> getAll() {
        return evenementRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }
    private EvenementDTO convertToDTO(Evenement e) {
        EvenementDTO dto = new EvenementDTO();
        dto.setId(e.getId());
        dto.setTitre(e.getTitre());
        dto.setDescription(e.getDescription());
        dto.setDateEvenement(e.getDateEvenement());
        dto.setHeure(e.getHeure());
        dto.setLieu(e.getLieu());
        dto.setEstPublic(e.isEstPublic());
        dto.setEtat(e.getEtat());

        if (e.getClub() != null) {
            dto.setClubId(e.getClub().getId());
            dto.setClubNom(e.getClub().getNom());
        }

        dto.setNombreInscrits(
            e.getInscrits() != null ? e.getInscrits().size() : 0
        );
        dto.setInscrits(e.getInscrits().stream()
                .map(u -> new UserDTO(u.getId(), u.getNom(), u.getPrenom(), u.getEmail()))
                .sorted((a, b) -> (a.getNom() + a.getPrenom()).compareToIgnoreCase(b.getNom() + b.getPrenom()))
                .toList());

        return dto;
    }


    // ---------------- GET BY CLUB ----------------
    public List<Evenement> getByClub(Long clubId) {
        return evenementRepository.findByClubId(clubId);
    }

    // ---------------- INSCRIPTION ----------------
    public void inscrire(Long evenementId, String username) {

        User etudiant = userRepository.findByEmail(username);
        if (etudiant == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé");

        Evenement ev = evenementRepository.findById(evenementId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Événement non trouvé"));

        // If not public, must be member
        if (!ev.isEstPublic() && !etudiant.getClubs().contains(ev.getClub())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'êtes pas membre de ce club");
        }

        if (ev.getInscrits().contains(etudiant)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Déjà inscrit");
        }

        ev.getInscrits().add(etudiant);
        evenementRepository.save(ev);
    }

    // ---------------- DESINSCRIPTION ----------------
    public void desinscrire(Long evenementId, String username) {

        User etudiant = userRepository.findByEmail(username);
        if (etudiant == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé");

        Evenement ev = evenementRepository.findById(evenementId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Événement non trouvé"));

        ev.getInscrits().remove(etudiant);
        evenementRepository.save(ev);
    }
}