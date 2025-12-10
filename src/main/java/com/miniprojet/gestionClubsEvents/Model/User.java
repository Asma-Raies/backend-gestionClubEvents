package com.miniprojet.gestionClubsEvents.Model;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter @Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String telephone;
    private String niveau;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

   
    @ManyToMany(mappedBy = "members")
    @JsonIgnore  
    private Set<Club> clubs = new HashSet<>();

    // Événements inscrits
    @ManyToMany(mappedBy = "inscrits")
    private Set<Evenement> evenementsInscrits = new HashSet<>();
}