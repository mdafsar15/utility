package com.ndmc.ndmc_record.serviceImpl;

import com.ndmc.ndmc_record.config.Constants;
import com.ndmc.ndmc_record.dto.*;
import com.ndmc.ndmc_record.model.*;
import com.ndmc.ndmc_record.repository.*;
import com.ndmc.ndmc_record.service.AuthService;
import com.ndmc.ndmc_record.service.ReportService;
import com.ndmc.ndmc_record.utils.CommonUtil;

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

import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.TreeMap;

import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    private final Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);
    @Autowired
    BirthRepository birthRepository;
    @Autowired
    SBirthRepository sBirthRepository;
    @Autowired
    DeathRepository deathRepository;

    @Autowired
    CertificatePrintRepository certificatePrintRepository;
    @Autowired
    AuthServiceImpl authService;

    @Autowired
    SlaDetailsRepository slaDetailsRepository;
    @Autowired
    DashboardServiceImpl dashboardService;
    @Override
    public ApiResponse getReprtSearchData(ReportSearchDto reportSearchDto, String recordType, HttpServletRequest request) {

        ApiResponse apiResponse = new ApiResponse();
        Map<String, Object> result = new HashMap<String, Object>();

        if(Constants.RECORD_TYPE_BIRTH.equalsIgnoreCase(recordType)) {

            //Map<String, Long> total = new HashMap<String, Long>();
            Map<String, Object> birth = new HashMap<String, Object>();
            Map<String, Object> inst = new HashMap<String, Object>();
            Map<String, Object> dom = new HashMap<String, Object>();



            Long totalMaleAtHospital = 0L;
            Long totalFemaleAtHospital = 0L;
            Long totalAmbiguousAtHospital = 0L;
            Long totalAtHospital = 0L;

            Long totalMaleAtHome = 0L;
            Long totalFemaleAtHome = 0L;
            Long totalAmbiguousAtHome = 0L;
            Long totalAtHome = 0L;

            List<BirthModel> allBirthRecords = birthRepository.findRecordsBetweenRegStartAndRegEndDate(reportSearchDto.getRegStartDate(), reportSearchDto.getRegEndDate(), Constants.RECORD_STATUS_APPROVED);
            logger.info("ALL BIRTH RECORDS" + allBirthRecords);
          // List<BirthModel>  listBirthAtHospital = allBirthRecords.stream().filter((r -> r.getEventPlaceFlag().equals("H") && r.getGenderCode().equals("F"))).collect(Collectors.toList());
            totalMaleAtHospital = allBirthRecords.stream().filter((r -> r.getEventPlaceFlag().equals(Constants.EVENT_FLAG_HOSPITAL) && r.getGenderCode().equals(Constants.GENDER_CODE_M))).count();
            totalFemaleAtHospital = allBirthRecords.stream().filter((r -> r.getEventPlaceFlag().equals(Constants.EVENT_FLAG_HOSPITAL) && r.getGenderCode().equals(Constants.GENDER_CODE_F))).count();
            totalAmbiguousAtHospital = allBirthRecords.stream().filter((r -> r.getEventPlaceFlag().equals(Constants.EVENT_FLAG_HOSPITAL) && r.getGenderCode().equals(Constants.GENDER_CODE_A))).count();
            totalAtHospital = allBirthRecords.stream().filter((r -> r.getEventPlaceFlag().equals(Constants.EVENT_FLAG_HOSPITAL))).count();

            totalMaleAtHome = allBirthRecords.stream().filter((r -> r.getEventPlaceFlag().equals(Constants.EVENT_FLAG_HOME) && r.getGenderCode().equals(Constants.GENDER_CODE_M))).count();
            totalFemaleAtHome = allBirthRecords.stream().filter((r -> r.getEventPlaceFlag().equals(Constants.EVENT_FLAG_HOME) && r.getGenderCode().equals(Constants.GENDER_CODE_F))).count();
            totalAmbiguousAtHome = allBirthRecords.stream().filter((r -> r.getEventPlaceFlag().equals(Constants.EVENT_FLAG_HOME) && r.getGenderCode().equals(Constants.GENDER_CODE_A))).count();
            totalAtHome = allBirthRecords.stream().filter((r -> r.getEventPlaceFlag().equals(Constants.EVENT_FLAG_HOME))).count();


            inst.put("TotalMale", totalMaleAtHospital);
            inst.put("TotalFemale", totalFemaleAtHospital);
            inst.put("TotalAmbiguous", totalAmbiguousAtHospital);
            inst.put("Total", totalAtHospital);



            dom.put("TotalMale", totalMaleAtHome);
            dom.put("TotalFemale", totalFemaleAtHome);
            dom.put("TotalAmbiguous", totalAmbiguousAtHome);
            dom.put("Total", totalAtHome);

            birth.put("Institutional", inst);
            birth.put("Domiciliary", dom);

           // birth.put("Birth", birth);
            result.put("Birth",birth);
            apiResponse.setData(result);
        }

        else if(Constants.RECORD_TYPE_SBIRTH.equalsIgnoreCase(recordType)) {

            //Map<String, Long> total = new HashMap<String, Long>();
            Map<String, Object> sBirth = new HashMap<String, Object>();
            Map<String, Object> inst = new HashMap<String, Object>();
            Map<String, Object> dom = new HashMap<String, Object>();



            Long totalMaleAtHospital = 0L;
            Long totalFemaleAtHospital = 0L;
            Long totalAmbiguousAtHospital = 0L;
            Long totalAtHospital = 0L;

            Long totalMaleAtHome = 0L;
            Long totalFemaleAtHome = 0L;
            Long totalAmbiguousAtHome = 0L;
            Long totalAtHome = 0L;

            List<SBirthModel> allSbirthRecords = sBirthRepository.findRecordsBetweenRegStartAndRegEndDate(reportSearchDto.getRegStartDate(), reportSearchDto.getRegEndDate(), Constants.RECORD_STATUS_APPROVED);
            logger.info("ALL BIRTH RECORDS" + allSbirthRecords);
            // List<BirthModel>  listBirthAtHospital = allBirthRecords.stream().filter((r -> r.getEventPlaceFlag().equals("H") && r.getGenderCode().equals("F"))).collect(Collectors.toList());
            totalMaleAtHospital = allSbirthRecords.stream().filter((r -> r.getEventPlaceFlag().equals(Constants.EVENT_FLAG_HOSPITAL) && r.getGenderCode().equals(Constants.GENDER_CODE_M))).count();
            totalFemaleAtHospital = allSbirthRecords.stream().filter((r -> r.getEventPlaceFlag().equals(Constants.EVENT_FLAG_HOSPITAL) && r.getGenderCode().equals(Constants.GENDER_CODE_F))).count();
            totalAmbiguousAtHospital = allSbirthRecords.stream().filter((r -> r.getEventPlaceFlag().equals(Constants.EVENT_FLAG_HOSPITAL) && r.getGenderCode().equals(Constants.GENDER_CODE_A))).count();
            totalAtHospital = allSbirthRecords.stream().filter((r -> r.getEventPlaceFlag().equals(Constants.EVENT_FLAG_HOSPITAL))).count();

            totalMaleAtHome = allSbirthRecords.stream().filter((r -> r.getEventPlaceFlag().equals(Constants.EVENT_FLAG_HOME) && r.getGenderCode().equals(Constants.GENDER_CODE_M))).count();
            totalFemaleAtHome = allSbirthRecords.stream().filter((r -> r.getEventPlaceFlag().equals(Constants.EVENT_FLAG_HOME) && r.getGenderCode().equals(Constants.GENDER_CODE_F))).count();
            totalAmbiguousAtHome = allSbirthRecords.stream().filter((r -> r.getEventPlaceFlag().equals(Constants.EVENT_FLAG_HOME) && r.getGenderCode().equals(Constants.GENDER_CODE_A))).count();
            totalAtHome = allSbirthRecords.stream().filter((r -> r.getEventPlaceFlag().equals(Constants.EVENT_FLAG_HOME))).count();


            inst.put("TotalMale", totalMaleAtHospital);
            inst.put("TotalFemale", totalFemaleAtHospital);
            inst.put("TotalAmbiguous", totalAmbiguousAtHospital);
            inst.put("Total", totalAtHospital);



            dom.put("TotalMale", totalMaleAtHome);
            dom.put("TotalFemale", totalFemaleAtHome);
            dom.put("TotalAmbiguous", totalAmbiguousAtHome);
            dom.put("Total", totalAtHome);

            sBirth.put("Institutional", inst);
            sBirth.put("Domiciliary", dom);

            // birth.put("Birth", birth);
            result.put("StillBirth",sBirth);
            apiResponse.setData(result);
        }


        else if(Constants.RECORD_TYPE_DEATH.equalsIgnoreCase(recordType)) {

            //Map<String, Long> total = new HashMap<String, Long>();
            Map<String, Object> death = new HashMap<String, Object>();
            Map<String, Object> inst = new HashMap<String, Object>();
            Map<String, Object> dom = new HashMap<String, Object>();



            Long totalMaleAtHospital = 0L;
            Long totalFemaleAtHospital = 0L;
            Long totalAmbiguousAtHospital = 0L;
            Long totalAtHospital = 0L;

            Long totalMaleAtHome = 0L;
            Long totalFemaleAtHome = 0L;
            Long totalAmbiguousAtHome = 0L;
            Long totalAtHome = 0L;

            List<DeathModel> allDeathRecords = deathRepository.findRecordsBetweenRegStartAndRegEndDate(reportSearchDto.getRegStartDate(), reportSearchDto.getRegEndDate(), Constants.RECORD_STATUS_APPROVED);
            logger.info("ALL DEath RECORDS" + allDeathRecords);
            // List<BirthModel>  listBirthAtHospital = allBirthRecords.stream().filter((r -> r.getEventPlaceFlag().equals("H") && r.getGenderCode().equals("F"))).collect(Collectors.toList());
            totalMaleAtHospital = allDeathRecords.stream().filter((r -> r.getEventPlaceFlag().equals(Constants.EVENT_FLAG_HOSPITAL) && r.getGenderCode().equals(Constants.GENDER_CODE_M))).count();
            totalFemaleAtHospital = allDeathRecords.stream().filter((r -> r.getEventPlaceFlag().equals(Constants.EVENT_FLAG_HOSPITAL) && r.getGenderCode().equals(Constants.GENDER_CODE_F))).count();
            totalAmbiguousAtHospital = allDeathRecords.stream().filter((r -> r.getEventPlaceFlag().equals(Constants.EVENT_FLAG_HOSPITAL) && r.getGenderCode().equals(Constants.GENDER_CODE_A))).count();
            totalAtHospital = allDeathRecords.stream().filter((r -> r.getEventPlaceFlag().equals(Constants.EVENT_FLAG_HOSPITAL))).count();

            totalMaleAtHome = allDeathRecords.stream().filter((r -> r.getEventPlaceFlag().equals(Constants.EVENT_FLAG_HOME) && r.getGenderCode().equals(Constants.GENDER_CODE_M))).count();
            totalFemaleAtHome = allDeathRecords.stream().filter((r -> r.getEventPlaceFlag().equals(Constants.EVENT_FLAG_HOME) && r.getGenderCode().equals(Constants.GENDER_CODE_F))).count();
            totalAmbiguousAtHome = allDeathRecords.stream().filter((r -> r.getEventPlaceFlag().equals(Constants.EVENT_FLAG_HOME) && r.getGenderCode().equals(Constants.GENDER_CODE_A))).count();
            totalAtHome = allDeathRecords.stream().filter((r -> r.getEventPlaceFlag().equals(Constants.EVENT_FLAG_HOME))).count();


            inst.put("TotalMale", totalMaleAtHospital);
            inst.put("TotalFemale", totalFemaleAtHospital);
            inst.put("TotalAmbiguous", totalAmbiguousAtHospital);
            inst.put("Total", totalAtHospital);



            dom.put("TotalMale", totalMaleAtHome);
            dom.put("TotalFemale", totalFemaleAtHome);
            dom.put("TotalAmbiguous", totalAmbiguousAtHome);
            dom.put("Total", totalAtHome);

            death.put("Institutional", inst);
            death.put("Domiciliary", dom);

            // birth.put("Birth", birth);
            result.put("Death",death);
            apiResponse.setData(result);
        }

        return apiResponse;
    }

    @Override
    public ApiResponse getVitalEventReportsData(ReportSearchDto reportSearchDto, HttpServletRequest request) {
        ApiResponse apiResponse = new ApiResponse();
        Map<String, Object> result = new HashMap<String, Object>();
        Map<String, Object> birth = new HashMap<String, Object>();
        Map<String, Object> sBirth = new HashMap<String, Object>();
        Map<String, Object> death = new HashMap<String, Object>();
        Map<String, Object> infOne = new HashMap<String, Object>();
        Map<String, Object> infFive = new HashMap<String, Object>();

        Long mBirth = 0L;
        Long fBirth = 0L;
        Long aBirth = 0L;

        Long mSbirth = 0L;
        Long fSbirth = 0L;
        Long aSbirth = 0L;

        Long mDeath = 0L;
        Long fDeath = 0L;
        Long aDeath = 0L;

        Long mInfOne = 0L;
        Long fInfOne = 0L;
        Long aInfOne = 0L;

        Long mInfFive = 0L;
        Long fInfFive = 0L;
        Long aInfFive = 0L;

        List<Object[]> numberOfInfantOne = getInfantDeathReportLessThanOne(reportSearchDto);
        List<Object[]> numberOfInfantOnetoFive = getInfantDeathReportOneToFive(reportSearchDto);
        List<Object[]> birthData = getTotalBirthRegistration(reportSearchDto);
        List<Object[]> sBirthData = getTotalStillBirthRegistration(reportSearchDto);
        List<Object[]> deathData = getTotalDeathRegistration(reportSearchDto);
      //  Object birthReports = birthRepository.getBirthVitalReports(reportSearchDto.getRegStartDate(), reportSearchDto.getRegEndDate(), Constants.RECORD_STATUS_APPROVED);
        //logger.info("birthReports ===="+birthData);
        birth.put(Constants.GENDER_CODE_A, 0);
        birth.put(Constants.GENDER_CODE_F, 0);
        birth.put(Constants.GENDER_CODE_M, 0);

        sBirth.put(Constants.GENDER_CODE_A, 0);
        sBirth.put(Constants.GENDER_CODE_F, 0);
        sBirth.put(Constants.GENDER_CODE_M, 0);

        death.put(Constants.GENDER_CODE_A, 0);
        death.put(Constants.GENDER_CODE_F, 0);
        death.put(Constants.GENDER_CODE_M, 0);

        infOne.put(Constants.GENDER_CODE_A, 0);
        infOne.put(Constants.GENDER_CODE_F, 0);
        infOne.put(Constants.GENDER_CODE_M, 0);

        infFive.put(Constants.GENDER_CODE_A, 0);
        infFive.put(Constants.GENDER_CODE_F, 0);
        infFive.put(Constants.GENDER_CODE_M, 0);

        for(Object[] data:birthData)
        {
            //record_type ,pr_by, no_of_copies
            //data[0] => record_type, data[1] => pr_by, data[2] => count
           if(data[0] !=null && Constants.GENDER_CODE_A.equals(data[0])){
               aBirth = aBirth + Long.parseLong(data[1].toString());
           }
           else  if(data[0] !=null && Constants.GENDER_CODE_F.equals(data[0])){
               fBirth = fBirth + Long.parseLong(data[1].toString());
           }
           else  if(data[0] !=null && Constants.GENDER_CODE_M.equals(data[0])){
               mBirth = mBirth + Long.parseLong(data[1].toString());
           }
         //   logger.info("==data====="+data[0]+"====="+data[1]);

        }

        for(Object[] data:deathData)
        {
            //record_type ,pr_by, no_of_copies
            //data[0] => record_type, data[1] => pr_by, data[2] => count
            if(data[0] !=null && Constants.GENDER_CODE_A.equals(data[0])){
                aDeath = aDeath + Long.parseLong(data[1].toString());
            }
            else  if(data[0] !=null && Constants.GENDER_CODE_F.equals(data[0])){
                fDeath = fDeath + Long.parseLong(data[1].toString());
            }
            else  if(data[0] !=null && Constants.GENDER_CODE_M.equals(data[0])){
                mDeath = mDeath + Long.parseLong(data[1].toString());
            }
          //  logger.info("==data====="+data[0]+"====="+data[1]);

        }

        for(Object[] data:sBirthData)
        {
            //record_type ,pr_by, no_of_copies
            //data[0] => record_type, data[1] => pr_by, data[2] => count
            if(data[0] !=null && Constants.GENDER_CODE_A.equals(data[0])){
                aSbirth = aSbirth + Long.parseLong(data[1].toString());
            }
            else  if(data[0] !=null && Constants.GENDER_CODE_F.equals(data[0])){
                fSbirth = fSbirth + Long.parseLong(data[1].toString());
            }
            else  if(data[0] !=null && Constants.GENDER_CODE_M.equals(data[0])){
                mSbirth = mSbirth + Long.parseLong(data[1].toString());
            }
           // logger.info("==data====="+data[0]+"====="+data[1]);

        }

        for(Object[] data:numberOfInfantOne)
        {
            //record_type ,pr_by, no_of_copies
            //data[0] => record_type, data[1] => pr_by, data[2] => count
            if(data[0] !=null && Constants.GENDER_CODE_A.equals(data[0])){
                aInfOne = aInfOne + Long.parseLong(data[1].toString());
            }
            else  if(data[0] !=null && Constants.GENDER_CODE_F.equals(data[0])){
                fInfOne = fInfOne + Long.parseLong(data[1].toString());
            }
            else  if(data[0] !=null && Constants.GENDER_CODE_M.equals(data[0])){
                mInfOne = mInfOne + Long.parseLong(data[1].toString());
            }
          //  logger.info("==data====="+data[0]+"====="+data[1]);

        }

        for(Object[] data:numberOfInfantOnetoFive)
        {
            //record_type ,pr_by, no_of_copies
            //data[0] => record_type, data[1] => pr_by, data[2] => count
            if(data[0] !=null && Constants.GENDER_CODE_A.equals(data[0])){
                aInfFive = aInfFive + Long.parseLong(data[1].toString());
            }
            else  if(data[0] !=null && Constants.GENDER_CODE_F.equals(data[0])){
                fInfFive = fInfFive + Long.parseLong(data[1].toString());
            }
            else  if(data[0] !=null && Constants.GENDER_CODE_M.equals(data[0])){
                mInfFive = mInfFive + Long.parseLong(data[1].toString());
            }
         //   logger.info("==data====="+data[0]+"====="+data[1]);

        }


        birth.put(Constants.GENDER_CODE_A, aBirth);
        birth.put(Constants.GENDER_CODE_F, fBirth);
        birth.put(Constants.GENDER_CODE_M, mBirth);

        sBirth.put(Constants.GENDER_CODE_A, aSbirth);
        sBirth.put(Constants.GENDER_CODE_F, fSbirth);
        sBirth.put(Constants.GENDER_CODE_M, mSbirth);

        death.put(Constants.GENDER_CODE_A, aDeath);
        death.put(Constants.GENDER_CODE_F, fDeath);
        death.put(Constants.GENDER_CODE_M, mDeath);

        infOne.put(Constants.GENDER_CODE_A, aInfOne);
        infOne.put(Constants.GENDER_CODE_F, fInfOne);
        infOne.put(Constants.GENDER_CODE_M, mInfOne);

        infFive.put(Constants.GENDER_CODE_A, aInfFive);
        infFive.put(Constants.GENDER_CODE_F, fInfFive);
        infFive.put(Constants.GENDER_CODE_M, mInfFive);

        result.put(Constants.RECORD_BIRTH_CAP, birth );
        result.put(Constants.RECORD_DEATH_CAP, death );


        result.put(Constants.RECORD_SBIRTH_CAP, sBirth );
        result.put(Constants.INFANT_DEATH_UNDER1, infOne);
        result.put(Constants.INFANT_DEATH_UNDER5, infFive);

        apiResponse.setData(result);
        return apiResponse;
    }

    @Override
    public ApiResponse getEventReportsData(ReportSearchDto reportSearchDto, HttpServletRequest request) {
        ApiResponse apiResponse = new ApiResponse();
        Map<String, Object> result = new HashMap<String, Object>();


        Map birthData = getBirthRecordsByEventFlag(reportSearchDto);

       Map sBirthData = getSbirthRecordsByEventFlag(reportSearchDto);
       Map deathData = getDeathRecordsByEventFlag(reportSearchDto);


        result.put(Constants.REPORT_BIRTH, birthData);
        result.put(Constants.REPORT_STILL_BIRTH, sBirthData);
        result.put(Constants.REPORT_DEATH, deathData);

        apiResponse.setData(result);
        return apiResponse;
    }

    @Override
    public ApiResponse getCountOfCertificate(PrintCountDto printCountDto, HttpServletRequest request) {

        ApiResponse res = new ApiResponse();
        Long onlineCertB = 0L;
        Long hospitalCertB = 0L;
        Long cfcCertB = 0L;

        Long onlineCertD = 0L;
        Long hospitalCertD = 0L;
        Long cfcCertD = 0L;

        Long onlineCertS = 0L;
        Long hospitalCertS = 0L;
        Long cfcCertS = 0L;

        Map<String, Object> result = new HashMap<String, Object>();
        Map<String, Object> birth = new HashMap<String, Object>();
        Map<String, Object> sBirth = new HashMap<String, Object>();
        Map<String, Object> death = new HashMap<String, Object>();

        List<Object[]> allPrintedCerts = certificatePrintRepository.getAllCertificatesCount(printCountDto.getPrintDateStart().atTime(00, 00, 00), printCountDto.getPrintDateEnd().atTime(23, 59, 59));



        birth.put(Constants.PRINTED_AT_HOSPITAL, 0);
        birth.put(Constants.PRINTED_AT_ONLINE, 0);
        birth.put(Constants.PRINTED_AT_CFC, 0);

        sBirth.put(Constants.PRINTED_AT_HOSPITAL, 0);
        sBirth.put(Constants.PRINTED_AT_ONLINE, 0);
        sBirth.put(Constants.PRINTED_AT_CFC, 0);

        death.put(Constants.PRINTED_AT_HOSPITAL, 0);
        death.put(Constants.PRINTED_AT_ONLINE, 0);
        death.put(Constants.PRINTED_AT_CFC, 0);

        for(Object[] data:allPrintedCerts)
        {
            //record_type ,pr_by, no_of_copies
            //data[0] => record_type, data[1] => pr_by, data[2] => count
            if(Constants.RECORD_TYPE_BIRTH.equals(data[0])){
                if(Constants.PRINTED_AT_HOSPITAL.equals(data[1])) {
                    hospitalCertB = hospitalCertB + Long.parseLong(data[2].toString());
                }
                else if(Constants.PRINTED_AT_ONLINE.equals(data[1])){
                    onlineCertB = onlineCertB + Long.parseLong(data[2].toString());
                }
                else if(Constants.PRINTED_AT_CFC.equals(data[1])){
                    cfcCertB = cfcCertB + Long.parseLong(data[2].toString());
                }
               // birth.put(data[0].toString(), data[2] );
            }
            else if(Constants.RECORD_TYPE_DEATH.equals(data[0])){
                if(Constants.PRINTED_AT_HOSPITAL.equals(data[1])) {
                    hospitalCertD = hospitalCertD + Long.parseLong(data[2].toString());
                }
                else if(Constants.PRINTED_AT_ONLINE.equals(data[1])){
                    onlineCertD = onlineCertD + Long.parseLong(data[2].toString());
                }
                else if(Constants.PRINTED_AT_CFC.equals(data[1])){
                    cfcCertD = cfcCertD + Long.parseLong(data[2].toString());
                }
                // birth.put(data[0].toString(), data[2] );
            }
            // RECORD_TYPE_SBIRTH == STILL-BIRTH 29-04-22 As per DB
            if(Constants.RECORD_TYPE_SBIRTH.equals(data[0])){
                if(Constants.PRINTED_AT_HOSPITAL.equals(data[1])) {
                    hospitalCertS = hospitalCertS + Long.parseLong(data[2].toString());
                }
                else if(Constants.PRINTED_AT_ONLINE.equals(data[1])){
                    onlineCertS = onlineCertS + Long.parseLong(data[2].toString());
                }
                else if(Constants.PRINTED_AT_CFC.equals(data[1])){
                    cfcCertS = cfcCertS + Long.parseLong(data[2].toString());
                }
                // birth.put(data[0].toString(), data[2] );
            }




            logger.info("==data====="+data[0] + ""+data[1]+""+data[2]);

        }

        birth.put(Constants.PRINTED_AT_ONLINE, onlineCertB);
        birth.put(Constants.PRINTED_AT_HOSPITAL, hospitalCertB);
        birth.put(Constants.PRINTED_AT_CFC, cfcCertB);

        death.put(Constants.PRINTED_AT_ONLINE, onlineCertD);
        death.put(Constants.PRINTED_AT_HOSPITAL, hospitalCertD);
        death.put(Constants.PRINTED_AT_CFC, cfcCertD);

        sBirth.put(Constants.PRINTED_AT_ONLINE, onlineCertS);
        sBirth.put(Constants.PRINTED_AT_HOSPITAL, hospitalCertS);
        sBirth.put(Constants.PRINTED_AT_CFC, cfcCertS);

        result.put(Constants.RECORD_TYPE_BIRTH, birth);
        result.put(Constants.RECORD_TYPE_DEATH, death);
        result.put(Constants.RECORD_TYPE_STILLBIRTH, sBirth);


        res.setData(result);


        return res;
    }

    /*
    * Desc: Get Report of self CFC User
    * Author:Deepak
    * Date: 18-05-22
    * */

//    public ApiResponse getUserReports(DailyReportDto dailyReportDto) {
//
//        ApiResponse res = new ApiResponse();
//     //   String userId = authService.getUserIdFromRequest(request);
//        List<SlaDetailsModel> slaRecords = findAll(dailyReportDto);
//        Object result = getRecordsAndCount(slaRecords);
//        res.setStatus(HttpStatus.OK);
//        res.setData(result);
//        return res;
//    }

//    public ApiResponse getUserReports(DailyReportDto dailyReportDto) {
//
//        ApiResponse res = new ApiResponse();
//        Map<String, Object> finalResult = new HashMap<String, Object>();
//
//        Map<String, DailyReportCount> total = new HashMap<String, DailyReportCount>();
//        Map<String, Object> amount = new HashMap<String, Object>();
//
//        // Map<String, Object> result = new HashMap<String, Object>();
//
//        List<SlaDetailsModel> slaRecords = new ArrayList<>();
//        List<BirthModel> birthRecords = new ArrayList<>();
//        List<DeathModel> deathRecords = new ArrayList<>();
//        List<SBirthModel> sBirthRecords = new ArrayList<>();
//
//        logger.info("=== curent user id =="+dailyReportDto.getUserId());
//
//
//        slaRecords = findAll(dailyReportDto);
//        birthRecords = getBirthRecords(dailyReportDto);
//        sBirthRecords = getSBirthRecords(dailyReportDto);
//        deathRecords = getDeathRecords(dailyReportDto);
//
//        Float totalAmount = 0F;
//
//        getRecordsAndCount(slaRecords, total);
//        getBirthRecordsAndCount(birthRecords, total);
//        getSBirthRecordsAndCount(sBirthRecords, total);
//        getDeathRecordsAndCount(deathRecords, total);
//
//        for (Map.Entry<String, DailyReportCount> entry : total.entrySet()) {
//            String key = entry.getKey();
//            DailyReportCount value = entry.getValue();
//            // amount.put(key, value);
//            logger.info("==== TOTAL MAP KEY =="+key+"==== TOTAL MAP VALUE =="+value.getAmount());
//            totalAmount += value.getAmount();
//        }
//
//
//        finalResult.put("total", total);
//        finalResult.put("birth_records", birthRecords);
//        finalResult.put("still_birth_records", sBirthRecords);
//        finalResult.put("death_records", deathRecords);
//        finalResult.put("sla_records", slaRecords);
//        finalResult.put("totalAmount", totalAmount);
//
//
//        res.setStatus(HttpStatus.OK);
//        res.setData(finalResult);
//        return res;
//    }

    @Override
    public ApiResponse getUserReports(DailyReportDto dailyReportDto) {

        ApiResponse res = new ApiResponse();
        Map<String, Object> finalResult = new HashMap<String, Object>();

        Map<String, DailyReportCount> total = new HashMap<String, DailyReportCount>();
        Map<String, Object> amount = new HashMap<String, Object>();

        // Map<String, Object> result = new HashMap<String, Object>();

        List<SlaDetailsModel> slaRecords = new ArrayList<>();
        List<BirthModel> birthRecords = new ArrayList<>();
        List<DeathModel> deathRecords = new ArrayList<>();
        List<SBirthModel> sBirthRecords = new ArrayList<>();

        logger.info("=== curent user id =="+dailyReportDto.getUserId());


        slaRecords = findAll(dailyReportDto);
        birthRecords = getBirthRecords(dailyReportDto);
        sBirthRecords = getSBirthRecords(dailyReportDto);
        deathRecords = getDeathRecords(dailyReportDto);

        List<BirthReportResponse> birthList = new ArrayList<>();
        List<SBirthReportResponse> sBirthList = new ArrayList<>();
        List<DeathReportResponse> deathList = new ArrayList<>();
        List<SlaRecordResponse> slaRecordList = new ArrayList<>();



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


        if(birthRecords != null) {


            for (BirthModel birthModel : birthRecords) {
                BirthReportResponse birthReportResponse = new BirthReportResponse();
                BeanUtils.copyProperties(birthModel, birthReportResponse);
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



                birthReportResponse.setMotherLiteracyDesc(motherLit);
                birthReportResponse.setFatherLiteracyDesc(fatherLit);
                birthReportResponse.setFatherReligionDesc(fatherRel);
                birthReportResponse.setMotherReligionDesc(motherRel);
                birthReportResponse.setFatherOccupationDesc(fatherOcpn);
                birthReportResponse.setMotherOccupationDesc(motherOcpn);

                birthList.add(birthReportResponse);
            }
        }

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



                sBirthReportResponse.setMotherLiteracyDesc(motherLit);
                sBirthReportResponse.setFatherLiteracyDesc(fatherLit);
                sBirthReportResponse.setFatherReligionDesc(fatherRel);
                sBirthReportResponse.setMotherReligionDesc(motherRel);
                sBirthReportResponse.setFatherOccupationDesc(fatherOcpn);
                sBirthReportResponse.setMotherOccupationDesc(motherOcpn);

                sBirthList.add(sBirthReportResponse);
            }
        }

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

        if(slaRecords != null){
            for(SlaDetailsModel slaDetailsModel:slaRecords){

                SlaRecordResponse slaRecordResponse = new SlaRecordResponse();
                BeanUtils.copyProperties(slaDetailsModel, slaRecordResponse);

                if(slaDetailsModel.getTransactionType() != null && Constants.NAME_INCLUSION_TRANSACTION.equalsIgnoreCase(slaDetailsModel.getTransactionType())){
                    LocalDateTime dateOfEvent = birthRepository.getDateOfEventByBirthId(slaDetailsModel.getBndId());
                    slaRecordResponse.setDateOfEvent(dateOfEvent);
                }
                slaRecordList.add(slaRecordResponse);
            }

        }


        Float totalAmount = 0F;

        getRecordsAndCount(slaRecords, total);
        getBirthRecordsAndCount(birthRecords, total);
        getSBirthRecordsAndCount(sBirthRecords, total);
        getDeathRecordsAndCount(deathRecords, total);

        for (Map.Entry<String, DailyReportCount> entry : total.entrySet()) {
            String key = entry.getKey();
            DailyReportCount value = entry.getValue();
            // amount.put(key, value);
            //  logger.info("==== TOTAL MAP KEY =="+key+"==== TOTAL MAP VALUE =="+value.getAmount());
            totalAmount += value.getAmount();
        }


        finalResult.put("total", total);
        finalResult.put("birth_records", birthList);
        finalResult.put("still_birth_records", sBirthList);
        finalResult.put("death_records", deathList);
        finalResult.put("sla_records", slaRecordList);
        finalResult.put("totalAmount", totalAmount);


        res.setStatus(HttpStatus.OK);
        res.setData(finalResult);
        return res;
    }


    /*
     * Desc: Get Report of Particulr CFC user
     * Author:Deepak
     * Date: 18-05-22
     * */
    @Override
    public ApiResponse getCfcReports(DailyReportDto dailyReportDto) {

        ApiResponse res = new ApiResponse();
        Map<String, Object> finalResult = new HashMap<String, Object>();

         Map<String, DailyReportCount> total = new HashMap<String, DailyReportCount>();
         Map<String, Object> amount = new HashMap<String, Object>();

        // Map<String, Object> result = new HashMap<String, Object>();

        List<SlaDetailsModel> slaRecords = new ArrayList<>();
        List<BirthModel> birthRecords = new ArrayList<>();
        List<DeathModel> deathRecords = new ArrayList<>();
        List<SBirthModel> sBirthRecords = new ArrayList<>();

        logger.info("=== curent user id =="+dailyReportDto.getUserId());


            slaRecords = findAll(dailyReportDto);
           logger.info("=== SLA RECORRDS ==="+slaRecords);
            birthRecords = getBirthRecords(dailyReportDto);
            sBirthRecords = getSBirthRecords(dailyReportDto);
            deathRecords = getDeathRecords(dailyReportDto);

            List<BirthReportResponse> birthList = new ArrayList<>();
            List<SBirthReportResponse> sBirthList = new ArrayList<>();
            List<DeathReportResponse> deathList = new ArrayList<>();
            List<SlaRecordResponse> slaRecordList = new ArrayList<>();




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


            if(birthRecords != null) {


                for (BirthModel birthModel : birthRecords) {
                    BirthReportResponse birthReportResponse = new BirthReportResponse();
                    BeanUtils.copyProperties(birthModel, birthReportResponse);
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



                    birthReportResponse.setMotherLiteracyDesc(motherLit);
                    birthReportResponse.setFatherLiteracyDesc(fatherLit);
                    birthReportResponse.setFatherReligionDesc(fatherRel);
                    birthReportResponse.setMotherReligionDesc(motherRel);
                    birthReportResponse.setFatherOccupationDesc(fatherOcpn);
                    birthReportResponse.setMotherOccupationDesc(motherOcpn);

                    birthList.add(birthReportResponse);
                }
            }

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



                sBirthReportResponse.setMotherLiteracyDesc(motherLit);
                sBirthReportResponse.setFatherLiteracyDesc(fatherLit);
                sBirthReportResponse.setFatherReligionDesc(fatherRel);
                sBirthReportResponse.setMotherReligionDesc(motherRel);
                sBirthReportResponse.setFatherOccupationDesc(fatherOcpn);
                sBirthReportResponse.setMotherOccupationDesc(motherOcpn);

                sBirthList.add(sBirthReportResponse);
            }
        }

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

            if(slaRecords != null){
                for(SlaDetailsModel slaDetailsModel:slaRecords){

                    SlaRecordResponse slaRecordResponse = new SlaRecordResponse();
                    BeanUtils.copyProperties(slaDetailsModel, slaRecordResponse);

                    if(slaDetailsModel.getTransactionType() != null && Constants.NAME_INCLUSION_TRANSACTION.equalsIgnoreCase(slaDetailsModel.getTransactionType())){
                        LocalDateTime dateOfEvent = birthRepository.getDateOfEventByBirthId(slaDetailsModel.getBndId());
                        slaRecordResponse.setDateOfEvent(dateOfEvent);
                    }
                    slaRecordList.add(slaRecordResponse);
                }

            }


            Float totalAmount = 0F;

        getRecordsAndCount(slaRecords, total);
        getBirthRecordsAndCount(birthRecords, total);
        getSBirthRecordsAndCount(sBirthRecords, total);
        getDeathRecordsAndCount(deathRecords, total);

            for (Map.Entry<String, DailyReportCount> entry : total.entrySet()) {
                String key = entry.getKey();
                DailyReportCount value = entry.getValue();
               // amount.put(key, value);
              //  logger.info("==== TOTAL MAP KEY =="+key+"==== TOTAL MAP VALUE =="+value.getAmount());
                totalAmount += value.getAmount();
    }
        finalResult.put("total", total);
        finalResult.put("birth_records", birthList);
        finalResult.put("still_birth_records", sBirthList);
        finalResult.put("death_records", deathList);
        finalResult.put("sla_records", slaRecordList);
        finalResult.put("totalAmount", totalAmount);


        res.setStatus(HttpStatus.OK);
        res.setData(finalResult);
        return res;
    }


    private List<BirthModel> getBirthRecords(DailyReportDto dailyReportDto){
        // Long organizationId = authService.getOrganizationIdFromUserId(userId);
        return birthRepository.findAll(new Specification<BirthModel>() {
            @Override
            public Predicate toPredicate(Root<BirthModel> root, CriteriaQuery<?> query,
                                         CriteriaBuilder criteriaBuilder) {
                try {
                    List<Predicate> predicates = new ArrayList<>();
                    // logger.info("filterDto1:" + filterDto1);
                    DailyReportDto filterDto = dailyReportDto;

                    if (!CommonUtil.checkNullOrBlank(filterDto.getRegStartDate() + "") && !CommonUtil.checkNullOrBlank(filterDto.getRegEndDate() + "")) {
                        // registrationDatetime
                        CommonUtil.betweenDates(filterDto.getRegStartDate().atTime(00, 00, 00), filterDto.getRegEndDate().atTime(23, 59, 59));
                        predicates
                                .add(criteriaBuilder.and(criteriaBuilder.between(root.get(Constants.CREATED_AT),
                                        filterDto.getRegStartDate().atTime(00, 00, 00), filterDto.getRegEndDate().atTime(23, 59, 59))));
                    }

                    if (!CommonUtil.checkNullOrBlank(filterDto.getOrgId())) {
                        // Division code
                        predicates.add(criteriaBuilder.and(
                                criteriaBuilder.equal(root.get(Constants.USER_ORGID), filterDto.getOrgId())));
                    }

                    if (!CommonUtil.checkNullOrBlank(filterDto.getUserId())) {
                        // User id
                        predicates.add(criteriaBuilder.and(
                                criteriaBuilder.equal(root.get(Constants.USER_ID), filterDto.getUserId())));
                    }

//                    if (!CommonUtil.checkNullOrBlank(filterDto.getTransactionType())) {
//                        // User id
//                        predicates.add(criteriaBuilder.and(
//                                criteriaBuilder.equal(root.get(Constants.TRANSACTION_TYPE), filterDto.getTransactionType())));
//                    }
                    /*
                     * Status = PENDING & APPROVED
                     * */

                    predicates.add(criteriaBuilder.or(criteriaBuilder.equal(root.get(Constants.STATUS),Constants.RECORD_STATUS_APPROVED),
                            criteriaBuilder.equal(root.get(Constants.STATUS),Constants.RECORD_STATUS_PENDING)));
                    //  predicates.add(criteriaBuilder.or(criteriaBuilder.equal(root.get(Constants.STATUS),Constants.RECORD_STATUS_PENDING )));
                    //  predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.STATUS),Constants.RECORD_STATUS_PENDING)));

                    return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
                } catch (Exception e) {
                    logger.error("Exception {}... ", e);
                    throw e;
                }
            }

        });
    }
    private List<SBirthModel> getSBirthRecords(DailyReportDto dailyReportDto){
        // Long organizationId = authService.getOrganizationIdFromUserId(userId);
        return sBirthRepository.findAll(new Specification<SBirthModel>() {
            @Override
            public Predicate toPredicate(Root<SBirthModel> root, CriteriaQuery<?> query,
                                         CriteriaBuilder criteriaBuilder) {
                try {
                    List<Predicate> predicates = new ArrayList<>();
                    // logger.info("filterDto1:" + filterDto1);
                    DailyReportDto filterDto = dailyReportDto;

                    if (!CommonUtil.checkNullOrBlank(filterDto.getRegStartDate() + "") && !CommonUtil.checkNullOrBlank(filterDto.getRegEndDate() + "")) {
                        // registrationDatetime
                        CommonUtil.betweenDates(filterDto.getRegStartDate().atTime(00, 00, 00), filterDto.getRegEndDate().atTime(23, 59, 59));
                        predicates
                                .add(criteriaBuilder.and(criteriaBuilder.between(root.get(Constants.CREATED_AT),
                                        filterDto.getRegStartDate().atTime(00, 00, 00), filterDto.getRegEndDate().atTime(23, 59, 59))));
                    }

                    if (!CommonUtil.checkNullOrBlank(filterDto.getOrgId())) {
                        // Division code
                        predicates.add(criteriaBuilder.and(
                                criteriaBuilder.equal(root.get(Constants.USER_ORGID), filterDto.getOrgId())));
                    }

                    if (!CommonUtil.checkNullOrBlank(filterDto.getUserId())) {
                        // User id
                        predicates.add(criteriaBuilder.and(
                                criteriaBuilder.equal(root.get(Constants.USER_ID), filterDto.getUserId())));
                    }

//                    if (!CommonUtil.checkNullOrBlank(filterDto.getTransactionType())) {
//                        // User id
//                        predicates.add(criteriaBuilder.and(
//                                criteriaBuilder.equal(root.get(Constants.TRANSACTION_TYPE), filterDto.getTransactionType())));
//                    }
                    /*
                     * Status = PENDING & APPROVED
                     * */

                    predicates.add(criteriaBuilder.or(criteriaBuilder.equal(root.get(Constants.STATUS),Constants.RECORD_STATUS_APPROVED),
                            criteriaBuilder.equal(root.get(Constants.STATUS),Constants.RECORD_STATUS_PENDING)));
                    //  predicates.add(criteriaBuilder.or(criteriaBuilder.equal(root.get(Constants.STATUS),Constants.RECORD_STATUS_PENDING )));
                    //  predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.STATUS),Constants.RECORD_STATUS_PENDING)));

                    return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
                } catch (Exception e) {
                    logger.error("Exception {}... ", e);
                    throw e;
                }
            }

        });
    }
    private List<DeathModel> getDeathRecords(DailyReportDto dailyReportDto){
        // Long organizationId = authService.getOrganizationIdFromUserId(userId);
        return deathRepository.findAll(new Specification<DeathModel>() {
            @Override
            public Predicate toPredicate(Root<DeathModel> root, CriteriaQuery<?> query,
                                         CriteriaBuilder criteriaBuilder) {
                try {
                    List<Predicate> predicates = new ArrayList<>();
                    // logger.info("filterDto1:" + filterDto1);
                    DailyReportDto filterDto = dailyReportDto;

                    if (!CommonUtil.checkNullOrBlank(filterDto.getRegStartDate() + "") && !CommonUtil.checkNullOrBlank(filterDto.getRegEndDate() + "")) {
                        // registrationDatetime
                        CommonUtil.betweenDates(filterDto.getRegStartDate().atTime(00, 00, 00), filterDto.getRegEndDate().atTime(23, 59, 59));
                        predicates
                                .add(criteriaBuilder.and(criteriaBuilder.between(root.get(Constants.CREATED_AT),
                                        filterDto.getRegStartDate().atTime(00, 00, 00), filterDto.getRegEndDate().atTime(23, 59, 59))));
                    }

                    if (!CommonUtil.checkNullOrBlank(filterDto.getOrgId())) {
                        // Division code
                        predicates.add(criteriaBuilder.and(
                                criteriaBuilder.equal(root.get(Constants.USER_ORGID), filterDto.getOrgId())));
                    }

                    if (!CommonUtil.checkNullOrBlank(filterDto.getUserId())) {
                        // User id
                        predicates.add(criteriaBuilder.and(
                                criteriaBuilder.equal(root.get(Constants.USER_ID), filterDto.getUserId())));
                    }

//                    if (!CommonUtil.checkNullOrBlank(filterDto.getTransactionType())) {
//                        // User id
//                        predicates.add(criteriaBuilder.and(
//                                criteriaBuilder.equal(root.get(Constants.TRANSACTION_TYPE), filterDto.getTransactionType())));
//                    }
                    /*
                     * Status = PENDING & APPROVED
                     * */

                    predicates.add(criteriaBuilder.or(criteriaBuilder.equal(root.get(Constants.STATUS),Constants.RECORD_STATUS_APPROVED),
                            criteriaBuilder.equal(root.get(Constants.STATUS),Constants.RECORD_STATUS_PENDING)));
                    //  predicates.add(criteriaBuilder.or(criteriaBuilder.equal(root.get(Constants.STATUS),Constants.RECORD_STATUS_PENDING )));
                    //  predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.STATUS),Constants.RECORD_STATUS_PENDING)));

                    return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
                } catch (Exception e) {
                    logger.error("Exception {}... ", e);
                    throw e;
                }
            }

        });
    }

    private Object getBirthRecordsAndCount(List<BirthModel> birthRecords, Map<String, DailyReportCount> total) {




        DailyReportCount birth = new DailyReportCount();


        for(BirthModel birthModel: birthRecords){

          //  if(!CommonUtil.checkNullOrBlank(birthModel.getTransactionType()) && Constants.RECORD_APPROVED.equalsIgnoreCase(birthModel.getTransactionType())){

              //  logger.info("===== get amount ==="+birthModel.getLateFee());
                //logger.info("===== birthCorrection.getNoOfApplication()==="+birthModel.getNoOfApplication());
                if(birthModel.getLateFee() == null) {
                    birthModel.setLateFee(0F);
                }
                birth.setAmount(birth.getAmount()+birthModel.getLateFee());

           // }
        }

        birth.setNoOfApplication(birthRecords.stream().count());
        total.put(Constants.RECORD_TYPE_BIRTH, birth);

        return total;
    }

    private Object getSBirthRecordsAndCount(List<SBirthModel> sBirthRecords, Map<String, DailyReportCount> total) {


        DailyReportCount sBirth = new DailyReportCount();

        for(SBirthModel sBirthModel: sBirthRecords){

         //   if(!CommonUtil.checkNullOrBlank(sBirthModel.getTransactionType()) && Constants.RECORD_APPROVED.equalsIgnoreCase(sBirthModel.getTransactionType())){

              //  logger.info("===== get amount ==="+sBirthModel.getLateFee());
                //logger.info("===== birthCorrection.getNoOfApplication()==="+birthModel.getNoOfApplication());
                if(sBirthModel.getLateFee() == null) {
                    sBirthModel.setLateFee(0F);
                }
                sBirth.setAmount(sBirth.getAmount()+sBirthModel.getLateFee());

           // }
        }


        sBirth.setNoOfApplication(sBirthRecords.stream().count());
        total.put(Constants.RECORD_TYPE_STILL_BIRTH, sBirth);


        return total;
    }

    private Object getDeathRecordsAndCount(List<DeathModel> deathRecords, Map<String, DailyReportCount> total) {


        DailyReportCount death = new DailyReportCount();
        for(DeathModel deathModel: deathRecords){

           // if(!CommonUtil.checkNullOrBlank(deathModel.getTransactionType()) && Constants.RECORD_APPROVED.equalsIgnoreCase(deathModel.getTransactionType())){

               // logger.info("===== get amount ==="+deathModel.getLateFee());
                //logger.info("===== birthCorrection.getNoOfApplication()==="+birthModel.getNoOfApplication());
                if(deathModel.getLateFee() == null) {
                    deathModel.setLateFee(0F);
                }
                death.setAmount(death.getAmount()+deathModel.getLateFee());
           // }
        }

        death.setNoOfApplication(deathRecords.stream().count());
        total.put(Constants.RECORD_TYPE_DEATH, death);
        return total;
    }

    private Object getRecordsAndCount(List<SlaDetailsModel> slaRecords, Map<String, DailyReportCount> total) {


        DailyReportCount stillBirthCorrection = new DailyReportCount();
        DailyReportCount birthCorrection = new DailyReportCount();
        DailyReportCount deathCorrection = new DailyReportCount();
        DailyReportCount printBirthCertificate = new DailyReportCount();
        DailyReportCount printSBirthCertificate = new DailyReportCount();
        DailyReportCount printDeathCertificate = new DailyReportCount();
        DailyReportCount nameInclusion = new DailyReportCount();


        // setZero(birthCorrection, stillBirthCorrection, deathCorrection, printBirthCertificate, printDeathCertificate, printSBirthCertificate, nameInclusion);


        for(SlaDetailsModel slaDetailsModel: slaRecords){
            if(!CommonUtil.checkNullOrBlank(slaDetailsModel.getTransactionType()) && Constants.BIRTH_CORRECTION.equalsIgnoreCase(slaDetailsModel.getTransactionType())){

                logger.info("===== get amount ==="+slaDetailsModel.getAmount());
                logger.info("===== birthCorrection.getNoOfApplication()==="+birthCorrection.getNoOfApplication());
                if(slaDetailsModel.getAmount() == null) {
                    slaDetailsModel.setAmount(0F);
                }if(birthCorrection.getNoOfApplication() == null){
                    birthCorrection.setNoOfApplication(0L);
                }
                birthCorrection.setAmount(birthCorrection.getAmount()+slaDetailsModel.getAmount());
                birthCorrection.setNoOfApplication(birthCorrection.getNoOfApplication() + 1);
            }
            else if(!CommonUtil.checkNullOrBlank(slaDetailsModel.getTransactionType()) && Constants.DEATH_CORRECTION.equalsIgnoreCase(slaDetailsModel.getTransactionType())){
                if(slaDetailsModel.getAmount() == null) {
                    slaDetailsModel.setAmount(0F);
                }
                if(deathCorrection.getNoOfApplication() == null){
                    deathCorrection.setNoOfApplication(0L);
                }
                deathCorrection.setAmount(birthCorrection.getAmount()+slaDetailsModel.getAmount());
                deathCorrection.setNoOfApplication(deathCorrection.getNoOfApplication() + 1);
            }

            else if(!CommonUtil.checkNullOrBlank(slaDetailsModel.getTransactionType()) && Constants.STILL_BIRTH_CORRECTION.equalsIgnoreCase(slaDetailsModel.getTransactionType())){
                if(slaDetailsModel.getAmount() == null) {
                    slaDetailsModel.setAmount(0F);
                }
                if(stillBirthCorrection.getNoOfApplication() == null){
                    stillBirthCorrection.setNoOfApplication(0L);
                }
                stillBirthCorrection.setAmount(stillBirthCorrection.getAmount()+slaDetailsModel.getAmount());
                stillBirthCorrection.setNoOfApplication(stillBirthCorrection.getNoOfApplication() + 1);
            }

            else if(!CommonUtil.checkNullOrBlank(slaDetailsModel.getTransactionType()) && Constants.TRANSACTION_TYPE_PRINT_BIRTH_CERT.equalsIgnoreCase(slaDetailsModel.getTransactionType())){
                if(slaDetailsModel.getAmount() == null) {
                    slaDetailsModel.setAmount(0F);
                }
                if(printBirthCertificate.getNoOfApplication() == null){
                    printBirthCertificate.setNoOfApplication(0L);
                }
                if(printBirthCertificate.getNoOfCopy() == null){
                    printBirthCertificate.setNoOfCopy(0L);
                }
                printBirthCertificate.setAmount(printBirthCertificate.getAmount()+slaDetailsModel.getAmount());
                printBirthCertificate.setNoOfApplication(printBirthCertificate.getNoOfApplication() + 1);
                printBirthCertificate.setNoOfCopy(printBirthCertificate.getNoOfCopy() + 1);
            }

            else if(!CommonUtil.checkNullOrBlank(slaDetailsModel.getTransactionType()) && Constants.TRANSACTION_TYPE_PRINT_STILL_BIRTH_CERT.equalsIgnoreCase(slaDetailsModel.getTransactionType())){
                if(slaDetailsModel.getAmount() == null) {
                    slaDetailsModel.setAmount(0F);
                }
                if(printSBirthCertificate.getNoOfApplication() == null){
                    printSBirthCertificate.setNoOfApplication(0L);
                }
                if(printSBirthCertificate.getNoOfCopy() == null){
                    printSBirthCertificate.setNoOfCopy(0L);
                }
                printSBirthCertificate.setAmount(printSBirthCertificate.getAmount()+slaDetailsModel.getAmount());
                printSBirthCertificate.setNoOfApplication(printSBirthCertificate.getNoOfApplication() + 1);
                printSBirthCertificate.setNoOfCopy(printSBirthCertificate.getNoOfCopy() + 1);
            }
            else if(!CommonUtil.checkNullOrBlank(slaDetailsModel.getTransactionType()) && Constants.TRANSACTION_TYPE_PRINT_DEATH_CERT.equalsIgnoreCase(slaDetailsModel.getTransactionType())){
                if(slaDetailsModel.getAmount() == null) {
                    slaDetailsModel.setAmount(0F);
                }
                if(printDeathCertificate.getNoOfApplication() == null){
                    printDeathCertificate.setNoOfApplication(0L);
                }
                if(printDeathCertificate.getNoOfCopy() == null){
                    printDeathCertificate.setNoOfCopy(0L);
                }
                printDeathCertificate.setAmount(printDeathCertificate.getAmount()+slaDetailsModel.getAmount());
                printDeathCertificate.setNoOfApplication(printDeathCertificate.getNoOfApplication() + 1);
                printDeathCertificate.setNoOfCopy(printDeathCertificate.getNoOfCopy() + 1);
            }
            else if(!CommonUtil.checkNullOrBlank(slaDetailsModel.getTransactionType()) && Constants.RECORD_NAME_INCLUSION.equalsIgnoreCase(slaDetailsModel.getTransactionType())){
                if(slaDetailsModel.getAmount() == null) {
                    slaDetailsModel.setAmount(0F);
                }
                if(nameInclusion.getNoOfApplication() == null){
                    nameInclusion.setNoOfApplication(0L);
                }

                nameInclusion.setAmount(nameInclusion.getAmount()+slaDetailsModel.getAmount());
                nameInclusion.setNoOfApplication(nameInclusion.getNoOfApplication() + 1);
                // nameInclusion.setNoOfCopy(nameInclusion.getNoOfCopy() + 1);
            }

        }
        total.put(Constants.STILL_BIRTH_CORRECTION, stillBirthCorrection);
        total.put(Constants.BIRTH_CORRECTION, birthCorrection);
        total.put(Constants.DEATH_CORRECTION, deathCorrection);
        total.put(Constants.TRANSACTION_TYPE_PRINT_BIRTH_CERT, printBirthCertificate);
        total.put(Constants.TRANSACTION_TYPE_PRINT_STILL_BIRTH_CERT, printSBirthCertificate);
        total.put(Constants.TRANSACTION_TYPE_PRINT_DEATH_CERT, printDeathCertificate);
        total.put(Constants.RECORD_NAME_INCLUSION, nameInclusion);



        return total;
    }

    private void setZero(DailyReportCount birthCorrection, DailyReportCount stillBirthCorrection, DailyReportCount deathCorrection,
                         DailyReportCount printBirthCertificate, DailyReportCount printDeathCertificate, DailyReportCount printSBirthCertificate,
                         DailyReportCount nameInclusion) {

        if(birthCorrection == null){
            birthCorrection.setAmount(0F);
            birthCorrection.setNoOfApplication(0L);
        }
        else if(stillBirthCorrection == null){
            stillBirthCorrection.setAmount(0F);
            stillBirthCorrection.setNoOfApplication(0L);
        }
        else if(deathCorrection == null){
            deathCorrection.setAmount(0F);
            deathCorrection.setNoOfApplication(0L);
        }
        else if(printBirthCertificate == null){
            printBirthCertificate.setAmount(0F);
            printBirthCertificate.setNoOfApplication(0L);
            printBirthCertificate.setNoOfCopy(0L);
        }
        else if(printDeathCertificate == null){
            printDeathCertificate.setAmount(0F);
            printDeathCertificate.setNoOfApplication(0L);
            printDeathCertificate.setNoOfCopy(0L);
        }
        else if(printSBirthCertificate == null){
            printSBirthCertificate.setAmount(0F);
            printSBirthCertificate.setNoOfApplication(0L);
            printSBirthCertificate.setNoOfCopy(0L);
        }
        else if(nameInclusion == null){
            nameInclusion.setAmount(0F);
            nameInclusion.setNoOfApplication(0L);
        }
    }




    /*
     * Desc: Get Report of Online name inclusion
     * Author:Deepak
     * Date: 18-05-22
     * */

    @Override
    public ApiResponse getOnlineNameInclusionReports(DailyReportDto dailyReportDto) {
        ApiResponse res = new ApiResponse();
        //   String userId = authService.getUserIdFromRequest(request);
        List<OnlineSlaDetailsModel> onlineSlaDetailsModels = new ArrayList<>();
        List<SlaDetailsModel> slaRecords = findAll(dailyReportDto);
        for(SlaDetailsModel slaDetailsModel: slaRecords){
            OnlineSlaDetailsModel onlineSlaDetailsModel = new OnlineSlaDetailsModel();
            BeanUtils.copyProperties(slaDetailsModel, onlineSlaDetailsModel);
            onlineSlaDetailsModels.add(onlineSlaDetailsModel);
        }

        res.setStatus(HttpStatus.OK);
        res.setData(onlineSlaDetailsModels);
        return res;
    }

    @Override
    public ApiResponse getCauseOfDeathReports(FilterDto filterDto, HttpServletRequest request) throws Exception {
        return dashboardService.dashboardFilteredData(filterDto, Constants.RECORD_TYPE_DEATH, request);

    }

    private List<SlaDetailsModel> findAll(DailyReportDto dailyReportDto){
       // Long organizationId = authService.getOrganizationIdFromUserId(userId);
        return slaDetailsRepository.findAll(new Specification<SlaDetailsModel>() {
            @Override
            public Predicate toPredicate(Root<SlaDetailsModel> root, CriteriaQuery<?> query,
                                         CriteriaBuilder criteriaBuilder) {
                try {
                    List<Predicate> predicates = new ArrayList<>();
                    // logger.info("filterDto1:" + filterDto1);
                    DailyReportDto filterDto = dailyReportDto;

                    if (!CommonUtil.checkNullOrBlank(filterDto.getRegStartDate() + "") && !CommonUtil.checkNullOrBlank(filterDto.getRegEndDate() + "")) {
                        // registrationDatetime
                        CommonUtil.betweenDates(filterDto.getRegStartDate().atTime(00, 00, 00), filterDto.getRegEndDate().atTime(23, 59, 59));
                        predicates
                                .add(criteriaBuilder.and(criteriaBuilder.between(root.get(Constants.CREATED_AT),
                                        filterDto.getRegStartDate().atTime(00, 00, 00), filterDto.getRegEndDate().atTime(23, 59, 59))));
                    }

                    if (!CommonUtil.checkNullOrBlank(filterDto.getOrgId())) {
                        // Division code
                        predicates.add(criteriaBuilder.and(
                                criteriaBuilder.equal(root.get(Constants.SLA_ORGID), filterDto.getOrgId())));
                    }

                    if (!CommonUtil.checkNullOrBlank(filterDto.getUserId())) {
                        // User id
                        predicates.add(criteriaBuilder.and(
                                criteriaBuilder.equal(root.get(Constants.USER_ID), filterDto.getUserId())));
                    }

                    if (!CommonUtil.checkNullOrBlank(filterDto.getTransactionType())) {
                        // User id
                        predicates.add(criteriaBuilder.and(
                                criteriaBuilder.equal(root.get(Constants.TRANSACTION_TYPE), filterDto.getTransactionType())));
                    }

                    if (!CommonUtil.checkNullOrBlank(filterDto.getRegistrationNumber())) {
                        // Registration Number
                        predicates.add(criteriaBuilder.and(
                                criteriaBuilder.equal(root.get(Constants.REGISTRATION_NUMBER), filterDto.getRegistrationNumber())));
                    }

                    if (!CommonUtil.checkNullOrBlank(filterDto.getApplicationNumber())) {
                        // Application Number
                        predicates.add(criteriaBuilder.and(
                                criteriaBuilder.equal(root.get(Constants.APPL_NUMBER), filterDto.getApplicationNumber())));
                    }
                    /*
                    * Status = PENDING & APPROVED
                    * */

                     predicates.add(criteriaBuilder.or(criteriaBuilder.equal(root.get(Constants.STATUS),Constants.RECORD_STATUS_APPROVED),
                             criteriaBuilder.equal(root.get(Constants.STATUS),Constants.RECORD_STATUS_PENDING)));
                   //  predicates.add(criteriaBuilder.or(criteriaBuilder.equal(root.get(Constants.STATUS),Constants.RECORD_STATUS_PENDING )));
                  //  predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.STATUS),Constants.RECORD_STATUS_PENDING)));

                    return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
                } catch (Exception e) {
                    logger.error("Exception {}... ", e);
                    throw e;
                }
            }

        });
    }


    private Map<String, Object> getBirthRecordsByEventFlag(ReportSearchDto reportSearchDto) {
        List<Object[]> birthData = birthRepository.getBirthRecordsByEventPlaceFlag(reportSearchDto.getRegStartDate(), reportSearchDto.getRegEndDate(), Constants.RECORD_STATUS_APPROVED);

        Map<String, Object> birth = new HashMap<String, Object>();
        Map<String, Object> inst = new HashMap<String, Object>();
        Map<String, Object> dom = new HashMap<String, Object>();

        Long instTotal = 0L;
        Long domTotal = 0L;

        inst.put(Constants.GENDER_CODE_A, 0);
        inst.put(Constants.GENDER_CODE_F, 0);
        inst.put(Constants.GENDER_CODE_M, 0);
        dom.put(Constants.GENDER_CODE_A, 0);
        dom.put(Constants.GENDER_CODE_F, 0);
        dom.put(Constants.GENDER_CODE_M, 0);

//        birth.put(Constants.REPORT_INST, inst);
//        birth.put(Constants.REPORT_DOM, dom);
//        birth.put(Constants.TOTAL, instTotal+domTotal);

        for(Object[] data:birthData)
        {
            //gender_code,event_place_flag, COUNT(*)
            //data[0] => gender_code, data[1] => event_place_flag, data[2] => count
            if( data[0] != null && data[1].equals(Constants.EVENT_FLAG_HOSPITAL) ){
                instTotal = instTotal + Long.parseLong(data[2].toString()) ;
                // instTotal = instTotal + ((BigInteger) data[2]).intValue();

                inst.put(data[0].toString(), data[2] );
            }
            else if( data[0] != null && data[1].equals(Constants.EVENT_FLAG_HOME) ){

                domTotal = domTotal + Long.parseLong(data[2].toString()) ;
                // domTotal = domTotal + ((BigInteger) data[2]).intValue();
                dom.put(data[0].toString(), data[2] );
            }

            logger.info("==data====="+data[0] + ""+data[1]+""+data[2]);

        }
        inst.put(Constants.TOTAL, instTotal);
        dom.put(Constants.TOTAL, domTotal);
        birth.put(Constants.REPORT_INST, inst);
        birth.put(Constants.REPORT_DOM, dom);
        birth.put(Constants.TOTAL, instTotal+domTotal);


        return birth;
    }

    private Map<String, Object> getSbirthRecordsByEventFlag(ReportSearchDto reportSearchDto) {
        List<Object[]> sBirthData = sBirthRepository.getSbirthRecordsByEventPlaceFlag(reportSearchDto.getRegStartDate(), reportSearchDto.getRegEndDate(), Constants.RECORD_STATUS_APPROVED);

        Map<String, Object> sBirth = new HashMap<String, Object>();
        Map<String, Object> inst = new HashMap<String, Object>();
        Map<String, Object> dom = new HashMap<String, Object>();

        Long instTotal = 0L;
        Long domTotal = 0L;

        inst.put(Constants.GENDER_CODE_A, 0);
        inst.put(Constants.GENDER_CODE_F, 0);
        inst.put(Constants.GENDER_CODE_M, 0);
        dom.put(Constants.GENDER_CODE_A, 0);
        dom.put(Constants.GENDER_CODE_F, 0);
        dom.put(Constants.GENDER_CODE_M, 0);


        for(Object[] data:sBirthData)
        {
            if( data[0] != null && data[1].equals(Constants.EVENT_FLAG_HOSPITAL)){
                instTotal = instTotal + Long.parseLong(data[2].toString()) ;
                // instTotal = instTotal + ((BigInteger) data[2]).intValue();

                inst.put(data[0].toString(), data[2] );
            }
            else if(data[0] != null && data[1].equals(Constants.EVENT_FLAG_HOME) ){

                domTotal = domTotal + Long.parseLong(data[2].toString()) ;
                // domTotal = domTotal + ((BigInteger) data[2]).intValue();
                dom.put(data[0].toString(), data[2] );
            }

            logger.info("==data====="+data[0] + ""+data[1]+""+data[2]);

        }
        inst.put(Constants.TOTAL, instTotal);
        dom.put(Constants.TOTAL, domTotal);
        sBirth.put(Constants.REPORT_INST, inst);
        sBirth.put(Constants.REPORT_DOM, dom);
        sBirth.put(Constants.TOTAL, instTotal+domTotal);

        return sBirth;
    }

    private Map<String, Object> getDeathRecordsByEventFlag(ReportSearchDto reportSearchDto) {
        List<Object[]> deathData = deathRepository.getDeathRecordsByEventPlaceFlag(reportSearchDto.getRegStartDate(), reportSearchDto.getRegEndDate(), Constants.RECORD_STATUS_APPROVED);

        Map<String, Object> death = new HashMap<String, Object>();
        Map<String, Object> inst = new HashMap<String, Object>();
        Map<String, Object> dom = new HashMap<String, Object>();

        Long instTotal = 0L;
        Long domTotal = 0L;

        inst.put(Constants.GENDER_CODE_A, 0);
        inst.put(Constants.GENDER_CODE_F, 0);
        inst.put(Constants.GENDER_CODE_M, 0);
        dom.put(Constants.GENDER_CODE_A, 0);
        dom.put(Constants.GENDER_CODE_F, 0);
        dom.put(Constants.GENDER_CODE_M, 0);

        for(Object[] data:deathData)
        {
            if(data[0] != null && data[1].equals(Constants.EVENT_FLAG_HOSPITAL)){
                instTotal = instTotal + Long.parseLong(data[2].toString()) ;
                // instTotal = instTotal + ((BigInteger) data[2]).intValue();

                inst.put(data[0].toString(), data[2] );
            }
            else if(data[0] != null && data[1].equals(Constants.EVENT_FLAG_HOME) ){

                domTotal = domTotal + Long.parseLong(data[2].toString()) ;
                // domTotal = domTotal + ((BigInteger) data[2]).intValue();
                dom.put(data[0].toString(), data[2] );
            }

            logger.info("==data====="+data[0] + ""+data[1]+""+data[2]);

        }
        inst.put(Constants.TOTAL, instTotal);
        dom.put(Constants.TOTAL, domTotal);
        death.put(Constants.REPORT_INST, inst);
        death.put(Constants.REPORT_DOM, dom);
        death.put(Constants.TOTAL, instTotal+domTotal);

        return death;
    }



    private List<Object[]> getTotalDeathRegistration(ReportSearchDto reportSearchDto) {
        List<Object[]> responseData = deathRepository.getTotalDeathRecords(reportSearchDto.getRegStartDate(), reportSearchDto.getRegEndDate(), Constants.RECORD_STATUS_APPROVED);
        return responseData;
    }

    private List<Object[]> getTotalStillBirthRegistration(ReportSearchDto reportSearchDto) {

        List<Object[]> responseData = sBirthRepository.getTotalSBirthRecords(reportSearchDto.getRegStartDate(), reportSearchDto.getRegEndDate(), Constants.RECORD_STATUS_APPROVED);
        return responseData;
    }

    private List<Object[]> getTotalBirthRegistration(ReportSearchDto reportSearchDto) {

        List<Object[]> responseData = birthRepository.getTotalBirthRecords(reportSearchDto.getRegStartDate(), reportSearchDto.getRegEndDate(), Constants.RECORD_STATUS_APPROVED);
        return responseData;
    }


    private List<Object[]> getInfantDeathReportLessThanOne(ReportSearchDto reportSearchDto) {
        DeathModel deathModel = new DeathModel();
        List<Object[]> numberOfInfantLessThanOne = deathRepository.getInfantDeathReportLessThanOne(reportSearchDto.getRegStartDate(), reportSearchDto.getRegEndDate(), Constants.RECORD_STATUS_APPROVED);
        return numberOfInfantLessThanOne;
    }

    private List<Object[]> getInfantDeathReportOneToFive(ReportSearchDto reportSearchDto) {
        DeathModel deathModel = new DeathModel();
        List<Object[]> numberOfInfantOneToFive = deathRepository.getInfantDeathReportOneToFive(reportSearchDto.getRegStartDate(), reportSearchDto.getRegEndDate(), Constants.RECORD_STATUS_APPROVED);
        return numberOfInfantOneToFive;
    }


    /***
     * Date :- 26 May 2022
     * 
     * Developer :- Rizwan Khot
     * 
     * MIS Report 
     * 
     * @param reportSearchDto: ReportSearchDto - contains start date, end date, cause of death and division code
     * 
     * @return List<Object[]>
     * 0- Year
     * 1- Montrh
     * 2- Name of Month
     * 3- Gender Code
     * 4- Event place flag
     * 5- Total
     */

    @Override
    public ApiResponse getMisReport(ReportSearchDto reportSearchDto, HttpServletRequest request, String dateType,
            String eventType) throws Exception {
            List<Object[]> resultList = new ArrayList<>();
            
            if(Constants.EVENT.equalsIgnoreCase(dateType)){
               CommonUtil.betweenDates(reportSearchDto.getEventStartDate().atStartOfDay(), reportSearchDto.getEventEndDate().atTime(23, 59, 59));
            }else if(Constants.REGISTRATION.equalsIgnoreCase(dateType)){
               CommonUtil.betweenDates(reportSearchDto.getRegStartDate().atStartOfDay(), reportSearchDto.getRegEndDate().atTime(23, 59, 59));
            }

            if(Constants.RECORD_TYPE_BIRTH.equalsIgnoreCase(eventType)) {
                resultList = getBirthMisReport(reportSearchDto, dateType);
                // if(Constants.EVENT.equalsIgnoreCase(dateType)) {
                //     resultList = birthRepository.getMisCountsByEventDate(reportSearchDto.getEventStartDate().atStartOfDay(), reportSearchDto.getEventEndDate().atTime(23, 23, 59), Constants.RECORD_STATUS_APPROVED);                   
                // } else if(Constants.REGISTRATION.equalsIgnoreCase(dateType)) {
                //     resultList = birthRepository.getMisCountsByRegistrationDate(reportSearchDto.getRegStartDate().atStartOfDay(), reportSearchDto.getRegEndDate().atTime(23, 23, 59), Constants.RECORD_STATUS_APPROVED);
                // }
            } else if(Constants.RECORD_TYPE_DEATH.equalsIgnoreCase(eventType)) {
                resultList = getDeathMisReport(reportSearchDto, dateType);
                // if(Constants.EVENT.equalsIgnoreCase(dateType)) {
                //     resultList = deathRepository.getMisCountsByEventDate(reportSearchDto.getEventStartDate().atStartOfDay(), reportSearchDto.getEventEndDate().atTime(23, 23, 59), Constants.RECORD_STATUS_APPROVED);                   
                // } else if(Constants.REGISTRATION.equalsIgnoreCase(dateType)) {
                //     resultList = deathRepository.getMisCountsByRegistrationDate(reportSearchDto.getRegStartDate().atStartOfDay(), reportSearchDto.getRegEndDate().atTime(23, 23, 59), Constants.RECORD_STATUS_APPROVED);
                // }
                //logger.debug("Death MIS Report : " + resultList);
            }
            Map<String, Object> monthYearMap = new TreeMap<>();
            if(resultList != null && resultList.size()>0) {
                for(Object[] obj : resultList) {
                    String monthYear = obj[0].toString() +  " "+ obj[2].toString();
                    Map<String, Object> dataMap = (Map<String, Object>) monthYearMap.get(monthYear);

                    if(dataMap == null) {
                        dataMap = new HashMap<>();
                        dataMap.put("Month", obj[2].toString());
                        dataMap.put("Year", obj[0].toString());
                        dataMap.put(Constants.EVENT_FLAG_HOSPITAL, getZeroEventMap());
                        dataMap.put(Constants.EVENT_FLAG_HOME, getZeroEventMap());

                    }
                    //logger.debug("==obj[4]====="+obj[4]);
                    if(obj[4] != null && (obj[4].equals(Constants.EVENT_FLAG_HOSPITAL) || obj[4].equals(Constants.EVENT_FLAG_HOME))
                            && obj[3] != null && (obj[3].equals(Constants.GENDER_CODE_A) || obj[3].equals(Constants.GENDER_CODE_F) || obj[3].equals(Constants.GENDER_CODE_M))) {
                        Map<String, Long> genderMap =(Map<String, Long>) dataMap.get(obj[4].toString());
                        // logger.debug("==genderMap====="+genderMap);
                        // logger.debug("==obj[3]====="+obj[3]);
                        // logger.debug("==obj[5]====="+obj[5]);
                        // logger.debug("genderMap.get(obj[3].toString())====="+genderMap.get(obj[3].toString()));
                        genderMap.put(obj[3].toString(), (genderMap.get(obj[3].toString()) == null ? 0 : genderMap.get(obj[3].toString())) + Long.parseLong((obj[5] == null ? 0L : obj[5]) + "") );
                        dataMap.put(obj[4].toString(), genderMap);
                        monthYearMap.put(monthYear, dataMap);
                    }

                }
            }
        return new ApiResponse(null, HttpStatus.OK, new ArrayList<>(monthYearMap.values()));
    }

    @Autowired
    private EntityManagerFactory emFactory; 

    private List<Object[]> getDeathMisReport(ReportSearchDto reportSearchDto, String dateType) throws Exception {
        
          EntityManager em = emFactory.createEntityManager();  
          em.getTransaction().begin( );  
            
        
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();  
        CriteriaQuery<Object[]> query = criteriaBuilder.createQuery(Object[].class);  
        Root<DeathModel> root = query.from(DeathModel.class); 
           
        // return deathRepository.getMISReport(new Specification<DeathModel>() {
        //     @Override
        //     public Predicate toPredicate(Root<DeathModel> root, CriteriaQuery<?> query,
        //                                  CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                List<Selection<?>> selections = new ArrayList<>();
                List<Expression<?>> expressions = new ArrayList<>();

                if(Constants.REGISTRATION.equalsIgnoreCase(dateType)) {
                    if(reportSearchDto.getRegStartDate() != null) {
                        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("registrationDatetime"), reportSearchDto.getRegStartDate().atStartOfDay()));
                    }
                    if(reportSearchDto.getRegEndDate() != null) {
                        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("registrationDatetime"), reportSearchDto.getRegEndDate().atTime(23, 59, 59)));
                    }

                    selections.add(criteriaBuilder.function("Year", BigInteger.class, root.get("registrationDatetime")));//"Year(registrationDatetime)"));
                    selections.add(criteriaBuilder.function("MONTH", BigInteger.class, root.get("registrationDatetime")));//"Year(registrationDatetime)"));
                    selections.add(criteriaBuilder.function("MONTHNAME", String.class, root.get("registrationDatetime")));//"Year(registrationDatetime)"));
                        
                    expressions.add(criteriaBuilder.function("Year", BigInteger.class, root.get("registrationDatetime")));//"Year(registrationDatetime)"));
                    expressions.add(criteriaBuilder.function("MONTH", BigInteger.class, root.get("registrationDatetime")));//"Year(registrationDatetime)"));
                    expressions.add(criteriaBuilder.function("MONTHNAME", String.class, root.get("registrationDatetime")));//"Year(registrationDatetime)"));
                    
                    
                } else if(Constants.EVENT.equalsIgnoreCase(dateType)) {
                    if(reportSearchDto.getEventStartDate() != null) {
                        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), reportSearchDto.getEventStartDate().atStartOfDay()));
                    }
                    if(reportSearchDto.getEventEndDate() != null) {
                        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), reportSearchDto.getEventEndDate().atTime(23, 59, 59)));
                    }
                    
                    selections.add(criteriaBuilder.function("Year", BigInteger.class, root.get("eventDate")));//"Year(registrationDatetime)"));
                    selections.add(criteriaBuilder.function("MONTH", BigInteger.class, root.get("eventDate")));//"Year(registrationDatetime)"));
                    selections.add(criteriaBuilder.function("MONTHNAME", String.class, root.get("eventDate")));//"Year(registrationDatetime)"));
                       
                    expressions.add(criteriaBuilder.function("Year", BigInteger.class, root.get("eventDate")));//"Year(registrationDatetime)"));
                    expressions.add(criteriaBuilder.function("MONTH", BigInteger.class, root.get("eventDate")));//"Year(registrationDatetime)"));
                    expressions.add(criteriaBuilder.function("MONTHNAME", String.class, root.get("eventDate")));//"Year(registrationDatetime)"));
                    
                }

                
                if(!CommonUtil.checkNullOrBlank( reportSearchDto.getDivisionCode())) {
                    predicates.add(criteriaBuilder.equal(root.get("divisionCode"), reportSearchDto.getDivisionCode()));
                }
                
                if(!CommonUtil.checkNullOrBlank( reportSearchDto.getCauseOfDeath())) {
                    predicates.add(criteriaBuilder.equal(root.get("causeOfDeath"), reportSearchDto.getCauseOfDeath()));
                }
                //Year(registration_datetime), MONTH(registration_datetime), MONTHNAME(registration_datetime), gender_code, event_place_flag, Count(*) 
                // selections.add(root.get("Year(registrationDatetime)"));
                // selections.add(root.get("MONTH(registrationDatetime)"));
                // selections.add(root.get("MONTHNAME(registration_datetime)"));
                selections.add(root.get("genderCode"));
                selections.add(root.get("eventPlaceFlag"));
                //selections.add(root.get("count(*)"));
                if(Constants.REGISTRATION.equalsIgnoreCase(dateType)) {
                    selections.add(criteriaBuilder.function("count", BigInteger.class, root.get("registrationDatetime")));//"Year(registrationDatetime)"));
                    
                } else if(Constants.EVENT.equalsIgnoreCase(dateType)) {
                    selections.add(criteriaBuilder.function("count", BigInteger.class, root.get("eventDate")));//"Year(registrationDatetime)"));
                    
                }
                // expressions.add(root.get("Year(registration_datetime)"));
                // expressions.add(root.get("MONTH(registration_datetime)"));
                // expressions.add(root.get("MONTHNAME(registration_datetime)"));
                expressions.add(root.get("genderCode"));
                expressions.add(root.get("eventPlaceFlag"));
                
                //, criteriaBuilder.count(root)
                query.multiselect(selections).groupBy(expressions).where(predicates.toArray(new Predicate[predicates.size()]));
        //         return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])); 
        //     }
        // });
        return em.createQuery(query).getResultList();
    }


    private List<Object[]> getBirthMisReport(ReportSearchDto reportSearchDto, String dateType) throws Exception {
        
          EntityManager em = emFactory.createEntityManager();  
          em.getTransaction().begin( );  
            
        
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();  
        CriteriaQuery<Object[]> query = criteriaBuilder.createQuery(Object[].class);  
        Root<BirthModel> root = query.from(BirthModel.class); 
           
        // return deathRepository.getMISReport(new Specification<DeathModel>() {
        //     @Override
        //     public Predicate toPredicate(Root<DeathModel> root, CriteriaQuery<?> query,
        //                                  CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                List<Selection<?>> selections = new ArrayList<>();
                List<Expression<?>> expressions = new ArrayList<>();

                if(Constants.REGISTRATION.equalsIgnoreCase(dateType)) {
                    if(reportSearchDto.getRegStartDate() != null) {
                        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("registrationDatetime"), reportSearchDto.getRegStartDate().atStartOfDay()));
                    }
                    if(reportSearchDto.getRegEndDate() != null) {
                        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("registrationDatetime"), reportSearchDto.getRegEndDate().atTime(23, 59, 59)));
                    }

                    selections.add(criteriaBuilder.function("Year", BigInteger.class, root.get("registrationDatetime")));//"Year(registrationDatetime)"));
                    selections.add(criteriaBuilder.function("MONTH", BigInteger.class, root.get("registrationDatetime")));//"Year(registrationDatetime)"));
                    selections.add(criteriaBuilder.function("MONTHNAME", String.class, root.get("registrationDatetime")));//"Year(registrationDatetime)"));
                        
                    expressions.add(criteriaBuilder.function("Year", BigInteger.class, root.get("registrationDatetime")));//"Year(registrationDatetime)"));
                    expressions.add(criteriaBuilder.function("MONTH", BigInteger.class, root.get("registrationDatetime")));//"Year(registrationDatetime)"));
                    expressions.add(criteriaBuilder.function("MONTHNAME", String.class, root.get("registrationDatetime")));//"Year(registrationDatetime)"));
                    
                
                } else if(Constants.EVENT.equalsIgnoreCase(dateType)) {
                    if(reportSearchDto.getEventStartDate() != null) {
                        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), reportSearchDto.getEventStartDate().atStartOfDay()));
                    }
                    if(reportSearchDto.getEventEndDate() != null) {
                        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), reportSearchDto.getEventEndDate().atTime(23, 59, 59)));
                    }

                    selections.add(criteriaBuilder.function("Year", BigInteger.class, root.get("eventDate")));//"Year(registrationDatetime)"));
                    selections.add(criteriaBuilder.function("MONTH", BigInteger.class, root.get("eventDate")));//"Year(registrationDatetime)"));
                    selections.add(criteriaBuilder.function("MONTHNAME", String.class, root.get("eventDate")));//"Year(registrationDatetime)"));
                       
                    expressions.add(criteriaBuilder.function("Year", BigInteger.class, root.get("eventDate")));//"Year(registrationDatetime)"));
                    expressions.add(criteriaBuilder.function("MONTH", BigInteger.class, root.get("eventDate")));//"Year(registrationDatetime)"));
                    expressions.add(criteriaBuilder.function("MONTHNAME", String.class, root.get("eventDate")));//"Year(registrationDatetime)"));
                    

                }
            
                if(!CommonUtil.checkNullOrBlank( reportSearchDto.getDivisionCode())) {
                    predicates.add(criteriaBuilder.equal(root.get("divisionCode"), reportSearchDto.getDivisionCode()));
                }
                //Year(registration_datetime), MONTH(registration_datetime), MONTHNAME(registration_datetime), gender_code, event_place_flag, Count(*) 
                // selections.add(root.get("Year(registrationDatetime)"));
                // selections.add(root.get("MONTH(registrationDatetime)"));
                // selections.add(root.get("MONTHNAME(registration_datetime)"));
                selections.add(root.get("genderCode"));
                selections.add(root.get("eventPlaceFlag"));
                //selections.add(root.get("count(*)"));

                if(Constants.REGISTRATION.equalsIgnoreCase(dateType)) {
                    selections.add(criteriaBuilder.function("count", BigInteger.class, root.get("registrationDatetime")));//"Year(registrationDatetime)"));
                    
                } else if(Constants.EVENT.equalsIgnoreCase(dateType)) {
                    selections.add(criteriaBuilder.function("count", BigInteger.class, root.get("eventDate")));//"Year(registrationDatetime)"));
                    
                }
                // expressions.add(root.get("Year(registration_datetime)"));
                // expressions.add(root.get("MONTH(registration_datetime)"));
                // expressions.add(root.get("MONTHNAME(registration_datetime)"));
                expressions.add(root.get("genderCode"));
                expressions.add(root.get("eventPlaceFlag"));
                
                //, criteriaBuilder.count(root)
                query.multiselect(selections).groupBy(expressions).where(predicates.toArray(new Predicate[predicates.size()]));
        //         return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])); 
        //     }
        // });
        return em.createQuery(query).getResultList();
    }

    private Map<String, Long> getZeroEventMap() {
        Map<String, Long> zeroMap = new HashMap<>();
        zeroMap.put(Constants.GENDER_CODE_A, 0L);
        zeroMap.put(Constants.GENDER_CODE_F, 0L);
        zeroMap.put(Constants.GENDER_CODE_M, 0L);
        return zeroMap;
    }

    /**** 
     * MIS Report End
    */

}
