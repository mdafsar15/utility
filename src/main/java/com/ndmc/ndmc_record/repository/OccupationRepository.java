package com.ndmc.ndmc_record.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ndmc.ndmc_record.model.OccupationsModel;

@Repository
public interface OccupationRepository extends JpaRepository<OccupationsModel, Long> {

	List<OccupationsModel> findByDeleteFlag(String deleteFlag);

}
