package co.codigo.bookingsystem.domain.waitlist.entity;

import co.codigo.bookingsystem.domain.audit.Auditable;
import co.codigo.bookingsystem.domain.classschedule.entity.ClassSchedule;
import co.codigo.bookingsystem.domain.packageplan.entity.PackagePlan;
import co.codigo.bookingsystem.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pkg_id", nullable = false)
    private PackagePlan packagePlan;

    public Waitlist(User user, ClassSchedule waitingClass, PackagePlan packagePlan) {
        this.user = user;
        this.waitingClass = waitingClass;
        this.packagePlan = packagePlan;
    }
}