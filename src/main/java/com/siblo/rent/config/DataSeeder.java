package com.siblo.rent.config;

import com.siblo.rent.entity.*;
import com.siblo.rent.entity.Court.CourtStatus;
import com.siblo.rent.entity.TimeSlot.SlotStatus;
import com.siblo.rent.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SportRepository sportRepository;
    private final VenueRepository venueRepository;
    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, SportRepository sportRepository,
                      VenueRepository venueRepository, CourtRepository courtRepository,
                      TimeSlotRepository timeSlotRepository, BookingRepository bookingRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.sportRepository = sportRepository;
        this.venueRepository = venueRepository;
        this.courtRepository = courtRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.bookingRepository = bookingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        User admin = User.builder().name("Admin User").email("admin@siblo.com")
            .password(passwordEncoder.encode("admin123")).role(User.Role.ADMIN)
            .membershipTier("System Master").build();
        userRepository.save(admin);

        User member = User.builder().name("John Doe").email("john@siblo.com")
            .password(passwordEncoder.encode("john123")).role(User.Role.MEMBER)
            .membershipTier("PREMIUM MEMBER").build();
        userRepository.save(member);

        Sport basketball = sportRepository.save(Sport.builder().name("Basketball").slug("basketball").icon("🏀").locationCount(12).build());
        Sport futsal = sportRepository.save(Sport.builder().name("Futsal").slug("futsal").icon("⚽").locationCount(8).build());
        Sport padel = sportRepository.save(Sport.builder().name("Padel").slug("padel").icon("🎾").locationCount(15).build());
        Sport badminton = sportRepository.save(Sport.builder().name("Badminton").slug("badminton").icon("🏸").locationCount(6).build());
        Sport tennis = sportRepository.save(Sport.builder().name("Tennis").slug("tennis").icon("🎾").locationCount(4).build());

        Venue downtown = venueRepository.save(Venue.builder().name("Downtown Arena").address("123 Main St").zone("Central").latitude(-6.2088).longitude(106.8456).build());
        Venue glassHub = venueRepository.save(Venue.builder().name("Glass Hub").address("456 Park Ave").zone("Westside").latitude(-6.2250).longitude(106.8000).build());
        Venue eastSide = venueRepository.save(Venue.builder().name("East Side Complex").address("789 East Blvd").zone("Eastside").latitude(-6.2200).longitude(106.8800).build());
        Venue grandSlam = venueRepository.save(Venue.builder().name("Grand Slam Center").address("321 Sports Rd").zone("Central").latitude(-6.2400).longitude(106.8600).build());

        Court court1 = courtRepository.save(Court.builder().venue(downtown).sport(basketball)
            .name("Skyline Hoops Premium").description("Premium basketball court with professional flooring and stadium lighting.")
            .surfaceType("Hardwood").indoor(true).pricePerHour(199000).capacity(10)
            .rating(4.9).reviewCount(128).status(CourtStatus.ACTIVE).badgeLabel("AVAILABLE").build());

        courtRepository.save(Court.builder().venue(glassHub).sport(padel)
            .name("Velocity Padel Center").description("Professional padel court with synthetic turf and glass walls.")
            .surfaceType("Synthetic").indoor(true).pricePerHour(260000).capacity(4)
            .rating(5.0).reviewCount(89).status(CourtStatus.ACTIVE).badgeLabel("TOP RATED").build());

        courtRepository.save(Court.builder().venue(eastSide).sport(futsal)
            .name("Striker Futsal Indoor").description("Indoor futsal court with high-quality artificial grass.")
            .surfaceType("Artificial Grass").indoor(true).pricePerHour(255000).capacity(12)
            .rating(4.7).reviewCount(215).status(CourtStatus.ACTIVE).badgeLabel("2 SLOTS LEFT").build());

        courtRepository.save(Court.builder().venue(grandSlam).sport(tennis)
            .name("Grand Slam Center - Court 04").description("Premium professional acrylic surface with advanced shock absorption technology.")
            .surfaceType("Acrylic").indoor(true).pricePerHour(350000).capacity(4)
            .rating(4.8).reviewCount(56).status(CourtStatus.ACTIVE).badgeLabel("PREMIUM").build());

        courtRepository.save(Court.builder().venue(downtown).sport(badminton)
            .name("Downtown Badminton Hall").description("Professional badminton court with wooden flooring.")
            .surfaceType("Wooden").indoor(true).pricePerHour(150000).capacity(4)
            .rating(4.5).reviewCount(34).status(CourtStatus.ACTIVE).badgeLabel("AVAILABLE").build());

        courtRepository.save(Court.builder().venue(downtown).sport(futsal)
            .name("Lapangan Futsal").surfaceType("Hard Court").indoor(true).pricePerHour(255000).capacity(10)
            .rating(4.5).reviewCount(100).status(CourtStatus.ACTIVE).badgeLabel("PREMIUM").build());

        courtRepository.save(Court.builder().venue(eastSide).sport(basketball)
            .name("Lapangan Voli").surfaceType("Clay").indoor(false).pricePerHour(150000).capacity(12)
            .rating(4.2).reviewCount(78).status(CourtStatus.ACTIVE).build());

        courtRepository.save(Court.builder().venue(glassHub).sport(padel)
            .name("Lapangan Padel").surfaceType("Synthetic").indoor(true).pricePerHour(135000).capacity(4)
            .rating(4.0).reviewCount(45).status(CourtStatus.MAINTENANCE).build());

        List<Court> allCourts = courtRepository.findAll();
        LocalDate today = LocalDate.now();
        int[] hours = {6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22};

        for (Court court : allCourts) {
            for (int day = 0; day < 14; day++) {
                LocalDate date = today.plusDays(day);
                for (int hour : hours) {
                    LocalTime start = LocalTime.of(hour, 0);
                    SlotStatus status = (day == 0 && hour >= 18 && hour <= 20) ? SlotStatus.BOOKED : SlotStatus.AVAILABLE;
                    timeSlotRepository.save(TimeSlot.builder().court(court).date(date)
                        .startTime(start).endTime(start.plusHours(1)).status(status).build());
                }
            }
        }

        bookingRepository.save(Booking.builder().user(member).court(court1).date(today.minusDays(2))
            .startTime(LocalTime.of(20, 0)).endTime(LocalTime.of(21, 0))
            .totalPrice(199000).status(Booking.BookingStatus.COMPLETED).build());

        bookingRepository.save(Booking.builder().user(member).court(court1).date(today.minusDays(5))
            .startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(12, 0))
            .totalPrice(520000).status(Booking.BookingStatus.COMPLETED).build());

        bookingRepository.save(Booking.builder().user(member).court(court1).date(today.plusDays(2))
            .startTime(LocalTime.of(18, 0)).endTime(LocalTime.of(19, 0))
            .totalPrice(255000).status(Booking.BookingStatus.CONFIRMED).build());

        bookingRepository.save(Booking.builder().user(member).court(court1).date(today.plusDays(1))
            .startTime(LocalTime.of(14, 0)).endTime(LocalTime.of(16, 0))
            .totalPrice(700000).status(Booking.BookingStatus.PENDING_PAYMENT).build());
    }
}
