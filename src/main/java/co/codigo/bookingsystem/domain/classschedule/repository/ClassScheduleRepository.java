package co.codigo.bookingsystem.domain.classschedule.repository;

import co.codigo.bookingsystem.domain.classschedule.entity.ClassSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, Long> {
    List<ClassSchedule> findByCountryCode(String countryCode);

    @Query("""
    SELECT cs FROM ClassSchedule cs
    WHERE cs.startTime > CURRENT_TIMESTAMP
    """)
    List<ClassSchedule> findAllActiveClassSchedules();
}