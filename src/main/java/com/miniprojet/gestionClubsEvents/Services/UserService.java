package com.miniprojet.gestionClubsEvents.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.miniprojet.gestionClubsEvents.Config.CustomUserDetails;
import com.miniprojet.gestionClubsEvents.DTO.UpdateProfileDTO;
import com.miniprojet.gestionClubsEvents.DTO.UserDTO;
import com.miniprojet.gestionClubsEvents.Model.Admin;
import com.miniprojet.gestionClubsEvents.Model.Club;
import com.miniprojet.gestionClubsEvents.Model.Etudiant;
import com.miniprojet.gestionClubsEvents.Model.Moderateur;
import com.miniprojet.gestionClubsEvents.Model.User;
import com.miniprojet.gestionClubsEvents.Repository.ClubRepository;
import com.miniprojet.gestionClubsEvents.Repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
    }
 
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            return null;
        }

        // Maintenant tu es sûr que c’est ton CustomUserDetails
        if (auth.getPrincipal() instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getUser();
        }

        // Fallback de sécurité (ne devrait jamais arriver)
        String email = auth.getName();
        return userRepository.findByEmail(email).orElse(null);
    }


   
    public UserDTO addUser(UserDTO userDTO, String role) {
        User user;

        switch (role.toLowerCase()) {
        case "admin":
            user = new Admin();
            break;
        case "moderateur":
            user = new Moderateur();
            break;
        case "etudiant":
            user = new Etudiant();
            break;
        default:
            throw new IllegalArgumentException("Rôle invalide : " + role);
    }


        user.setNom(userDTO.getNom());
        user.setPrenom(userDTO.getPrenom());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user = userRepository.save(user);
        return mapToDTO(user);
    }


    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Si c'est un modérateur, libérer son club
        if (user instanceof Moderateur) {
            Moderateur mod = (Moderateur) user;
            if (mod.getClub() != null) {
                Club club = mod.getClub();
                club.setModerateur(null);
                clubRepository.save(club);
            }
        }

        userRepository.delete(user);
    }

    // === Modifier un utilisateur (par Admin) ===
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        user.setNom(userDTO.getNom());
        user.setPrenom(userDTO.getPrenom());
        user.setEmail(userDTO.getEmail());

        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        user = userRepository.save(user);
        return mapToDTO(user);
    }
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, 
                    "Utilisateur non trouvé avec l'ID : " + id
                ));
    }

    // === Modifier son propre profil (nom, prénom, mot de passe) ===
    public UserDTO updateProfile(UpdateProfileDTO dto) {
        User currentUser = getCurrentUser();

        if (dto.getNom() != null) currentUser.setNom(dto.getNom());
        if (dto.getPrenom() != null) currentUser.setPrenom(dto.getPrenom());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            currentUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        currentUser = userRepository.save(currentUser);
        return mapToDTO(currentUser);
    }

    // === Assigner un utilisateur à un club (par Admin) ===
    public void assignUserToClub(Long userId, Long clubId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("Club non trouvé"));

        user.getClubs().add(club);
        userRepository.save(user);
    }

    // === Retirer un utilisateur d'un club ===
    public void removeUserFromClub(Long userId, Long clubId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("Club non trouvé"));

        user.getClubs().remove(club);
        userRepository.save(user);
    }

    // === Lister tous les utilisateurs ===
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    public List<UserDTO> getAvailableModerateurs() {
        return userRepository.findAvailableModerateurs().stream()
                .map(this::mapToDTO)
                .toList();
    }

    // Tous les modérateurs (même ceux déjà assignés)
    public List<UserDTO> getAllModerateurs() {
        return userRepository.findAllModerateurs().stream()
                .map(this::mapToDTO)
                .toList();
    }
    // === Mapper Entity → DTO ===
 // UserService.java → Remplace ta méthode mapToDTO par ÇA
    public UserDTO mapToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setNom(user.getNom());
        dto.setPrenom(user.getPrenom());
        dto.setEmail(user.getEmail());
        dto.setCreatedAt(user.getCreatedAt());

        // Rôle
        if (user instanceof Admin) {
            dto.setRole("ADMIN");
        } else if (user instanceof Moderateur) {
            dto.setRole("MODERATEUR");
            Moderateur mod = (Moderateur) user;
            if (mod.getClub() != null) {
                dto.setClubId(mod.getClub().getId());        // ← club du modérateur
                dto.setClubNom(mod.getClub().getNom());
            }
        } else {
            dto.setRole("ETUDIANT");
        }
        if (user instanceof Etudiant etu) {
            dto.setEnabled(etu.isEnabled());
        } else {
            dto.setEnabled(true); // pour Admin / Moderateur
        }
      
        dto.setClubIds(new HashSet<>()); // ou null si tu t'en fous pour l'instant

        return dto;
    }
    
    
}