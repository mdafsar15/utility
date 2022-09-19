package com.ndmc.ndmc_record.repository;

import com.ndmc.ndmc_record.model.CauseofDeathModel;
import com.ndmc.ndmc_record.model.CertificatePrintModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CertificatePrintRepository extends JpaRepository<CertificatePrintModel, Long> {

    @Query(value="select * from certificate_print where record_id =:recordId and record_type=:recordType order by print_sequence_no desc limit 1", nativeQuery = true)
    CertificatePrintModel findLatestPrint(Long recordId, String recordType);

    List<CertificatePrintModel> findByPrintApplicationNumber(String printApplicationNumber);

    //@Query(value="SELECT gender_code, COUNT(*) FROM birth WHERE (created_at  between :regStartDate and :regEndDate) AND status=:recordStatusApproved GROUP BY (gender_code)", nativeQuery = true)
    @Query(value="SELECT c.record_type, SUBSTRING(c.print_application_number, 1,1)  pr_by, COUNT(application_number) no_of_copies \n" +
            "FROM certificate_print  c \n" +
            "WHERE SUBSTRING(c.print_application_number, 1,1) IN ('H', 'O', 'C') AND (c.printed_at >=:printDateStart and" +
            " c.printed_at <=:printDateEnd) GROUP BY c.record_type, pr_by;", nativeQuery = true)
    List<Object[]> getAllCertificatesCount(LocalDateTime printDateStart, LocalDateTime printDateEnd);


}
