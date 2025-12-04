package com.miniprojet.gestionClubsEvents.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.miniprojet.gestionClubsEvents.DTO.UpdateProfileDTO;
import com.miniprojet.gestionClubsEvents.DTO.UserDTO;
import com.miniprojet.gestionClubsEvents.Repository.ClubRepository;
import com.miniprojet.gestionClubsEvents.Services.UserService;

import lombok.RequiredArgsConstructor;



@RestController
@RequestMapping("/api/moderateurs")
@RequiredArgsConstructor
public class ModerateurController {

    private final UserService userService;
    private final ClubRepository clubRepository; // pour vérifier si un user est déjà modérateur

    // Endpoint utilisé par la page AddClub
    @GetMapping("/available")
    public ResponseEntity<List<UserDTO>> getAvailableModerateurs() {
        return ResponseEntity.ok(userService.getAvailableModerateurs());
    }
    
}