package com.siblo.rent.repository;

import com.siblo.rent.entity.Booking;
import com.siblo.rent.entity.Booking.BookingStatus;
import com.siblo.rent.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Booking> findByUserIdAndStatusOrderByDateDesc(Long userId, BookingStatus status);

    List<Booking> findByCourtIdAndDate(Long courtId, LocalDate date);

    long countByUserIdAndStatus(Long userId, BookingStatus status);

    long countByStatus(BookingStatus status);

    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.date = :date AND b.status IN ('CONFIRMED', 'COMPLETED')")
    Long sumRevenueByDate(@Param("date") LocalDate date);

    @Query("SELECT b FROM Booking b WHERE b.date = :date ORDER BY b.startTime")
    List<Booking> findByDateOrderByStartTime(@Param("date") LocalDate date);
}
