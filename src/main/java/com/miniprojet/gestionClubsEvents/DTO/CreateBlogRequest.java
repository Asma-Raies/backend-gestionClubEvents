package com.miniprojet.gestionClubsEvents.DTO;

import com.miniprojet.gestionClubsEvents.Model.CategorieBlog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBlogRequest {

    private String titre;
    private String contenu;
    private String imageUrl;
    private String fichierUrl;
    private String fichierNom;
    private CategorieBlog categorie;
    private Long clubId;
}
