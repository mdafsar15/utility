package com.ndmc.ndmc_record.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ndmc.ndmc_record.model.MedicalAttentionsModel;

@Repository
public interface MedicalAttentionsRepository extends JpaRepository<MedicalAttentionsModel, Long> {

	List<MedicalAttentionsModel> findByDeleteFlag(String deleteFlag);

}