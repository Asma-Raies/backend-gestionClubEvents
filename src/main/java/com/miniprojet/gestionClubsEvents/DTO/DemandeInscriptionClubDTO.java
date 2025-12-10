package com.miniprojet.gestionClubsEvents.DTO;

import java.time.LocalDateTime;
import java.util.List;

import com.miniprojet.gestionClubsEvents.Model.CategorieBlog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeInscriptionClubDTO {

    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String niveau;
    private String motivation;
    private Long clubId;
}
