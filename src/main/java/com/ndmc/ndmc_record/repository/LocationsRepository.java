package com.ndmc.ndmc_record.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ndmc.ndmc_record.model.LocationsModel;

@Repository
public interface LocationsRepository extends JpaRepository<LocationsModel, String> {

	List<LocationsModel> findByDeleteFlag(String deleteFlag);

}