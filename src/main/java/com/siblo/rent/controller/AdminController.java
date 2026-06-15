package com.siblo.rent.controller;

import com.siblo.rent.dto.*;
import com.siblo.rent.service.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final CourtService courtService;
    private final BookingService bookingService;

    public AdminController(AdminService adminService, CourtService courtService, BookingService bookingService) {
        this.adminService = adminService;
        this.courtService = courtService;
        this.bookingService = bookingService;
    }

    @GetMapping("/stats/dashboard")
    public ResponseEntity<AdminStatsDTO> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/courts")
    public ResponseEntity<List<CourtDTO>> getCourts() {
        return ResponseEntity.ok(courtService.getAllCourtsForAdmin());
    }

    @PostMapping("/courts")
    public ResponseEntity<CourtDTO> addCourt(@RequestBody CourtRequest request) {
        return ResponseEntity.ok(courtService.addCourt(request));
    }

    @PutMapping("/courts/{id}")
    public ResponseEntity<CourtDTO> updateCourt(@PathVariable Long id, @RequestBody CourtRequest request) {
        return ResponseEntity.ok(courtService.updateCourt(id, request));
    }

    @PatchMapping("/courts/{id}/availability")
    public ResponseEntity<Void> toggleAvailability(@PathVariable Long id) {
        courtService.toggleAvailability(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/courts/{id}")
    public ResponseEntity<Void> deleteCourt(@PathVariable Long id) {
        courtService.deleteCourt(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bookings/timeline")
    public ResponseEntity<List<BookingDTO>> getTimeline(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) date = LocalDate.now();
        return ResponseEntity.ok(bookingService.getTimeline(date));
    }
}
