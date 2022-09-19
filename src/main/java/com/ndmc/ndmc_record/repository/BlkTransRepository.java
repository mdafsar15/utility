package com.ndmc.ndmc_record.repository;

import com.ndmc.ndmc_record.model.BlkTransLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlkTransRepository extends JpaRepository<BlkTransLog, Long> {
}
