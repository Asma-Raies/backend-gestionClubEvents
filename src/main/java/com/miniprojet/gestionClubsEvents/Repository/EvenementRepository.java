package com.miniprojet.gestionClubsEvents.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.miniprojet.gestionClubsEvents.Model.Evenement;

public interface EvenementRepository extends JpaRepository<Evenement, Long> {
    List<Evenement> findByClubId(Long clubId);
    List<Evenement> findByEstPublicTrue();
    List<Evenement> findByInscritsId(Long userId);
}