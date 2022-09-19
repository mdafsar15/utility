package com.ndmc.ndmc_record.repository;

import com.ndmc.ndmc_record.model.CertificateHistoryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CertificateHistoryRepository extends JpaRepository<CertificateHistoryModel, Long> {
 Optional<CertificateHistoryModel> findByUniquenum(String printApplicationNumber);
}
