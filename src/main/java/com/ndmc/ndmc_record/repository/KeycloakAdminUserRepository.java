package com.ndmc.ndmc_record.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ndmc.ndmc_record.model.KeycloakModel;
public interface KeycloakAdminUserRepository extends JpaRepository<KeycloakModel, Long> {


}