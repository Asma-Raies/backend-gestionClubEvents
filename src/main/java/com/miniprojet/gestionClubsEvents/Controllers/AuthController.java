package com.miniprojet.gestionClubsEvents.Controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import com.miniprojet.gestionClubsEvents.Config.CustomUserDetailsService;
import com.miniprojet.gestionClubsEvents.Config.JwtUtil;
import com.miniprojet.gestionClubsEvents.DTO.LoginDTO;
import com.miniprojet.gestionClubsEvents.DTO.UserDTO;
import com.miniprojet.gestionClubsEvents.Model.User;
import com.miniprojet.gestionClubsEvents.Services.UserService;

import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired 
    private UserService userService ; 

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Email ou mot de passe incorrect");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginDTO.getEmail());
        final String jwt = jwtUtil.generateToken(userDetails);

        User user = userService.findByEmail(loginDTO.getEmail());
        UserDTO userDTO = userService.mapToDTO(user);

        return ResponseEntity.ok(Map.of("token", jwt, "user", userDTO));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build(); 
        }
        return ResponseEntity.ok(userService.mapToDTO(currentUser));
    }

}