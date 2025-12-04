package com.miniprojet.gestionClubsEvents.DTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor   
public class AdminStatsDTO {
    private long totalUsers;
    private long totalClubs;
    private long totalEvents;
    private long upcomingEvents;

    private List<MonthlyActivity> monthlyActivity;        
    private List<ClubCategoryStats> clubDistribution;     

    public record MonthlyActivity(String month, long members, long events) {}
    public record ClubCategoryStats(String name, long count, double percentage, String color) {}
}

