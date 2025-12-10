package com.miniprojet.gestionClubsEvents.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.miniprojet.gestionClubsEvents.Model.Moderateur;
import com.miniprojet.gestionClubsEvents.Model.User;

public interface ModerateurRepository extends JpaRepository<Moderateur, Long> {
	
}
