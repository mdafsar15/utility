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
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeathRepository extends JpaRepository<DeathModel, Long> {
    @Query(value="select * from death where status =:statusName && organization_code =:orgCode", nativeQuery = true)
    List<DeathModel> getDeathDataByStatusAndOrganization(String statusName, String orgCode);

    @Modifying(clearAutomatically = true)
    @Query(value="update death d set d.status =:recordStatus where d.death_id =:id", nativeQuery = true)
    int updateDeathStatusById(Long id, String recordStatus);

    @Query(value="select * from death where application_number =:applNo", nativeQuery = true)
    Optional<DeathModel> getDeathDetailsByApplNo(String applNo);

    @Query(value="select * from death where application_number =:applNo", nativeQuery = true)
    Optional<DeathModel> findByApplicationNumber(String applNo);

    @Query(value="Select distinct date(created_at), count(*) from death where date(created_at) > current_date() - interval 7 day group by date(created_at)", nativeQuery = true)
    List<Object[]> get7DaysDeathRecord();


    @Query(value="Select distinct date(created_at), count(*) from death where organization_code =:orgCode and date(created_at) > current_date() - interval 7 day group by date(created_at)", nativeQuery = true)
    List<Object[]> get7DaysDeathRecordByOrg(String orgCode);

    @Modifying(clearAutomatically = true)
    @Query(value="update death b set b.status =:recordStatus, b.approved_by =:approvedBy, b.approved_at =:approvedAt where b.application_number =:applNo", nativeQuery = true)
    int approveDeathStatusByApplNo(String applNo, String recordStatus, String approvedBy, LocalDateTime approvedAt);

    @Modifying(clearAutomatically = true)
    @Query(value="update death b set b.status =:recordStatus, b.rejected_by =:rejectedBy, b.rejected_at =:rejectedAt where b.application_number =:applNo", nativeQuery = true)
    int rejectDeathStatusByApplNo(String applNo, String recordStatus, String rejectedBy, LocalDateTime rejectedAt);



    @Query(value="select * from death where organization_code =:orgCode and (status =:pending or status =:rejected or ( status =:draft and user_id =:userId)) order by death_id DESC", nativeQuery = true)
    List<DeathModel> getCreatorRecords(Long userId, String orgCode, String pending, String rejected, String draft);

    //org = xyz and (status = pending or ( status = draft and user=abc))
    @Query(value="select * from death where organization_code =:orgCode and status =:pending order by death_id DESC", nativeQuery = true)
    List<DeathModel> getApproverRecords(String orgCode, String pending);

    @Query(value="select * from death where (status =:pending or status =:rejected or ( status =:recordStatusDraft and user_id =:userId)) order by death_id DESC", nativeQuery = true)
    List<DeathModel> getAdminRecords(Long userId, String pending, String rejected,String recordStatusDraft);



    // ########### Filter Start #######################
    //############# 1. Filter Queries with all args Start#########################
    @Query(value="SELECT * from birth where organization_code =:orgCode and (application_number=:applicationNo OR registration_number=:registrationNo) and ( status =:draft and user_id =:userId) and (date(created_at) between :startDate and :endDate)", nativeQuery = true)
    List<DeathModel> filterDeathRecordsForHospitalCfcDraft(String registrationNo, String applicationNo, String startDate, String endDate, String orgCode, String draft, Long userId);

    //Filter Queries with all args
    @Query(value="SELECT * from birth where organization_code =:orgCode and (application_number=:applicationNo OR registration_number=:registrationNo) and status=:status and (date(created_at) between :startDate and :endDate)", nativeQuery = true)
    List<DeathModel> filterDeathRecordsForHospital(String registrationNo, String applicationNo, String startDate, String endDate, String status, String orgCode);

    //Filter Queries with all args For Admin
    @Query(value="SELECT * from birth where (application_number=:applicationNo or registration_number=:registrationNo) and status=:status and (date(created_at) between :startDate and :endDate)", nativeQuery = true)
    List<DeathModel> filterDeathRecordsForAdmin(String registrationNo, String applicationNo, String startDate, String endDate, String status);

    //Filter Queries with all args For Admin
    @Query(value="SELECT * from death where application_number=:applicationNo and registration_number=:registrationNo and (date(created_at) between :startDate and :endDate) and ( status =:draft and user_id =:userId)", nativeQuery = true)
    List<DeathModel> filterDeathRecordsForAdminDraft(String registrationNo, String applicationNo, String startDate, String endDate, String draft, Long userId);
    //############# Filter Queries with all args End#########################

    // ################# 2. Filter Queries with start and End Date Start #############################
    //Filter Queries with start and End Date Except Admin User
    //select * from birth where organization_code =:orgCode and (status =:pending or status =:rejected or ( status =:draft and user_id =:userId))

    //Filter For admin
    @Query(value="SELECT * from death where (date(created_at) between :startDate and :endDate) and ( status !=:draft or ( status =:draft and user_id =:userId))", nativeQuery = true)
    List<DeathModel> filterDeathRecordsWithStarAndEndDateForAdmin(String startDate, String endDate);


    @Query(value="SELECT * from death where (date(created_at) between :startDate and :endDate) and ( status =:draft and user_id =:userId))", nativeQuery = true)
    List<DeathModel> filterDeathRecordsWithStarAndEndDateForAdminDraft(String startDate, String endDate, String draft, Long userId);

    @Query(value="SELECT * from death where organization_code =:orgCode and (date(created_at) between :startDate and :endDate) and ( status !=:draft or ( status =:draft and user_id =:userId))", nativeQuery = true)
    List<DeathModel> filterDeathRecordsWithStarAndEndDateForHospital(String orgCode, String startDate, String endDate, String draft, Long userId);


    @Query(value="SELECT * from death where organization_code =:orgCode and (date(created_at) between :startDate and :endDate) and ( status !=:draft or ( status =:draft and user_id =:userId))", nativeQuery = true)
    List<DeathModel> filterDeathRecordsWithStarAndEndDateForHospitalDraft(String orgCode, String startDate, String endDate, String draft, Long userId);

    // ************************** Filter Queries with start and End Date End *********************************

    // ################# 3. Filter Queries with start, End Date and Status Start #############################
    //Filter Queries with start and End Date Except Admin User
    //select * from birth where organization_code =:orgCode and (status =:pending or status =:rejected or ( status =:draft and user_id =:userId))

    //Filter For admin
    @Query(value="SELECT * from death where (date(created_at) between :startDate and :endDate) and status =:status", nativeQuery = true)
    List<DeathModel> filterDeathRecordsWithStartEndDateStatusForAdmin(String startDate, String endDate, String status);


    @Query(value="SELECT * from death where (date(created_at) between :startDate and :endDate) and ( status =:draft and user_id =:userId))", nativeQuery = true)
    List<DeathModel> filterDeathRecordsWithStartEndDateStatusForAdminDraft(String startDate, String endDate, String draft, Long userId);

    @Query(value="SELECT * from death where organization_code =:orgCode and (date(created_at) between :startDate and :endDate) and status =:status", nativeQuery = true)
    List<DeathModel> filterDeathRecordsWithStartEndDateStatusForHospital(String orgCode, String startDate, String endDate, String status);


    @Query(value="SELECT * from death where organization_code =:orgCode and (date(created_at) between :startDate and :endDate) and ( status =:draft and user_id =:userId)", nativeQuery = true)
    List<DeathModel> filterDeathRecordsWithStartEndDateStatusForHospitalDraft(String orgCode, String startDate, String endDate, String draft, Long userId);

    // *********************** Filter Queries with start , status and End Date End ***************************

   // Filter conditions
   // 1. StartDate and End date
   // 2. Reg No and App No
   // 3. All args ()

    // ################# 3. Filter Queries with start, End Date and Status Start #############################
    //Filter Queries with start and End Date Except Admin User
    //select * from birth where organization_code =:orgCode and (status =:pending or status =:rejected or ( status =:draft and user_id =:userId))

    //Filter For admin
    @Query(value="SELECT * from death where (date(created_at) between :startDate and :endDate) and registration_number =:regNo", nativeQuery = true)
    List<DeathModel> filterDeathRecordsWithRegNoForAdmin(String startDate, String endDate, String regNo);


//    @Query(value="SELECT * from birth where (date(created_at) between :startDate and :endDate) and registration_number =:regNo or ( status =:draft and user_id =:userId))", nativeQuery = true)
//    List<BirthModel> filterBirthRecordsWithRegNoForAdminDraft(String startDate, String endDate, String draft, Long userId, String regNo);

    @Query(value="SELECT * from death where organization_code =:orgCode and (date(created_at) between :startDate and :endDate) and registration_number =:regNo", nativeQuery = true)
    List<DeathModel> filterDeathRecordsWithRegNoForHospital(String orgCode, String startDate, String endDate, String regNo);


//    @Query(value="SELECT * from birth where organization_code =:orgCode and (date(created_at) between :startDate and :endDate) and registration_number =:regNo or ( status =:draft and user_id =:userId))", nativeQuery = true)
//    List<BirthModel> filterBirthRecordsWithRegNoForHospitalDraft(String orgCode, String startDate, String endDate, String draft, Long userId, String regNo);

    // *********************** Filter Queries with start , status and End Date End ***************************

    // ################# 4. Filter Queries with start,End and Applicatication Number #############################
    //Filter Queries with start and End Date Except Admin User
    //select * from birth where organization_code =:orgCode and (status =:pending or status =:rejected or ( status =:draft and user_id =:userId))

    //Filter For admin
    @Query(value="SELECT * from death where (date(created_at) between :startDate and :endDate) and application_number =:appNo", nativeQuery = true)
    List<DeathModel> filterDeathRecordsWithAppNoForAdmin(String startDate, String endDate, String appNo);


//    @Query(value="SELECT * from birth where (date(created_at) between :startDate and :endDate) and application_number =:appNo or ( status =:draft and user_id =:userId))", nativeQuery = true)
//    List<BirthModel> filterBirthRecordsWithAppNoForAdminDraft(String startDate, String endDate, String draft, Long userId, String appNo);

    @Query(value="SELECT * from death where organization_code =:orgCode and (date(created_at) between :startDate and :endDate) and application_number =:appNo", nativeQuery = true)
    List<DeathModel> filterDeathRecordsWithAppNoForHospital(String orgCode, String startDate, String endDate, String appNo);

//
//    @Query(value="SELECT * from birth where organization_code =:orgCode and (date(created_at) between :startDate and :endDate) and application_number =:appNo or ( status =:draft and user_id =:userId))", nativeQuery = true)
//    List<BirthModel> filterBirthRecordsWithAppNoForHospitalDraft(String orgCode, String startDate, String endDate, String draft, Long userId, String appNo);

    // *********************** Filter Queries with start , status and End Date End ***************************


    // ################# 4. Filter Queries with start,End and Applicatication Number #############################
    //Filter Queries with start and End Date Except Admin User
    //select * from birth where organization_code =:orgCode and (status =:pending or status =:rejected or ( status =:draft and user_id =:userId))

    //Filter For admin
    @Query(value="SELECT * from death where (date(created_at) between :startDate and :endDate) and (application_number =:appNo and registration_number =:regNo)", nativeQuery = true)
    List<DeathModel> filterDeathRecordsWithoutStatusForAdmin(String startDate, String endDate, String appNo, String regNo);


    @Query(value="SELECT * from death where (date(created_at) between :startDate and :endDate) and (application_number =:appNo and status =:status)", nativeQuery = true)
    List<DeathModel> filterDeathRecordsWithoutRegnoForAdmin(String startDate, String endDate, String appNo, String status);

    @Query(value="SELECT * from death where (date(created_at) between :startDate and :endDate) and (registration_number =:regNo and status =:status)", nativeQuery = true)
    List<DeathModel> filterDeathRecordsWithoutApplNoForAdmin(String startDate, String endDate, String regNo, String status);

//
//    @Query(value="SELECT * from birth where (date(created_at) between :startDate and :endDate) and (application_number =:appNo and registration_number =:regNo) or ( status =:draft and user_id =:userId))", nativeQuery = true)
//    List<BirthModel> filterBirthRecordsWithoutStatusForAdminDraft(String startDate, String endDate, String draft, Long userId, String appNo, String regNo);

    @Query(value="SELECT * from death where organization_code =:orgCode and (date(created_at) between :startDate and :endDate) and (application_number =:appNo and registration_number =:regNo)", nativeQuery = true)
    List<DeathModel> filterDeathRecordsWithoutStatusForHospital(String orgCode, String startDate, String endDate, String appNo, String regNo);

    @Query(value="SELECT * from death where organization_code =:orgCode and (date(created_at) between :startDate and :endDate) and (application_number =:appNo and status =:status)", nativeQuery = true)
    List<DeathModel> filterDeathRecordsWithoutRegnoForHospital(String orgCode, String startDate, String endDate, String appNo, String status);

    @Query(value="SELECT * from death where organization_code =:orgCode and (date(created_at) between :startDate and :endDate) and (registration_number =:regNo and status =:status)", nativeQuery = true)
    List<DeathModel> filterDeathRecordsWithoutApplNoForHospital(String orgCode, String startDate, String endDate, String regNo, String status);


    Long countByOrganizationCode(String organizationCode);





//    @Query(value="SELECT * from birth where organization_code =:orgCode and (date(created_at) between :startDate and :endDate) and (application_number =:appNo and registration_number =:regNo) or ( status =:draft and user_id =:userId))", nativeQuery = true)
//    List<BirthModel> filterBirthRecordsWithoutStatusForHospitalDraft(String orgCode, String startDate, String endDate, String draft, Long userId, String appNo, String regNo);




    //Filter query with all Args for CFC
    @Query(value="SELECT * from death where (application_number=:applicationNo OR registration_number=:registrationNo) and (date(created_at) between :startDate and :endDate) and status =:status", nativeQuery = true)
    List<DeathModel> filterDeathRecordsForCfc(String registrationNo, String applicationNo, String startDate, String endDate, String status);

    @Query(value="SELECT * from death where (date(created_at) between :startDate and :endDate) and ( status =:recordStatusApproved or (status =:recordStatusDraft and user_id =:userId) or (status=:recordStatusPending or status =:recordStatusRejected) and organization_code =:organizationCode)", nativeQuery = true)
    List<DeathModel> filterDeathRecordsWithStarAndEndDateForCfc(String organizationCode, Long userId, String startDate, String endDate, String recordStatusDraft, String recordStatusPending, String recordStatusApproved, String recordStatusRejected);

    @Query(value="SELECT * from death where (date(created_at) between :startDate and :endDate) and status =:status", nativeQuery = true)
    List<DeathModel> filterDeathRecordsWithStartEndDateStatusForCfc(String startDate, String endDate, String status);


    // *********************** Filter Queries with start , status and End Date End ***************************


    @Query(value="SELECT * FROM death WHERE status=:status AND modified_at <= (NOW() - INTERVAL :hour HOUR) ", nativeQuery = true)
    List<DeathModel> getByStatusWithHour(@Param("status")  String status, @Param("hour") int hour);

    @Query(value="SELECT COUNT(*) FROM death WHERE organization_code =:orgCode AND status=:pending", nativeQuery = true)
    Long getPendingRowsByOrganization(String pending, String orgCode);

    List<DeathModel> findAll(Specification<DeathModel> specification);

    List<DeathModel> findByStatus(String status);

    @Query(value="SELECT * from death where (created_at between :regStartDate and :regEndDate) and status =:recordStatusApproved", nativeQuery = true)
    List<DeathModel> findRecordsBetweenRegStartAndRegEndDate(LocalDate regStartDate, LocalDate regEndDate, String recordStatusApproved);

    @Query(value="SELECT gender_code, COUNT(*) as count FROM death WHERE (created_at between :regStartDate and :regEndDate) AND status=:recordStatusApproved and deceased_age <= 1 GROUP BY gender_code", nativeQuery = true)
    List<Object[]> getInfantDeathReportLessThanOne(LocalDate regStartDate, LocalDate regEndDate, String recordStatusApproved);


    @Query(value="SELECT gender_code, COUNT(*) as count FROM death WHERE (created_at between :regStartDate and :regEndDate) AND status=:recordStatusApproved and deceased_age >= 1 and deceased_age < 5 GROUP BY gender_code", nativeQuery = true)
    List<Object[]> getInfantDeathReportOneToFive(LocalDate regStartDate, LocalDate regEndDate, String recordStatusApproved);

    @Query(value="SELECT gender_code, COUNT(*) FROM death WHERE (created_at between :regStartDate and :regEndDate) AND status=:recordStatusApproved AND gender_code IS NOT NULL GROUP BY (gender_code)", nativeQuery = true)
    List<Object[]> getTotalDeathRecords(LocalDate regStartDate, LocalDate regEndDate, String recordStatusApproved);


    @Query(value="SELECT gender_code,event_place_flag, COUNT(*) FROM death WHERE (created_at between :regStartDate and :regEndDate) AND status=:recordStatusApproved GROUP BY gender_code, event_place_flag", nativeQuery = true)
    List<Object[]> getDeathRecordsByEventPlaceFlag(LocalDate regStartDate, LocalDate regEndDate, String recordStatusApproved);

    @Query(value="SELECT * FROM death WHERE modified_at > '2022-04-20' AND modified_at < '2022-04-28'", nativeQuery = true)
    List<DeathModel> getBlockchainReCorrectDate();
    
    @Query(value="SELECT Year(event_date), MONTH(event_date), MONTHNAME(event_date), gender_code, event_place_flag, Count(*) FROM death WHERE  (event_date between :eventStartDate and :eventEndDate) AND status=:recordStatusApproved AND gender_code IS NOT null GROUP BY Year(event_date), MONTH(event_date), MONTHNAME(event_date), gender_code, event_place_flag ORDER BY Year(event_date), MONTH(event_date), gender_code", nativeQuery = true)
    List<Object[]> getMisCountsByEventDate(@Param("eventStartDate") LocalDateTime eventStartDate, @Param("eventEndDate") LocalDateTime eventEndDate, @Param("recordStatusApproved") String recordStatusApproved);

    @Query(value="SELECT Year(registration_datetime), MONTH(registration_datetime), MONTHNAME(registration_datetime), gender_code, event_place_flag, Count(*) FROM death WHERE  (registration_datetime between :regStartDate and :regEndDate) AND status=:recordStatusApproved AND gender_code IS NOT null GROUP BY Year(registration_datetime), MONTH(registration_datetime), MONTHNAME(registration_datetime), gender_code, event_place_flag ORDER BY Year(registration_datetime), MONTH(registration_datetime), gender_code", nativeQuery = true)
    List<Object[]> getMisCountsByRegistrationDate(@Param("regStartDate") LocalDateTime regStartDate, @Param("regEndDate") LocalDateTime regEndDate, @Param("recordStatusApproved") String recordStatusApproved);

}
