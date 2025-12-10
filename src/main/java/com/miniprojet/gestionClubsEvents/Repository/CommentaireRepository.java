package com.miniprojet.gestionClubsEvents.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.miniprojet.gestionClubsEvents.Model.Commentaire;

public interface CommentaireRepository extends JpaRepository<Commentaire, Long> {
    List<Commentaire> findByBlogIdAndParentIsNullOrderByDateCommentaireAsc(Long blogId);
}