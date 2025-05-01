package co.codigo.bookingsystem.domain.user.service;

import co.codigo.bookingsystem.common.enumerations.AppRole;
import co.codigo.bookingsystem.domain.user.entity.Role;

import java.util.List;
import java.util.Optional;

public interface RoleService {

    Role createNewRole(AppRole appRole);

    Optional<Role> findByRoleName(AppRole role);

    List<Role> findAllRoles();

    boolean existsByRoleName(String roleName);

}