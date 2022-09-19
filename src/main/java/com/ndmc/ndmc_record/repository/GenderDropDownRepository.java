package com.ndmc.ndmc_record.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ndmc.ndmc_record.model.GendersModel;

@Repository
public interface GenderDropDownRepository extends JpaRepository<GendersModel, Long> {

	List<GendersModel> findByDeleteFlag(String deleteFlag);

}
