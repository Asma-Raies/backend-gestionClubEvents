package com.miniprojet.gestionClubsEvents.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Data
@Getter 
@Setter
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String password; // Seulement en création
    private String role;
    private Set<Long> clubIds;
    public UserDTO(Long id, String nom, String prenom, String email) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
    }
    public UserDTO() {}
    
 
}