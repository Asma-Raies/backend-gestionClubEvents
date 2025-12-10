package com.miniprojet.gestionClubsEvents.Controllers;




import com.miniprojet.gestionClubsEvents.Config.CustomUserDetails;
import com.miniprojet.gestionClubsEvents.DTO.ClubDTO;
import com.miniprojet.gestionClubsEvents.DTO.UserDTO;
import com.miniprojet.gestionClubsEvents.Model.Club;
import com.miniprojet.gestionClubsEvents.Model.User;
import com.miniprojet.gestionClubsEvents.Repository.UserRepository;
import com.miniprojet.gestionClubsEvents.Services.ClubService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;
@CrossOrigin(origins = "http://localhost:3000")

@RestController
@RequestMapping("/api/clubs")
public class ClubController {

    @Autowired
    private ClubService clubService;
    @Autowired
    private UserRepository userRepository;

  
    @GetMapping
    public ResponseEntity<List<ClubDTO>> getAllClubs() {
        List<ClubDTO> clubs = clubService.getAllClubs();
        return ResponseEntity.ok(clubs);
    }

 
    @GetMapping("/{id}")
    public ResponseEntity<ClubDTO> getClubById(@PathVariable Long id) {
        ClubDTO clubDTO = clubService.getClubById(id);
        return ResponseEntity.ok(clubDTO);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClubDTO> addClub(
            @RequestPart("club") ClubDTO clubDTO,
            @RequestPart(value = "logo", required = false) MultipartFile logo) {

        // Gestion sécurisée du logo
        if (logo != null && !logo.isEmpty()) {
            try {
               
            	Path uploadDir = Paths.get(System.getProperty("user.home"), "gestion-clubs-uploads");

               
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                    System.out.println("Dossier 'uploads' créé : " + uploadDir.toAbsolutePath());
                }

               
                String originalName = logo.getOriginalFilename();
                String extension = "";
                if (originalName != null && originalName.contains(".")) {
                    extension = originalName.substring(originalName.lastIndexOf("."));
                }
                String fileName = System.currentTimeMillis() + extension;

                Path filePath = uploadDir.resolve(fileName);

             
                logo.transferTo(filePath.toFile());

                
                clubDTO.setPathUrl("/uploads/" + fileName);

            } catch (Exception e) {
                e.printStackTrace();
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Impossible d'enregistrer le logo : " + e.getMessage());
            }
        }

        // Création du club
        ClubDTO created = clubService.addClub(clubDTO);
        return ResponseEntity.ok(created);
    }


    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClubDTO> updateClub(
            @PathVariable Long id,
            @RequestPart("club") ClubDTO clubDTO,
            @RequestPart(value = "logo", required = false) MultipartFile logo) {

       
        if (logo != null && !logo.isEmpty()) {
            try {
                Path uploadDir = Paths.get(System.getProperty("user.home"), "gestion-clubs-uploads");
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                String fileName = System.currentTimeMillis() + "_" + logo.getOriginalFilename();
                Path filePath = uploadDir.resolve(fileName);

                logo.transferTo(filePath.toFile());

                // On met à jour le chemin dans le DTO avant de l'envoyer au service
                clubDTO.setPathUrl("/uploads/" + fileName);

            } catch (Exception e) {
                e.printStackTrace();
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Impossible d'enregistrer le nouveau logo");
            }
        }
        // Si pas de nouveau logo → on garde l'ancien (pathUrl reste inchangé ou null)

        ClubDTO updated = clubService.updateClub(id, clubDTO);
        return ResponseEntity.ok(updated);
    }

    // === DELETE club ===
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClub(@PathVariable Long id) {
        clubService.deleteClub(id);
        return ResponseEntity.noContent().build();
    }

    // === Assign moderator to club ===
    @PostMapping("/{clubId}/assign-moderateur/{moderateurId}")
    public ResponseEntity<ClubDTO> assignModerateur(
            @PathVariable Long clubId,
            @PathVariable Long moderateurId) {
        ClubDTO updatedClub = clubService.assignModerateur(clubId, moderateurId);
        return ResponseEntity.ok(updatedClub);
    }
    
    @GetMapping("/{id}/details")
    public ResponseEntity<ClubDTO> getClubDetails(@PathVariable Long id) {
        ClubDTO details = clubService.getClubDetails(id);
        return ResponseEntity.ok(details);
    }
    @GetMapping("/mes-clubs")
    public List<Club> getMyClubs(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));
        return clubService.getClubsByModerateur(user);
    }

    @PreAuthorize("hasRole('MODERATEUR')")
    @GetMapping("/my")
    public ResponseEntity<ClubDTO> getMyClub(@AuthenticationPrincipal CustomUserDetails customUser) {
        User moderator = customUser.getUser();
        Club club = clubService.getMyClub(moderator);
        return ResponseEntity.ok(clubService.ConverttoDTO(club));
    }

    @PreAuthorize("hasRole('MODERATEUR')")
    @PutMapping("/my/{clubId}")
    public ResponseEntity<ClubDTO> updateMyClub(
            @PathVariable Long clubId,
            @RequestBody ClubDTO dto,
            @AuthenticationPrincipal CustomUserDetails customUser) {

        User moderator = customUser.getUser();
        ClubDTO updated = clubService.updateMyClub(clubId, dto, moderator);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('MODERATEUR')")
    @GetMapping("/my/members")
    public ResponseEntity<List<UserDTO>> getMyClubMembers(@AuthenticationPrincipal CustomUserDetails customUser) {
        User moderator = customUser.getUser();
        Club club = clubService.getMyClub(moderator);
        List<UserDTO> members = clubService.getMembresClub(club.getId(), moderator);
        return ResponseEntity.ok(members);
    }
    
}
