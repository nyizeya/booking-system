package co.codigo.bookingsystem.domain.classschedule.repository;

import co.codigo.bookingsystem.domain.classschedule.entity.ClassSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, Long> {
    
    @Query("SELECT c FROM ClassSchedule c WHERE c.countryCode = :countryCode " +
           "AND c.startTime > :now ORDER BY c.startTime ASC")
    List<ClassSchedule> findUpcomingClassesByCountry(@Param("countryCode") String countryCode,
                                                     @Param("now") LocalDateTime now);
    
    @Query("SELECT c FROM ClassSchedule c WHERE c.countryCode = :countryCode " +
           "AND c.startTime BETWEEN :start AND :end ORDER BY c.startTime ASC")
    List<ClassSchedule> findByCountryAndDateRange(@Param("countryCode") String countryCode,
                                                  @Param("start") LocalDateTime start,
                                                  @Param("end") LocalDateTime end);

    @Query("SELECT c FROM ClassSchedule c WHERE c.startTime < :endDate")
    List<ClassSchedule> findByEndTimeBefore(@Param("endDate") LocalDateTime endDate);

    @Query("SELECT c FROM ClassSchedule c LEFT JOIN FETCH c.bookings WHERE c.id = :classId")
    Optional<ClassSchedule> findByIdWithBookings(@Param("classId") Long classId);
    
    @Query("SELECT c FROM ClassSchedule c LEFT JOIN FETCH c.waitlists WHERE c.id = :classId")
    Optional<ClassSchedule> findByIdWithWaitlists(@Param("classId") Long classId);
}