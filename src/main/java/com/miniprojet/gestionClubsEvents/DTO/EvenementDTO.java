package com.miniprojet.gestionClubsEvents.DTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import com.miniprojet.gestionClubsEvents.Model.EtatEvenement;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter 
@Setter
public class EvenementDTO {
	   private Long id;
	private String titre ;
    private String description ;
    private LocalDate dateEvenement;
    private LocalTime heure;
    private String lieu;
    private boolean estPublic;
    private EtatEvenement etat;
    private  Long clubId ; 
    private String clubNom;
    private List<UserDTO> inscrits;
    private int nombreInscrits;
    private boolean dejaInscrit;
    private boolean peutModifier;
    private boolean peutSupprimer;
    public EvenementDTO(Long id, String titre, LocalDate dateEvenement, LocalTime heure, String etat, int nombreInscrits) {
        this.id = id;
        this.titre = titre;
        this.dateEvenement = dateEvenement;
        this.heure = heure;
        this.etat = EtatEvenement.valueOf(etat); // ou garde String si tu veux
        this.nombreInscrits = nombreInscrits;
    }

    public EvenementDTO() {}


    
}
