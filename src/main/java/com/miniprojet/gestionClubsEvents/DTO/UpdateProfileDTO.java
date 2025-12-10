package com.miniprojet.gestionClubsEvents.DTO;

import java.util.Set;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter 
@Setter
public class UpdateProfileDTO {
	 private String nom;
	    private String prenom;
	    private String password;
}
