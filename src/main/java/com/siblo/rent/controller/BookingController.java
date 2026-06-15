package com.siblo.rent.controller;

import com.siblo.rent.dto.BookingDTO;
import com.siblo.rent.dto.BookingRequest;
import com.siblo.rent.entity.User;
import com.siblo.rent.repository.UserRepository;
import com.siblo.rent.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;

    public BookingController(BookingService bookingService, UserRepository userRepository) {
        this.bookingService = bookingService;
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyBookings(Authentication auth,
            @RequestParam(required = false) Boolean upcoming) {
        User user = getUser(auth);
        if (Boolean.TRUE.equals(upcoming))
            return ResponseEntity.ok(bookingService.getUpcomingBookings(user.getId()));
        return ResponseEntity.ok(bookingService.getUserBookings(user.getId()));
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request, Authentication auth) {
        User user = getUser(auth);
        try { return ResponseEntity.ok(bookingService.createBooking(request, user.getId())); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id, Authentication auth) {
        User user = getUser(auth);
        try { return ResponseEntity.ok(bookingService.cancelBooking(id, user.getId())); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<?> payBooking(@PathVariable Long id, Authentication auth) {
        User user = getUser(auth);
        try { return ResponseEntity.ok(bookingService.payBooking(id, user.getId())); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    private User getUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
