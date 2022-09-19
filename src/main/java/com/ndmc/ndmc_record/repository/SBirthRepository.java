package com.ndmc.ndmc_record.repository;

import com.ndmc.ndmc_record.model.BirthModel;
import com.ndmc.ndmc_record.model.DeathModel;
import com.ndmc.ndmc_record.model.SBirthModel;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SBirthRepository extends JpaRepository<SBirthModel, Long> {


    // ########### Filter Start #######################
    //############# 1. Filter Queries with all args Start#########################
    @Query(value="SELECT * from still_birth where organization_code =:orgCode and (application_number=:applicationNo OR registration_number=:registrationNo) and ( status =:draft and user_id =:userId) and (date(created_at) between :startDate and :endDate)", nativeQuery = true)
    List<SBirthModel> filterSbirthRecordsForHospitalDraft(String registrationNo, String applicationNo, String startDate, String endDate, String orgCode, String draft, Long userId);

    //Filter Queries with all args
    @Query(value="SELECT * from still_birth where organization_code =:orgCode and (application_number=:applicationNo OR registration_number=:registrationNo) and status=:status and (date(created_at) between :startDate and :endDate)", nativeQuery = true)
    List<SBirthModel> filterSbirthRecordsForHospital(String registrationNo, String applicationNo, String startDate, String endDate, String status, String orgCode);

    //Filter Queries with all args For Admin
    @Query(value="SELECT * from still_birth where (application_number=:applicationNo or registration_number=:registrationNo) and status=:status and (date(created_at) between :startDate and :endDate)", nativeQuery = true)
    List<SBirthModel> filterSbirthRecordsForAdmin(String registrationNo, String applicationNo, String startDate, String endDate, String status);

    //Filter Queries with all args For Admin
    @Query(value="SELECT * from still_birth where application_number=:applicationNo and registration_number=:registrationNo and (date(created_at) between :startDate and :endDate) and ( status =:draft and user_id =:userId)", nativeQuery = true)
    List<SBirthModel> filterSbirthRecordsForAdminDraft(String registrationNo, String applicationNo, String startDate, String endDate, String draft, Long userId);
    //############# Filter Queries with all args End#########################

    // ################# 2. Filter Queries with start and End Date Start #############################
    //Filter Queries with start and End Date Except Admin User
    //select * from birth where organization_code =:orgCode and (status =:pending or status =:rejected or ( status =:draft and user_id =:userId))

    //Filter For admin
    @Query(value="SELECT * from still_birth where (date(created_at) between :startDate and :endDate) and ( status !=:draft or ( status =:draft and user_id =:userId))", nativeQuery = true)
    List<SBirthModel> filterSbirthRecordsWithStarAndEndDateForAdmin(String startDate, String endDate);


    @Query(value="SELECT * from still_birth where (date(created_at) between :startDate and :endDate) and ( status =:draft and user_id =:userId))", nativeQuery = true)
    List<SBirthModel> filterSbirthRecordsWithStarAndEndDateForAdminDraft(String startDate, String endDate, String draft, Long userId);

    @Query(value="SELECT * from still_birth where organization_code =:orgCode and (date(created_at) between :startDate and :endDate) and ( status !=:draft or ( status =:draft and user_id =:userId))", nativeQuery = true)
    List<SBirthModel> filterSbirthRecordsWithStarAndEndDateForHospital(String orgCode, String startDate, String endDate, String draft, Long userId);


    @Query(value="SELECT * from still_birth where organization_code =:orgCode and (date(created_at) between :startDate and :endDate) and ( status !=:draft or ( status =:draft and user_id =:userId))", nativeQuery = true)
    List<SBirthModel> filterSbirthRecordsWithStarAndEndDateForHospitalDraft(String orgCode, String startDate, String endDate, String draft, Long userId);

    // ************************** Filter Queries with start and End Date End *********************************

    // ################# 3. Filter Queries with start, End Date and Status Start #############################
    //Filter Queries with start and End Date Except Admin User
    //select * from birth where organization_code =:orgCode and (status =:pending or status =:rejected or ( status =:draft and user_id =:userId))

    //Filter For admin
    @Query(value="SELECT * from still_birth where (date(created_at) between :startDate and :endDate) and status =:status", nativeQuery = true)
    List<SBirthModel> filterSbirthRecordsWithStartEndDateStatusForAdmin(String startDate, String endDate, String status);


    @Query(value="SELECT * from still_birth where (date(created_at) between :startDate and :endDate) and ( status =:draft and user_id =:userId))", nativeQuery = true)
    List<SBirthModel> filterSbirthRecordsWithStartEndDateStatusForAdminDraft(String startDate, String endDate, String draft, Long userId);

    @Query(value="SELECT * from still_birth where organization_code =:orgCode and (date(created_at) between :startDate and :endDate) and status =:status", nativeQuery = true)
    List<SBirthModel> filterSbirthRecordsWithStartEndDateStatusForHospital(String orgCode, String startDate, String endDate, String status);


    @Query(value="SELECT * from still_birth where organization_code =:orgCode and (date(created_at) between :startDate and :endDate) and ( status =:draft and user_id =:userId)", nativeQuery = true)
    List<SBirthModel> filterSbirthRecordsWithStartEndDateStatusForHospitalDraft(String orgCode, String startDate, String endDate, String draft, Long userId);

    // *********************** Filter Queries with start , status and End Date End ***************************


    // ################# 3. Filter Queries with start, End Date and Status Start #############################
    //Filter Queries with start and End Date Except Admin User
    //select * from birth where organization_code =:orgCode and (status =:pending or status =:rejected or ( status =:draft and user_id =:userId))

    //Filter For admin
    @Query(value="SELECT * from still_birth where (date(created_at) between :startDate and :endDate) and registration_number =:regNo", nativeQuery = true)
    List<SBirthModel> filterSbirthRecordsWithRegNoForAdmin(String startDate, String endDate, String regNo);


//    @Query(value="SELECT * from birth where (date(created_at) between :startDate and :endDate) and registration_number =:regNo or ( status =:draft and user_id =:userId))", nativeQuery = true)
//    List<BirthModel> filterSbirthRecordsWithRegNoForAdminDraft(String startDate, String endDate, String draft, Long userId, String regNo);

    @Query(value="SELECT * from still_birth where organization_code =:orgCode and (date(created_at) between :startDate and :endDate) and registration_number =:regNo", nativeQuery = true)
    List<SBirthModel> filterSbirthRecordsWithRegNoForHospital(String orgCode, String startDate, String endDate, String regNo);


//    @Query(value="SELECT * from birth where organization_code =:orgCode and (date(created_at) between :startDate and :endDate) and registration_number =:regNo or ( status =:draft and user_id =:userId))", nativeQuery = true)
//    List<SBirthModel> filterBirthRecordsWithRegNoForHospitalDraft(String orgCode, String startDate, String endDate, String draft, Long userId, String regNo);

    // *********************** Filter Queries with start , status and End Date End ***************************

    // ################# 4. Filter Queries with start,End and Applicatication Number #############################
    //Filter Queries with start and End Date Except Admin User
    //select * from birth where organization_code =:orgCode and (status =:pending or status =:rejected or ( status =:draft and user_id =:userId))

    //Filter For admin
    @Query(value="SELECT * from still_birth where (date(created_at) between :startDate and :endDate) and application_number =:appNo", nativeQuery = true)
    List<SBirthModel> filterSbirthRecordsWithAppNoForAdmin(String startDate, String endDate, String appNo);


//    @Query(value="SELECT * from birth where (date(created_at) between :startDate and :endDate) and application_number =:appNo or ( status =:draft and user_id =:userId))", nativeQuery = true)
//    List<BirthModel> filterBirthRecordsWithAppNoForAdminDraft(String startDate, String endDate, String draft, Long userId, String appNo);

    @Query(value="SELECT * from still_birth where organization_code =:orgCode and (date(created_at) between :startDate and :endDate) and application_number =:appNo", nativeQuery = true)
    List<SBirthModel> filterSbirthRecordsWithAppNoForHospital(String orgCode, String startDate, String endDate, String appNo);

//
//    @Query(value="SELECT * from birth where organization_code =:orgCode and (date(created_at) between :startDate and :endDate) and application_number =:appNo or ( status =:draft and user_id =:userId))", nativeQuery = true)
//    List<BirthModel> filterBirthRecordsWithAppNoForHospitalDraft(String orgCode, String startDate, String endDate, String draft, Long userId, String appNo);

    // *********************** Filter Queries with start , status and End Date End ***************************


    // ################# 4. Filter Queries with start,End and Applicatication Number #############################
    //Filter Queries with start and End Date Except Admin User
    //select * from birth where organization_code =:orgCode and (status =:pending or status =:rejected or ( status =:draft and user_id =:userId))

    //Filter For admin
    @Query(value="SELECT * from still_birth where (date(created_at) between :startDate and :endDate) and (application_number =:appNo and registration_number =:regNo)", nativeQuery = true)
    List<SBirthModel> filterSbirthRecordsWithoutStatusForAdmin(String startDate, String endDate, String appNo, String regNo);

    @Query(value="SELECT * from still_birth where (date(created_at) between :startDate and :endDate) and (application_number =:appNo and status =:status)", nativeQuery = true)
    List<SBirthModel> filterSbirthRecordsWithoutRegnoForAdmin(String startDate, String endDate, String appNo, String status);

    @Query(value="SELECT * from still_birth where (date(created_at) between :startDate and :endDate) and (registration_number =:regNo and status =:status)", nativeQuery = true)
    List<SBirthModel> filterSbirthRecordsWithoutApplNoForAdmin(String startDate, String endDate, String regNo, String status);

//
//    @Query(value="SELECT * from birth where (date(created_at) between :startDate and :endDate) and (application_number =:appNo and registration_number =:regNo) or ( status =:draft and user_id =:userId))", nativeQuery = true)
//    List<BirthModel> filterBirthRecordsWithoutStatusForAdminDraft(String startDate, String endDate, String draft, Long userId, String appNo, String regNo);

    @Query(value="SELECT * from still_birth where organization_code =:orgCode and (date(created_at) between :startDate and :endDate) and (application_number =:appNo and registration_number =:regNo)", nativeQuery = true)
    List<SBirthModel> filterSbirthRecordsWithoutStatusForHospital(String orgCode, String startDate, String endDate, String appNo, String regNo);

    @Query(value="SELECT * from still_birth where organization_code =:orgCode and (date(created_at) between :startDate and :endDate) and (application_number =:appNo and status =:status)", nativeQuery = true)
    List<SBirthModel> filterSbirthRecordsWithoutRegnoForHospital(String orgCode, String startDate, String endDate, String appNo, String status);

    @Query(value="SELECT * from still_birth where organization_code =:orgCode and (date(created_at) between :startDate and :endDate) and (registration_number =:regNo and status =:status)", nativeQuery = true)
    List<SBirthModel> filterSbirthRecordsWithoutApplNoForHospital(String orgCode, String startDate, String endDate, String regNo, String status);




    //Filter query with all Args for CFC
    @Query(value="SELECT * from still_birth where (application_number=:applicationNo OR registration_number=:registrationNo) and (date(created_at) between :startDate and :endDate) and status =:status", nativeQuery = true)
    List<SBirthModel> filterSbirthRecordsForCfc(String registrationNo, String applicationNo, String startDate, String endDate, String status);

    @Query(value="SELECT * from still_birth where (date(created_at) between :startDate and :endDate) and ( status =:recordStatusApproved or (status =:recordStatusDraft and user_id =:userId) or (status=:recordStatusPending or status =:recordStatusRejected) and organization_code =:organizationCode)", nativeQuery = true)
    List<SBirthModel> filterSbirthRecordsWithStarAndEndDateForCfc(String organizationCode, Long userId, String startDate, String endDate, String recordStatusDraft, String recordStatusPending, String recordStatusApproved, String recordStatusRejected);

    @Query(value="SELECT * from still_birth where (date(created_at) between :startDate and :endDate) and status =:status", nativeQuery = true)
    List<SBirthModel> filterSbirthRecordsWithStartEndDateStatusForCfc(String startDate, String endDate, String status);


//    @Query(value="SELECT * from birth where organization_code =:orgCode and (date(created_at) between :startDate and :endDate) and (application_number =:appNo and registration_number =:regNo) or ( status =:draft and user_id =:userId))", nativeQuery = true)
//    List<BirthModel> filterBirthRecordsWithoutStatusForHospitalDraft(String orgCode, String startDate, String endDate, String draft, Long userId, String appNo, String regNo);

    // *********************** Filter Queries with start , status and End Date End ***************************


    @Query(value="select * from still_birth where status =:statusName && organization_code =:orgCode", nativeQuery = true)
    List<SBirthModel> getSBirthDataByStatusAndOrganization(String statusName, String orgCode);

    @Modifying(clearAutomatically = true)
    @Query(value="update still_birth b set b.status =:recordStatus where b.sbirth_id =:id", nativeQuery = true)
    int updateSBirthStatusById(Long id, String recordStatus);

    @Modifying(clearAutomatically = true)
    @Query(value="update still_birth b set b.status =:recordStatus, b.approved_by =:approvedBy, b.approved_at =:approvedAt where b.application_number =:applNo", nativeQuery = true)
    int approveSBirthStatusByApplNo(String applNo, String recordStatus, String approvedBy, LocalDateTime approvedAt);

    @Modifying(clearAutomatically = true)
    @Query(value="update still_birth b set b.status =:recordStatus, b.rejected_by =:rejectedBy, b.rejected_at =:rejectedAt where b.application_number =:applNo", nativeQuery = true)
    int rejectSBirthStatusByApplNo(String applNo, String recordStatus, String rejectedBy, LocalDateTime rejectedAt);


    @Query(value="select * from still_birth where organization_code =:orgCode and (status =:pending or status =:rejected or ( status =:draft and user_id =:userId)) order by sbirth_id DESC", nativeQuery = true)
    List<SBirthModel> getCreatorRecords(Long userId, String orgCode, String pending, String rejected, String draft);

    //org = xyz and (status = pending or ( status = draft and user=abc))
    @Query(value="select * from still_birth where organization_code =:orgCode and status =:pending order by sbirth_id DESC", nativeQuery = true)
    List<SBirthModel> getApproverRecords(String orgCode, String pending);

    @Query(value="select * from still_birth where (status =:pending or status =:rejected or ( status =:recordStatusDraft and user_id =:userId)) order by sbirth_id DESC", nativeQuery = true)
    List<SBirthModel> getAdminRecords(Long userId, String pending, String rejected,String recordStatusDraft);

    @Query(value="SELECT * FROM still_birth WHERE status=:status AND modified_at <= (NOW() - INTERVAL :hour HOUR) ", nativeQuery = true)
    List<SBirthModel> getByStatusWithHour(@Param("status")  String status, @Param("hour") int hour);

    @Query(value="SELECT COUNT(*) FROM still_birth WHERE organization_code =:orgCode AND status=:pending", nativeQuery = true)
    Long getPendingRowsByOrganization(String pending, String orgCode);

    List<SBirthModel> findAll(Specification<SBirthModel> specification);

    Long countByOrganizationCode(String organizationCode);

    @Query(value="SELECT * from still_birth where (created_at between :regStartDate and :regEndDate) and status =:recordStatusApproved", nativeQuery = true)
    List<SBirthModel> findRecordsBetweenRegStartAndRegEndDate(LocalDate regStartDate, LocalDate regEndDate, String recordStatusApproved);

    @Query(value="SELECT gender_code, COUNT(*) FROM still_birth WHERE (created_at  between :regStartDate and :regEndDate) AND status=:recordStatusApproved AND gender_code IS NOT NULL GROUP BY (gender_code)", nativeQuery = true)
    List<Object[]> getTotalSBirthRecords(LocalDate regStartDate, LocalDate regEndDate, String recordStatusApproved);

    @Query(value="SELECT gender_code,event_place_flag, COUNT(*) FROM still_birth WHERE (created_at between :regStartDate and :regEndDate) AND status=:recordStatusApproved GROUP BY gender_code, event_place_flag", nativeQuery = true)
    List<Object[]> getSbirthRecordsByEventPlaceFlag(LocalDate regStartDate, LocalDate regEndDate, String recordStatusApproved);

    @Query(value="SELECT * FROM still_birth WHERE modified_at > '2022-04-20' AND modified_at < '2022-04-28'", nativeQuery = true)
    List<SBirthModel> getBlockchainReCorrectDate();

    Optional<SBirthModel> findByApplicationNumber(String applNo);
}
