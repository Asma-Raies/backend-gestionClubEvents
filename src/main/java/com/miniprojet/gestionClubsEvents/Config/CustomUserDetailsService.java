package com.miniprojet.gestionClubsEvents.Config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.miniprojet.gestionClubsEvents.Model.Admin;
import com.miniprojet.gestionClubsEvents.Model.Moderateur;
import com.miniprojet.gestionClubsEvents.Model.User;
import com.miniprojet.gestionClubsEvents.Repository.UserRepository;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("Utilisateur non trouvé: " + email);
        }

        String role = "ROLE_";
        if (user instanceof Admin) {
            role += "ADMIN";
        } else if (user instanceof Moderateur) {
            role += "MODERATEUR";
        } else {
            role += "ETUDIANT";
        }

        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }
}