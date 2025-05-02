package co.codigo.bookingsystem.domain.classschedule.entity;

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
        name = "class_schedule",
        uniqueConstraints = {
                @UniqueConstraint(name = "class_name", columnNames = "name"),
        }
)
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class ClassSchedule extends Auditable implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;
    
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

    public ClassSchedule(String name, String countryCode, LocalDateTime startTime, LocalDateTime endTime, Integer requiredCredits, Integer maxCapacity, Integer durationMinutes) {
        this.name = name;
        this.countryCode = countryCode;
        this.requiredCredits = requiredCredits;
        this.maxCapacity = maxCapacity;
        this.durationMinutes = durationMinutes;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}