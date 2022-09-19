package com.ndmc.ndmc_record.repository;

import com.ndmc.ndmc_record.model.BirthModel;

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
public interface BirthRepository extends JpaRepository<BirthModel, Long> {

    @Query(value="select * from birth where application_number =:applNo", nativeQuery = true)
    Optional<BirthModel> findByApplicationNumber(String applNo);

    @Query(value="select * from birth where status =:statusName && organization_code =:orgCode", nativeQuery = true)
    List<BirthModel> getBirthDataByStatusAndOrganization(String statusName, String orgCode);

    @Query(value="select * from birth where application_number =:applNo", nativeQuery = true)
    Optional<BirthModel> getBirthDetailsByApplNo(String applNo);

    @Query(value="Select distinct date(created_at), count(*) from birth where date(created_at) > current_date() - interval 7 day group by date(created_at)", nativeQuery = true)
    List<Object[]> get7DaysBirthRecord();

    @Query(value="Select distinct date(created_at), count(*) from birth where organization_code =:orgCode and date(created_at) > current_date() - interval 7 day group by date(created_at)", nativeQuery = true)
    List<Object[]> get7DaysBirthRecordByOrg(String orgCode);

    @Modifying(clearAutomatically = true)
    @Query(value="update birth b set b.status =:recordStatus, b.approved_by =:approvedBy, b.approved_at =:approvedAt where b.birth_id =:birthId", nativeQuery = true)
    int approveBirthStatusByBirthId(Long birthId, String recordStatus, String approvedBy, LocalDateTime approvedAt);

    @Modifying(clearAutomatically = true)
    @Query(value="update birth b set b.status =:recordStatus, b.rejected_by =:rejectedBy, b.rejected_at =:rejectedAt where b.birth_id =:birthId", nativeQuery = true)
    int rejectBirthStatusByBirthId(Long birthId, String recordStatus, String rejectedBy, LocalDateTime rejectedAt);

//    @Query(value="SELECT * FROM birth WHERE status=:status AND created_at <= (NOW() - INTERVAL :hour HOUR) ", nativeQuery = true)
//	List<BirthModel> getByStatusWithHour(@Param("status")  String status,@Param("hour") int hour);


    // ########### Filter Start #######################
    //############# 1. Filter Queries with all args Start#########################
    @Query(value="SELECT * from birth where organization_code =:orgCode and (application_number=:applicationNo OR registration_number=:registrationNo) and ( status =:draft and user_id =:userId) and (date(created_at) between :startDate and :endDate)", nativeQuery = true)
    List<BirthModel> filterBirthRecordsForHospitalCfcDraft(String registrationNo, String applicationNo, String startDate, String endDate, String orgCode, String draft, Long userId);

 // Filter query with all Args for CFC
//    @Query(value="SELECT * from birth where organization_code =:orgCode and (application_number=:applicationNo OR registration_number=:registrationNo) and ( status =:draft and user_id =:userId) and (date(created_at) between :startDate and :endDate)", nativeQuery = true)
//    List<BirthModel> filterBirthRecordsForCfcDraft(String registrationNo, String applicationNo, String startDate, String endDate, String orgCode, String draft, Long userId);

    //Filter Queries with all args
    @Query(value="SELECT * from birth where organization_code =:orgCode and (application_number=:applicationNo OR registration_number=:registrationNo) and status=:status and (date(created_at) between :startDate and :endDate)", nativeQuery = true)
    List<BirthModel> filterBirthRecordsForHospital(String registrationNo, String applicationNo, String startDate, String endDate, String status, String orgCode);


    //Filter Queries with all args For Admin
    @Query(value="SELECT * from birth where (application_number=:applicationNo or registration_number=:registrationNo) and status=:status and (date(created_at) between :startDate and :endDate)", nativeQuery = true)
    List<BirthModel> filterBirthRecordsForAdmin(String registrationNo, String applicationNo, String startDate, String endDate, String status);

    //Filter Queries with all args For Admin
    @Query(value="SELECT * from birth where application_number=:applicationNo and registration_number=:registrationNo and (date(created_at) between :startDate and :endDate) and ( status =:draft and user_id =:userId)", nativeQuery = true)
    List<BirthModel> filterBirthRecordsForAdminDraft(String registrationNo, String applicationNo, String startDate, String endDate, String draft, Long userId);
   //############# Filter Queries with all args End#########################

    //CFCDRAFT
  //  @Query(value="SELECT * from birth where application_number=:applicationNo and registration_number=:registrationNo and (date(created_at) between :startDate and :endDate) and ( status =:approved or (status =:draft and user_id =:userId) or ((status=:pending or status =:rejected) and organization_code =:orgCode)", nativeQuery = true)

    // ################# 2. Filter Queries with start and End Date Start #############################
    //Filter Queries with start and End Date Except Admin User
    //select * from birth where organization_code =:orgCode and (status =:pending or status =:rejected or ( status =:draft and user_id =:userId))

  //Filter For admin
    @Query(value="SELECT * from birth where (date(created_at) between :startDate and :endDate) and ( status !=:draft or ( status =:draft and user_id =:userId))", nativeQuery = true)
    List<BirthModel> filterBirthRecordsWithStarAndEndDateForAdmin(String startDate, String endDate);


    @Query(value="SELECT * from birth where (date(created_at) between :startDate and :endDate) and ( status =:draft and user_id =:userId))", nativeQuery = true)
    List<BirthModel> filterBirthRecordsWithStarAndEndDateForAdminDraft(String startDate, String endDate, String draft, Long userId);

    @Query(value="SELECT * from birth where organization_code =:orgCode and (date(created_at) between :startDate and :endDate) and ( status !=:draft or ( status =:draft and user_id =:userId))", nativeQuery = true)
    List<BirthModel> filterBirthRecordsWithStarAndEndDateForHospital(String orgCode, String startDate, String endDate, String draft, Long userId);


    @Query(value="SELECT * from birth where organization_code =:orgCode and (date(created_at) between :startDate and :endDate) and ( status !=:draft or ( status =:draft and user_id =:userId))", nativeQuery = true)
    List<BirthModel> filterBirthRecordsWithStarAndEndDateForHospitalCfcDraft(String orgCode, String startDate, String endDate, String draft, Long userId);

    // ************************** Filter Queries with start and End Date End *********************************

    // ################# 3. Filter Queries with start, End Date and Status Start #############################
    //Filter Queries with start and End Date Except Admin User
    //select * from birth where organization_code =:orgCode and (status =:pending or status =:rejected or ( status =:draft and user_id =:userId))

    //Filter For admin
    @Query(value="SELECT * from birth where (date(created_at) between :startDate and :endDate) and status =:status", nativeQuery = true)
    List<BirthModel> filterBirthRecordsWithStartEndDateStatusForAdmin(String startDate, String endDate, String status);


    @Query(value="SELECT * from birth where (date(created_at) between :startDate and :endDate) and ( status =:draft and user_id =:userId))", nativeQuery = true)
    List<BirthModel> filterBirthRecordsWithStartEndDateStatusForAdminDraft(String startDate, String endDate, String draft, Long userId);

    @Query(value="SELECT * from birth where organization_code =:orgCode and (date(created_at) between :startDate and :endDate) and status =:status", nativeQuery = true)
    List<BirthModel> filterBirthRecordsWithStartEndDateStatusForHospital(String orgCode, String startDate, String endDate, String status);


    @Query(value="SELECT * from birth where organization_code =:orgCode and (date(created_at) between :startDate and :endDate) and ( status =:draft and user_id =:userId)", nativeQuery = true)
    List<BirthModel> filterBirthRecordsWithStartEndDateStatusForHospitalCfcDraft(String orgCode, String startDate, String endDate, String draft, Long userId);

    // *********************** Filter Queries with start , status and End Date End ***************************


    // ################# 3. Filter Queries with start, End Date and Status Start #############################
    //Filter Queries with start and End Date Except Admin User
    //select * from birth where organization_code =:orgCode and (status =:pending or status =:rejected or ( status =:draft and user_id =:userId))

    //Filter For admin
    @Query(value="SELECT * from birth where (date(created_at) between :startDate and :endDate) and registration_number =:regNo", nativeQuery = true)
    List<BirthModel> filterBirthRecordsWithRegNoForAdmin(String startDate, String endDate, String regNo);


//    @Query(value="SELECT * from birth where (date(created_at) between :startDate and :endDate) and registration_number =:regNo or ( status =:draft and user_id =:userId))", nativeQuery = true)
//    List<BirthModel> filterBirthRecordsWithRegNoForAdminDraft(String startDate, String endDate, String draft, Long userId, String regNo);

    @Query(value="SELECT * from birth where organization_code =:orgCode and (date(created_at) between :startDate and :endDate) and registration_number =:regNo", nativeQuery = true)
    List<BirthModel> filterBirthRecordsWithRegNoForHospital(String orgCode, String startDate, String endDate, String regNo);


//    @Query(value="SELECT * from birth where organization_code =:orgCode and (date(created_at) between :startDate and :endDate) and registration_number =:regNo or ( status =:draft and user_id =:userId))", nativeQuery = true)
//    List<BirthModel> filterBirthRecordsWithRegNoForHospitalDraft(String orgCode, String startDate, String endDate, String draft, Long userId, String regNo);

    // *********************** Filter Queries with start , status and End Date End ***************************

    // ################# 4. Filter Queries with start,End and Applicatication Number #############################
    //Filter Queries with start and End Date Except Admin User
    //select * from birth where organization_code =:orgCode and (status =:pending or status =:rejected or ( status =:draft and user_id =:userId))

    //Filter For admin
    @Query(value="SELECT * from birth where (date(created_at) between :startDate and :endDate) and application_number =:appNo", nativeQuery = true)
    List<BirthModel> filterBirthRecordsWithAppNoForAdmin(String startDate, String endDate, String appNo);


//    @Query(value="SELECT * from birth where (date(created_at) between :startDate and :endDate) and application_number =:appNo or ( status =:draft and user_id =:userId))", nativeQuery = true)
//    List<BirthModel> filterBirthRecordsWithAppNoForAdminDraft(String startDate, String endDate, String draft, Long userId, String appNo);

    @Query(value="SELECT * from birth where organization_code =:orgCode and (date(created_at) between :startDate and :endDate) and application_number =:appNo", nativeQuery = true)
    List<BirthModel> filterBirthRecordsWithAppNoForHospital(String orgCode, String startDate, String endDate, String appNo);

//
//    @Query(value="SELECT * from birth where organization_code =:orgCode and (date(created_at) between :startDate and :endDate) and application_number =:appNo or ( status =:draft and user_id =:userId))", nativeQuery = true)
//    List<BirthModel> filterBirthRecordsWithAppNoForHospitalDraft(String orgCode, String startDate, String endDate, String draft, Long userId, String appNo);

    // *********************** Filter Queries with start , status and End Date End ***************************


    // ################# 4. Filter Queries with start,End and Applicatication Number #############################
    //Filter Queries with start and End Date Except Admin User
    //select * from birth where organization_code =:orgCode and (status =:pending or status =:rejected or ( status =:draft and user_id =:userId))

    //Filter For admin
    @Query(value="SELECT * from birth where (date(created_at) between :startDate and :endDate) and (application_number =:appNo and registration_number =:regNo)", nativeQuery = true)
    List<BirthModel> filterBirthRecordsWithoutStatusForAdmin(String startDate, String endDate, String appNo, String regNo);


    @Query(value="SELECT * from birth where (date(created_at) between :startDate and :endDate) and (application_number =:appNo and status =:status)", nativeQuery = true)
    List<BirthModel> filterBirthRecordsWithoutRegnoForAdmin(String startDate, String endDate, String appNo, String status);

    @Query(value="SELECT * from birth where (date(created_at) between :startDate and :endDate) and (registration_number =:regNo and status =:status)", nativeQuery = true)
    List<BirthModel> filterBirthRecordsWithoutApplNoForAdmin(String startDate, String endDate, String regNo, String status);

//
//    @Query(value="SELECT * from birth where (date(created_at) between :startDate and :endDate) and (application_number =:appNo and registration_number =:regNo) or ( status =:draft and user_id =:userId))", nativeQuery = true)
//    List<BirthModel> filterBirthRecordsWithoutStatusForAdminDraft(String startDate, String endDate, String draft, Long userId, String appNo, String regNo);

    @Query(value="SELECT * from birth where organization_code =:orgCode and (date(created_at) between :startDate and :endDate) and (application_number =:applNo and registration_number =:regNo)", nativeQuery = true)
    List<BirthModel> filterBirthRecordsWithoutStatusForHospital(String orgCode, String startDate, String endDate, String applNo, String regNo);


    @Query(value="SELECT * from birth where organization_code =:orgCode and (date(created_at) between :startDate and :endDate) and (application_number =:applNo and status =:status)", nativeQuery = true)
    List<BirthModel> filterBirthRecordsWithoutRegnoForHospital(String orgCode, String startDate, String endDate, String applNo, String status);

    @Query(value="SELECT * from birth where organization_code =:orgCode and (date(created_at) between :startDate and :endDate) and (registration_number =:regNo and status =:status)", nativeQuery = true)
    List<BirthModel> filterBirthRecordsWithoutApplNoForHospital(String orgCode, String startDate, String endDate, String regNo, String status);

//    @Query(value="SELECT * from birth where organization_code =:orgCode and (date(created_at) between :startDate and :endDate) and (application_number =:appNo and registration_number =:regNo) or ( status =:draft and user_id =:userId))", nativeQuery = true)
//    List<BirthModel> filterBirthRecordsWithoutStatusForHospitalDraft(String orgCode, String startDate, String endDate, String draft, Long userId, String appNo, String regNo);

    // *********************** Filter Queries with start , status and End Date End ***************************


    @Query(value="select * from birth where organization_code =:orgCode and (status =:pending or status =:rejected or ( status =:draft and user_id =:userId)) order by birth_id DESC", nativeQuery = true)
    List<BirthModel> getCreatorRecords(Long userId, String orgCode, String pending, String rejected, String draft);

    //org = xyz and (status = pending or ( status = draft and user=abc))
    @Query(value="select * from birth where organization_code =:orgCode and status =:pending order by birth_id DESC", nativeQuery = true)
    List<BirthModel> getApproverRecords(String orgCode, String pending);


    @Query(value="select * from birth where (status =:pending or status =:rejected or ( status =:recordStatusDraft and user_id =:userId)) order by birth_id DESC", nativeQuery = true)
    List<BirthModel> getAdminRecords(Long userId, String pending, String rejected, String recordStatusDraft);

    @Query(value="SELECT * FROM birth WHERE status=:status AND modified_at <= (NOW() - INTERVAL :hour HOUR) ", nativeQuery = true)
	List<BirthModel> getByStatusWithHour(@Param("status")  String status,@Param("hour") int hour);

    @Query(value="SELECT COUNT(*) FROM birth WHERE organization_code =:orgCode AND status=:pending", nativeQuery = true)
    Long getPendingRowsByOrganization(String pending, String orgCode);

    Long countByOrganizationCode(String organizationCode);

    //=======================Filter For CFC START ===========================================
    //Filter query with all Args for CFC
    @Query(value="SELECT * from birth where (application_number=:applicationNo OR registration_number=:registrationNo) and (date(created_at) between :startDate and :endDate) and status =:status", nativeQuery = true)
    List<BirthModel> filterBirthRecordsForCfc(String registrationNo, String applicationNo, String startDate, String endDate, String status);

    @Query(value="SELECT * from birth where (created_at between :startDate and :endDate) and ( status =:recordStatusApproved or (status =:recordStatusDraft and user_id =:userId) or (status=:recordStatusPending or status =:recordStatusRejected) and organization_code =:organizationCode)", nativeQuery = true)
    List<BirthModel> filterBirthRecordsWithStarAndEndDateForCfc(String organizationCode, Long userId, String startDate, String endDate, String recordStatusDraft, String recordStatusPending, String recordStatusApproved, String recordStatusRejected);

    @Query(value="SELECT * from birth where (created_at between :startDate and :endDate) and status =:status", nativeQuery = true)
    List<BirthModel> filterBirthRecordsWithStartEndDateStatusForCfc(String startDate, String endDate, String status);

    List<BirthModel> findAll(Specification<BirthModel> specification);

    @Query(value="SELECT * from birth where (created_at between :regStartDate and :regEndDate) and status =:recordStatusApproved", nativeQuery = true)
    List<BirthModel> findRecordsBetweenRegStartAndRegEndDate(LocalDate regStartDate, LocalDate regEndDate, String recordStatusApproved);


    @Query(value="SELECT gender_code, COUNT(*) FROM birth WHERE (created_at  between :regStartDate and :regEndDate) AND status=:recordStatusApproved AND gender_code IS NOT NULL GROUP BY (gender_code)", nativeQuery = true)
    List<Object[]> getTotalBirthRecords(LocalDate regStartDate, LocalDate regEndDate, String recordStatusApproved);


    @Query(value="SELECT gender_code,event_place_flag, COUNT(*) FROM birth WHERE (created_at between :regStartDate and :regEndDate) AND status=:recordStatusApproved GROUP BY gender_code, event_place_flag", nativeQuery = true)
    List<Object[]> getBirthRecordsByEventPlaceFlag(LocalDate regStartDate, LocalDate regEndDate, String recordStatusApproved);


//    @Query(value="SELECT * from birth where (date(created_at) between :startDate and :endDate) and ( status =:approved or (status =:draft and user_id =:userId) or ((status=:pending or status =:rejected) and organization_code =:orgCode)", nativeQuery = true)
//    List<BirthModel> filterBirthRecordsWithStartEndDateStatusForCfc(String status, String organizationCode, String startDate, String endDate, String draft, String pending, String approved, String rejected);

    @Query(value="SELECT * FROM birth WHERE modified_at > '2022-04-20' AND modified_at < '2022-04-28'", nativeQuery = true)
    List<BirthModel> getBlockchainReCorrectDate();

    @Query(value="SELECT Year(event_date), MONTH(event_date), MONTHNAME(event_date), gender_code, event_place_flag, Count(*) FROM birth WHERE  (event_date between :eventStartDate and :eventEndDate) AND status=:recordStatusApproved AND gender_code IS NOT null GROUP BY Year(event_date), MONTH(event_date), MONTHNAME(event_date), gender_code, event_place_flag ORDER BY Year(event_date), MONTH(event_date), gender_code", nativeQuery = true)
    List<Object[]> getMisCountsByEventDate(@Param("eventStartDate") LocalDateTime eventStartDate, @Param("eventEndDate") LocalDateTime eventEndDate, @Param("recordStatusApproved") String recordStatusApproved);

    @Query(value="SELECT Year(registration_datetime), MONTH(registration_datetime), MONTHNAME(registration_datetime), gender_code, event_place_flag, Count(*) FROM birth WHERE  (registration_datetime between :regStartDate and :regEndDate) AND status=:recordStatusApproved AND gender_code IS NOT null GROUP BY Year(registration_datetime), MONTH(registration_datetime), MONTHNAME(registration_datetime), gender_code, event_place_flag ORDER BY Year(registration_datetime), MONTH(registration_datetime), gender_code", nativeQuery = true)
    List<Object[]> getMisCountsByRegistrationDate(@Param("regStartDate") LocalDateTime regStartDate, @Param("regEndDate") LocalDateTime regEndDate, @Param("recordStatusApproved") String recordStatusApproved);

    @Query(value="SELECT event_date from birth where birth_id=:bndId", nativeQuery = true)
    LocalDateTime getDateOfEventByBirthId(Long bndId);
}


