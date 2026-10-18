package com.project.backend.booking;

import com.project.backend.queue.WaitingRoomController;
import com.project.backend.queue.WaitingRoomService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    private final ReservationService service;
    private final WaitingRoomService queue;

    public ReservationController(ReservationService service, WaitingRoomService queue) {
        this.service = service;
        this.queue = queue;
    }

    @PostMapping
    public ReservationService.ReservationView reserve(@RequestBody ReserveRequest request, HttpSession session) {
        Long userId = WaitingRoomController.requireUser(session);
        queue.claimAdmission(request.queueTicket(), request.showId(), userId);
        try {
            ReservationService.ReservationView result =
                    service.reserve(request.showId(), userId, request.seats(), request.queueTicket());
            // This service call returns after the database transaction commits.
            queue.complete(request.queueTicket(), userId);
            return result;
        } catch (RuntimeException e) {
            queue.releaseClaim(request.queueTicket(), userId);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ReservationService.ReservationView get(@PathVariable Long id, HttpSession session) {
        return service.get(id, WaitingRoomController.requireUser(session));
    }

    @PostMapping("/{id}/payment")
    public ReservationService.ReservationView pay(@PathVariable Long id,
                                                   @RequestBody MockPaymentRequest request,
                                                   HttpSession session) {
        return service.pay(id, WaitingRoomController.requireUser(session), request.simulateSuccess());
    }

    public record ReserveRequest(Long showId, List<String> seats, String queueTicket) {}
    public record MockPaymentRequest(boolean simulateSuccess) {}
}
