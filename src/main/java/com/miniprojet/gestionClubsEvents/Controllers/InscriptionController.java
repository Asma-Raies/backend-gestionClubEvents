package com.miniprojet.gestionClubsEvents.Controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.miniprojet.gestionClubsEvents.Config.CustomUserDetails;
import com.miniprojet.gestionClubsEvents.DTO.DemandeInscriptionClubDTO;
import com.miniprojet.gestionClubsEvents.DTO.PendingAccountDTO;
import com.miniprojet.gestionClubsEvents.DTO.UserDTO;
import com.miniprojet.gestionClubsEvents.Model.DemandeInscription;
import com.miniprojet.gestionClubsEvents.Model.Etudiant;
import com.miniprojet.gestionClubsEvents.Model.StatutDemande;
import com.miniprojet.gestionClubsEvents.Model.User;
import com.miniprojet.gestionClubsEvents.Services.InscriptionService;
import com.miniprojet.gestionClubsEvents.Services.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/inscriptions")
@RequiredArgsConstructor
public class InscriptionController {

    private final InscriptionService inscriptionService;
    private final UserService userService; // pour current user

    // Visiteur soumet une demande
    @PostMapping("/club")
    public ResponseEntity<String> demanderInscriptionClub(@RequestBody DemandeInscriptionClubDTO dto) {
        inscriptionService.saveDemandeClub(dto);
        return ResponseEntity.ok("Demande envoyée avec succès ! L'administrateur vous contactera bientôt.");
    }

   // @PostMapping("/{id}/approuver")
    public ResponseEntity<String> approuverDemande(
            @PathVariable("id") Long demandeId,
            @RequestParam("dateEntretien") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime dateEntretien) {

        inscriptionService.approuverDemande(demandeId, dateEntretien);

        return ResponseEntity.ok("Demande approuvée et email envoyé.");
    }
    @GetMapping("/attente")
    public ResponseEntity<List<DemandeInscription>> getDemandesEnAttente() {
        List<DemandeInscription> demandes = inscriptionService.getDemandesEnAttente();
        return ResponseEntity.ok(demandes);
    }
    @PreAuthorize("hasRole('MODERATEUR')")
    @GetMapping("/my-club/attente")
    public ResponseEntity<List<DemandeInscription>> getDemandesMonClub(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        User moderator = userDetails.getUser();
        List<DemandeInscription> demandes = inscriptionService.getDemandesEnAttentePourMonClub(moderator);
        return ResponseEntity.ok(demandes);
    }

    @PreAuthorize("hasRole('MODERATEUR')")
    @PostMapping("/my-club/{id}/approuver")
    public ResponseEntity<String> approuverDemandeMonClub(
            @PathVariable Long id,
            @RequestParam("dateEntretien") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateEntretien,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User moderator = userDetails.getUser();
        inscriptionService.approuverDemandeMonClub(id, dateEntretien, moderator);
        return ResponseEntity.ok("Demande approuvée ! Email envoyé au candidat.");
    }
    @PostMapping("/{id}/approuver")
    public ResponseEntity<Void> approuver(@PathVariable Long id, @RequestParam String dateEntretien) {
        LocalDateTime dt = LocalDateTime.parse(dateEntretien); // ou parse avec formatter
        inscriptionService.approuverDemande(id, dt);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/moderateur/pending-accounts")
    public ResponseEntity<List<UserDTO>> getPendingAccountsForModerator() {
        User moderator = userService.getCurrentUser();
        List<Etudiant> list = inscriptionService.getPendingCreatedAccountsForModerator(moderator);
        List<UserDTO> dtos = list.stream().map(userService::mapToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/moderateur/accept/{userId}")
    public ResponseEntity<Void> moderatorAccept(@PathVariable Long userId) {
        User moderator = userService.getCurrentUser();
        inscriptionService.moderatorAcceptAccount(userId, moderator);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/moderateur/{clubId}/reject/{userId}")
    public ResponseEntity<Void> moderatorReject(@PathVariable Long clubId, @PathVariable Long userId) {
        User moderator = userService.getCurrentUser();
        inscriptionService.moderatorRejectAccount(userId, clubId, moderator);
        return ResponseEntity.ok().build();
    }
 // InscriptionController.java → AJOUTE CET ENDPOINT
    @PreAuthorize("hasRole('MODERATEUR')")
    @GetMapping("/my-club/pending-accounts")
    public ResponseEntity<List<PendingAccountDTO>> getPendingAccountsForMyClub(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User moderator = userDetails.getUser();
        List<PendingAccountDTO> pending = inscriptionService.getPendingAccountsForMyClub(moderator);
        return ResponseEntity.ok(pending);
    }
}