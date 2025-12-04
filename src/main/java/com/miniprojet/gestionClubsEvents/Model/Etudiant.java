package com.miniprojet.gestionClubsEvents.Model;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "etudiant")
@Data
public class Etudiant extends User {
    


   /* private UUID  codeEtudiant;
    private String classe ; 
    private String diplome ;
    private String ecole ; */
    
}
