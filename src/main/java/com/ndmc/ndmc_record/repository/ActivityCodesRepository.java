package com.ndmc.ndmc_record.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ndmc.ndmc_record.model.ActivityCodesModel;
@Repository
public interface ActivityCodesRepository extends JpaRepository<ActivityCodesModel, Long> {

	List<ActivityCodesModel> findByDeleteFlag(String deleteFlag);

}