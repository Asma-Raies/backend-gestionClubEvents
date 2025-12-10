package com.miniprojet.gestionClubsEvents.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.miniprojet.gestionClubsEvents.Model.Club;
import com.miniprojet.gestionClubsEvents.Model.User;
public interface ClubRepository extends JpaRepository<Club, Long> {
	boolean existsByNom(String nom);

   
    Club findByNom(String nom);
    List<Club> findAllByModerateurIsNotNull();
    boolean existsByModerateurId(Long moderateurId);
    Optional<Club> findByModerateurId(Long id);
    @Query("SELECT DISTINCT c FROM Club c LEFT JOIN FETCH c.members WHERE c.id = :clubId")
    Optional<Club> findByIdWithMembers(@Param("clubId") Long clubId);
    @Query("SELECT m FROM Club c JOIN c.members m WHERE c.id = :clubId")
    List<User> findMembersByClubId(@Param("clubId") Long clubId);
    @Query("SELECT c FROM Club c JOIN c.members m WHERE m.id = :userId")
    List<Club> findByMemberId(@Param("userId") Long userId);
}