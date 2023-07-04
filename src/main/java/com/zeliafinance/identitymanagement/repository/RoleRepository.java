package com.zeliafinance.identitymanagement.repository;

import com.zeliafinance.identitymanagement.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Role findByRoleName(String roleName);

}
