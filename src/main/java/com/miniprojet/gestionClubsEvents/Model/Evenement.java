package com.miniprojet.gestionClubsEvents.Model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
@Entity
@Table(name = "evenements")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
public class Evenement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private String description;
    private LocalDate dateEvenement;
    private LocalTime heure;
    private String lieu;

    private boolean estPublic = false;

    @Enumerated(EnumType.STRING)
    private EtatEvenement etat = EtatEvenement.A_VENIR;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    @JsonIgnore  // event → club → events
    private Club club;

    @ManyToMany
    @JoinTable(
        name = "inscription_evenement",
        joinColumns = @JoinColumn(name = "evenement_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore // event → users → events
    private Set<User> inscrits = new HashSet<>();
}

