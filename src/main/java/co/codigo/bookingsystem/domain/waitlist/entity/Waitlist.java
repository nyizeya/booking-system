package co.codigo.bookingsystem.domain.waitlist.entity;

import co.codigo.bookingsystem.common.enumerations.WaitlistStatus;
import co.codigo.bookingsystem.domain.audit.Auditable;
import co.codigo.bookingsystem.domain.classschedule.entity.ClassSchedule;
import co.codigo.bookingsystem.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "waitlists")
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class Waitlist extends Auditable implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassSchedule waitingClass;
    
    @Column(nullable = false)
    private Integer position;
    
    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt = LocalDateTime.now();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WaitlistStatus status = WaitlistStatus.PENDING;

    public Waitlist(User user, ClassSchedule waitingClass, WaitlistStatus status, Integer position, LocalDateTime addedAt) {
        this.user = user;
        this.waitingClass = waitingClass;
        this.position = position;
        this.status = status;
        this.addedAt = addedAt;
    }
}