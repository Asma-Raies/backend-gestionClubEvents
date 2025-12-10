package com.miniprojet.gestionClubsEvents.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Set;


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
    private String telephone;
    private String niveau;
    private Set<Long> clubIds;
    private Long clubId;
    private String clubNom;
    private LocalDateTime createdAt;
    private Boolean enabled;
    public UserDTO(Long id, String nom, String prenom, String email) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
    }
    public UserDTO(Long id, String nom, String prenom, String email , String telephone,String niveau ) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.niveau=niveau;
        this.telephone=telephone;
    }
    public UserDTO() {}
    
 
}