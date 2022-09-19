package com.ndmc.ndmc_record.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ndmc.ndmc_record.model.DeliveryMethodModel;

@Repository
public interface DeliveryMethodRepository extends JpaRepository<DeliveryMethodModel, Long> {

	List<DeliveryMethodModel> findByDeleteFlag(String deleteFlag);

}