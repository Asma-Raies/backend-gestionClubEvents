package com.miniprojet.gestionClubsEvents.Controllers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.miniprojet.gestionClubsEvents.DTO.UpdateProfileDTO;
import com.miniprojet.gestionClubsEvents.DTO.UserDTO;
import com.miniprojet.gestionClubsEvents.Model.Etudiant;
import com.miniprojet.gestionClubsEvents.Model.User;
import com.miniprojet.gestionClubsEvents.Repository.UserRepository;
import com.miniprojet.gestionClubsEvents.Services.UserService;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepo;
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> addUser(@RequestBody UserDTO userDTO, @RequestParam String role) {
        UserDTO created = userService.addUser(userDTO, role);
        return ResponseEntity.ok(created);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        UserDTO updated = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserDTO> updateProfile(@RequestBody UpdateProfileDTO dto) {
        UserDTO updated = userService.updateProfile(dto);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{userId}/club/{clubId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignToClub(@PathVariable Long userId, @PathVariable Long clubId) {
        userService.assignUserToClub(userId, clubId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getById(id);
        return ResponseEntity.ok(user);
    }
    @PatchMapping("/block/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATEUR')")
    public ResponseEntity<?> blockUser(@PathVariable Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!(user instanceof Etudiant)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seuls les étudiants peuvent être bloqués");
        }

        Etudiant etudiant = (Etudiant) user;
        etudiant.setBlocked(!etudiant.isBlocked());
        userRepo.save(etudiant);

        return ResponseEntity.ok(Map.of(
            "blocked", etudiant.isBlocked(),
            "message", etudiant.isBlocked() ? "Étudiant bloqué" : "Étudiant débloqué"
        ));
    }
}