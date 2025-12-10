package com.miniprojet.gestionClubsEvents.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.miniprojet.gestionClubsEvents.Model.Reaction;
import com.miniprojet.gestionClubsEvents.Model.TypeReaction;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    Optional<Reaction> findByBlogIdAndAuteurId(Long blogId, Long userId);
    boolean existsByBlogIdAndAuteurId(Long blogId, Long userId);
   // Optional<Reaction> findByBlogIdAndAuteurId(Long blogId, Long auteurId);

    // Compte le nombre de réactions d'un certain type pour un blog
    long countByBlogIdAndType(Long blogId, TypeReaction type);

    // Vérifie si une réaction spécifique existe
    boolean existsByBlogIdAndAuteurIdAndType(Long blogId, Long auteurId, TypeReaction type);
}