package co.codigo.bookingsystem.domain.booking.entity;

import co.codigo.bookingsystem.domain.audit.Auditable;
import co.codigo.bookingsystem.domain.classschedule.entity.ClassSchedule;
import co.codigo.bookingsystem.domain.purchasedpkg.entity.PurchasedPackage;
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
@Table(
        name = "bookings",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "class_id"}),
        }
)
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class Booking extends Auditable implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassSchedule bookedClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchased_package_id", nullable = false)
    private PurchasedPackage purchasedPackage;
    
    @Column(name = "booked_at", nullable = false)
    private LocalDateTime bookedAt = LocalDateTime.now();
    
    @Column(name = "checked_in")
    private Boolean checkedIn = false;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Column(name = "credit_refunded")
    private Boolean creditRefunded = false;

    public Booking(User user, ClassSchedule bookedClass, LocalDateTime bookedAt) {
        this.user = user;
        this.bookedClass = bookedClass;
        this.bookedAt = bookedAt;

    }
}