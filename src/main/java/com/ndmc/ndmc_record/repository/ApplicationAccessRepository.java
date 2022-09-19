package com.ndmc.ndmc_record.repository;

import com.ndmc.ndmc_record.model.ApplicationAccessControlModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationAccessRepository extends JpaRepository<ApplicationAccessControlModel, Long> {

    ApplicationAccessControlModel findByKeycloakAuth(String yes);
}
