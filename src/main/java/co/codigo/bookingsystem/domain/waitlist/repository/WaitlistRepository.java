package co.codigo.bookingsystem.domain.waitlist.repository;

import co.codigo.bookingsystem.domain.waitlist.entity.Waitlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {
    boolean existsByUserIdAndWaitingClassId(Long userId, Long classId);

    Optional<Waitlist> findTopByOrderByCreatedAtAsc();
}