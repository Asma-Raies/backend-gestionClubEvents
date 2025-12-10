package com.miniprojet.gestionClubsEvents.DTO;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter
public class LoginDTO {
	  private String email;
	    private String password;
	    private boolean enbaled ;
}
