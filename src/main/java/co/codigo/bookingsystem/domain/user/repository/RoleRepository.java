package co.codigo.bookingsystem.domain.user.repository;

import co.codigo.bookingsystem.common.enumerations.AppRole;
import co.codigo.bookingsystem.domain.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    @Query("SELECT r FROM Role r WHERE r.roleName = :roleName")
    Optional<Role> findByRoleName(@Param("roleName") AppRole roleName);

    boolean existsByRoleName(String roleName);

}