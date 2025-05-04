package co.codigo.bookingsystem.domain.packageplan.entity;

import jakarta.persistence.Embeddable;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@NoArgsConstructor
public class UserPackageId implements Serializable {

    private Long userId;
    private Long packagePlanId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserPackageId)) return false;
        UserPackageId that = (UserPackageId) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(packagePlanId, that.packagePlanId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, packagePlanId);
    }

    public UserPackageId(Long userId, Long packagePlanId) {
        this.userId = userId;
        this.packagePlanId = packagePlanId;
    }
}
