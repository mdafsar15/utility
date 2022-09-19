package com.ndmc.ndmc_record.repository;

import com.ndmc.ndmc_record.model.BirthModel;
import com.ndmc.ndmc_record.model.CitizenBirthModel;
import com.ndmc.ndmc_record.model.CitizenDeathModel;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CitizenDeathRepository extends JpaRepository<CitizenDeathModel, Long> {
    List<CitizenDeathModel> findByOrganizationIdAndStatus(Long organizationId, String recordStatusPending);

    CitizenDeathModel findByDeathIdTempAndStatusAndOrganizationId(long parseLong, String recordStatusPending, long parseLong1);
    List<CitizenDeathModel> findAll(Specification<CitizenDeathModel> specification);
}
