package com.project.backend.catalog;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/shows")
public class CatalogController {
    private final ShowRepository shows;
    private final SeatRepository seats;

    public CatalogController(ShowRepository shows, SeatRepository seats) {
        this.shows = shows;
        this.seats = seats;
    }

    @GetMapping
    public List<Show> list() {
        return shows.findAll();
    }

    @GetMapping("/{id}")
    public Show detail(@PathVariable Long id) {
        return shows.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Show not found"));
    }

    @GetMapping("/{id}/seats")
    public List<SeatView> seats(@PathVariable Long id) {
        detail(id);
        Instant now = Instant.now();
        return seats.findByShowIdOrderByCode(id).stream()
                .map(s -> new SeatView(s.getCode(), s.isAvailable(now) ? "AVAILABLE" : s.getStatus().name()))
                .toList();
    }

    public record SeatView(String code, String status) {}
}
