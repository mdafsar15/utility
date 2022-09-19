package com.ndmc.ndmc_record.repository;

import java.util.List;
import java.util.Optional;

import com.ndmc.ndmc_record.model.OrganizationModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends JpaRepository<OrganizationModel, Long> {

    List<OrganizationModel> findByOrganizationType(String organizationType);

    List<OrganizationModel> findByOrganizationTypeAndStatus(String cFC, String status);

    List<OrganizationModel> findByStatus(String status);


}