package com.ndmc.ndmc_record.repository;

import com.ndmc.ndmc_record.model.LateFee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LateFeeRepository extends JpaRepository<LateFee, String> {
}
