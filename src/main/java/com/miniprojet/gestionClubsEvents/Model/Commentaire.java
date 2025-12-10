package com.miniprojet.gestionClubsEvents.Model;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Entity
@Table(name = "commentaires")
@Getter 
@Setter
public class Commentaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String contenu;

    private LocalDateTime dateCommentaire = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auteur_id", nullable = false)
    private User auteur;

    @ManyToOne(fetch = FetchType.LAZY)
    private Commentaire parent; // pour les réponses

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Commentaire> reponses = new ArrayList<>();

    public String getAuteurNomComplet() {
        return auteur.getPrenom() + " " + auteur.getNom();
    }
}