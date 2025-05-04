package co.codigo.bookingsystem.domain.waitlist.repository;

import co.codigo.bookingsystem.domain.waitlist.entity.Waitlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {

    @Modifying
    @Query("DELETE FROM Waitlist w WHERE w.waitingClass.id = :classId")
    void deleteByWaitingClassId(@Param("classId") Long classId);

    boolean existsByUserIdAndWaitingClassId(Long userId, Long classId);

    Optional<Waitlist> findTopByOrderByCreatedAtAsc();

    @Query("SELECT w FROM Waitlist w WHERE w.waitingClass.endTime < CURRENT_TIMESTAMP")
    List<Waitlist> findExpiredWaitlist();
}