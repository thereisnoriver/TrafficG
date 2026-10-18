package com.project.backend.queue;

import com.project.backend.catalog.ShowRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/queue")
public class WaitingRoomController {
    private final WaitingRoomService queue;
    private final ShowRepository shows;

    public WaitingRoomController(WaitingRoomService queue, ShowRepository shows) {
        this.queue = queue;
        this.shows = shows;
    }

    @PostMapping("/join")
    public WaitingRoomService.QueueStatus join(@RequestBody JoinRequest request, HttpSession session) {
        if (request.showId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Show ID required");
        }
        if (!shows.existsById(request.showId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Show not found");
        }
        return queue.join(request.showId(), requireUser(session));
    }

    @GetMapping("/{ticketId}")
    public WaitingRoomService.QueueStatus status(@PathVariable String ticketId, HttpSession session) {
        return queue.status(ticketId, requireUser(session));
    }

    @DeleteMapping("/{ticketId}")
    public void leave(@PathVariable String ticketId, HttpSession session) {
        queue.complete(ticketId, requireUser(session));
    }

    public static Long requireUser(HttpSession session) {
        Object userId = session.getAttribute("userId");
        if (!(userId instanceof Long)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sign in to continue");
        }
        return (Long) userId;
    }

    public record JoinRequest(Long showId) {}
}
