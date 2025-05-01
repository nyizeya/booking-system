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
    
    @Query("SELECT w FROM Waitlist w WHERE w.waitingClass.id = :classId " +
           "AND w.status = 'PENDING' ORDER BY w.addedAt ASC")
    List<Waitlist> findPendingWaitlistForClass(@Param("classId") Long classId);
    
    @Query("SELECT w FROM Waitlist w WHERE w.user.id = :userId " +
           "AND w.waitingClass.startTime > :now AND w.status = 'PENDING'")
    List<Waitlist> findActiveWaitlistsForUser(@Param("userId") Long userId, @Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(w) FROM Waitlist w WHERE w.waitingClass.id = :classId " +
           "AND w.status = 'PENDING'")
    int countPendingWaitlistForClass(@Param("classId") Long classId);
    
    @Modifying
    @Query("UPDATE Waitlist w SET w.status = 'PROMOTED' " +
           "WHERE w.id = :waitlistId AND w.status = 'PENDING'")
    int promoteWaitlistEntry(@Param("waitlistId") Long waitlistId);
    
    @Modifying
    @Query("UPDATE Waitlist w SET w.status = 'EXPIRED' " +
           "WHERE w.waitingClass.id = :classId AND w.status = 'PENDING'")
    int expireWaitlistForClass(@Param("classId") Long classId);

    Optional<Waitlist> findTopByWaitingClassIdOrderByAddedAtAsc(Long classId);
    List<Waitlist> findByWaitingClassId(Long classId);

    boolean existsByUserIdAndWaitingClassId(Long userId, Long classId);
}