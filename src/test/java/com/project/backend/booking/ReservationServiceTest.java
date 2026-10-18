package com.project.backend.booking;

import com.project.backend.catalog.Seat;
import com.project.backend.catalog.SeatRepository;
import com.project.backend.catalog.Show;
import com.project.backend.catalog.ShowRepository;
import com.project.backend.queue.WaitingRoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ReservationServiceTest {
    @Autowired ReservationService reservations;
    @Autowired ShowRepository shows;
    @Autowired SeatRepository seats;
    @Autowired WaitingRoomService queue;

    @Test
    void confirmsHeldSeatsAndRejectsDoubleBooking() {
        Show show = shows.save(new Show("Test concert", "A test performance", "", "2026-12-01", "2026-12-01"));
        seats.save(new Seat(show.getId(), "A1"));
        String firstTicket = queue.join(show.getId(), 100L).ticketId();
        var hold = reservations.reserve(show.getId(), 100L, List.of("A1"), firstTicket);
        assertEquals("PENDING_PAYMENT", hold.status());
        assertEquals(6000, hold.totalAmount());

        String secondTicket = queue.join(show.getId(), 200L).ticketId();
        assertThrows(ResponseStatusException.class,
                () -> reservations.reserve(show.getId(), 200L, List.of("A1"), secondTicket));

        assertThrows(ResponseStatusException.class, () -> reservations.pay(hold.id(), 200L, true));
        assertEquals("CONFIRMED", reservations.pay(hold.id(), 100L, true).status());
        assertEquals("CONFIRMED", reservations.pay(hold.id(), 100L, true).status());
        assertEquals(Seat.Status.SOLD, seats.findByShowIdOrderByCode(show.getId()).get(0).getStatus());
    }

    @Test
    void rollsBackMultipleSeatsWhenOneIsUnavailable() {
        Show show = shows.save(new Show("Second concert", "Test", "", "2026-12-02", "2026-12-02"));
        seats.saveAll(List.of(new Seat(show.getId(), "A1"), new Seat(show.getId(), "A2")));
        String first = queue.join(show.getId(), 300L).ticketId();
        reservations.reserve(show.getId(), 300L, List.of("A1"), first);

        String second = queue.join(show.getId(), 400L).ticketId();
        assertThrows(ResponseStatusException.class,
                () -> reservations.reserve(show.getId(), 400L, List.of("A1", "A2"), second));
        assertEquals(Seat.Status.AVAILABLE,
                seats.findByShowIdOrderByCode(show.getId()).stream()
                        .filter(seat -> seat.getCode().equals("A2")).findFirst().orElseThrow().getStatus());
    }
}
