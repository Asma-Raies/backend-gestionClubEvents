package com.miniprojet.gestionClubsEvents.Repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.miniprojet.gestionClubsEvents.Model.Evenement;

public interface EvenementRepository extends JpaRepository<Evenement, Long> {
    List<Evenement> findByClubId(Long clubId);
    List<Evenement> findByEstPublicTrue();
    List<Evenement> findByInscritsId(Long userId);
    @Query("SELECT e FROM Evenement e WHERE e.estPublic = true ORDER BY e.dateEvenement DESC, e.heure DESC")
    List<Evenement> findAllPublic();
    @Query("""
    	    SELECT e FROM Evenement e 
    	    WHERE e.estPublic = true 
    	       OR e.club IN (SELECT c FROM Club c JOIN c.members m WHERE m.id = :userId)
    	    ORDER BY e.dateEvenement DESC
    	    """)
    	List<Evenement> findAllVisibleForStudent(@Param("userId") Long userId);

}