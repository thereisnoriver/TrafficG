package com.project.backend.queue;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

class WaitingRoomServiceTest {
    @Test
    void limitsAdmissionsAndPromotesFirstWaitingTicket() {
        WaitingRoomService queue = new WaitingRoomService();
        long show = 42L;
        var first = queue.join(show, 1L);
        queue.join(show, 2L);
        queue.join(show, 3L);
        var fourth = queue.join(show, 4L);

        assertEquals("ADMITTED", first.state());
        assertEquals("WAITING", fourth.state());
        assertEquals(1, fourth.position());

        queue.complete(first.ticketId(), 1L);
        assertEquals("ADMITTED", queue.status(fourth.ticketId(), 4L).state());
    }

    @Test
    void ticketsAreOwnedAndCannotBeClaimedTwice() {
        WaitingRoomService queue = new WaitingRoomService();
        var ticket = queue.join(1L, 10L);
        assertThrows(ResponseStatusException.class,
                () -> queue.status(ticket.ticketId(), 999L));

        queue.claimAdmission(ticket.ticketId(), 1L, 10L);
        assertThrows(ResponseStatusException.class,
                () -> queue.claimAdmission(ticket.ticketId(), 1L, 10L));
        queue.releaseClaim(ticket.ticketId(), 10L);
        assertDoesNotThrow(() -> queue.claimAdmission(ticket.ticketId(), 1L, 10L));
    }

    @Test
    void queuesAreIsolatedByShow() {
        WaitingRoomService queue = new WaitingRoomService();
        for (long user = 1; user <= 3; user++) queue.join(1L, user);
        assertEquals("ADMITTED", queue.join(2L, 4L).state());
        assertEquals("WAITING", queue.join(1L, 5L).state());
    }
}
