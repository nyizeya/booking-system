package co.codigo.bookingsystem.domain.booking.entity;

import co.codigo.bookingsystem.common.enumerations.BookingStatus;
import co.codigo.bookingsystem.domain.audit.Auditable;
import co.codigo.bookingsystem.domain.availableclass.entity.AvailableClass;
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
    private AvailableClass bookedClass;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.CONFIRMED;
    
    @Column(name = "booked_at", nullable = false)
    private LocalDateTime bookedAt = LocalDateTime.now();
    
    @Column(name = "checked_in")
    private Boolean checkedIn = false;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Column(name = "credit_refunded")
    private Boolean creditRefunded = false;
}