package co.codigo.bookingsystem.domain.booking.repository;

import co.codigo.bookingsystem.common.enumerations.BookingStatus;
import co.codigo.bookingsystem.domain.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    List<Booking> findByUserId(Long userId);
    
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.bookedClass.startTime > :now " +
           "ORDER BY b.bookedClass.startTime ASC")
    List<Booking> findUpcomingByUser(@Param("userId") Long userId, @Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.bookedClass.id = :classId " +
           "AND b.status = com.codigo.bookingsystem.common.enumerations.BookingStatus.CONFIRMED")
    int countConfirmedBookingsForClass(@Param("classId") Long classId);
    
    @Query("SELECT b FROM Booking b WHERE b.bookedClass.id = :classId " +
           "AND b.status = com.codigo.bookingsystem.common.enumerations.BookingStatus.CONFIRMED " +
           "ORDER BY b.bookedAt ASC")
    List<Booking> findConfirmedBookingsForClass(@Param("classId") Long classId);
    
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId " +
           "AND b.bookedClass.startTime BETWEEN :start AND :end")
    List<Booking> findUserBookingsBetweenDates(@Param("userId") Long userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Modifying
    @Query("UPDATE Booking b SET b.status = 'CANCELLED', b.cancelledAt = :now " +
           "WHERE b.id = :bookingId AND b.status = 'CONFIRMED'")
    int cancelBooking(@Param("bookingId") Long bookingId, @Param("now") LocalDateTime now);

    boolean existsByUserIdAndBookedClassIdAndStatus(Long userId, Long bookedClassId, BookingStatus status);

    long countByBookedClassIdAndStatus(Long bookedClassId, BookingStatus status);
}