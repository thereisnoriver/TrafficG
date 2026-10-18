package com.project.backend.catalog;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class DemoCatalogData {
    @Bean
    @ConditionalOnProperty(name = "app.demo.seed", havingValue = "true")
    CommandLineRunner seedShows(ShowRepository shows, SeatRepository seats) {
        return args -> {
            if (shows.count() != 0) return;
            List<Show> examples = List.of(
                    new Show("Chamber Music Evening", "An intimate chamber-music performance.", "", "2026-11-14", "2026-11-14"),
                    new Show("Autumn Jazz Session", "An evening of contemporary jazz.", "", "2026-11-21", "2026-11-21"),
                    new Show("Piano Recital", "A program of classical piano works.", "", "2026-12-05", "2026-12-05"));
            for (Show show : shows.saveAll(examples)) {
                List<Seat> inventory = new ArrayList<>();
                for (char row = 'A'; row <= 'F'; row++) {
                    for (int number = 1; number <= 8; number++) {
                        inventory.add(new Seat(show.getId(), row + Integer.toString(number)));
                    }
                }
                seats.saveAll(inventory);
            }
        };
    }
}
