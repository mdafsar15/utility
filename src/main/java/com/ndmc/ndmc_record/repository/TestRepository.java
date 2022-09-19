package com.ndmc.ndmc_record.repository;

import com.ndmc.ndmc_record.model.TestModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestRepository extends JpaRepository<TestModel, Long> {
}
