package com.miniprojet.gestionClubsEvents.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentaireDTO {

    private Long id;
    private String contenu;
    private LocalDateTime dateCommentaire;
    private Long auteurId;
    private String auteurNomComplet;
    private String auteurPhoto;
    private List<CommentaireDTO> reponses;
}