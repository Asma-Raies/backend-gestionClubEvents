package com.miniprojet.gestionClubsEvents.Services;

import com.miniprojet.gestionClubsEvents.DTO.AdminStatsDTO;
import com.miniprojet.gestionClubsEvents.Model.Club;
import com.miniprojet.gestionClubsEvents.Model.EtatEvenement;
import com.miniprojet.gestionClubsEvents.Model.Evenement;
import com.miniprojet.gestionClubsEvents.Model.User;
import com.miniprojet.gestionClubsEvents.Repository.ClubRepository;
import com.miniprojet.gestionClubsEvents.Repository.EvenementRepository;
import com.miniprojet.gestionClubsEvents.Repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final UserRepository userRepo;
    private final ClubRepository clubRepo;
    private final EvenementRepository eventRepo;

    public AdminStatsDTO getAllStats() {

        List<User> users = userRepo.findAll();
        List<Club> clubs = clubRepo.findAll();
        List<Evenement> events = eventRepo.findAll();

        // === Stats globales ===
        long totalUsers = users.size();
        long totalClubs = clubs.size();
        long totalEvents = events.size();
        long upcomingEvents = events.stream()
                .filter(e -> e.getEtat() == EtatEvenement.A_VENIR)
                .count();

        // === Activité mensuelle (6 derniers mois) ===
        List<AdminStatsDTO.MonthlyActivity> monthly = getLast6Months(users, events);

        // === Distribution des clubs par catégorie ===
        List<AdminStatsDTO.ClubCategoryStats> distribution = getClubDistribution(clubs);

        return new AdminStatsDTO(
                totalUsers, totalClubs, totalEvents, upcomingEvents,
                monthly, distribution
        );
    }

    private List<AdminStatsDTO.MonthlyActivity> getLast6Months(List<User> users, List<Evenement> events) {
        List<AdminStatsDTO.MonthlyActivity> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.from(today.minusMonths(i));
            String monthName = ym.getMonth()
                    .getDisplayName(TextStyle.SHORT, Locale.FRENCH); // Jan, Fév, Mar...

            long newMembers = users.stream()
                    .filter(u -> u.getCreatedAt() != null)
                    .filter(u -> YearMonth.from(u.getCreatedAt().toLocalDate()).equals(ym))
                    .count();

            long newEvents = events.stream()
                    .filter(e -> e.getDateEvenement() != null)
                    .filter(e -> YearMonth.from(e.getDateEvenement()).equals(ym))
                    .count();

            result.add(new AdminStatsDTO.MonthlyActivity(monthName, newMembers, newEvents));
        }
        return result;
    }

    private List<AdminStatsDTO.ClubCategoryStats> getClubDistribution(List<Club> clubs) {
        if (clubs.isEmpty()) return List.of();

        // Convert enum to String explicitly to avoid Map<Object, Long>
        Map<String, Long> map = clubs.stream()
                .collect(Collectors.groupingBy(
                        (Club c) -> c.getCategory() != null ? c.getCategory().name() : "OTHER",
                        Collectors.counting()
                ));

        long total = clubs.size();

        List<AdminStatsDTO.ClubCategoryStats> list = new ArrayList<>();
        String[] colors = {"#f97316", "#fb923c", "#1e293b", "#475569", "#dc2626", "#7c3aed"};

        int colorIndex = 0;
        for (var entry : map.entrySet()) {
            // Map enum names to user-friendly labels
            String name = switch (entry.getKey()) {
                case "TECHNICAL" -> "Tech Club";
                case "SPORTS" -> "Sports Club";
                case "ENTREPRENEURSHIP" -> "Business Club";
                case "ARTISTIC" -> "Arts Club";
                case "ACADEMIC" -> "Academic Club";
                case "CULTURAL" -> "Cultural Club";
                case "SOCIAL" -> "Social Club";
                case "SCIENTIFIC" -> "Scientific Club";
                case "ENVIRONMENTAL" -> "Environmental Club";
                default -> "Other Club";
            };

            double percent = Math.round(entry.getValue() * 1000.0 / total) / 10.0;

            list.add(new AdminStatsDTO.ClubCategoryStats(
                    name,
                    entry.getValue(),
                    percent,
                    colors[colorIndex++ % colors.length]
            ));
        }

        // Sort descending by count
        return list.stream()
                .sorted(Comparator.comparing(AdminStatsDTO.ClubCategoryStats::count).reversed())
                .toList();
    }

}
