package com.miniprojet.gestionClubsEvents.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.miniprojet.gestionClubsEvents.Model.Club;
public interface ClubRepository extends JpaRepository<Club, Long> {
	boolean existsByNom(String nom);

   
    Club findByNom(String nom);
    List<Club> findAllByModerateurIsNotNull();
    boolean existsByModerateurId(Long moderateurId);
    List<Club> findByModerateurId(Long id);

}