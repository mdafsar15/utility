package com.ndmc.ndmc_record.repository;

import com.ndmc.ndmc_record.model.ActivityCodesModel;
import com.ndmc.ndmc_record.model.BlockchainRePushSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlockchainRePushRepository extends JpaRepository<BlockchainRePushSummary, Long> {

}