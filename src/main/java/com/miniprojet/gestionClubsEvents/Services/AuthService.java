package com.miniprojet.gestionClubsEvents.Services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.miniprojet.gestionClubsEvents.Config.JwtUtil;
import com.miniprojet.gestionClubsEvents.DTO.AuthDTO;
import com.miniprojet.gestionClubsEvents.DTO.LoginDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Authentifie l'utilisateur et retourne un token JWT
     */
    public AuthDTO login(LoginDTO loginDTO) {
        try {
            // 1. Vérifie les credentials
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginDTO.getEmail(),
                    loginDTO.getPassword()
                )
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Email ou mot de passe incorrect");
        }

        // 2. Charge les détails de l'utilisateur
        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginDTO.getEmail());

        // 3. Génère le JWT
        final String jwt = jwtUtil.generateToken(userDetails);

        // 4. Retourne la réponse
        return new AuthDTO(jwt);
    }
}