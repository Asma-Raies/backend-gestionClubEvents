package com.miniprojet.gestionClubsEvents.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.miniprojet.gestionClubsEvents.Model.Categorie;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter
public class ClubDTO {
    private Long id;
    private String nom;
    private String description;
    private Long moderateurId;
    private String moderateurNom;
    private String moderateurPrenom;
    private String moderateurEmail;
    private Categorie category;
    private String pathUrl;
    private Boolean isActive;
    private LocalDate foundedDate;

    // AJOUTE ÇA
    private int membresCount;
    private int evenementsCount;
    private int evenementsAVenirCount;
    private List<UserDTO> membres;
    private List<EvenementDTO> evenements;

    // Getters et setters
}