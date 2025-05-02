package co.codigo.bookingsystem.domain.booking.repository;

import co.codigo.bookingsystem.domain.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    List<Booking> findByUserId(Long userId);

    Optional<Booking> findByIdAndUserId(Long id, Long userId);

    Optional<Booking> findByUserIdAndBookedClassId(Long userId, Long bookedClassId);

    boolean existsByIdAndUserId(Long id, Long userId);

    long countByBookedClassId(Long bookedClassId);
}