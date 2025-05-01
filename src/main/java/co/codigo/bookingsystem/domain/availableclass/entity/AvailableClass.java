package co.codigo.bookingsystem.domain.availableclass.entity;

import co.codigo.bookingsystem.domain.audit.Auditable;
import co.codigo.bookingsystem.domain.booking.entity.Booking;
import co.codigo.bookingsystem.domain.waitlist.entity.Waitlist;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "available_class",
        uniqueConstraints = {
                @UniqueConstraint(name = "class_name", columnNames = "name"),
        }
)
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class AvailableClass extends Auditable implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Column(name = "required_credits", nullable = false)
    private Integer requiredCredits;
    
    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity;
    
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
    
    @OneToMany(mappedBy = "bookedClass", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "waitingClass", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Waitlist> waitlists = new ArrayList<>();
}