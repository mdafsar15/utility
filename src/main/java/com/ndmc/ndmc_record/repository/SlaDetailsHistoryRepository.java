package com.ndmc.ndmc_record.repository;

import com.ndmc.ndmc_record.model.SlaDetailsHistoryModel;
import com.ndmc.ndmc_record.model.SlaDetailsModel;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SlaDetailsHistoryRepository extends JpaRepository<SlaDetailsHistoryModel, Long> {

}