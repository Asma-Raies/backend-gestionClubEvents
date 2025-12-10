package com.miniprojet.gestionClubsEvents.Config;


import org.hibernate.Hibernate;
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
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Cherche dans TOUTES les sous-classes
        Optional<User> userOpt = userRepository.findByEmail(email);

        User user = userOpt.orElseThrow(() -> 
            new UsernameNotFoundException("Utilisateur non trouvé : " + email));

        // Force le chargement complet (évite les proxies vides)
        Hibernate.initialize(user);

        return new CustomUserDetails(user);
    }

}