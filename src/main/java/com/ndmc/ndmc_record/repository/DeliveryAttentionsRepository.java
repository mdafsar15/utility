package com.ndmc.ndmc_record.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ndmc.ndmc_record.model.DeliveryAttentionsModel;
import com.ndmc.ndmc_record.model.LiteracyModel;
@Repository
public interface DeliveryAttentionsRepository extends JpaRepository<DeliveryAttentionsModel, Long> {

	List<DeliveryAttentionsModel> findByDeleteFlag(String deleteFlag);

}