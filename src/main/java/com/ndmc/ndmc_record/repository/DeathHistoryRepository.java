package com.ndmc.ndmc_record.repository;

import com.ndmc.ndmc_record.model.BirthHistoryModel;
import com.ndmc.ndmc_record.model.DeathHistoryModel;
import com.ndmc.ndmc_record.model.DeathModel;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeathHistoryRepository extends JpaRepository<DeathHistoryModel, Long> {

    @Query(value="select * from death_history where death_id =:recordId order by death_history_id desc limit 1", nativeQuery = true)
    DeathHistoryModel findLatestDeathHistory(Long recordId);

    List<DeathHistoryModel> findAll(Specification<DeathHistoryModel> specification);

}
