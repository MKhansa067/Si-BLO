package com.siblo.rent.service;

import com.siblo.rent.dto.CourtDTO;
import com.siblo.rent.dto.CourtRequest;
import com.siblo.rent.dto.TimeSlotDTO;
import com.siblo.rent.entity.*;
import com.siblo.rent.entity.Court.CourtStatus;
import com.siblo.rent.entity.TimeSlot.SlotStatus;
import com.siblo.rent.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourtService {

    private final CourtRepository courtRepository;
    private final SportRepository sportRepository;
    private final VenueRepository venueRepository;
    private final TimeSlotRepository timeSlotRepository;

    public CourtService(CourtRepository courtRepository, SportRepository sportRepository,
                        VenueRepository venueRepository, TimeSlotRepository timeSlotRepository) {
        this.courtRepository = courtRepository;
        this.sportRepository = sportRepository;
        this.venueRepository = venueRepository;
        this.timeSlotRepository = timeSlotRepository;
    }

    public List<CourtDTO> getActiveCourts(Long sportId) {
        List<Court> courts = sportId != null
            ? courtRepository.findActiveCourts(sportId)
            : courtRepository.findByStatus(CourtStatus.ACTIVE);
        return courts.stream().map(CourtDTO::fromEntity).collect(Collectors.toList());
    }

    public List<CourtDTO> searchCourts(String query) {
        return courtRepository.searchCourts(query).stream()
            .map(CourtDTO::fromEntity).collect(Collectors.toList());
    }

    public CourtDTO getCourtById(Long id) {
        return courtRepository.findById(id).map(CourtDTO::fromEntity)
            .orElseThrow(() -> new RuntimeException("Court not found"));
    }

    public List<TimeSlotDTO> getAvailability(Long courtId, LocalDate date) {
        return timeSlotRepository.findByCourtIdAndDateOrderByStartTime(courtId, date).stream()
            .map(TimeSlotDTO::fromEntity).collect(Collectors.toList());
    }

    public long getAvailableCourtsCount() {
        return timeSlotRepository.countCourtsWithAvailableSlots(LocalDate.now());
    }

    @Transactional
    public CourtDTO addCourt(CourtRequest request) {
        Sport sport = sportRepository.findById(request.getSportId())
            .orElseThrow(() -> new RuntimeException("Sport not found"));
        Court court = new Court();
        court.setName(request.getName());
        court.setDescription(request.getDescription());
        court.setSurfaceType(request.getSurfaceType());
        court.setIndoor(request.getIndoor() != null ? request.getIndoor() : true);
        court.setPricePerHour(request.getPricePerHour());
        court.setCapacity(request.getCapacity());
        court.setRating(0.0);
        court.setReviewCount(0);
        court.setStatus(CourtStatus.ACTIVE);
        court.setSport(sport);
        court.setOpenTime(request.getOpenTime() != null ? request.getOpenTime() : LocalTime.of(6, 0));
        court.setCloseTime(request.getCloseTime() != null ? request.getCloseTime() : LocalTime.of(22, 0));
        if (request.getVenueId() != null) {
            venueRepository.findById(request.getVenueId()).ifPresent(v -> court.setVenue(v));
        }
        return CourtDTO.fromEntity(courtRepository.save(court));
    }

    @Transactional
    public CourtDTO updateCourt(Long id, CourtRequest request) {
        Court court = courtRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Court not found"));
        court.setName(request.getName());
        court.setDescription(request.getDescription());
        court.setSurfaceType(request.getSurfaceType());
        court.setIndoor(request.getIndoor() != null ? request.getIndoor() : court.getIndoor());
        court.setPricePerHour(request.getPricePerHour());
        court.setCapacity(request.getCapacity());
        if (request.getImageUrl() != null) court.setImageUrl(request.getImageUrl());
        if (request.getOpenTime() != null) court.setOpenTime(request.getOpenTime());
        if (request.getCloseTime() != null) court.setCloseTime(request.getCloseTime());
        if (request.getStatus() != null) {
            court.setStatus(CourtStatus.valueOf(request.getStatus()));
        }
        if (request.getSportId() != null) {
            court.setSport(sportRepository.findById(request.getSportId())
                .orElseThrow(() -> new RuntimeException("Sport not found")));
        }
        return CourtDTO.fromEntity(courtRepository.save(court));
    }

    @Transactional
    public void toggleAvailability(Long id) {
        Court court = courtRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Court not found"));
        court.setStatus(court.getStatus() == CourtStatus.ACTIVE ? CourtStatus.INACTIVE : CourtStatus.ACTIVE);
        courtRepository.save(court);
    }

    @Transactional
    public void deleteCourt(Long id) { courtRepository.deleteById(id); }

    public List<CourtDTO> getAllCourtsForAdmin() {
        return courtRepository.findAll().stream().map(CourtDTO::fromEntity).collect(Collectors.toList());
    }

    @Transactional
    public void generateSlots(Long courtId, int days) {
        Court court = courtRepository.findById(courtId)
            .orElseThrow(() -> new RuntimeException("Court not found"));
        LocalTime open = court.getOpenTime() != null ? court.getOpenTime() : LocalTime.of(6, 0);
        LocalTime close = court.getCloseTime() != null ? court.getCloseTime() : LocalTime.of(22, 0);
        LocalDate today = LocalDate.now();

        for (int day = 0; day < days; day++) {
            LocalDate date = today.plusDays(day);
            for (int hour = open.getHour(); hour < close.getHour(); hour++) {
                boolean exists = timeSlotRepository.existsByCourtIdAndDateAndStartTime(courtId, date, LocalTime.of(hour, 0));
                if (!exists) {
                    timeSlotRepository.save(TimeSlot.builder().court(court).date(date)
                        .startTime(LocalTime.of(hour, 0)).endTime(LocalTime.of(hour + 1, 0))
                        .status(SlotStatus.AVAILABLE).build());
                }
            }
        }
    }
}
