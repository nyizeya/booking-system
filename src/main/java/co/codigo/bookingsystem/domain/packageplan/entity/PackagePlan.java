package co.codigo.bookingsystem.domain.packageplan.entity;

import co.codigo.bookingsystem.domain.audit.Auditable;
import co.codigo.bookingsystem.domain.purchasedpkg.entity.PurchasedPackage;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "package_plans",
        uniqueConstraints = {
                @UniqueConstraint(name = "pkg_name", columnNames = "name"),
        }
)
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class PackagePlan extends Auditable implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    @Column(nullable = false)
    private Integer credits;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @OneToMany(mappedBy = "packagePlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchasedPackage> purchasedPackages = new ArrayList<>();

    public boolean isActive() {
        return expiryDate.isAfter(LocalDateTime.now());
    }

    public PackagePlan(String name, String countryCode, Integer credits, BigDecimal price, LocalDateTime expiryDate) {
        this.name = name;
        this.countryCode = countryCode;
        this.credits = credits;
        this.price = price;
        this.expiryDate = expiryDate;
    }
}