package com.miniprojet.gestionClubsEvents.Controllers;

import com.miniprojet.gestionClubsEvents.DTO.AdminStatsDTO;
import com.miniprojet.gestionClubsEvents.Services.AdminStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDTO> getAdminStats() {
        AdminStatsDTO stats = adminStatsService.getAllStats();
        return ResponseEntity.ok(stats);
    }
}