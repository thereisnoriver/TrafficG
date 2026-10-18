package com.project.backend.booking;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "reservations")
public class Reservation {
    public enum Status { PENDING_PAYMENT, CONFIRMED, CANCELLED, EXPIRED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long showId;
    @Column(nullable = false)
    private Long userId;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "reservation_seats", joinColumns = @JoinColumn(name = "reservation_id"))
    @Column(name = "seat_code")
    private List<String> seats;
    @Column(nullable = false)
    private int amount;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING_PAYMENT;
    private Instant holdExpiresAt;

    protected Reservation() {}

    public Reservation(Long showId, Long userId, List<String> seats, int amount, Instant holdExpiresAt) {
        this.showId = showId;
        this.userId = userId;
        this.seats = List.copyOf(seats);
        this.amount = amount;
        this.holdExpiresAt = holdExpiresAt;
    }

    public Long getId() { return id; }
    public Long getShowId() { return showId; }
    public Long getUserId() { return userId; }
    public List<String> getSeats() { return seats; }
    public int getAmount() { return amount; }
    public Status getStatus() { return status; }
    public Instant getHoldExpiresAt() { return holdExpiresAt; }
    public void confirm() { status = Status.CONFIRMED; }
    public void cancel() { status = Status.CANCELLED; }
    public void expire() { status = Status.EXPIRED; }
}
