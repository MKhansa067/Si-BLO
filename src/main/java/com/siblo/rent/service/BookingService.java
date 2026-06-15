package com.siblo.rent.service;

import com.siblo.rent.dto.BookingDTO;
import com.siblo.rent.dto.BookingRequest;
import com.siblo.rent.entity.*;
import com.siblo.rent.entity.Booking.BookingStatus;
import com.siblo.rent.entity.TimeSlot.SlotStatus;
import com.siblo.rent.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository, CourtRepository courtRepository,
                          TimeSlotRepository timeSlotRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.courtRepository = courtRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.userRepository = userRepository;
    }

    public List<BookingDTO> getUserBookings(Long userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(BookingDTO::fromEntity).collect(Collectors.toList());
    }

    public List<BookingDTO> getUpcomingBookings(Long userId) {
        return bookingRepository.findByUserIdAndStatusOrderByDateDesc(userId, BookingStatus.CONFIRMED).stream()
            .map(BookingDTO::fromEntity).collect(Collectors.toList());
    }

    @Transactional
    public BookingDTO createBooking(BookingRequest request, Long userId) {
        Court court = courtRepository.findById(request.getCourtId())
            .orElseThrow(() -> new RuntimeException("Court not found"));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        List<TimeSlot> slots = timeSlotRepository.findAllById(request.getSlotIds());
        if (slots.isEmpty()) throw new RuntimeException("No valid slots selected");

        for (TimeSlot slot : slots) {
            if (slot.getStatus() != SlotStatus.AVAILABLE)
                throw new RuntimeException("Slot " + slot.getId() + " is no longer available");
            slot.setStatus(SlotStatus.BOOKED);
        }
        timeSlotRepository.saveAll(slots);

        int totalPrice = slots.size() * court.getPricePerHour();
        LocalTime startTime = slots.get(0).getStartTime();
        LocalTime endTime = slots.get(slots.size() - 1).getEndTime();
        LocalDate date = LocalDate.parse(request.getDate());

        Booking booking = new Booking();
        booking.setUser(user); booking.setCourt(court);
        booking.setSlotIds(request.getSlotIds()); booking.setDate(date);
        booking.setStartTime(startTime); booking.setEndTime(endTime);
        booking.setTotalPrice(totalPrice); booking.setStatus(BookingStatus.PENDING_PAYMENT);
        return BookingDTO.fromEntity(bookingRepository.save(booking));
    }

    @Transactional
    public BookingDTO cancelBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!booking.getUser().getId().equals(userId))
            throw new RuntimeException("Not authorized to cancel this booking");
        booking.setStatus(BookingStatus.CANCELLED);
        List<TimeSlot> slots = timeSlotRepository.findAllById(booking.getSlotIds());
        for (TimeSlot slot : slots) slot.setStatus(SlotStatus.AVAILABLE);
        timeSlotRepository.saveAll(slots);
        return BookingDTO.fromEntity(bookingRepository.save(booking));
    }

    @Transactional
    public BookingDTO payBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!booking.getUser().getId().equals(userId))
            throw new RuntimeException("Not authorized");
        booking.setStatus(BookingStatus.CONFIRMED);
        return BookingDTO.fromEntity(bookingRepository.save(booking));
    }

    public List<BookingDTO> getTimeline(LocalDate date) {
        return bookingRepository.findByDateOrderByStartTime(date).stream()
            .map(BookingDTO::fromEntity).collect(Collectors.toList());
    }
}
