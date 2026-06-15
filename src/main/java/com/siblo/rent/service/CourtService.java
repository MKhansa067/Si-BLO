package com.siblo.rent.service;

import com.siblo.rent.dto.CourtDTO;
import com.siblo.rent.dto.CourtRequest;
import com.siblo.rent.dto.TimeSlotDTO;
import com.siblo.rent.entity.*;
import com.siblo.rent.entity.Court.CourtStatus;
import com.siblo.rent.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
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
}
