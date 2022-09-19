package com.ndmc.ndmc_record.controller;

import com.ndmc.ndmc_record.config.Constants;
import com.ndmc.ndmc_record.dto.*;
import com.ndmc.ndmc_record.service.*;
import com.ndmc.ndmc_record.serviceImpl.SlaDetailsServiceImpl;
import com.ndmc.ndmc_record.utils.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;
import java.util.regex.Pattern;
import org.apache.http.client.utils.URIBuilder;

@RestController
@RequestMapping("api/v1/online")
//@CrossOrigin(origins = "*")
public class OnlinePublicController {

    private final Logger logger = LoggerFactory.getLogger(OnlinePublicController.class);

    @Autowired
    OnlinePublicService onlinePublicService;

    @Autowired
    private DropDownService dropDownService;

    @Autowired
    SlaDetailsService slaDetailsService;

    @Autowired
    ReportService reportService;


    @PostMapping("/search/{type}/enquiry")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_PUBLIC + "')")
    public ApiResponse onlineSearchEnquiry(@PathVariable String type,@RequestBody OnlinePrintRequestDto printRequestDto,
                                           HttpServletRequest request) throws Exception {

    logger.info("Calling onlineSearchBirthEnquiry OnlinePrintRequestDto ====== "+type +", "+ printRequestDto);
    ApiResponse apiResponse = new ApiResponse();
   /* if (printRequestDto.getApplNo() != null && printRequestDto.getDateOfEvent() != null) {
        if (printRequestDto.getApplNo().trim().length() <=  0) {
            apiResponse.setMsg(Constants.PASS_APPL_NO_MESSAGE);
            apiResponse.setStatus(HttpStatus.BAD_REQUEST);
            return apiResponse;
        }
    }else if (printRequestDto.getDateOfEvent() != null
            && printRequestDto.getGenderCode() != null
            && printRequestDto.getFatherName() != null
            && printRequestDto.getMotherName() != null
            && (printRequestDto.getDivisionCode() != null
                || printRequestDto.getOrganizationCode() != null)) {
                if (printRequestDto.getFatherName().trim().length() < 3) {
                    apiResponse.setMsg(Constants.PASS_MOTHER_FATHER_MESSAGE);
                    apiResponse.setStatus(HttpStatus.BAD_REQUEST);
                    return apiResponse;
                }
                if ( printRequestDto.getMotherName().trim().length() < 3) {
                    apiResponse.setMsg(Constants.PASS_MOTHER_FATHER_MESSAGE);
                    apiResponse.setStatus(HttpStatus.BAD_REQUEST);
                    return apiResponse;
                }
                if (CommonUtil.checkNullOrBlank(printRequestDto.getGenderCode().trim())) {
                    apiResponse.setMsg(Constants.PASS_GENDER_MESSAGE);
                    apiResponse.setStatus(HttpStatus.BAD_REQUEST);
                    return apiResponse;
                }
        }else {
            apiResponse.setMsg(Constants.NOT_PERMITTED+ ""+ Constants.PASS_VALID_MESSAGE );
            apiResponse.setStatus(HttpStatus.BAD_REQUEST);
            return apiResponse;
        }
        */
        if(Constants.RECORD_TYPE_BIRTH.toLowerCase().equalsIgnoreCase(type)) {
             return onlinePublicService.onlineSearchBirthEnquiry(printRequestDto, request);
        }
        else if(Constants.FILTER_NAME_INCLUSION.toLowerCase().equalsIgnoreCase(type)) {

            /*
            * printRequestDto.getDateOfEvent() != null
            * Added this condtion to get rid of null Exception if event date is missing in request
            * Deepak
            * 09-05-22
            * */
            if(printRequestDto.getDateOfEvent() != null ) {
                Long days = Duration.between(printRequestDto.getDateOfEvent().atTime(00, 00, 00), LocalDateTime.now()).toDays();
                if (days >= Constants.FILTER_DATE_NAME_INCLUSION) {
                    apiResponse.setMsg(Constants.NAME_INCLUSION_NOT_ALLOW_MESSAGE + "" + Constants.FILTER_DATE_NAME_INCLUSION);
                    apiResponse.setStatus(HttpStatus.BAD_REQUEST);
                    return apiResponse;
                } else if (days < 0) {
                    apiResponse.setMsg(Constants.NAME_INCLUSION_NOT_ALLOW_MESSAGE + " futures !");
                    apiResponse.setStatus(HttpStatus.BAD_REQUEST);
                    return apiResponse;
                }
            }
            return onlinePublicService.onlineSearchInclusionEnquiry(printRequestDto, request);
        }


        else if(Constants.RECORD_TYPE_SBIRTH.toLowerCase().equalsIgnoreCase(type)) {
            return onlinePublicService.onlineSearchStillBirthEnquiry(printRequestDto, request);
        }
        else if(Constants.RECORD_TYPE_DEATH.toLowerCase().equalsIgnoreCase(type)) {
            return onlinePublicService.onlineSearchDeathEnquiry(printRequestDto, request);
        }
        else{
            apiResponse.setStatus(HttpStatus.BAD_REQUEST);
            apiResponse.setMsg(Constants.CHECK_REQUEST_MESSAGE);
            return apiResponse;
        }
    }

    @PostMapping("/{type}/print/request")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_PUBLIC + "')")
    public ApiResponse saveOnlineApplicantRequest(@PathVariable(value = "type") String type,
                                           @RequestBody PrintRequestDto printRequestDto,
                                           HttpServletRequest request) throws Exception {
        logger.info("Calling saveOnlineApplicantRequest PrintRequestDto ====== "+type +", "+ printRequestDto);
        //   "transactionType": "ONLINE_PRINT_BIRTH","recordType": "BIRTH"
        //  "transactionType": "ONLINE_NAME_INCLUSION","recordType": "NAME_INCLUSION"
        //   "transactionType": "ONLINE_PRINT_STILL_BIRTH","recordType": "STILL_BIRTH"
        //   "transactionType": "ONLINE_PRINT_DEATH","recordType": "DEATH"

        ApiResponse apiResponse = new ApiResponse();
        if (CommonUtil.checkNullOrBlank(printRequestDto.getTransactionType()+"")
            || CommonUtil.checkNullOrBlank(printRequestDto.getRecordType()+"")
            || CommonUtil.checkNullOrBlank(printRequestDto.getApplicantContact()+"")
            || CommonUtil.checkNullOrBlank(printRequestDto.getApplicantAddress()+"")
            || CommonUtil.checkNullOrBlank(printRequestDto.getDueDate()+"")
            || CommonUtil.checkNullOrBlank(printRequestDto.getApplicantEmailId()+"")
            || CommonUtil.checkNullOrBlank(printRequestDto.getNoOfCopies()+"")) {
            apiResponse.setMsg(Constants.PASS_VALID_ONLINE_REQUEST_MESSAGE);
            apiResponse.setStatus(HttpStatus.BAD_REQUEST);
            return apiResponse;
        }
        if (Constants.FILTER_NAME_INCLUSION.toLowerCase().equalsIgnoreCase(type)
                && CommonUtil.checkNullOrBlank(printRequestDto.getChildName()+"")) {
            apiResponse.setMsg(Constants.PASS_VALID_ONLINE_REQUEST_MESSAGE+", childName ");
            return apiResponse;
        }
        if(!Pattern.compile("^(.+)@(.+)$").matcher(printRequestDto.getApplicantEmailId()).matches()) {
            apiResponse.setMsg(Constants.EMAILID_MESSAGE);
            apiResponse.setStatus(HttpStatus.BAD_REQUEST);
            return apiResponse;
        }
        if(printRequestDto.getApplicantContact().trim().length() != 10) {
            apiResponse.setMsg(Constants.MOBILE_MESSAGE);
            apiResponse.setStatus(HttpStatus.BAD_REQUEST);
            return apiResponse;
        }else{
            try {
                Long.parseLong(printRequestDto.getApplicantContact().trim());
            }catch(NumberFormatException e) {
                apiResponse.setMsg(Constants.MOBILE_MESSAGE);
                apiResponse.setStatus(HttpStatus.BAD_REQUEST);
                return apiResponse;
            }
        }

        if((
        Constants.RECORD_TYPE_BIRTH.toLowerCase().equalsIgnoreCase(type)
        && Constants.RECORD_TYPE_ONLINE_PRINT_BIRTH.equals(printRequestDto.getTransactionType())
        && Constants.RECORD_TYPE_BIRTH.equals(printRequestDto.getRecordType()))
        || (
        Constants.FILTER_NAME_INCLUSION.toLowerCase().equalsIgnoreCase(type)
        && Constants.RECORD_TYPE_ONLINE_NAME_INCLUSION.equals(printRequestDto.getTransactionType())
        && Constants.RECORD_NAME_INCLUSION.equals(printRequestDto.getRecordType())
        ) || (
        Constants.RECORD_TYPE_SBIRTH.toLowerCase().equalsIgnoreCase(type)
        && Constants.RECORD_TYPE_ONLINE_PRINT_STILL_BIRTH.equals(printRequestDto.getTransactionType())
        && Constants.RECORD_TYPE_STILL_BIRTH.equals(printRequestDto.getRecordType())
        ) || (
        Constants.RECORD_TYPE_DEATH.toLowerCase().equalsIgnoreCase(type)
                && Constants.RECORD_TYPE_ONLINE_PRINT_DEATH.equals(printRequestDto.getTransactionType())
                && Constants.RECORD_TYPE_DEATH.equals(printRequestDto.getRecordType())
        )){
            return slaDetailsService.saveOnlinePrintRequest(printRequestDto, request);
        }
      else{
            apiResponse.setStatus(HttpStatus.BAD_REQUEST);
            apiResponse.setMsg(Constants.NOT_PERMITTED);
            return apiResponse;
        }
    }

    @PutMapping("/print/{id}/{type}")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_PUBLIC + "')")
    public ApiResponse updatePrintRequest( @PathVariable(value = "id") Long slaId,
                                           @RequestBody PrintRequestDto printRequestDto,
                                           @PathVariable(value = "type") String type,
                                          HttpServletRequest request) throws Exception {
        logger.info("Calling updatePrintRequest PrintRequestDto ====== "+type +", "+ printRequestDto);

        if((
                Constants.RECORD_TYPE_BIRTH.toLowerCase().equalsIgnoreCase(type)
                        && Constants.RECORD_TYPE_ONLINE_PRINT_BIRTH.equals(printRequestDto.getTransactionType())
                        && Constants.RECORD_TYPE_BIRTH.equals(printRequestDto.getRecordType()))
                ||  (
                Constants.RECORD_TYPE_SBIRTH.toLowerCase().equalsIgnoreCase(type)
                        && Constants.RECORD_TYPE_ONLINE_PRINT_STILL_BIRTH.equals(printRequestDto.getTransactionType())
                        && Constants.RECORD_TYPE_STILL_BIRTH.equals(printRequestDto.getRecordType())
        ) || (
                Constants.RECORD_TYPE_DEATH.toLowerCase().equalsIgnoreCase(type)
                        && Constants.RECORD_TYPE_ONLINE_PRINT_DEATH.equals(printRequestDto.getTransactionType())
                        && Constants.RECORD_TYPE_DEATH.equals(printRequestDto.getRecordType())
        )){
            return slaDetailsService.updatePrintRequest(slaId,printRequestDto, request);
        }else if( Constants.FILTER_NAME_INCLUSION.toLowerCase().equalsIgnoreCase(type)
                && Constants.RECORD_TYPE_ONLINE_NAME_INCLUSION.equals(printRequestDto.getTransactionType())
                && Constants.RECORD_NAME_INCLUSION.equals(printRequestDto.getRecordType())) {
            ApiResponse updatePrintRequest = slaDetailsService.updatePrintRequest(slaId,printRequestDto, request);
            logger.debug("updatePrintRequest  " + updatePrintRequest.getStatus());

            if(!updatePrintRequest.getStatus().equals(HttpStatus.OK)){
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setStatus(HttpStatus.BAD_REQUEST);
                apiResponse.setMsg(Constants.ACTION_ALREADY_APPLIED);
                return apiResponse;
            }
            return updatePrintRequest;
        }else{
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(HttpStatus.BAD_REQUEST);
            apiResponse.setMsg(Constants.NOT_PERMITTED);
            return apiResponse;
        }

    }


    @GetMapping("/get/hospitals")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_PUBLIC + "')")
    public ResponseEntity<?> getHospitalList() {
        return dropDownService.getHospitals();
    }

    @PostMapping("/name-inclusion/report")
    @PreAuthorize("hasAnyRole('"+ Constants.ROLE_CFC_REGISTRAR + "', '"+ Constants.ROLE_CHIEF_REGISTRAR + "', '"+ Constants.ROLE_PUBLIC + "')")
    public ApiResponse getOnlineNameInclusionReports(@RequestBody DailyReportDto dailyReportDto) throws Exception {
        dailyReportDto.setUserId(null);
        dailyReportDto.setOrgId(null);
        dailyReportDto.setTransactionType(Constants.RECORD_TYPE_ONLINE_NAME_INCLUSION);
        return reportService.getOnlineNameInclusionReports(dailyReportDto);
    }

    @PostMapping("/correction/search/{type}")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_PUBLIC + "')")
    public ApiResponse getRecordForOnlineCorrection(@PathVariable("type") String type, @RequestBody OnlinePrintRequestDto printRequestDto, HttpServletRequest request) throws Exception{

        ApiResponse response = new ApiResponse();
        logger.info("== RECORD TYPE ==="+ type);

        if(type != null && Constants.RECORD_TYPE_BIRTH.equalsIgnoreCase(type)){

            if(printRequestDto.getDateOfEvent() != null ) {
                Long days = Duration.between(printRequestDto.getDateOfEvent().atTime(00, 00, 00), LocalDateTime.now()).toDays();

                logger.info("=== DAYS ==="+days);
                if (days >= Constants.FILTER_DATE_DATA_CORRECTION) {
                    response.setMsg(Constants.DATA_CORRECTION_NOT_ALLOW_MESSAGE + "" + Constants.FILTER_DATE_NAME_INCLUSION);
                    response.setStatus(HttpStatus.BAD_REQUEST);
                    return response;

                } else if (days < 0) {
                    response.setMsg(Constants.DATA_CORRECTION_NOT_ALLOW_MESSAGE + " futures !");
                    response.setStatus(HttpStatus.BAD_REQUEST);
                    return response;
                }

            }
            response = onlinePublicService.onlineSearchBirthEnquiry(printRequestDto, request);
        }
        if(type != null && Constants.RECORD_TYPE_SBIRTH.equalsIgnoreCase(type)){

            if(printRequestDto.getDateOfEvent() != null ) {
                Long days = Duration.between(printRequestDto.getDateOfEvent().atTime(00, 00, 00), LocalDateTime.now()).toDays();
                if (days >= Constants.FILTER_DATE_DATA_CORRECTION) {
                    response.setMsg(Constants.DATA_CORRECTION_NOT_ALLOW_MESSAGE + "" + Constants.FILTER_DATE_NAME_INCLUSION);
                    response.setStatus(HttpStatus.BAD_REQUEST);
                    return response;

                } else if (days < 0) {
                    response.setMsg(Constants.DATA_CORRECTION_NOT_ALLOW_MESSAGE + " futures !");
                    response.setStatus(HttpStatus.BAD_REQUEST);
                    return response;
                }
            }
            response = onlinePublicService.onlineSearchStillBirthEnquiry(printRequestDto, request);
        }
        if(type != null && Constants.RECORD_TYPE_DEATH.equalsIgnoreCase(type)){
            if(printRequestDto.getDateOfEvent() != null ) {
                Long days = Duration.between(printRequestDto.getDateOfEvent().atTime(00, 00, 00), LocalDateTime.now()).toDays();
                if (days >= Constants.FILTER_DATE_DATA_CORRECTION) {
                    response.setMsg(Constants.DATA_CORRECTION_NOT_ALLOW_MESSAGE + "" + Constants.FILTER_DATE_NAME_INCLUSION);
                    response.setStatus(HttpStatus.BAD_REQUEST);
                    return response;
                } else if (days < 0) {
                    response.setMsg(Constants.DATA_CORRECTION_NOT_ALLOW_MESSAGE + " futures !");
                    response.setStatus(HttpStatus.BAD_REQUEST);
                    return response;
                }
            }
            response = onlinePublicService.onlineSearchDeathEnquiry(printRequestDto, request);
        }
        return response;
    }

    // Birth Correction

    @PostMapping("/correction/birth")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_PUBLIC + "')")
    public ApiResponse saveLegalBirthCorrection(@RequestBody BirthCorrectionDto birthCorrectionDto,
                                           HttpServletRequest request) throws Exception {
        logger.info("==== saveBirthCorrection service call request : birthCorrectionDto = >>>" + birthCorrectionDto.toString());

        return slaDetailsService.saveOnlineBirthCorrection(birthCorrectionDto, request);
    }



    @PostMapping("/correction/birth/{slaId}")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_PUBLIC + "')")
    public ApiResponse updateBirthCorrection(@RequestBody BirthCorrectionDto birthCorrectionDto, @PathVariable(name = "slaId") Long slaId,
                                             HttpServletRequest request) throws Exception {
        logger.info("updateBirthCorrection service call request : birthCorrectionDto = >>>" + birthCorrectionDto.toString());

        return slaDetailsService.updateOnlineBirthCorrection(slaId, birthCorrectionDto, request);
    }


    @PostMapping("/correction/death")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_PUBLIC + "')")
    public ApiResponse saveDeathCorrection(@RequestBody DeathCorrectionDto stillBirthCorrectionDto,
                                           HttpServletRequest request) throws Exception {
        logger.info("saveDeathCorrection service call request : DeathCorrectionDto = >>>" + stillBirthCorrectionDto.toString());
        return slaDetailsService.saveOnlineDeathCorrection(stillBirthCorrectionDto, request);
    }

    @PostMapping("/correction/death/{slaId}")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_PUBLIC + "')")
    public ApiResponse updateDeathCorrection(@RequestBody DeathCorrectionDto deathCorrectionDto, @PathVariable("slaId") Long slaId,
                                             HttpServletRequest request) throws Exception {
        return slaDetailsService.updateDeathCorrection(slaId, deathCorrectionDto, request);
    }

    @PostMapping("/correction/sbirth")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_PUBLIC + "')")
    public ApiResponse saveStillBirthCorrection(@RequestBody StillBirthCorrectionDto stillBirthCorrectionDto,
                                                HttpServletRequest request) throws Exception {
        logger.info("saveStillBirthCorrection service call request : StillBirthCorrectionDto = >>>" + stillBirthCorrectionDto.toString());
        return slaDetailsService.saveOnlineStillBirthCorrection(stillBirthCorrectionDto, request);
    }

    @PostMapping("/correction/sbirth/{slaId}")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_PUBLIC + "')")
    public ApiResponse updateStillBirthCorrection(@RequestBody StillBirthCorrectionDto stillBirthCorrectionDto, @PathVariable("slaId") Long slaId,
                                                  HttpServletRequest request) throws Exception {
        return slaDetailsService.updateStillBirthCorrection(slaId, stillBirthCorrectionDto, request);
    }
/*
* This action is responsible for Showing List of Online Corrected Record to Registrar,
* so that he/she can generate
* Appointment with respect of their sla id if STATUS IS PENDING
* Appointment generation is not valid for Approved Sla records
* */
    @GetMapping("/correction/{recordType}/list")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_PUBLIC + "')")
    public ApiResponse getCorrectionDetails(@PathVariable("recordType") String recordType, HttpServletRequest request)
            throws Exception {
        return slaDetailsService.getOnlineCorrectionList(recordType, request);
    }

    @GetMapping("/correction/details/{recordType}/{bndId}")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_PUBLIC + "')")
    public ApiResponse getCorrectionDetails(@PathVariable(name = "bndId") Long bndId,
                                            @PathVariable(name = "recordType") String recordType,
                                            HttpServletRequest request)
            throws Exception {
        ApiResponse res = new ApiResponse();
        if(recordType != null && Constants.RECORD_TYPE_BIRTH.equalsIgnoreCase(recordType)) {
            res =  slaDetailsService.getOnlineSlaDetails(bndId, Constants.ONLINE_BIRTH_CORRECTION, Constants.RECORD_TYPE_BIRTH, request);
        }
        else if(recordType != null && Constants.RECORD_TYPE_DEATH.equalsIgnoreCase(recordType)) {
            res = slaDetailsService.getOnlineSlaDetails(bndId, Constants.ONLINE_DEATH_CORRECTION, Constants.RECORD_TYPE_DEATH, request);
        }
        else if(recordType != null && Constants.RECORD_TYPE_SBIRTH.equalsIgnoreCase(recordType)) {
            res = slaDetailsService.getOnlineSlaDetails(bndId, Constants.ONLINE_STILL_BIRTH_CORRECTION, Constants.RECORD_TYPE_SBIRTH, request);
        }
        return res;
    }

    @PostMapping("/appointment/{slaId}")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_PUBLIC + "')")
    public ApiResponse createOnlineAppointment(@PathVariable("slaId") Long slaId, @RequestBody AppointmentDto appointmentDto,
                                                  HttpServletRequest request) throws Exception {
        return slaDetailsService.onlineAppointmentBySlaId(slaId, appointmentDto, request);
    }

    @PutMapping("/appointment/update/{aptId}")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_PUBLIC + "')")
    public ApiResponse updateOnlineAppointment(@PathVariable("aptId") Long aptId, @RequestBody AppointmentDto appointmentDto,
                                               HttpServletRequest request) throws Exception {
        return slaDetailsService.updateOnlineAppointmentByAptId(aptId, appointmentDto, request);
    }

    @GetMapping("/appointment/{status}/list")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_PUBLIC + "')")
    public ApiResponse getOnlineAppointmentByStatus(@PathVariable("status") String status, HttpServletRequest request) throws Exception {
        return slaDetailsService.getOnlineAppointmentByStatus(status, request);
    }

    @PostMapping("/self/register")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_CREATOR + "')")
    public ApiResponse createLinkForSelfRegister(@RequestBody SelfRegistrationLinkDto selfRegistrationLinkDto, HttpServletRequest request) throws Exception {
        return onlinePublicService.createLinkForSelfRegister(selfRegistrationLinkDto, request);
    }

    @GetMapping("/review/{type}")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_PUBLIC + "')")
    public ApiResponse reviewRecordsByApplNo(@PathVariable("type") String recordType,
                                             @RequestParam(value = "appNo") String applNo) throws Exception {
        return onlinePublicService.reviewRecordsByApplNo(recordType, applNo);
    }

    @PostMapping("/citizen/register/birth")
    public ApiResponse citizenBirthRegisterEvent(@RequestBody CitizenBirthDto citizenBirthDto, @RequestParam(value = "orgId") String orgId) throws Exception {
        return onlinePublicService.citizenBirthRegisterEvent(citizenBirthDto, orgId);
    }

    @PostMapping("/citizen/register/death")
    public ApiResponse citizenDeathRegisterEvent(@RequestBody DeathDto deathDto, @RequestParam(value = "orgId") String orgId) throws Exception {
        return onlinePublicService.citizenDeathRegisterEvent(deathDto, orgId);

    }
    @PostMapping("/citizen/register/sbirth")
    public ApiResponse citizenSBirthRegisterEvent(@RequestBody SBirthDto sBirthDto, @RequestParam(value = "orgId") String orgId) throws Exception {
        return onlinePublicService.citizenSBirthRegisterEvent(sBirthDto, orgId);

    }

    @GetMapping("/get/citizen/{type}/record")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_CREATOR + "')")
    public ApiResponse getCitizenRecordsByType(@PathVariable String type, HttpServletRequest request) throws Exception {
        return onlinePublicService.getCitizenRecordsByType(type, request);

    }

    @GetMapping("/view/citizen/{type}/{tempId}")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_CREATOR + "')")
    public ApiResponse viewCitizenRecordsByTypeAndId(@PathVariable String type, @PathVariable String tempId, HttpServletRequest request) throws Exception {
        return onlinePublicService.viewCitizenRecordsByTypeAndId(type, tempId, request);

    }

    @GetMapping("/send-sms")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_PUBLIC + "')")
    public String createLinkForSelfRegister() throws Exception {
       // return CommonUtil.shortenUrl(url);
        String applNo = "AIM0000039/2017";
        String encodedApplNo = Base64.getEncoder().encodeToString(applNo.getBytes());
        //String originalUrl = Constants.REVIEW_UAT_URL+"b/"+encodedApplNo;
        String type = "/B";
        String originalUrl = Constants.REVIEW_UAT_URL+applNo+type;
       // String originalUrl = "124.247.205.123:9191?aim00000392017/B";
       // String originalUrl = "124.247.205.123?AIM00000049/2017/B";
       // String originalUrl = URLEncoder.encode(originalUrlRaw, StandardCharsets.UTF_8);
       // byte[] englishBytes = englishString.getBytes();

       // String originalUrl = new String(englishBytes, StandardCharsets.UTF_8);
        //String shortedUrl = CommonUtil.shortenUrl(originalUrl);
        logger.info("ORIGINAL URL ==="+originalUrl);
        CommonUtil commonUtil = new CommonUtil();
        try{
            commonUtil.sendTextMessage("Demo", "9560807234", "AIM0000039/2017", Constants.RECORD_TYPE_BIRTH, Constants.NEW_APPROVAL_REQUEST, "", "", originalUrl,"", "");
        }catch (Exception e){
            logger.info("===SMS EXCEPTION =="+e);
        }

       // return  onlinePublicService.generateShortLink(url);
        return "SMS Sent";
    }

    @PostMapping("/citizen/filter/{type}")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_CREATOR + "')")
    public ApiResponse getFilteredData(@PathVariable String type, @RequestBody CFCFilterDto cfcFilterDto, HttpServletRequest request) throws Exception {
        return onlinePublicService.getFilteredData(type, cfcFilterDto, request);

    }
}
