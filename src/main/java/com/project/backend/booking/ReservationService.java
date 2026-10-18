package com.project.backend.booking;

import com.project.backend.catalog.Seat;
import com.project.backend.catalog.SeatRepository;
import com.project.backend.catalog.ShowRepository;
import com.project.backend.queue.WaitingRoomService;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class ReservationService {
    private static final int PRICE_PER_SEAT = 6000;
    private static final Duration HOLD_LIFETIME = Duration.ofMinutes(5);

    private final SeatRepository seats;
    private final ShowRepository shows;
    private final ReservationRepository reservations;
    private final WaitingRoomService queue;

    public ReservationService(SeatRepository seats, ShowRepository shows,
                              ReservationRepository reservations, WaitingRoomService queue) {
        this.seats = seats;
        this.shows = shows;
        this.reservations = reservations;
        this.queue = queue;
    }

    @Transactional
    public ReservationView reserve(Long showId, Long userId, List<String> requested, String ticket) {
        if (showId == null || !shows.existsById(showId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Show not found");
        }
        if (requested == null || requested.isEmpty() || requested.size() > 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose between one and four seats");
        }
        List<String> codes = requested.stream().sorted().distinct().toList();
        if (codes.size() != requested.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate seat requested");
        }
        queue.requireAdmission(ticket, showId, userId);

        // Database row locks serialize competing reservations for the same seats.
        List<Seat> locked = seats.lockForReservation(showId, codes);
        if (locked.size() != codes.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown seat");
        }
        Instant now = Instant.now();
        if (locked.stream().anyMatch(seat -> !seat.isAvailable(now))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "One or more seats are unavailable");
        }

        Instant expires = now.plus(HOLD_LIFETIME);
        Reservation reservation = reservations.saveAndFlush(
                new Reservation(showId, userId, codes, codes.size() * PRICE_PER_SEAT, expires));
        locked.forEach(seat -> seat.hold(reservation.getId(), expires));
        return view(reservation);
    }

    @Transactional(readOnly = true)
    public ReservationView get(Long id, Long userId) {
        Reservation reservation = reservations.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));
        requireOwner(reservation, userId);
        return view(reservation);
    }

    @Transactional
    public ReservationView pay(Long id, Long userId, boolean simulateSuccess) {
        Reservation reservation = reservations.lockById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));
        requireOwner(reservation, userId);
        if (reservation.getStatus() == Reservation.Status.CONFIRMED) return view(reservation);
        if (reservation.getStatus() != Reservation.Status.PENDING_PAYMENT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Reservation is no longer payable");
        }
        List<Seat> locked = seats.lockByReservation(id);
        if (!reservation.getHoldExpiresAt().isAfter(Instant.now())) {
            locked.forEach(Seat::release);
            reservation.expire();
            return view(reservation);
        }
        if (simulateSuccess) {
            locked.forEach(Seat::confirm);
            reservation.confirm();
        } else {
            locked.forEach(Seat::release);
            reservation.cancel();
        }
        return view(reservation);
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void expireHolds() {
        for (Reservation reservation : reservations.findByStatusAndHoldExpiresAtBefore(
                Reservation.Status.PENDING_PAYMENT, Instant.now())) {
            // Lock reservation rows first, in the same order as payment confirmation.
            if (reservation.getStatus() != Reservation.Status.PENDING_PAYMENT
                    || reservation.getHoldExpiresAt().isAfter(Instant.now())) continue;
            List<Seat> locked = seats.lockByReservation(reservation.getId());
            locked.forEach(Seat::release);
            reservation.expire();
        }
    }

    private void requireOwner(Reservation reservation, Long userId) {
        if (!reservation.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found");
        }
    }

    private ReservationView view(Reservation reservation) {
        return new ReservationView(reservation.getId(), reservation.getShowId(),
                reservation.getSeats(), reservation.getAmount(),
                reservation.getStatus().name(), reservation.getHoldExpiresAt());
    }

    public record ReservationView(Long id, Long showId, List<String> seats,
                                  int totalAmount, String status, Instant expiresAt) {}
}
