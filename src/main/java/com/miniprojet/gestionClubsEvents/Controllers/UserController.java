package com.miniprojet.gestionClubsEvents.Controllers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.miniprojet.gestionClubsEvents.DTO.UpdateProfileDTO;
import com.miniprojet.gestionClubsEvents.DTO.UserDTO;
import com.miniprojet.gestionClubsEvents.Model.User;
import com.miniprojet.gestionClubsEvents.Services.UserService;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

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
}