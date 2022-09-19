package com.ndmc.ndmc_record.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ndmc.ndmc_record.model.ReligionsModel;

@Repository
public interface ReligionsRepository extends JpaRepository<ReligionsModel, Long> {

	List<ReligionsModel> findByDeleteFlag(String deleteFlag);

}
