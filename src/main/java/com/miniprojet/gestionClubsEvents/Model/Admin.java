package com.miniprojet.gestionClubsEvents.Model;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "admin")
@Data
public class Admin extends User {
   /*private UUID code ; 
	private boolean isSuperAdmin;*/
}