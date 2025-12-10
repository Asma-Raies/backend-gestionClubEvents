package com.miniprojet.gestionClubsEvents.Model;

import java.time.LocalDateTime;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "demandes_inscription")
public class DemandeInscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String niveau;
    private String motivation;
    private LocalDateTime dateEntretien;
    private Long createdUserId ;
    @Enumerated(EnumType.STRING)
    private StatutDemande statut = StatutDemande.EN_ATTENTE;

    @CreationTimestamp
    private LocalDateTime dateDemande;

    @ManyToOne
    @JoinColumn(name = "club_id")
    private Club club;
}