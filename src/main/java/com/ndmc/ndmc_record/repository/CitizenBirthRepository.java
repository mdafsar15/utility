package com.ndmc.ndmc_record.repository;

import com.ndmc.ndmc_record.model.BirthModel;
import com.ndmc.ndmc_record.model.CitizenBirthModel;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CitizenBirthRepository extends JpaRepository<CitizenBirthModel, Long> {
    List<CitizenBirthModel> findByOrganizationIdAndStatus(Long organizationId, String recordStatusPending);

    CitizenBirthModel findByBirthIdTempAndStatusAndOrganizationId(long parseLong, String recordStatusPending, long parseLong1);
    List<CitizenBirthModel> findAll(Specification<CitizenBirthModel> specification);
}
