package com.miniprojet.gestionClubsEvents.DTO;

import java.time.LocalDateTime;

public record PendingAccountDTO(
 Long userId,
 String prenom,
 String nom,
 String email,
 String telephone,
 String niveau,
 LocalDateTime dateEntretien,
 Long demandeId
) {}