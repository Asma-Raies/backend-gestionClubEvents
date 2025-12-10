package com.miniprojet.gestionClubsEvents.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import com.miniprojet.gestionClubsEvents.Model.CategorieBlog;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogDTO {

    private Long id;
    private String titre;
    private String contenu;
    private String imageUrl;
    private String fichierUrl;
    private String fichierNom;
    private CategorieBlog categorie;
    private LocalDateTime datePublication;
    private Long clubId;
    private String clubNom;
    private Long auteurId;
    private String auteurNomComplet;
    private String auteurRole;
    private String auteurPhoto;
    private long likesCount;
    private boolean likedByCurrentUser;
    private List<CommentaireDTO> commentaires;
}
