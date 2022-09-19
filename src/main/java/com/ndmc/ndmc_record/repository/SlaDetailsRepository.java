package com.ndmc.ndmc_record.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ndmc.ndmc_record.model.SlaDetailsModel;

@Repository
public interface SlaDetailsRepository extends JpaRepository<SlaDetailsModel, Long> {

    //Optional<SlaDetailsModel> findByBndIdAndTransactionTypeAndCertificateTypeAndStatus(Long bndId, String transactionType,
    //		String certificateType, String status);

    List<SlaDetailsModel> findAll(Specification<SlaDetailsModel> specification);

    List<SlaDetailsModel> findByCertificateTypeAndStatus(String recordTypeDeath, String status);

    Optional<SlaDetailsModel> findByCertificateTypeAndBndId(String recordType, Long bndId);

    List<SlaDetailsModel> findByCertificateTypeAndTransactionTypeAndStatus(String recordType, String transactionType,
            String status);

    List<SlaDetailsModel> findBySlaDetailsIdInAndStatus(List<Long> slaDetailIds, String status);

    Optional<SlaDetailsModel> findBySlaDetailsIdAndBndIdAndTransactionTypeAndRecordTypeAndApplNo(Long slaId, Long bndId, String transactionType, String recordType, String applNo);

    Optional<SlaDetailsModel> findByBndIdAndTransactionTypeAndCertificateTypeAndStatusAndUserId(Long bndId, String transactionType, String certificateType, String status, String username);

    List<SlaDetailsModel> findBySlaOrganizationId(Long organizationId);

    @Query(value="SELECT sla_details_id FROM sla_details WHERE sla_organization_id=:organizationId", nativeQuery = true)
    List<Long> findSlaOrganizationId(Long organizationId);

    SlaDetailsModel findBySlaDetailsIdAndStatus(Long slaId, String recordStatusPending);

    List<SlaDetailsModel> findByStatusAndTransactionType(String recordStatusPending, String onlineBirthCorrection);
}