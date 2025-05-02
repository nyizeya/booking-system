package co.codigo.bookingsystem.domain.waitlist.service;

import co.codigo.bookingsystem.common.exceptions.ConflictException;
import co.codigo.bookingsystem.domain.classschedule.entity.ClassSchedule;
import co.codigo.bookingsystem.domain.user.service.UserService;
import co.codigo.bookingsystem.domain.waitlist.entity.Waitlist;
import co.codigo.bookingsystem.domain.waitlist.repository.WaitlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WaitlistService {

    private final WaitlistRepository waitlistRepository;
    private final UserService userService;

    @Transactional
    public Waitlist addToWaitlist(Long userId, ClassSchedule classSchedule) {
        log.info("Adding userId [{}] to waitlist for classId [{}]", userId, classSchedule.getId());

        if (waitlistRepository.existsByUserIdAndWaitingClassId(userId, classSchedule.getId())) {
            throw new ConflictException("User already in waitlist");
        }

        Waitlist waitlist = new Waitlist();
        waitlist.setUser(userService.getUserById(userId));
        waitlist.setWaitingClass(classSchedule);
        return waitlistRepository.save(waitlist);
    }

    @Transactional
    public void removeFromWaitlist(Waitlist waitlist) {
        log.info("Removing userId [{}] from waitlist for classId [{}]", waitlist.getUser().getId(), waitlist.getWaitingClass().getId());
        waitlistRepository.delete(waitlist);
    }
}