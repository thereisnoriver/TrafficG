package com.project.backend.queue;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * A server-owned, single-instance waiting room inspired by TrafficJam.
 * Replace the in-memory state with a shared store before horizontal scaling.
 */
@Service
public class WaitingRoomService {
    private static final int MAX_ADMITTED = 3;
    private static final Duration ADMISSION_LIFETIME = Duration.ofMinutes(10);
    private static final Duration WAIT_LIFETIME = Duration.ofMinutes(30);

    private final Map<Long, LinkedHashMap<String, Ticket>> queues = new HashMap<>();
    private final Map<String, Ticket> tickets = new HashMap<>();

    private static class Ticket {
        final String id = UUID.randomUUID().toString();
        final Long showId;
        final Long userId;
        final Instant joinedAt = Instant.now();
        Instant admittedAt;
        boolean claimed;

        Ticket(Long showId, Long userId) {
            this.showId = showId;
            this.userId = userId;
        }
    }

    public record QueueStatus(String ticketId, String state, int position, int waitingCount) {}

    public synchronized QueueStatus join(Long showId, Long userId) {
        cleanup(showId);
        LinkedHashMap<String, Ticket> queue = queues.computeIfAbsent(showId, ignored -> new LinkedHashMap<>());
        for (Ticket existing : queue.values()) {
            if (existing.userId.equals(userId)) return view(existing);
        }
        Ticket ticket = new Ticket(showId, userId);
        queue.put(ticket.id, ticket);
        tickets.put(ticket.id, ticket);
        promote(showId);
        return view(ticket);
    }

    public synchronized QueueStatus status(String ticketId, Long userId) {
        Ticket ticket = ownedTicket(ticketId, userId);
        cleanup(ticket.showId);
        if (!tickets.containsKey(ticketId)) {
            throw new ResponseStatusException(HttpStatus.GONE, "Waiting-room ticket expired");
        }
        return view(ticket);
    }

    public synchronized void requireAdmission(String ticketId, Long showId, Long userId) {
        Ticket ticket = ownedTicket(ticketId, userId);
        cleanup(showId);
        if (!tickets.containsKey(ticketId) || !ticket.showId.equals(showId) || ticket.admittedAt == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Waiting-room admission required");
        }
    }

    public synchronized void claimAdmission(String ticketId, Long showId, Long userId) {
        requireAdmission(ticketId, showId, userId);
        Ticket ticket = ownedTicket(ticketId, userId);
        if (ticket.claimed) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Admission is already being used");
        }
        ticket.claimed = true;
    }

    public synchronized void releaseClaim(String ticketId, Long userId) {
        Ticket ticket = tickets.get(ticketId);
        if (ticket != null && ticket.userId.equals(userId)) ticket.claimed = false;
    }

    public synchronized void complete(String ticketId, Long userId) {
        Ticket ticket = ownedTicket(ticketId, userId);
        remove(ticket);
        promote(ticket.showId);
    }

    private Ticket ownedTicket(String ticketId, Long userId) {
        Ticket ticket = tickets.get(ticketId);
        if (ticket == null || !ticket.userId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Waiting-room ticket not found");
        }
        return ticket;
    }

    private void cleanup(Long showId) {
        LinkedHashMap<String, Ticket> queue = queues.get(showId);
        if (queue == null) return;
        Instant now = Instant.now();
        for (Ticket ticket : new ArrayList<>(queue.values())) {
            Duration lifetime = ticket.admittedAt == null ? WAIT_LIFETIME : ADMISSION_LIFETIME;
            Instant started = ticket.admittedAt == null ? ticket.joinedAt : ticket.admittedAt;
            if (!started.plus(lifetime).isAfter(now)) remove(ticket);
        }
        promote(showId);
    }

    private void promote(Long showId) {
        LinkedHashMap<String, Ticket> queue = queues.get(showId);
        if (queue == null) return;
        long admitted = queue.values().stream().filter(t -> t.admittedAt != null).count();
        for (Ticket ticket : queue.values()) {
            if (admitted >= MAX_ADMITTED) break;
            if (ticket.admittedAt == null) {
                ticket.admittedAt = Instant.now();
                admitted++;
            }
        }
    }

    private QueueStatus view(Ticket ticket) {
        LinkedHashMap<String, Ticket> queue = queues.get(ticket.showId);
        int waiting = 0;
        int position = 0;
        for (Ticket item : queue.values()) {
            if (item.admittedAt == null) {
                waiting++;
                if (position == 0 && item.id.equals(ticket.id)) position = waiting;
            }
        }
        return new QueueStatus(ticket.id, ticket.admittedAt == null ? "WAITING" : "ADMITTED", position, waiting);
    }

    private void remove(Ticket ticket) {
        tickets.remove(ticket.id);
        LinkedHashMap<String, Ticket> queue = queues.get(ticket.showId);
        if (queue != null) queue.remove(ticket.id);
    }
}
