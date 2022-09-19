package com.ndmc.ndmc_record.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ndmc.ndmc_record.model.ActivityCodesModel;
import com.ndmc.ndmc_record.model.MaritalStatusModel;

@Repository
public interface MaritalStatusRepository  extends JpaRepository<MaritalStatusModel, Long> {

	List<MaritalStatusModel> findByDeleteFlag(String deleteFlag);

}