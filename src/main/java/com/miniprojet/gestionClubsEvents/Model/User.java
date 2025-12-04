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
@Data
@Getter 
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;
    private String email;
    private String password;
    private LocalDateTime createdAt;
    @ManyToMany
    @JoinTable(
        name = "user_club",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "club_id")
    )
    @JsonIgnore   // avoid returning clubs
    private Set<Club> clubs = new HashSet<>();

    @ManyToMany(mappedBy = "inscrits")
    @JsonIgnore   // avoid recursive user → event → user
    private Set<Evenement> evenementsInscrits = new HashSet<>();
}
