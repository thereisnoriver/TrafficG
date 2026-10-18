package com.project.backend.catalog;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "show_seats", uniqueConstraints = @UniqueConstraint(columnNames = {"show_id", "code"}))
public class Seat {
    public enum Status { AVAILABLE, HELD, SOLD }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "show_id", nullable = false)
    private Long showId;

    @Column(nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.AVAILABLE;

    private Long reservationId;
    private Instant holdExpiresAt;

    protected Seat() {}

    public Seat(Long showId, String code) {
        this.showId = showId;
        this.code = code;
    }

    public Long getShowId() { return showId; }
    public String getCode() { return code; }
    public Status getStatus() { return status; }
    public Long getReservationId() { return reservationId; }

    public boolean isAvailable(Instant now) {
        return status == Status.AVAILABLE
                || (status == Status.HELD && holdExpiresAt != null && !holdExpiresAt.isAfter(now));
    }

    public void hold(Long reservationId, Instant expiresAt) {
        this.reservationId = reservationId;
        this.holdExpiresAt = expiresAt;
        this.status = Status.HELD;
    }

    public void confirm() {
        this.status = Status.SOLD;
        this.holdExpiresAt = null;
    }

    public void release() {
        this.status = Status.AVAILABLE;
        this.reservationId = null;
        this.holdExpiresAt = null;
    }
}
