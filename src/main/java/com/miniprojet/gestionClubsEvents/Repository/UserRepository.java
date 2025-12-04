package com.miniprojet.gestionClubsEvents.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.miniprojet.gestionClubsEvents.Model.Admin;
import com.miniprojet.gestionClubsEvents.Model.Moderateur;
import com.miniprojet.gestionClubsEvents.Model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    
    @Query("SELECT u FROM User u WHERE TYPE(u) = Moderateur")
    List<Moderateur> findAllModerateurs();

    @Query("SELECT m FROM Moderateur m WHERE m.club IS NULL")
    List<Moderateur> findAvailableModerateurs();

    
    @Query("SELECT u FROM User u WHERE TYPE(u) = Admin")
    List<Admin> findAllAdmins();

    User findByEmail(String email);
    boolean existsByEmail(String email);
}
