package com.ndmc.ndmc_record.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ndmc.ndmc_record.model.CauseofDeathModel;

@Repository
public interface CauseofDeathRepository extends JpaRepository<CauseofDeathModel, Long> {
    List<CauseofDeathModel> findByDeleteFlagAndCauseDescStartingWithIgnoreCase(String deleteFlag,String causeDesc);
    List<CauseofDeathModel> findByDeleteFlag(String deleteFlag);
}
