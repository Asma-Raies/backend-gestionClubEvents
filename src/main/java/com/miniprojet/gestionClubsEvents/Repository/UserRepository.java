package com.miniprojet.gestionClubsEvents.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.miniprojet.gestionClubsEvents.Model.Admin;
import com.miniprojet.gestionClubsEvents.Model.Club;
import com.miniprojet.gestionClubsEvents.Model.Etudiant;
import com.miniprojet.gestionClubsEvents.Model.Moderateur;
import com.miniprojet.gestionClubsEvents.Model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    
    @Query("SELECT u FROM User u WHERE TYPE(u) = Moderateur")
    List<Moderateur> findAllModerateurs();

    @Query("SELECT m FROM Moderateur m WHERE m.club IS NULL")
    List<Moderateur> findAvailableModerateurs();

    
    @Query("SELECT u FROM User u WHERE TYPE(u) = Admin")
    List<Admin> findAllAdmins();

   // User findByEmail(String email);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    @Query("SELECT u FROM User u JOIN u.clubs c WHERE c.id = :clubId")
    List<User> findByClubId(@Param("clubId") Long clubId);
    @Query("SELECT u FROM Etudiant u WHERE u.enabled = true AND :club NOT MEMBER OF u.clubs")
    List<Etudiant> findByClubsNotContainingAndEnabledTrue(@Param("club") Club club);

    // Users qui ont déjà été acceptés dans le club
    @Query("SELECT u FROM Etudiant u WHERE u.enabled = true AND :club MEMBER OF u.clubs")
    List<Etudiant> findByClubsContainingAndEnabledTrue(@Param("club") Club club);
}
