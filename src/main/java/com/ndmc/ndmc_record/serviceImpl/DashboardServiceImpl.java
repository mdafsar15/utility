package com.ndmc.ndmc_record.serviceImpl;

import com.ndmc.ndmc_record.config.Constants;
import com.ndmc.ndmc_record.dto.*;
import com.ndmc.ndmc_record.exception.DateRangeException;
import com.ndmc.ndmc_record.model.*;
import com.ndmc.ndmc_record.repository.*;
import com.ndmc.ndmc_record.service.DashboardService;
import com.ndmc.ndmc_record.utils.CommonUtil;
import com.ndmc.ndmc_record.utils.CustomBeanUtils;
import com.ndmc.ndmc_record.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class DashboardServiceImpl implements DashboardService {

    private final Logger logger = LoggerFactory.getLogger(DashboardServiceImpl.class);
    @Autowired
    BirthRepository birthRepository;

    @Autowired
    DeathRepository deathRepository;
    @Autowired
    SBirthRepository sBirthRepository;
    @Autowired
    AuthRepository authRepository;
    @Autowired
    AuthServiceImpl authService;
    @Autowired
    OrganizationRepository organizationRepository;

    @Override
    public ApiResponse getAllRecords(HttpServletRequest request) {

        Long  totalBirthRows = 0L;
        Long  totalDeathRows = 0L;
        Long  totalSBirthRows = 0L;

        Long totalPendingBirth = 0L;
        Long totalPendingSBirth = 0L;
        Long totalPendingDeath = 0L;

        //  JwtUtil jwtUtil = new JwtUtil();
        String userName = authService.getUserIdFromRequest(request);
        Optional<UserModel> currentUserOp = authRepository.findById(Long.parseLong(userName));
        UserModel currentUser = currentUserOp.get();

        // Get User type from OrganizationId
        String orgId = currentUser.getOrganizationId();
        Optional<OrganizationModel> organizationModelOp = organizationRepository.findById(Long.parseLong(orgId));
        OrganizationModel organizationModel = organizationModelOp.get();
        String orgType = organizationModel.getOrganizationType();
        String orgCode = organizationModel.getOrganisationCode();

        List<Object[]> last7DaysBirthCounts = new ArrayList<>();
        List<Object[]> last7DaysDeathRecords = new ArrayList<>();

        if (currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_ADMIN))){
            totalBirthRows = birthRepository.count();
            totalDeathRows = deathRepository.count();
            totalSBirthRows = sBirthRepository.count();
            last7DaysBirthCounts = birthRepository.get7DaysBirthRecord();
            last7DaysDeathRecords = deathRepository.get7DaysDeathRecord();
        }
        else if (currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_APPROVER))
                ||
                currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_APPROVER))
                ||
                currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CREATOR))
                ||
                currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_CREATOR))
                ||
                currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_REGISTRAR))){


            totalBirthRows = birthRepository.countByOrganizationCode(orgCode);
            logger.info("===== total birth rows ===="+totalBirthRows);
            totalDeathRows = deathRepository.countByOrganizationCode(orgCode);
            totalSBirthRows = sBirthRepository.countByOrganizationCode(orgCode);
            totalPendingBirth = birthRepository.getPendingRowsByOrganization(Constants.RECORD_STATUS_PENDING, orgCode);
            totalPendingSBirth = sBirthRepository.getPendingRowsByOrganization(Constants.RECORD_STATUS_PENDING, orgCode);
            totalPendingDeath = deathRepository.getPendingRowsByOrganization(Constants.RECORD_STATUS_PENDING, orgCode);

            last7DaysBirthCounts = birthRepository.get7DaysBirthRecordByOrg(orgCode);
            last7DaysDeathRecords = deathRepository.get7DaysDeathRecordByOrg(orgCode);
        }

        else
        {

            totalBirthRows = birthRepository.countByOrganizationCode(orgCode);
            logger.info("===== total birth rows ===="+totalBirthRows);
            totalDeathRows = deathRepository.countByOrganizationCode(orgCode);
            totalSBirthRows = sBirthRepository.countByOrganizationCode(orgCode);

            last7DaysBirthCounts = birthRepository.get7DaysBirthRecordByOrg(orgCode);
            last7DaysDeathRecords = deathRepository.get7DaysDeathRecordByOrg(orgCode);
        }
          Map<LocalDate, BigInteger> birthMap = CommonUtil.getMapFromList(last7DaysBirthCounts);
          Map<LocalDate, BigInteger> deathMap = CommonUtil.getMapFromList(last7DaysDeathRecords);
 //        Long  totalSBirthRows = sBirthRepository.count();

        LocalDate curDate = LocalDate.now();
        //loop for 7 days
        if(last7DaysBirthCounts.size() < 7) {

            for (Long i = 0L; i < 7; i++) {
             // set 0 if date not exist in map
               LocalDate date =  curDate.minusDays(i);
               //BigInteger count = birthMap.get(date);

               if(!birthMap.containsKey(date)){
                   birthMap.put(date, BigInteger.valueOf(0));
               }
                if(!deathMap.containsKey(date)){
                    deathMap.put(date, BigInteger.valueOf(0));
                }
            }
        }

        Map<String, Object> result = new HashMap<String, Object>();
        Map<String, Long> total = new HashMap<String, Long>();
        Map<String, Long> pending = new HashMap<String, Long>();
        Map<String, Object> sevenDays = new HashMap<String, Object>();
        //Map<String, Long> S = new HashMap<String, Long>();

        total.put("Birth", totalBirthRows);
        total.put("Death", totalDeathRows);
        total.put("SBirth", totalSBirthRows);

        pending.put("Birth", totalPendingBirth);
        pending.put("Death", totalPendingDeath);
        pending.put("SBirth", totalPendingSBirth);

        result.put("Total", total);
        result.put("Pending", pending);
       sevenDays.put("Birth", birthMap);
        sevenDays.put("Death", deathMap);
        result.put("SevenDays", sevenDays);
        ApiResponse res = new ApiResponse();
        res.setMsg("All Data is");
        res.setStatus(HttpStatus.ACCEPTED);
        res.setData(result);
        return res;
    }

    @Override
    public ApiResponse getFilteredData(FilterDto filterDto, String type, HttpServletRequest request) {

        ApiResponse response = new ApiResponse();
        JwtUtil jwtUtil = new JwtUtil();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String startDate = String.valueOf(filterDto.getStartDate());
        String endDate = String.valueOf(filterDto.getEndDate());

        String registrationNo = filterDto.getRegistrationNumber();
        String applicationNo = filterDto.getApplicationNumber();
        String status = filterDto.getStatus();

        String userName = authService.getUserIdFromRequest(request);
        Optional<UserModel> currentUserOp = authRepository.findById(Long.parseLong(userName));
        UserModel currentUser = currentUserOp.get();



        // Get User type from OrganizationId
        String orgId = currentUser.getOrganizationId();
        Optional<OrganizationModel> organizationModelOp = organizationRepository.findById(Long.parseLong(orgId));
        OrganizationModel organizationModel = organizationModelOp.get();
        String orgType = organizationModel.getOrganizationType();
        String orgCode = organizationModel.getOrganisationCode();

       // System.out.println("====== startDate===="+startDate+"=======Enddate========"+endDate);
        if(!CommonUtil.checkNullOrBlank(registrationNo) || !CommonUtil.checkNullOrBlank(applicationNo)) {
            LocalDate dateObj = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String date = dateObj.format(formatter);
            startDate = "0001-01-01";
            endDate   = date;
        }
        else if(CommonUtil.checkNullOrBlank(startDate)
                || CommonUtil.checkNullOrBlank(endDate))
        {
            response.setStatus(HttpStatus.BAD_REQUEST);
            response.setMsg(Constants.START_END_DATE_MANDATORY);
            return response;
        }else {
            CommonUtil.betweenDates(filterDto.getStartDate().atTime(00,00,00),filterDto.getEndDate().atTime(23,59,59));
        }


        if(type.equalsIgnoreCase(Constants.RECORD_TYPE_BIRTH)){

            List<BirthModel> birthRecords = new ArrayList<BirthModel>();
            logger.info("=========FILTER DTO IS ========"+filterDto+"==Type is==="+type);

            if(!CommonUtil.checkNullOrBlank(registrationNo) && !CommonUtil.checkNullOrBlank(applicationNo) && !CommonUtil.checkNullOrBlank(status)){
                if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_ADMIN) )) {

                    // If Status is draft and requested by Admin
                    if(status.equalsIgnoreCase(Constants.RECORD_STATUS_DRAFT)){
                        birthRecords = birthRepository.filterBirthRecordsForAdminDraft(registrationNo, applicationNo, startDate, endDate, Constants.RECORD_STATUS_DRAFT, currentUser.getUserId());

                    }
                    // If Status is Not Draft and requested by Admin
                    else{
                        birthRecords = birthRepository.filterBirthRecordsForAdmin(registrationNo, applicationNo, startDate, endDate, status);
                    }

                }
                else if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_CREATOR))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_APPROVER))) {

                    // If Status is draft and requested by Admin
                    if(status.equalsIgnoreCase(Constants.RECORD_STATUS_DRAFT)){
                        birthRecords = birthRepository.filterBirthRecordsForHospitalCfcDraft(registrationNo, applicationNo, startDate, endDate,
                                orgCode, Constants.RECORD_STATUS_DRAFT, currentUser.getUserId());
                    }
                    // If Status is not draft and requested by CREATOR, APPROVER
                    else if(status.equalsIgnoreCase(Constants.RECORD_STATUS_PENDING) || status.equalsIgnoreCase(Constants.RECORD_STATUS_REJECTED)) {
                        birthRecords = birthRepository.filterBirthRecordsForHospital(registrationNo, applicationNo, startDate, endDate, status, orgCode);
                        }

                    else if(status.equalsIgnoreCase(Constants.RECORD_STATUS_APPROVED)) {
                        birthRecords = birthRepository.filterBirthRecordsForCfc(registrationNo, applicationNo, startDate, endDate, status);
                    }


                }

                else{

                    // If Status is draft and requested by CREATOR, APPROVER
                    if(status.equalsIgnoreCase(Constants.RECORD_STATUS_DRAFT)){
                        birthRecords = birthRepository.filterBirthRecordsForHospitalCfcDraft(registrationNo, applicationNo, startDate, endDate,
                                orgCode, Constants.RECORD_STATUS_DRAFT, currentUser.getUserId());
                    }
                    // If Status is not draft and requested by CREATOR, APPROVER
                    else{
                        birthRecords = birthRepository.filterBirthRecordsForHospital(registrationNo, applicationNo, startDate, endDate, status, orgCode);
                    }

                }
            }
            else if(CommonUtil.checkNullOrBlank(registrationNo) && CommonUtil.checkNullOrBlank(applicationNo)
                    && CommonUtil.checkNullOrBlank(status)){

                if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_ADMIN) )) {

                    // If Status is draft and requested by Admin
                    if(status.equalsIgnoreCase(Constants.RECORD_STATUS_DRAFT)){
                        birthRecords = birthRepository.filterBirthRecordsWithStarAndEndDateForAdminDraft(startDate, endDate, Constants.RECORD_STATUS_DRAFT, currentUser.getUserId());
                    }
                    // If Status is Not Draft and requested by Admin
                    else{
                        birthRecords = birthRepository.filterBirthRecordsWithStarAndEndDateForAdmin(startDate, endDate);
                    }

                }
                else if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_CREATOR))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_APPROVER))){

                    if(status.equalsIgnoreCase(Constants.RECORD_STATUS_DRAFT)){
                        birthRecords = birthRepository.filterBirthRecordsWithStarAndEndDateForHospitalCfcDraft(orgCode, startDate, endDate, Constants.RECORD_STATUS_DRAFT, currentUser.getUserId());

                    }
                    // If Status is not draft and requested by CREATOR, APPROVER
                    else{
                        birthRecords = birthRepository.filterBirthRecordsWithStarAndEndDateForCfc(orgCode, currentUser.getUserId() ,startDate, endDate,
                                Constants.RECORD_STATUS_PENDING, Constants.RECORD_STATUS_DRAFT,
                                Constants.RECORD_STATUS_APPROVED, Constants.RECORD_STATUS_REJECTED);
                    }
                }
                else{

                    // If Status is draft and requested by CREATOR, APPROVER
                    if(status.equalsIgnoreCase(Constants.RECORD_STATUS_DRAFT)){
                        // logger.info("==== start, enddate with status========"+orgCode, startDate, endDate, Constants.RECORD_STATUS_DRAFT, currentUser.getUserId());
                        birthRecords = birthRepository.filterBirthRecordsWithStarAndEndDateForHospitalCfcDraft(orgCode, startDate, endDate, Constants.RECORD_STATUS_DRAFT, currentUser.getUserId());

                    }
                    // If Status is not draft and requested by CREATOR, APPROVER
                    else{
                        birthRecords = birthRepository.filterBirthRecordsWithStarAndEndDateForHospital(orgCode,startDate, endDate, Constants.RECORD_STATUS_DRAFT, currentUser.getUserId());
                    }
                }

            }
            else if(CommonUtil.checkNullOrBlank(registrationNo) && CommonUtil.checkNullOrBlank(applicationNo) && !CommonUtil.checkNullOrBlank(status)){
               // birthRecords = birthRepository.filterBirthRecordsWithStartEndDateAndStatus(startDate, endDate, status);

                if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_ADMIN) )) {

                    // If Status is draft and requested by Admin
                    if(status.equalsIgnoreCase(Constants.RECORD_STATUS_DRAFT)){
                        birthRecords = birthRepository.filterBirthRecordsWithStartEndDateStatusForAdminDraft(startDate, endDate, Constants.RECORD_STATUS_DRAFT, currentUser.getUserId());
                    }
                    // If Status is Not Draft and requested by Admin
                    else{
                        birthRecords = birthRepository.filterBirthRecordsWithStartEndDateStatusForAdmin(startDate, endDate, status);
                    }

                }
                else if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_CREATOR))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_APPROVER))){
                    if(status.equalsIgnoreCase(Constants.RECORD_STATUS_DRAFT)){
                        birthRecords = birthRepository.filterBirthRecordsWithStartEndDateStatusForHospitalCfcDraft(orgCode, startDate, endDate, Constants.RECORD_STATUS_DRAFT, currentUser.getUserId());
                    }
                    // If Status is not draft and requested by CREATOR, APPROVER
                    else if(status.equalsIgnoreCase(Constants.RECORD_STATUS_PENDING) || status.equalsIgnoreCase(Constants.RECORD_STATUS_REJECTED)){
                        birthRecords = birthRepository.filterBirthRecordsWithStartEndDateStatusForHospital(orgCode,startDate, endDate,status);
                    }
                    else if(status.equalsIgnoreCase(Constants.RECORD_STATUS_APPROVED)) {
                        birthRecords = birthRepository.filterBirthRecordsWithStartEndDateStatusForCfc(startDate, endDate, status);
                    }

                }
                else{

                    // If Status is draft and requested by CREATOR, APPROVER
                    if(status.equalsIgnoreCase(Constants.RECORD_STATUS_DRAFT)){
                        // logger.info("========== Start, End and Status ====="+orgCode, startDate, endDate, Constants.RECORD_STATUS_DRAFT, currentUser.getUserId());
                        birthRecords = birthRepository.filterBirthRecordsWithStartEndDateStatusForHospitalCfcDraft(orgCode, startDate, endDate, Constants.RECORD_STATUS_DRAFT, currentUser.getUserId());
                    }
                    // If Status is not draft and requested by CREATOR, APPROVER
                    else{
                        birthRecords = birthRepository.filterBirthRecordsWithStartEndDateStatusForHospital(orgCode,startDate, endDate, status);
                    }
                }


            }

            else if(!CommonUtil.checkNullOrBlank(registrationNo) && CommonUtil.checkNullOrBlank(applicationNo) && CommonUtil.checkNullOrBlank(status)){
                    if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_ADMIN))
                            || (currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_CREATOR))
                            || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_APPROVER)))) {

                        // If Status is Not Draft and requested by Admin
                        birthRecords = birthRepository.filterBirthRecordsWithRegNoForAdmin(startDate, endDate, registrationNo);

                    }else{
                        birthRecords = birthRepository.filterBirthRecordsWithRegNoForHospital(orgCode,startDate, endDate, registrationNo);

                    }
                }
            else if(CommonUtil.checkNullOrBlank(registrationNo) && !CommonUtil.checkNullOrBlank(applicationNo) && CommonUtil.checkNullOrBlank(status)){


                    if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_ADMIN))
                            || (currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_CREATOR))
                            || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_APPROVER)))) {

                        birthRecords = birthRepository.filterBirthRecordsWithAppNoForAdmin(startDate, endDate, applicationNo);
                        }

                    else{
                            birthRecords = birthRepository.filterBirthRecordsWithAppNoForHospital(orgCode,startDate, endDate, applicationNo);
                        }

            }
            else if(!CommonUtil.checkNullOrBlank(registrationNo) && !CommonUtil.checkNullOrBlank(applicationNo) && CommonUtil.checkNullOrBlank(status)){
               // birthRecords = birthRepository.filterBirthRecordsWithoutStatus(applicationNo, registrationNo, startDate, endDate);

                      if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_ADMIN))
                              || (currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_CREATOR))
                              || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_APPROVER)))) {

                            birthRecords = birthRepository.filterBirthRecordsWithoutStatusForAdmin(startDate, endDate, applicationNo, registrationNo);
                       }
                    else{
                            birthRecords = birthRepository.filterBirthRecordsWithoutStatusForHospital(orgCode,startDate, endDate, applicationNo, registrationNo);

                       }

            }
            // request = {applicationNumber, startDate, endDate, status}

            else if(!CommonUtil.checkNullOrBlank(applicationNo) && !CommonUtil.checkNullOrBlank(status)){
                // birthRecords = birthRepository.filterBirthRecordsWithoutStatus(applicationNo, registrationNo, startDate, endDate);

                if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_ADMIN))
                        || (currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_CREATOR))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_APPROVER)))) {

                    birthRecords = birthRepository.filterBirthRecordsWithoutRegnoForAdmin(startDate, endDate, applicationNo, status);
                }
                else{
                    birthRecords = birthRepository.filterBirthRecordsWithoutRegnoForHospital(orgCode,startDate, endDate, applicationNo, status);

                }

            }

            // request = {applicationNumber, startDate, endDate, status}

            else if(!CommonUtil.checkNullOrBlank(registrationNo) && !CommonUtil.checkNullOrBlank(status)){
                // birthRecords = birthRepository.filterBirthRecordsWithoutStatus(applicationNo, registrationNo, startDate, endDate);

                if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_ADMIN))
                        || (currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_CREATOR))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_APPROVER)))) {

                    birthRecords = birthRepository.filterBirthRecordsWithoutApplNoForAdmin(startDate, endDate, registrationNo, status);
                }
                else{
                    birthRecords = birthRepository.filterBirthRecordsWithoutApplNoForHospital(orgCode,startDate, endDate, registrationNo, status);

                }

            }

            birthRecords= birthRecords.stream().filter(birthM->!"Y".equals(birthM.getIsDeleted())).collect(Collectors.toList());
            Collections.sort(birthRecords, (o1, o2) -> (int)(o2.getBirthId() - o1.getBirthId()));
            // logger.info("=== Birth records sorted in Descending order===="+birthRecords);

            response.setStatus(HttpStatus.OK);
            response.setData(birthRecords);
        }
        else if(Constants.RECORD_TYPE_DEATH.equalsIgnoreCase(type)){

            List<DeathModel> deathRecords = new ArrayList<DeathModel>();
            // logger.info("=========FILTER DTO IS ========"+filterDto+"==Type is==="+type);
            if(!CommonUtil.checkNullOrBlank(registrationNo) && !CommonUtil.checkNullOrBlank(applicationNo) && !CommonUtil.checkNullOrBlank(status)){
                if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_ADMIN) )) {
                    // If Status is draft and requested by Admin
                    if(status.equalsIgnoreCase(Constants.RECORD_STATUS_DRAFT)){
                        deathRecords = deathRepository.filterDeathRecordsForAdminDraft(registrationNo, applicationNo, startDate, endDate, Constants.RECORD_STATUS_DRAFT, currentUser.getUserId());
                    }
                    // If Status is Not Draft and requested by Admin
                    else{
                        deathRecords = deathRepository.filterDeathRecordsForAdmin(registrationNo, applicationNo, startDate, endDate, status);
                    }

                }
                else if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_APPROVER))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_CREATOR) )){

                    if(status.equalsIgnoreCase(Constants.RECORD_STATUS_DRAFT)){
                        deathRecords = deathRepository.filterDeathRecordsForHospitalCfcDraft(registrationNo, applicationNo, startDate, endDate,
                                orgCode, Constants.RECORD_STATUS_DRAFT, currentUser.getUserId());
                    }
                    // If Status is not draft and requested by CREATOR, APPROVER
                    else if(status.equalsIgnoreCase(Constants.RECORD_STATUS_PENDING) || status.equalsIgnoreCase(Constants.RECORD_STATUS_REJECTED)){
                        deathRecords = deathRepository.filterDeathRecordsForHospital(registrationNo, applicationNo, startDate, endDate, status, orgCode);
                    }
                    else if(status.equalsIgnoreCase(Constants.RECORD_STATUS_APPROVED)) {
                        deathRecords = deathRepository.filterDeathRecordsForCfc(registrationNo, applicationNo, startDate, endDate, status);
                    }
                }
                else{
                    // If Status is draft and requested by CREATOR, APPROVER
                    if(status.equalsIgnoreCase(Constants.RECORD_STATUS_DRAFT)){
                        deathRecords = deathRepository.filterDeathRecordsForHospitalCfcDraft(registrationNo, applicationNo, startDate, endDate,
                                orgCode, Constants.RECORD_STATUS_DRAFT, currentUser.getUserId());
                    }
                    // If Status is not draft and requested by CREATOR, APPROVER
                    else{
                        deathRecords = deathRepository.filterDeathRecordsForHospital(registrationNo, applicationNo, startDate, endDate, status, orgCode);
                    }

                }
            }
            else if(CommonUtil.checkNullOrBlank(registrationNo) && CommonUtil.checkNullOrBlank(applicationNo) && CommonUtil.checkNullOrBlank(status)){

                if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_ADMIN) )) {

                    // If Status is draft and requested by Admin
                    if(status.equalsIgnoreCase(Constants.RECORD_STATUS_DRAFT)){
                        deathRecords = deathRepository.filterDeathRecordsWithStarAndEndDateForAdminDraft(startDate, endDate, Constants.RECORD_STATUS_DRAFT, currentUser.getUserId());
                    }
                    // If Status is Not Draft and requested by Admin
                    else{
                        deathRecords = deathRepository.filterDeathRecordsWithStarAndEndDateForAdmin(startDate, endDate);
                    }

                }
                else if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_CREATOR))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_APPROVER))){

                    if(status.equalsIgnoreCase(Constants.RECORD_STATUS_DRAFT)){
                        deathRecords = deathRepository.filterDeathRecordsWithStarAndEndDateForHospitalDraft(orgCode, startDate, endDate, Constants.RECORD_STATUS_DRAFT, currentUser.getUserId());

                    }
                    // If Status is not draft and requested by CREATOR, APPROVER
                    else{
                        deathRecords = deathRepository.filterDeathRecordsWithStarAndEndDateForCfc(orgCode, currentUser.getUserId() ,startDate, endDate,
                                Constants.RECORD_STATUS_PENDING, Constants.RECORD_STATUS_DRAFT,
                                Constants.RECORD_STATUS_APPROVED, Constants.RECORD_STATUS_REJECTED);
                    }
                }
                else{

                    // If Status is draft and requested by CREATOR, APPROVER
                    if(status.equalsIgnoreCase(Constants.RECORD_STATUS_DRAFT)){
                        deathRecords = deathRepository.filterDeathRecordsWithStarAndEndDateForHospitalDraft(orgCode, startDate, endDate, Constants.RECORD_STATUS_DRAFT, currentUser.getUserId());

                    }
                    // If Status is not draft and requested by CREATOR, APPROVER
                    else{
                        deathRecords = deathRepository.filterDeathRecordsWithStarAndEndDateForHospital(orgCode,startDate, endDate, Constants.RECORD_STATUS_DRAFT, currentUser.getUserId());
                    }
                }

            }
            else if(CommonUtil.checkNullOrBlank(registrationNo) && CommonUtil.checkNullOrBlank(applicationNo) && !CommonUtil.checkNullOrBlank(status)){
                // birthRecords = birthRepository.filterBirthRecordsWithStartEndDateAndStatus(startDate, endDate, status);

                if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_ADMIN) )) {

                    // If Status is draft and requested by Admin
                    if(status.equalsIgnoreCase(Constants.RECORD_STATUS_DRAFT)){
                        deathRecords = deathRepository.filterDeathRecordsWithStartEndDateStatusForAdminDraft(startDate, endDate, Constants.RECORD_STATUS_DRAFT, currentUser.getUserId());
                    }
                    // If Status is Not Draft and requested by Admin
                    else{
                        deathRecords = deathRepository.filterDeathRecordsWithStartEndDateStatusForAdmin(startDate, endDate, status);
                    }

                }
                else if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_CREATOR))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_APPROVER))){
                    if(status.equalsIgnoreCase(Constants.RECORD_STATUS_DRAFT)){
                        deathRecords = deathRepository.filterDeathRecordsWithStartEndDateStatusForHospitalDraft(orgCode, startDate, endDate, Constants.RECORD_STATUS_DRAFT, currentUser.getUserId());
                    }
                    // If Status is not draft and requested by CREATOR, APPROVER
                    else if(status.equalsIgnoreCase(Constants.RECORD_STATUS_PENDING) || status.equalsIgnoreCase(Constants.RECORD_STATUS_REJECTED)){
                        deathRecords = deathRepository.filterDeathRecordsWithStartEndDateStatusForHospital(orgCode,startDate, endDate, status);
                    }
                    else if(status.equalsIgnoreCase(Constants.RECORD_STATUS_APPROVED)) {
                        deathRecords = deathRepository.filterDeathRecordsWithStartEndDateStatusForCfc(startDate, endDate, status);
                    }

                }
                else{

                    // If Status is draft and requested by CREATOR, APPROVER
                    if(status.equalsIgnoreCase(Constants.RECORD_STATUS_DRAFT)){
                        deathRecords = deathRepository.filterDeathRecordsWithStartEndDateStatusForHospitalDraft(orgCode, startDate, endDate, Constants.RECORD_STATUS_DRAFT, currentUser.getUserId());
                    }
                    // If Status is not draft and requested by CREATOR, APPROVER
                    else{
                        deathRecords = deathRepository.filterDeathRecordsWithStartEndDateStatusForHospital(orgCode,startDate, endDate, status);
                    }
                }


            }

            else if(!CommonUtil.checkNullOrBlank(registrationNo) && CommonUtil.checkNullOrBlank(applicationNo) && CommonUtil.checkNullOrBlank(status)){
                if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_ADMIN))
                        || (currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_CREATOR))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_APPROVER)))) {

                    // If Status is Not Draft and requested by Admin
                    deathRecords = deathRepository.filterDeathRecordsWithRegNoForAdmin(startDate, endDate, registrationNo);

                }else{
                    deathRecords = deathRepository.filterDeathRecordsWithRegNoForHospital(orgCode,startDate, endDate, registrationNo);

                }
            }
            else if(CommonUtil.checkNullOrBlank(registrationNo) && !CommonUtil.checkNullOrBlank(applicationNo) && CommonUtil.checkNullOrBlank(status)){


                if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_ADMIN))
                        || (currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_CREATOR))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_APPROVER)))) {

                    deathRecords = deathRepository.filterDeathRecordsWithAppNoForAdmin(startDate, endDate, applicationNo);
                }

                else{
                    deathRecords = deathRepository.filterDeathRecordsWithAppNoForHospital(orgCode,startDate, endDate, applicationNo);
                }

            }
            else if(!CommonUtil.checkNullOrBlank(registrationNo) && !CommonUtil.checkNullOrBlank(applicationNo) && CommonUtil.checkNullOrBlank(status)){
                // birthRecords = birthRepository.filterBirthRecordsWithoutStatus(applicationNo, registrationNo, startDate, endDate);

                if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_ADMIN))
                        || (currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_CREATOR))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_APPROVER)))) {

                    deathRecords = deathRepository.filterDeathRecordsWithoutStatusForAdmin(startDate, endDate, applicationNo, registrationNo);
                }
                else{
                    deathRecords = deathRepository.filterDeathRecordsWithoutStatusForHospital(orgCode,startDate, endDate, applicationNo, registrationNo);

                }

            }
            // Payload={startDate, endDate, applicationNumber, status}

            else if(!CommonUtil.checkNullOrBlank(applicationNo) && !CommonUtil.checkNullOrBlank(status)){
                // birthRecords = birthRepository.filterBirthRecordsWithoutStatus(applicationNo, registrationNo, startDate, endDate);

                if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_ADMIN))
                        || (currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_CREATOR))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_APPROVER)))) {

                    deathRecords = deathRepository.filterDeathRecordsWithoutRegnoForAdmin(startDate, endDate, applicationNo, status);
                }
                else{
                    deathRecords = deathRepository.filterDeathRecordsWithoutRegnoForHospital(orgCode,startDate, endDate, applicationNo, status);

                }

            }

            else if(!CommonUtil.checkNullOrBlank(registrationNo) && !CommonUtil.checkNullOrBlank(status)){
                // birthRecords = birthRepository.filterBirthRecordsWithoutStatus(applicationNo, registrationNo, startDate, endDate);

                if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_ADMIN))
                        || (currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_CREATOR))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_APPROVER)))) {

                    deathRecords = deathRepository.filterDeathRecordsWithoutApplNoForAdmin(startDate, endDate, registrationNo, status);
                }
                else{
                    deathRecords = deathRepository.filterDeathRecordsWithoutApplNoForHospital(orgCode,startDate, endDate, registrationNo, status);

                }

            }
            deathRecords= deathRecords.stream().filter(deathM->!"Y".equals(deathM.getIsDeleted())).collect(Collectors.toList());

            response.setStatus(HttpStatus.OK);
            Collections.sort(deathRecords, (o1, o2) -> (int)(o2.getDeathId() - o1.getDeathId()));
            // logger.info("=== Death records sorted in Descending order===="+deathRecords);
            response.setData(deathRecords);
        }
        else if(Constants.RECORD_TYPE_SBIRTH.equalsIgnoreCase(type)){

            List<SBirthModel> sbirthRecords = new ArrayList<SBirthModel>();
            // logger.info("=========FILTER DTO IS ========"+filterDto+"==Type is==="+type);
            if(!CommonUtil.checkNullOrBlank(registrationNo) && !CommonUtil.checkNullOrBlank(applicationNo) && !CommonUtil.checkNullOrBlank(status)){
                if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_ADMIN) )) {

                    // If Status is draft and requested by Admin
                    if(status.equalsIgnoreCase(Constants.RECORD_STATUS_DRAFT)){
                        sbirthRecords = sBirthRepository.filterSbirthRecordsForAdminDraft(registrationNo, applicationNo, startDate, endDate, Constants.RECORD_STATUS_DRAFT, currentUser.getUserId());

                    }
                    // If Status is Not Draft and requested by Admin
                    else{
                        sbirthRecords = sBirthRepository.filterSbirthRecordsForAdmin(registrationNo, applicationNo, startDate, endDate, status);
                    }

                }
                else if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_APPROVER))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_CREATOR) )){

                    if(status.equalsIgnoreCase(Constants.RECORD_STATUS_DRAFT)){
                        sbirthRecords = sBirthRepository.filterSbirthRecordsForHospitalDraft(registrationNo, applicationNo, startDate, endDate,
                                orgCode, Constants.RECORD_STATUS_DRAFT, currentUser.getUserId());
                    }
                    // If Status is not draft and requested by CREATOR, APPROVER
                    else if(status.equalsIgnoreCase(Constants.RECORD_STATUS_PENDING) || status.equalsIgnoreCase(Constants.RECORD_STATUS_REJECTED)){
                        sbirthRecords = sBirthRepository.filterSbirthRecordsForHospital(registrationNo, applicationNo, startDate, endDate, status, orgCode);
                    }
                    else if(status.equalsIgnoreCase(Constants.RECORD_STATUS_APPROVED)) {
                        sbirthRecords = sBirthRepository.filterSbirthRecordsForCfc(registrationNo, applicationNo, startDate, endDate, status);
                    }
                }
                else{

                    // If Status is draft and requested by CREATOR, APPROVER
                    if(status.equalsIgnoreCase(Constants.RECORD_STATUS_DRAFT)){
                        sbirthRecords = sBirthRepository.filterSbirthRecordsForHospitalDraft(registrationNo, applicationNo, startDate, endDate,
                                orgCode, Constants.RECORD_STATUS_DRAFT, currentUser.getUserId());
                    }
                    // If Status is not draft and requested by CREATOR, APPROVER
                    else{
                        sbirthRecords = sBirthRepository.filterSbirthRecordsForHospital(registrationNo, applicationNo, startDate, endDate, status, orgCode);
                    }

                }
            }
            else if(CommonUtil.checkNullOrBlank(registrationNo) && CommonUtil.checkNullOrBlank(applicationNo) && CommonUtil.checkNullOrBlank(status)){

                if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_ADMIN) )) {

                    // If Status is draft and requested by Admin
                    if(status.equalsIgnoreCase(Constants.RECORD_STATUS_DRAFT)){
                        sbirthRecords = sBirthRepository.filterSbirthRecordsWithStarAndEndDateForAdminDraft(startDate, endDate, Constants.RECORD_STATUS_DRAFT, currentUser.getUserId());
                    }
                    // If Status is Not Draft and requested by Admin
                    else{
                        sbirthRecords = sBirthRepository.filterSbirthRecordsWithStarAndEndDateForAdmin(startDate, endDate);
                    }

                }
                else if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_CREATOR))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_APPROVER))){

                    if(status.equalsIgnoreCase(Constants.RECORD_STATUS_DRAFT)){
                        sbirthRecords = sBirthRepository.filterSbirthRecordsWithStarAndEndDateForHospitalDraft(orgCode, startDate, endDate, Constants.RECORD_STATUS_DRAFT, currentUser.getUserId());

                    }
                    // If Status is not draft and requested by CREATOR, APPROVER
                    else{
                        sbirthRecords = sBirthRepository.filterSbirthRecordsWithStarAndEndDateForCfc(orgCode, currentUser.getUserId() ,startDate, endDate,
                                Constants.RECORD_STATUS_PENDING, Constants.RECORD_STATUS_DRAFT,
                                Constants.RECORD_STATUS_APPROVED, Constants.RECORD_STATUS_REJECTED);
                    }
                }
                else{

                    // If Status is draft and requested by CREATOR, APPROVER
                    if(status.equalsIgnoreCase(Constants.RECORD_STATUS_DRAFT)){
                        sbirthRecords = sBirthRepository.filterSbirthRecordsWithStarAndEndDateForHospitalDraft(orgCode, startDate, endDate, Constants.RECORD_STATUS_DRAFT, currentUser.getUserId());

                    }
                    // If Status is not draft and requested by CREATOR, APPROVER
                    else{
                        sbirthRecords = sBirthRepository.filterSbirthRecordsWithStarAndEndDateForHospital(orgCode,startDate, endDate, Constants.RECORD_STATUS_DRAFT, currentUser.getUserId());
                    }
                }

            }
            else if(CommonUtil.checkNullOrBlank(registrationNo) && CommonUtil.checkNullOrBlank(applicationNo) && !CommonUtil.checkNullOrBlank(status)){
                // birthRecords = birthRepository.filterBirthRecordsWithStartEndDateAndStatus(startDate, endDate, status);

                if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_ADMIN)))
                 {

                    // If Status is draft and requested by Admin
                    if(status.equalsIgnoreCase(Constants.RECORD_STATUS_DRAFT)){
                        sbirthRecords = sBirthRepository.filterSbirthRecordsWithStartEndDateStatusForAdminDraft(startDate, endDate, Constants.RECORD_STATUS_DRAFT, currentUser.getUserId());
                    }
                    // If Status is Not Draft and requested by Admin
                    else{
                        sbirthRecords = sBirthRepository.filterSbirthRecordsWithStartEndDateStatusForAdmin(startDate, endDate, status);
                    }

                }
                else if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_CREATOR))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_APPROVER))){
                    if(status.equalsIgnoreCase(Constants.RECORD_STATUS_DRAFT)){
                        sbirthRecords = sBirthRepository.filterSbirthRecordsWithStartEndDateStatusForHospitalDraft(orgCode, startDate, endDate, Constants.RECORD_STATUS_DRAFT, currentUser.getUserId());
                    }
                    // If Status is not draft and requested by CREATOR, APPROVER
                    else if(status.equalsIgnoreCase(Constants.RECORD_STATUS_PENDING) || status.equalsIgnoreCase(Constants.RECORD_STATUS_REJECTED)){
                        sbirthRecords = sBirthRepository.filterSbirthRecordsWithStartEndDateStatusForHospital(orgCode,startDate, endDate, status);
                    }
                    else if(status.equalsIgnoreCase(Constants.RECORD_STATUS_APPROVED)) {
                        sbirthRecords = sBirthRepository.filterSbirthRecordsWithStartEndDateStatusForCfc(startDate, endDate, status);
                    }

                }
                else{

                    // If Status is draft and requested by CREATOR, APPROVER
                    if(status.equalsIgnoreCase(Constants.RECORD_STATUS_DRAFT)){
                        sbirthRecords = sBirthRepository.filterSbirthRecordsWithStartEndDateStatusForHospitalDraft(orgCode, startDate, endDate, Constants.RECORD_STATUS_DRAFT, currentUser.getUserId());
                    }
                    // If Status is not draft and requested by CREATOR, APPROVER
                    else{
                        sbirthRecords = sBirthRepository.filterSbirthRecordsWithStartEndDateStatusForHospital(orgCode,startDate, endDate, status);
                    }
                }
            }

            else if(!CommonUtil.checkNullOrBlank(registrationNo) && CommonUtil.checkNullOrBlank(applicationNo) && CommonUtil.checkNullOrBlank(status)){
                if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_ADMIN))
                        || (currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_CREATOR))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_APPROVER)))) {

                    // If Status is Not Draft and requested by Admin
                    sbirthRecords = sBirthRepository.filterSbirthRecordsWithRegNoForAdmin(startDate, endDate, registrationNo);

                }else{
                    sbirthRecords = sBirthRepository.filterSbirthRecordsWithRegNoForHospital(orgCode,startDate, endDate, registrationNo);

                }
            }
            else if(CommonUtil.checkNullOrBlank(registrationNo) && !CommonUtil.checkNullOrBlank(applicationNo) && CommonUtil.checkNullOrBlank(status)){


                if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_ADMIN))
                        || (currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_CREATOR))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_APPROVER)))) {

                    sbirthRecords = sBirthRepository.filterSbirthRecordsWithAppNoForAdmin(startDate, endDate, applicationNo);
                }

                else{
                    sbirthRecords = sBirthRepository.filterSbirthRecordsWithAppNoForHospital(orgCode,startDate, endDate, applicationNo);
                }

            }
            else if(!CommonUtil.checkNullOrBlank(registrationNo) && !CommonUtil.checkNullOrBlank(applicationNo) && CommonUtil.checkNullOrBlank(status)){
                // birthRecords = birthRepository.filterBirthRecordsWithoutStatus(applicationNo, registrationNo, startDate, endDate);

                if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_ADMIN))
                        || (currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_CREATOR))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_APPROVER)))) {

                    sbirthRecords = sBirthRepository.filterSbirthRecordsWithoutStatusForAdmin(startDate, endDate, applicationNo, registrationNo);
                }
                else{
                    sbirthRecords = sBirthRepository.filterSbirthRecordsWithoutStatusForHospital(orgCode,startDate, endDate, applicationNo, registrationNo);

                }

            }

            // Payload={startDate, endDate, applicationNumber, status}
            else if(!CommonUtil.checkNullOrBlank(applicationNo) && !CommonUtil.checkNullOrBlank(status)){
                // birthRecords = birthRepository.filterBirthRecordsWithoutStatus(applicationNo, registrationNo, startDate, endDate);

                if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_ADMIN))
                        || (currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_CREATOR))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_APPROVER)))) {

                    sbirthRecords = sBirthRepository.filterSbirthRecordsWithoutRegnoForAdmin(startDate, endDate, applicationNo, status);
                }
                else{
                    sbirthRecords = sBirthRepository.filterSbirthRecordsWithoutRegnoForHospital(orgCode,startDate, endDate, applicationNo, status);

                }

            }

            else if(!CommonUtil.checkNullOrBlank(registrationNo) && !CommonUtil.checkNullOrBlank(status)){
                // birthRecords = birthRepository.filterBirthRecordsWithoutStatus(applicationNo, registrationNo, startDate, endDate);

                if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_ADMIN))
                        || (currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_CREATOR))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_APPROVER)))) {

                    sbirthRecords = sBirthRepository.filterSbirthRecordsWithoutApplNoForAdmin(startDate, endDate, registrationNo, status);
                }
                else{
                    sbirthRecords = sBirthRepository.filterSbirthRecordsWithoutApplNoForHospital(orgCode,startDate, endDate,  registrationNo, status);

                }
            }
            sbirthRecords= sbirthRecords.stream().filter(sBirthM->!"Y".equals(sBirthM.getIsDeleted())).collect(Collectors.toList());

            response.setStatus(HttpStatus.OK);
            Collections.sort(sbirthRecords, (o1, o2) -> (int)(o2.getSbirthId() - o1.getSbirthId()));
            // logger.info("=== Still Birth records sorted in Descending order===="+sbirthRecords);
            response.setData(sbirthRecords);
        }
    return response;
    }

    private List<BirthModel> findAllBirth(FilterDto filterDto, String filterType, HttpServletRequest request) throws DateRangeException,Exception{
        String userName = authService.getUserIdFromRequest(request);
        Long organizationId = authService.getOrganizationIdFromUserId(userName);
        Optional<UserModel> currentUserOp = authRepository.findById(Long.parseLong(userName));
        UserModel currentUser = currentUserOp.get();
        Optional<OrganizationModel> organizationModelOp = organizationRepository.findById(organizationId);
        OrganizationModel organizationModel = organizationModelOp.get();
        String orgCode = organizationModel.getOrganisationCode();

        return birthRepository.findAll(new Specification<BirthModel>() {
            @Override
            public Predicate toPredicate(Root<BirthModel> root, CriteriaQuery<?> query,
                                         CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();

                if (!CommonUtil.checkNullOrBlank(filterDto.getStartDate()+"")
                        && !CommonUtil.checkNullOrBlank(filterDto.getEndDate()+"" )) {
                    // CreatedAt
                    CommonUtil.betweenDates(filterDto.getStartDate().atTime(00,00,00),filterDto.getEndDate().atTime(23,59,59));
                    predicates
                            .add(criteriaBuilder.and(criteriaBuilder.between(root.get(Constants.CREATED_AT),
                                    filterDto.getStartDate().atTime(00,00,00),filterDto.getEndDate().atTime(23,59,59))));
                }
                if (!CommonUtil.checkNullOrBlank(filterDto.getRegistrationNumber())) {
                    // registrationNumber
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.REGISTRATION_NUMBER),
                            filterDto.getRegistrationNumber())));
                }
                if (!CommonUtil.checkNullOrBlank(filterDto.getApplicationNumber())) {
                    // applicationNumber
                    predicates.add(criteriaBuilder.or(criteriaBuilder.equal(root.get(Constants.APPLICATION_NUMBER), filterDto.getApplicationNumber()),
                            criteriaBuilder.equal(root.get(Constants.ORIGINAL_APPLICATION_NUMBER), filterDto.getApplicationNumber())));
                }
                if (!CommonUtil.checkNullOrBlank(filterDto.getMotherName())) {
                    // motherName
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.like(root.get(Constants.MOTHER_NAME), filterDto.getMotherName()+"%")));
                }
                if (!CommonUtil.checkNullOrBlank(filterDto.getFatherName())) {
                    // fatherName
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.like(root.get(Constants.FATHER_NAME), filterDto.getFatherName()+"%")));
                }

                // Contact number
                if (!CommonUtil.checkNullOrBlank(filterDto.getContactNumber())) {

                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.equal(root.get(Constants.CONTACT_NUMBER), filterDto.getContactNumber())));
                }
                if (!CommonUtil.checkNullOrBlank(filterDto.getName())) {
                    // name
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.like(root.get(Constants.CHILD_NAME), filterDto.getName()+"%")));
                }
                if (!CommonUtil.checkNullOrBlank(filterDto.getEventPlace())) {
                    // eventPlace
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.equal(root.get(Constants.EVENT_PLACE), filterDto.getEventPlace())));
                }

                if (!CommonUtil.checkNullOrBlank(filterDto.getRegStartDate()+"")
                        && !CommonUtil.checkNullOrBlank(filterDto.getRegEndDate()+"")) {
                    // registrationDatetime
                    CommonUtil.betweenDates(filterDto.getRegStartDate().atTime(00,00,00),filterDto.getRegEndDate().atTime(23,59,59));
                    predicates
                            .add(criteriaBuilder.and(criteriaBuilder.between(root.get(Constants.REGISTRATION_DATE_TIME),
                                    filterDto.getRegStartDate().atTime(00,00,00),filterDto.getRegEndDate().atTime(23,59,59))));
                }
                if (!CommonUtil.checkNullOrBlank(filterDto.getEventStartDate()+"") && !CommonUtil.checkNullOrBlank(filterDto.getEventEndDate()+"")) {
                    // eventDate
                    CommonUtil.betweenDates(filterDto.getEventStartDate().atTime(00,00,00),filterDto.getEventEndDate().atTime(23,59,59));

                    predicates.add(criteriaBuilder.and(criteriaBuilder.between(root.get(Constants.EVENT_DATE),
                            filterDto.getEventStartDate().atTime(00,00,00),filterDto.getEventEndDate().atTime(23,59,59))));
                }
                if (!CommonUtil.checkNullOrBlank(filterDto.getStatus())) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.STATUS),
                            filterDto.getStatus())));
                }
                if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.USER_TYPE_HOSPITAL))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CREATOR))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_APPROVER))) {

                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.ORGANIZATION_CODE),orgCode)));

                    if(Constants.RECORD_STATUS_DRAFT.equalsIgnoreCase(filterDto.getStatus())){
                        predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.USER_ID),userName)));
                    }
                }
                if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.USER_TYPE_CFC))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_REGISTRAR))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_CREATOR))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_APPROVER))) {

                        if(Constants.RECORD_STATUS_DRAFT.equalsIgnoreCase(filterDto.getStatus())){
                            predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.USER_ID),userName)));
                        }
                        else if(Constants.RECORD_STATUS_PENDING.equalsIgnoreCase(filterDto.getStatus())
                                || Constants.RECORD_STATUS_REJECTED.equalsIgnoreCase(filterDto.getStatus())){
                            if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_CREATOR))){
                                predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.ORGANIZATION_CODE),orgCode)));
                            }
                        }
                }


                if (!CommonUtil.checkNullOrBlank(filterDto.getDivisionCode())) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.DIVISION_CODE),
                            filterDto.getDivisionCode())));
                }

                if (!CommonUtil.checkNullOrBlank(filterDto.getGenderCode())) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.GENDER_CODE),
                            filterDto.getGenderCode())));
                }
                
                /*DESC: User id addition for particular user for Weekly reports
                Author: Deepak
                Date: 19-05-22
                *
                * */
                if (!CommonUtil.checkNullOrBlank(filterDto.getUserId()+"")) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.USER_ID),
                            filterDto.getUserId())));
                }
                query.orderBy(criteriaBuilder.desc(root.get(Constants.EVENT_DATE)));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        });
    }

    private List<SBirthModel> findAllSBirth(FilterDto filterDto, String filterType, HttpServletRequest request) throws DateRangeException,Exception{
        String userName = authService.getUserIdFromRequest(request);
        Long organizationId = authService.getOrganizationIdFromUserId(userName);
        Optional<UserModel> currentUserOp = authRepository.findById(Long.parseLong(userName));
        UserModel currentUser = currentUserOp.get();
        Optional<OrganizationModel> organizationModelOp = organizationRepository.findById(organizationId);
        OrganizationModel organizationModel = organizationModelOp.get();
        String orgCode = organizationModel.getOrganisationCode();

        return sBirthRepository.findAll(new Specification<SBirthModel>() {
            @Override
            public Predicate toPredicate(Root<SBirthModel> root, CriteriaQuery<?> query,
                                         CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();

                if (!CommonUtil.checkNullOrBlank(filterDto.getStartDate()+"")
                        && !CommonUtil.checkNullOrBlank(filterDto.getEndDate()+"" )) {
                    // CreatedAt
                    CommonUtil.betweenDates(filterDto.getStartDate().atTime(00,00,00),filterDto.getEndDate().atTime(23,59,59));
                    predicates
                            .add(criteriaBuilder.and(criteriaBuilder.between(root.get(Constants.CREATED_AT),
                                    filterDto.getStartDate().atTime(00,00,00),filterDto.getEndDate().atTime(23,59,59))));
                }
                if (!CommonUtil.checkNullOrBlank(filterDto.getRegistrationNumber())) {
                    // registrationNumber
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.REGISTRATION_NUMBER),
                            filterDto.getRegistrationNumber())));
                }
                if (!CommonUtil.checkNullOrBlank(filterDto.getApplicationNumber())) {
                    // applicationNumber
                    predicates.add(criteriaBuilder.or(criteriaBuilder.equal(root.get(Constants.APPLICATION_NUMBER), filterDto.getApplicationNumber()),
                            criteriaBuilder.equal(root.get(Constants.ORIGINAL_APPLICATION_NUMBER), filterDto.getApplicationNumber())));
                }
                if (!CommonUtil.checkNullOrBlank(filterDto.getMotherName())) {
                    // motherName
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.like(root.get(Constants.MOTHER_NAME), filterDto.getMotherName()+"%")));
                }
                if (!CommonUtil.checkNullOrBlank(filterDto.getFatherName())) {
                    // fatherName
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.like(root.get(Constants.FATHER_NAME), filterDto.getFatherName()+"%")));
                }

                // Contact number
                if (!CommonUtil.checkNullOrBlank(filterDto.getContactNumber())) {

                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.equal(root.get(Constants.CONTACT_NUMBER), filterDto.getContactNumber())));
                }
                if (!CommonUtil.checkNullOrBlank(filterDto.getName())) {
                    // name
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.like(root.get(Constants.CHILD_NAME), filterDto.getName()+"%")));
                }
                if (!CommonUtil.checkNullOrBlank(filterDto.getEventPlace())) {
                    // eventPlace
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.equal(root.get(Constants.EVENT_PLACE), filterDto.getEventPlace())));
                }

                // HUSBAND WIFE NAME
//                if (!CommonUtil.checkNullOrBlank(filterDto.getHusbandWifeName())) {
//                    // eventPlace
//                    predicates.add(criteriaBuilder
//                            .and(criteriaBuilder.equal(root.get(Constants.HUSBAND_WIFE_NAME), filterDto.getHusbandWifeName())));
//                }

                // Gender code
                if (!CommonUtil.checkNullOrBlank(filterDto.getGenderCode())) {
                    // eventPlace
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.equal(root.get(Constants.GENDER_CODE), filterDto.getGenderCode())));
                }

                if (!CommonUtil.checkNullOrBlank(filterDto.getEventPlace())) {
                    // eventPlace
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.equal(root.get(Constants.EVENT_PLACE), filterDto.getEventPlace())));
                }

                if (!CommonUtil.checkNullOrBlank(filterDto.getRegStartDate()+"")
                        && !CommonUtil.checkNullOrBlank(filterDto.getRegEndDate()+"")) {
                    // registrationDatetime
                    CommonUtil.betweenDates(filterDto.getRegStartDate().atTime(00,00,00),filterDto.getRegEndDate().atTime(23,59,59));
                    predicates
                            .add(criteriaBuilder.and(criteriaBuilder.between(root.get(Constants.REGISTRATION_DATE_TIME),
                                    filterDto.getRegStartDate().atTime(00,00,00),filterDto.getRegEndDate().atTime(23,59,59))));
                }
                if (!CommonUtil.checkNullOrBlank(filterDto.getEventStartDate()+"") && !CommonUtil.checkNullOrBlank(filterDto.getEventEndDate()+"")) {
                    // eventDate
                    CommonUtil.betweenDates(filterDto.getEventStartDate().atTime(00,00,00),filterDto.getEventEndDate().atTime(23,59,59));

                    predicates.add(criteriaBuilder.and(criteriaBuilder.between(root.get(Constants.EVENT_DATE),
                            filterDto.getEventStartDate().atTime(00,00,00),filterDto.getEventEndDate().atTime(23,59,59))));
                }
                if (!CommonUtil.checkNullOrBlank(filterDto.getStatus())) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.STATUS),
                            filterDto.getStatus())));
                }
                if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.USER_TYPE_HOSPITAL))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CREATOR))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_APPROVER))) {

                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.ORGANIZATION_CODE),orgCode)));

                    if(Constants.RECORD_STATUS_DRAFT.equalsIgnoreCase(filterDto.getStatus())){
                        predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.USER_ID),userName)));
                    }
                }
                if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.USER_TYPE_CFC))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_REGISTRAR))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_CREATOR))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_APPROVER))) {

                    if(Constants.RECORD_STATUS_DRAFT.equalsIgnoreCase(filterDto.getStatus())){
                        predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.USER_ID),userName)));
                    }
                    else if(Constants.RECORD_STATUS_PENDING.equalsIgnoreCase(filterDto.getStatus())
                            || Constants.RECORD_STATUS_REJECTED.equalsIgnoreCase(filterDto.getStatus())){
                        if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_CREATOR))){
                            predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.ORGANIZATION_CODE),orgCode)));
                        }
                    }
                }
                if (!CommonUtil.checkNullOrBlank(filterDto.getDivisionCode())) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.DIVISION_CODE),
                            filterDto.getDivisionCode())));
                }

                 /*DESC: User id addition for particular user for Weekly reports
                Author: Deepak
                Date: 19-05-22
                *
                * */
                if (!CommonUtil.checkNullOrBlank(filterDto.getUserId()+"")) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.USER_ID),
                            filterDto.getUserId())));
                }
                query.orderBy(criteriaBuilder.desc(root.get(Constants.EVENT_DATE)));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        });
    }

    private List<DeathModel> findAllDeath(FilterDto filterDto, String filterType, HttpServletRequest request) throws DateRangeException,Exception{
        String userName = authService.getUserIdFromRequest(request);
        Long organizationId = authService.getOrganizationIdFromUserId(userName);
        Optional<UserModel> currentUserOp = authRepository.findById(Long.parseLong(userName));
        UserModel currentUser = currentUserOp.get();
        Optional<OrganizationModel> organizationModelOp = organizationRepository.findById(organizationId);
        OrganizationModel organizationModel = organizationModelOp.get();
        String orgCode = organizationModel.getOrganisationCode();

        return deathRepository.findAll(new Specification<DeathModel>() {
            @Override
            public Predicate toPredicate(Root<DeathModel> root, CriteriaQuery<?> query,
                                         CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();

                if (!CommonUtil.checkNullOrBlank(filterDto.getStartDate()+"")
                        && !CommonUtil.checkNullOrBlank(filterDto.getEndDate()+"" )) {
                    // CreatedAt
                    CommonUtil.betweenDates(filterDto.getStartDate().atTime(00,00,00),filterDto.getEndDate().atTime(23,59,59));
                    predicates
                            .add(criteriaBuilder.and(criteriaBuilder.between(root.get(Constants.CREATED_AT),
                                    filterDto.getStartDate().atTime(00,00,00),filterDto.getEndDate().atTime(23,59,59))));
                }
                if (!CommonUtil.checkNullOrBlank(filterDto.getRegistrationNumber())) {
                    // registrationNumber
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.REGISTRATION_NUMBER),
                            filterDto.getRegistrationNumber())));
                }
                if (!CommonUtil.checkNullOrBlank(filterDto.getApplicationNumber())) {
                    // applicationNumber
                    predicates.add(criteriaBuilder.or(criteriaBuilder.equal(root.get(Constants.APPLICATION_NUMBER), filterDto.getApplicationNumber()),
                            criteriaBuilder.equal(root.get(Constants.ORIGINAL_APPLICATION_NUMBER), filterDto.getApplicationNumber())));
                }
                if (!CommonUtil.checkNullOrBlank(filterDto.getMotherName())) {
                    // motherName
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.like(root.get(Constants.MOTHER_NAME), filterDto.getMotherName()+"%")));
                }
                if (!CommonUtil.checkNullOrBlank(filterDto.getFatherName())) {
                    // fatherName
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.like(root.get(Constants.FATHER_NAME), filterDto.getFatherName()+"%")));
                }

                if (!CommonUtil.checkNullOrBlank(filterDto.getName())) {
                    // name
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.like(root.get(Constants.CHILD_NAME), filterDto.getName()+"%")));
                }

                // Contact number
                if (!CommonUtil.checkNullOrBlank(filterDto.getContactNumber())) {

                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.equal(root.get(Constants.CONTACT_NUMBER), filterDto.getContactNumber())));
                }
                if (!CommonUtil.checkNullOrBlank(filterDto.getEventPlace())) {
                    // eventPlace
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.equal(root.get(Constants.EVENT_PLACE), filterDto.getEventPlace())));
                }

                // HUSBAND WIFE NAME
                if (!CommonUtil.checkNullOrBlank(filterDto.getHusbandWifeName())) {
                    // fatherName
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.like(root.get(Constants.HUSBAND_WIFE_NAME), filterDto.getHusbandWifeName()+"%")));
                }

                // Gender code
                if (!CommonUtil.checkNullOrBlank(filterDto.getGenderCode())) {
                    // eventPlace
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.equal(root.get(Constants.GENDER_CODE), filterDto.getGenderCode())));
                }

                if (!CommonUtil.checkNullOrBlank(filterDto.getRegStartDate()+"")
                        && !CommonUtil.checkNullOrBlank(filterDto.getRegEndDate()+"")) {
                    // registrationDatetime
                    CommonUtil.betweenDates(filterDto.getRegStartDate().atTime(00,00,00),filterDto.getRegEndDate().atTime(23,59,59));
                    predicates
                            .add(criteriaBuilder.and(criteriaBuilder.between(root.get(Constants.REGISTRATION_DATE_TIME),
                                    filterDto.getRegStartDate().atTime(00,00,00),filterDto.getRegEndDate().atTime(23,59,59))));
                }
                if (!CommonUtil.checkNullOrBlank(filterDto.getEventStartDate()+"") && !CommonUtil.checkNullOrBlank(filterDto.getEventEndDate()+"")) {
                    // eventDate
                    CommonUtil.betweenDates(filterDto.getEventStartDate().atTime(00,00,00),filterDto.getEventEndDate().atTime(23,59,59));

                    predicates.add(criteriaBuilder.and(criteriaBuilder.between(root.get(Constants.EVENT_DATE),
                            filterDto.getEventStartDate().atTime(00,00,00),filterDto.getEventEndDate().atTime(23,59,59))));
                }
                if (!CommonUtil.checkNullOrBlank(filterDto.getStatus())) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.STATUS),
                            filterDto.getStatus())));
                }
                if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.USER_TYPE_HOSPITAL))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CREATOR))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_APPROVER))) {

                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.ORGANIZATION_CODE),orgCode)));

                    if(Constants.RECORD_STATUS_DRAFT.equalsIgnoreCase(filterDto.getStatus())){
                        predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.USER_ID),userName)));
                    }
                }
                if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.USER_TYPE_CFC))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_REGISTRAR))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_CREATOR))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_APPROVER))) {

                    if(Constants.RECORD_STATUS_DRAFT.equalsIgnoreCase(filterDto.getStatus())){
                        predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.USER_ID),userName)));
                    }
                    else if(Constants.RECORD_STATUS_PENDING.equalsIgnoreCase(filterDto.getStatus())
                            || Constants.RECORD_STATUS_REJECTED.equalsIgnoreCase(filterDto.getStatus())){
                        if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_CREATOR))){
                            predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.ORGANIZATION_CODE),orgCode)));
                        }
                    }
                }
                if (!CommonUtil.checkNullOrBlank(filterDto.getDivisionCode())) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.DIVISION_CODE),
                            filterDto.getDivisionCode())));
                }

                 /*DESC: User id addition for particular user for Weekly reports
                Author: Deepak
                Date: 19-05-22
                *
                * */
                if (!CommonUtil.checkNullOrBlank(filterDto.getUserId()+"")) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.USER_ID),
                            filterDto.getUserId())));
                }

                 /*DESC: Get reports for Covid 19 Identified or not identified
                Author: Deepak
                Date: 20-05-22
                *
                * */
                if (!CommonUtil.checkNullOrBlank(filterDto.getCauseOfDeath())) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.CAUSE_OF_DEATH),
                            filterDto.getCauseOfDeath())));
                }
                query.orderBy(criteriaBuilder.desc(root.get(Constants.EVENT_DATE)));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        });
    }


    @Override
    public ApiResponse dashboardFilteredData(FilterDto filterDto, String type, HttpServletRequest request) throws Exception {
        ApiResponse response = new ApiResponse();

//        if(!CommonUtil.checkNullOrBlank(filterDto.getRegistrationNumber())
//                || !CommonUtil.checkNullOrBlank(filterDto.getApplicationNumber())) {
//            filterDto.setStartDate(null);
//            filterDto.setEndDate(null);
//            filterDto.setRegEndDate(null);
//            filterDto.setRegEndDate(null);
//            filterDto.setEventStartDate(null);
//            filterDto.setEventEndDate(null);
//        }

        List<BirthReportResponse> birthList = new ArrayList<>();
        List<SBirthReportResponse> sBirthList = new ArrayList<>();
        List<DeathReportResponse> deathList = new ArrayList<>();

        ChildDetails childDetails = new ChildDetails();


        String motherLit = "";
        String fatherLit = "";
        String motherRel = "";
        String fatherRel = "";
        String motherOcpn = "";
        String fatherOcpn = "";

        String deceasedLit = "";
        String deceasedRel = "";
        String deceasedOcpn = "";
        String deceasedMarital = "";
        String medicalAttention = "";
        String deliveryMethod = "";
        String deliveryAttention = "";


        if(Constants.RECORD_TYPE_BIRTH.equalsIgnoreCase(type)){
            List<BirthModel> birthRecords = findAllBirth(filterDto,type,request);
            birthRecords= birthRecords.stream().filter(birthM->!"Y".equals(birthM.getIsDeleted())).collect(Collectors.toList());
            response.setStatus(HttpStatus.OK);
            Collections.sort(birthRecords, (o1, o2) -> (int)(o2.getBirthId() - o1.getBirthId()));
             logger.info("=== Birth records sorted in Descending order===="+birthRecords);




            if(birthRecords != null) {


                for (BirthModel birthModel : birthRecords) {
                    BirthReportResponse birthReportResponse = new BirthReportResponse();
                    BeanUtils.copyProperties(birthModel, birthReportResponse);
                   // BeanUtils.copyProperties(childDetails, birthReportResponse);

                    logger.info("=== Child details==="+childDetails);
                    logger.info("=== birthReportResponse==="+birthReportResponse);
                    if(!CommonUtil.checkNullOrBlank(birthModel.getMotherLiteracy())) {
                        motherLit = CommonUtil.getLiteracyValueById(birthModel.getMotherLiteracy());
                    }
                    if(!CommonUtil.checkNullOrBlank(birthModel.getFatherLiteracy())) {
                        fatherLit = CommonUtil.getLiteracyValueById(birthModel.getFatherLiteracy());
                    }

                    if(!CommonUtil.checkNullOrBlank(birthModel.getMotherReligion())) {
                        motherRel = CommonUtil.getReligionValueById(birthModel.getMotherReligion());
                    }
                    if(!CommonUtil.checkNullOrBlank(birthModel.getFatherReligion())) {
                        fatherRel = CommonUtil.getReligionValueById(birthModel.getFatherReligion());
                    }

                    if(!CommonUtil.checkNullOrBlank(birthModel.getMotherOccupation())) {
                        motherOcpn = CommonUtil.getOccupationValueById(birthModel.getMotherOccupation());
                    }
                    if(!CommonUtil.checkNullOrBlank(birthModel.getFatherOccupation())) {
                        fatherOcpn = CommonUtil.getOccupationValueById(birthModel.getFatherOccupation());
                    }

                    if(!CommonUtil.checkNullOrBlank(birthModel.getDeliveryAttentionCode())) {
                        deliveryAttention = CommonUtil.getDeliveryAttentionById(birthModel.getDeliveryAttentionCode());
                    }

                    if(!CommonUtil.checkNullOrBlank(birthModel.getMethodOfDelivery())) {
                        deliveryMethod = CommonUtil.getDeliveryMethodById(birthModel.getMethodOfDelivery());
                    }



                    birthReportResponse.setMotherLiteracyDesc(motherLit);
                    birthReportResponse.setFatherLiteracyDesc(fatherLit);
                    birthReportResponse.setFatherReligionDesc(fatherRel);
                    birthReportResponse.setMotherReligionDesc(motherRel);
                    birthReportResponse.setFatherOccupationDesc(fatherOcpn);
                    birthReportResponse.setMotherOccupationDesc(motherOcpn);
                    birthReportResponse.setDeliveryAttentionDesc(deliveryAttention);
                    birthReportResponse.setMethodOfDeliveryDesc(deliveryMethod);


                    birthList.add(birthReportResponse);
                }
            }

            response.setData(birthList);
        }
        else if(Constants.RECORD_TYPE_DEATH.equalsIgnoreCase(type)){
            List<DeathModel> deathRecords = findAllDeath(filterDto,type,request);

            deathRecords= deathRecords.stream().filter(deathM->!"Y".equals(deathM.getIsDeleted())).collect(Collectors.toList());
            response.setStatus(HttpStatus.OK);
            Collections.sort(deathRecords, (o1, o2) -> (int)(o2.getDeathId() - o1.getDeathId()));
            // logger.info("=== Death records sorted in Descending order===="+deathRecords);

            if(deathRecords != null){



                for (DeathModel deathModel : deathRecords) {

                    DeathReportResponse deathReportResponse = new DeathReportResponse();
                    BeanUtils.copyProperties(deathModel, deathReportResponse);

                    if(!CommonUtil.checkNullOrBlank(deathModel.getEducationCode())) {
                        deceasedLit = CommonUtil.getLiteracyValueById(deathModel.getEducationCode());
                    }

                    if(!CommonUtil.checkNullOrBlank(deathModel.getReligionCode())) {
                        deceasedRel = CommonUtil.getReligionValueById(deathModel.getReligionCode());
                    }


                    if(!CommonUtil.checkNullOrBlank(deathModel.getOccupationCode())) {
                        deceasedOcpn = CommonUtil.getOccupationValueById(deathModel.getOccupationCode());
                    }

                    if(!CommonUtil.checkNullOrBlank(deathModel.getMaritalStatusCode()+"")) {
                        deceasedMarital = CommonUtil.getMaritalStatusById(deathModel.getMaritalStatusCode()+"");
                    }

                    if(!CommonUtil.checkNullOrBlank(deathModel.getMedicalAttentionCode()+"")) {
                        medicalAttention = CommonUtil.getMedicalAttentionByCode(deathModel.getMedicalAttentionCode()+"");
                    }
                    deathReportResponse.setEducationDesc(deceasedLit);
                    deathReportResponse.setReligionDesc(deceasedRel);
                    deathReportResponse.setOccupationDesc(deceasedOcpn);
                    deathReportResponse.setMaritalStatusDesc(deceasedMarital);
                    deathReportResponse.setMedicalAttentionDesc(medicalAttention);
                    deathList.add(deathReportResponse);
                }


            }


            response.setData(deathList);
        }
        else if(Constants.RECORD_TYPE_SBIRTH.equalsIgnoreCase(type)){
            List<SBirthModel> sBirthRecords = findAllSBirth(filterDto,type,request);
            sBirthRecords= sBirthRecords.stream().filter(sBirthM->!"Y".equals(sBirthM.getIsDeleted())).collect(Collectors.toList());
            response.setStatus(HttpStatus.OK);
            Collections.sort(sBirthRecords, (o1, o2) -> (int)(o2.getSbirthId() - o1.getSbirthId()));
            // logger.info("=== Still Birth records sorted in Descending order===="+sBirthRecords);
            if(sBirthRecords != null) {



                for (SBirthModel sBirthModel : sBirthRecords) {
                    SBirthReportResponse sBirthReportResponse = new SBirthReportResponse();
                    BeanUtils.copyProperties(sBirthModel, sBirthReportResponse);
                    if(!CommonUtil.checkNullOrBlank(sBirthModel.getMotherLiteracy())) {
                        motherLit = CommonUtil.getLiteracyValueById(sBirthModel.getMotherLiteracy());
                    }
                    if(!CommonUtil.checkNullOrBlank(sBirthModel.getFatherLiteracy())) {
                        fatherLit = CommonUtil.getLiteracyValueById(sBirthModel.getFatherLiteracy());
                    }

                    if(!CommonUtil.checkNullOrBlank(sBirthModel.getMotherReligion())) {
                        motherRel = CommonUtil.getReligionValueById(sBirthModel.getMotherReligion());
                    }
                    if(!CommonUtil.checkNullOrBlank(sBirthModel.getFatherReligion())) {
                        fatherRel = CommonUtil.getReligionValueById(sBirthModel.getFatherReligion());
                    }

                    if(!CommonUtil.checkNullOrBlank(sBirthModel.getMotherOccupation())) {
                        motherOcpn = CommonUtil.getOccupationValueById(sBirthModel.getMotherOccupation());
                    }
                    if(!CommonUtil.checkNullOrBlank(sBirthModel.getFatherOccupation())) {
                        fatherOcpn = CommonUtil.getOccupationValueById(sBirthModel.getFatherOccupation());
                    }

                    if(!CommonUtil.checkNullOrBlank(sBirthModel.getDeliveryAttentionCode())) {
                        deliveryAttention = CommonUtil.getDeliveryAttentionById(sBirthModel.getDeliveryAttentionCode());
                    }

                    if(!CommonUtil.checkNullOrBlank(sBirthModel.getMethodOfDelivery())) {
                        deliveryMethod = CommonUtil.getDeliveryMethodById(sBirthModel.getMethodOfDelivery());
                    }



                    sBirthReportResponse.setMotherLiteracyDesc(motherLit);
                    sBirthReportResponse.setFatherLiteracyDesc(fatherLit);
                    sBirthReportResponse.setFatherReligionDesc(fatherRel);
                    sBirthReportResponse.setMotherReligionDesc(motherRel);
                    sBirthReportResponse.setFatherOccupationDesc(fatherOcpn);
                    sBirthReportResponse.setMotherOccupationDesc(motherOcpn);
                    sBirthReportResponse.setDeliveryAttentionDesc(deliveryAttention);
                    sBirthReportResponse.setMethodOfDeliveryDesc(deliveryMethod);

                    sBirthList.add(sBirthReportResponse);
                }
            }

            response.setData(sBirthList);
        }
       return response;
    }
}