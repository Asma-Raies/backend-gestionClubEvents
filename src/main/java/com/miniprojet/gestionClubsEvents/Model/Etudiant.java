package com.miniprojet.gestionClubsEvents.Model;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "etudiant")
@Getter @Setter
public class Etudiant extends User {
    

	@Column(nullable = false)
	private boolean blocked = false;
	@Column(nullable = false)
	private boolean enabled = false; 
   /* private UUID  codeEtudiant;
    private String classe ; 
    private String diplome ;
    private String ecole ; */
    
}
