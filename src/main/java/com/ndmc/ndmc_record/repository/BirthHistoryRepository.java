package com.ndmc.ndmc_record.repository;

import com.ndmc.ndmc_record.model.BirthHistoryModel;
import com.ndmc.ndmc_record.model.BirthModel;
import com.ndmc.ndmc_record.model.SBirthHistoryModel;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BirthHistoryRepository extends JpaRepository<BirthHistoryModel, Long> {

    @Query(value="select * from birth_history where birth_id =:recordId order by birth_history_id desc limit 1", nativeQuery = true)
    BirthHistoryModel findLatestBirthHistory(Long recordId);

    List<BirthHistoryModel> findAll(Specification<BirthHistoryModel> specification);


}


