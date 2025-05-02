package co.codigo.bookingsystem.domain.waitlist.service;

import co.codigo.bookingsystem.common.exceptions.ConflictException;
import co.codigo.bookingsystem.domain.classschedule.entity.ClassSchedule;
import co.codigo.bookingsystem.domain.user.entity.User;
import co.codigo.bookingsystem.domain.waitlist.entity.Waitlist;
import co.codigo.bookingsystem.domain.waitlist.repository.WaitlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WaitlistService {

    private final WaitlistRepository waitlistRepository;

    public Optional<Waitlist> getTheOldestEntry() {
        return waitlistRepository.findTopByOrderByCreatedAtAsc();
    }

    @Transactional
    public void addToWaitlist(User user, ClassSchedule classSchedule) {
        log.info("Adding userId [{}] to waitlist for classId [{}]", user.getId(), classSchedule.getId());

        if (waitlistRepository.existsByUserIdAndWaitingClassId(user.getId(), classSchedule.getId())) {
            throw new ConflictException("User already in waitlist");
        }

        Waitlist waitlist = new Waitlist(user, classSchedule);
        waitlistRepository.save(waitlist);
    }

    @Transactional
    public void removeFromWaitlist(Waitlist waitlist) {
        log.info("Removing userId [{}] from waitlist for classId [{}]", waitlist.getUser().getId(), waitlist.getWaitingClass().getId());
        waitlistRepository.delete(waitlist);
    }
}