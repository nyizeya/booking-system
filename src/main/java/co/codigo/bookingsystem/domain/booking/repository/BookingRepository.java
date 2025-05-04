package co.codigo.bookingsystem.domain.booking.repository;

import co.codigo.bookingsystem.domain.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    List<Booking> findByUserId(Long userId);

    Optional<Booking> findByIdAndUserId(Long id, Long userId);

    Optional<Booking> findByUserIdAndBookedClassId(Long userId, Long bookedClassId);

    boolean existsByUserIdAndBookedClassId(Long userId, Long classId);

    long countByBookedClassId(Long bookedClassId);

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND " +
            "b.bookedClass.startTime < :newEndTime AND " +
            "b.bookedClass.endTime > :newStartTime")
    List<Booking> findOverlappingBookings(
            @Param("userId") Long userId,
            @Param("newStartTime") LocalDateTime newStartTime,
            @Param("newEndTime") LocalDateTime newEndTime
    );

}