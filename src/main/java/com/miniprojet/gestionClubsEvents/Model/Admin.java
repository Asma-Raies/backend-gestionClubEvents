package com.miniprojet.gestionClubsEvents.Model;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "admin")
@Getter @Setter
public class Admin extends User {
   /*private UUID code ; 
	private boolean isSuperAdmin;*/
}