package com.ndmc.ndmc_record.repository;

import com.ndmc.ndmc_record.model.CitizenDeathModel;
import com.ndmc.ndmc_record.model.CitizenSBirthModel;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CitizenSBirthRepository extends JpaRepository<CitizenSBirthModel, Long> {
    List<CitizenSBirthModel> findByOrganizationIdAndStatus(Long organizationId, String recordStatusPending);


    CitizenSBirthModel findBySbirthIdTempAndStatusAndOrganizationId(long parseLong, String recordStatusPending, long parseLong1);

    List<CitizenSBirthModel> findAll(Specification<CitizenSBirthModel> specification);
}
