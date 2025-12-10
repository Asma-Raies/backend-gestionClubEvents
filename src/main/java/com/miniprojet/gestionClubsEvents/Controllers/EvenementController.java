package com.miniprojet.gestionClubsEvents.Controllers;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.miniprojet.gestionClubsEvents.Config.CustomUserDetails;
import com.miniprojet.gestionClubsEvents.DTO.ClubDTO;
import com.miniprojet.gestionClubsEvents.DTO.EvenementDTO;
import com.miniprojet.gestionClubsEvents.Model.Evenement;
import com.miniprojet.gestionClubsEvents.Model.User;
import com.miniprojet.gestionClubsEvents.Services.ClubService;
import com.miniprojet.gestionClubsEvents.Services.EvenementService;

import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/evenements")
public class EvenementController {

    @Autowired
    private EvenementService service;
    @Autowired
    private ClubService clubService;
    @GetMapping("/public")
    public ResponseEntity<List<EvenementDTO>> getEvenementsPublic() {
        List<EvenementDTO> events = service.getEvenementsPublic();
        return ResponseEntity.ok(events);
    }
    @PostMapping
    public Evenement create(@RequestBody EvenementDTO dto, Principal principal) {
        return service.create(dto, principal.getName());
    }

    @PutMapping("/{id}")
    public Evenement update(@PathVariable Long id, @RequestBody EvenementDTO dto, Principal principal) {
        return service.update(id, dto, principal.getName());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Principal principal) {
        service.delete(id, principal.getName());
    }
    @GetMapping("/my-club")
    public List<EvenementDTO> getMyClubEvents(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return service.getMyClubEvents(userDetails.getUser());
    }
   
    @GetMapping
    public ResponseEntity<List<EvenementDTO>> getAll() {
        List<EvenementDTO> events = service.getAll();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/club/{clubId}")
    public List<Evenement> getByClub(@PathVariable Long clubId) {
        return service.getByClub(clubId);
    }
    @GetMapping("/{id}")
    public ResponseEntity<EvenementDTO> getEvenemenById(@PathVariable Long id) {
    	EvenementDTO eventDTO = service.getEvenementById(id);
        return ResponseEntity.ok(eventDTO);
    }

    @PostMapping("/{id}/inscrire")
    public void inscrire(@PathVariable Long id, Principal principal) {
        service.inscrire(id, principal.getName());
    }

    @PostMapping("/{id}/desinscrire")
    public void desinscrire(@PathVariable Long id, Principal principal) {
        service.desinscrire(id, principal.getName());
    }
    
    @PostMapping("/my-club")
    @PreAuthorize("hasRole('MODERATEUR')")
    public ResponseEntity<EvenementDTO> createMyClubEvent(
            @RequestBody EvenementDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Evenement event = service.createMyClubEvent(dto, userDetails.getUser());
        return ResponseEntity.ok(service.convertToDTO(event));
    }

    // MODIFICATION D'UN ÉVÉNEMENT PAR MODÉRATEUR
    @PutMapping("/my-club/{id}")
    @PreAuthorize("hasRole('MODERATEUR')")
    public ResponseEntity<EvenementDTO> updateMyClubEvent(
            @PathVariable Long id,
            @RequestBody EvenementDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Evenement updated = service.updateMyClubEvent(id, dto, userDetails.getUser());
        return ResponseEntity.ok(service.convertToDTO(updated));
    }
   

    // ClubController.java
    @GetMapping("/my-clubs")
    public List<ClubDTO> getMyClubs(@AuthenticationPrincipal CustomUserDetails user) {
        return clubService.getMyClubs(user.getUser());
    }
    @GetMapping("/student")
    public List<EvenementDTO> getEvenementsForStudent(@AuthenticationPrincipal CustomUserDetails user) {
        return service.getEvenementsForStudent(user.getUser());
    }
}