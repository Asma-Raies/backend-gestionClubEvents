package com.miniprojet.gestionClubsEvents.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.miniprojet.gestionClubsEvents.Model.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {
}