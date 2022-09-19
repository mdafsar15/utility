package com.ndmc.ndmc_record.repository;

import com.ndmc.ndmc_record.model.UserRoleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRoleModel, Long> {

    boolean existsByRoleName(String roleName);

    Set<UserRoleModel> findByRoleIdIn(List<Long> roles);

    List<UserRoleModel> findByType(String userType);

    // UserRoleModel findByName(String roleName);
    //UserRoleModel findByRoleName(String roleName);
}
