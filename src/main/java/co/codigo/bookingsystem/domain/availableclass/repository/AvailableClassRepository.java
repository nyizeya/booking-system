package co.codigo.bookingsystem.domain.availableclass.repository;

import co.codigo.bookingsystem.domain.availableclass.entity.AvailableClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AvailableClassRepository extends JpaRepository<AvailableClass, Long> {
    
    @Query("SELECT c FROM AvailableClass c WHERE c.countryCode = :countryCode " +
           "AND c.startTime > :now ORDER BY c.startTime ASC")
    List<AvailableClass> findUpcomingClassesByCountry(@Param("countryCode") String countryCode,
                                      @Param("now") LocalDateTime now);
    
    @Query("SELECT c FROM AvailableClass c WHERE c.countryCode = :countryCode " +
           "AND c.startTime BETWEEN :start AND :end ORDER BY c.startTime ASC")
    List<AvailableClass> findByCountryAndDateRange(@Param("countryCode") String countryCode,
                                         @Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);

    @Query("SELECT c FROM AvailableClass c WHERE c.startTime < :endDate")
    List<AvailableClass> findByEndTimeBefore(@Param("endDate") LocalDateTime endDate);

    @Query("SELECT c FROM AvailableClass c LEFT JOIN FETCH c.bookings WHERE c.id = :classId")
    Optional<AvailableClass> findByIdWithBookings(@Param("classId") Long classId);
    
    @Query("SELECT c FROM AvailableClass c LEFT JOIN FETCH c.waitlists WHERE c.id = :classId")
    Optional<AvailableClass> findByIdWithWaitlists(@Param("classId") Long classId);
}