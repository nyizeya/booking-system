package co.codigo.bookingsystem.domain.waitlist.repository;

import co.codigo.bookingsystem.domain.waitlist.entity.Waitlist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {
    boolean existsByUserIdAndWaitingClassId(Long userId, Long classId);
}