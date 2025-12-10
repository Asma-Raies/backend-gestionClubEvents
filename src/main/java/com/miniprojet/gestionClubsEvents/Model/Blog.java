package com.miniprojet.gestionClubsEvents.Model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
@Entity
@Table(name = "blogs")
@Getter
@Setter
public class Blog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String contenu;

    private String imageUrl;

    private String fichierUrl;
    private String fichierNom;
    private String fichierType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategorieBlog categorie;

    private LocalDateTime datePublication = LocalDateTime.now();

    // Auteur : peut être Étudiant, Modérateur ou Admin (tous héritent de User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auteur_id", nullable = false)
    private User auteur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @OneToMany(mappedBy = "blog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Commentaire> commentaires = new ArrayList<>();

    @OneToMany(mappedBy = "blog", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Reaction> reactions = new HashSet<>();

    // Helper methods
    public long getLikesCount() {
        return reactions.stream()
                .filter(r -> r.getType() == TypeReaction.LIKE)
                .count();
    }

    public String getAuteurRole() {
        if (auteur instanceof Admin) return "ADMIN";
        if (auteur instanceof Moderateur) return "MODERATEUR";
        return "ETUDIANT";
    }

    public String getAuteurNomComplet() {
        return auteur.getPrenom() + " " + auteur.getNom();
    }
}