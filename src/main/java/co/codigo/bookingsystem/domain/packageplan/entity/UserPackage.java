package co.codigo.bookingsystem.domain.packageplan.entity;

import co.codigo.bookingsystem.domain.audit.Auditable;
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
@Table(name = "user_packages")
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class UserPackage extends Auditable implements Serializable {

    @EmbeddedId
    private UserPackageId id = new UserPackageId();

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @MapsId("packagePlanId")
    @JoinColumn(name = "package_plan_id")
    private PackagePlan packagePlan;

    @Column(nullable = false)
    private Integer remainingCredits;
}
