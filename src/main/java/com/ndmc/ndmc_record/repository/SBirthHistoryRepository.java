package com.ndmc.ndmc_record.repository;

import com.ndmc.ndmc_record.model.BirthHistoryModel;
import com.ndmc.ndmc_record.model.SBirthHistoryModel;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SBirthHistoryRepository extends JpaRepository<SBirthHistoryModel, Long> {

    @Query(value="select * from still_birth_history where sbirth_id =:recordId order by sbirth_history_id desc limit 1", nativeQuery = true)
    SBirthHistoryModel findLatestSBirthHistory(Long recordId);

    List<SBirthHistoryModel> findAll(Specification<SBirthHistoryModel> specification);
}
