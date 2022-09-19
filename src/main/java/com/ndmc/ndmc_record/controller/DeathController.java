package com.ndmc.ndmc_record.controller;


import java.io.FileNotFoundException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.ndmc.ndmc_record.dto.*;
import com.ndmc.ndmc_record.utils.CommonUtil;
import com.ndmc.ndmc_record.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.beust.jcommander.internal.Nullable;
import com.ndmc.ndmc_record.config.Constants;
import com.ndmc.ndmc_record.model.DeathModel;
import com.ndmc.ndmc_record.service.CertificatePrintService;
import com.ndmc.ndmc_record.service.DeathService;
import com.ndmc.ndmc_record.service.SlaDetailsService;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/v1/death")
//@CrossOrigin(origins = "*")
public class DeathController {

    private final Logger logger = LoggerFactory.getLogger(DeathController.class);
    @Autowired
    DeathService deathService;

    @Autowired
    SlaDetailsService slaDetailsService;

    @Autowired
    CertificatePrintService certificatePrintService;

    @GetMapping("/records/{status}")
    @PreAuthorize("hasAnyRole('"+Constants.ROLE_ADMIN+"','"+Constants.ROLE_APPROVER+"', " +
            "'"+Constants.ROLE_CREATOR+"', '"+Constants.ROLE_CFC_APPROVER+"', " +
            "'"+Constants.ROLE_CFC_CREATOR+"', '" + Constants.ROLE_CFC_REGISTRAR + "')")
    public List<DeathModel> deathRecords(@PathVariable("status") String status, @RequestParam String orgCode)
    {
        return deathService.getDeathRecords(status, orgCode);
    }

    @GetMapping("/records")
    @PreAuthorize("hasAnyRole('"+Constants.ROLE_ADMIN+"','" + Constants.ROLE_CHIEF_REGISTRAR + "','"+Constants.ROLE_APPROVER+"', " +
            "'"+Constants.ROLE_CREATOR+"', '"+Constants.ROLE_CFC_APPROVER+"', " +
            "'"+Constants.ROLE_CFC_CREATOR+"', '" + Constants.ROLE_CFC_REGISTRAR + "')")
    public ApiResponse deathRecordsForUser(HttpServletRequest request) throws Exception
    {

        return deathService.getUsersDeathRecords(request);
    }


    @GetMapping("/details/{deathId}")
    @PreAuthorize("hasAnyRole('"+Constants.ROLE_ADMIN+"','"+Constants.ROLE_APPROVER+"', '"+Constants.ROLE_CREATOR+"', " +
            "'"+Constants.ROLE_CFC_APPROVER+"', '"+Constants.ROLE_CFC_CREATOR+"', '" + Constants.ROLE_CFC_REGISTRAR + "', '" + Constants.ROLE_PUBLIC_CREATOR + "', '" + Constants.ROLE_PUBLIC_APPROVER + "')")
    public ApiResponse birthRecordDetails(@PathVariable("deathId") Long deathId) throws Exception
    {
        return deathService.getDeathDetails(deathId);
    }

    @GetMapping("/all-records")
    @PreAuthorize("hasAnyRole('"+Constants.ROLE_ADMIN+"','" + Constants.ROLE_CHIEF_REGISTRAR + "','"+Constants.ROLE_APPROVER+"', '"+Constants.ROLE_CREATOR+"'," +
            " '"+Constants.ROLE_CFC_APPROVER+"', '"+Constants.ROLE_CFC_CREATOR+"', '" + Constants.ROLE_CFC_REGISTRAR + "')")
    public List<DeathModel> birthRecords()
    {
        return deathService.getAllDeathRecords();
    }

    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('"+Constants.ROLE_ADMIN+"','"+Constants.ROLE_CREATOR+"', " +
            "'"+Constants.ROLE_CFC_CREATOR+"', '" + Constants.ROLE_CFC_REGISTRAR + "', '" + Constants.ROLE_PUBLIC_CREATOR + "')")
    public ApiResponse addDeathRecord(@RequestPart("data") String data, HttpServletRequest request, @RequestParam(value = "sdmLetterImage", required = false) MultipartFile sdmLetterImage) throws Exception{
        logger.info("===== Hit DEATH register API ========");
        DeathDto deathDto = JsonUtil.getObjectFromJson(data, DeathDto.class);
        ApiResponse response = new ApiResponse();
        if(( CommonUtil.checkNullOrBlank(deathDto.getIsUnkownCase()) ||
                !Constants.IS_UNKNOWN_CASE.equalsIgnoreCase(deathDto.getIsUnkownCase()))
                && !Constants.RECORD_STATUS_DRAFT.equalsIgnoreCase(deathDto.getStatus())
                && (deathDto.getEventDate() == null || deathDto.getEventDate().equals("")))
        {
            response.setStatus(HttpStatus.BAD_REQUEST);
            response.setMsg(Constants.EVENT_DATE_BLANK);
        }
        else {
            response = deathService.saveDeathRecords(deathDto, request, sdmLetterImage);
        }
        //return new ResponseEntity("Death Record added Successfully", HttpStatus.OK);
        return response;

    }

    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('"+Constants.ROLE_ADMIN+"','"+Constants.ROLE_APPROVER+"', " +
            "'"+Constants.ROLE_CREATOR+"', '"+Constants.ROLE_CFC_APPROVER+"', " +
            "'"+Constants.ROLE_CFC_CREATOR+"', '" + Constants.ROLE_CFC_REGISTRAR + "', '" + Constants.ROLE_PUBLIC_CREATOR + "', '" + Constants.ROLE_PUBLIC_APPROVER + "')")
    public ApiResponse updateDeathRecord(@RequestPart("data") String data, HttpServletRequest request, @RequestParam(value = "sdmLetterImage", required = false) MultipartFile sdmLetterImage) throws Exception{
        DeathDto deathDto = JsonUtil.getObjectFromJson(data, DeathDto.class);
        return deathService.updateDeathRecords(deathDto, request, sdmLetterImage);
        // return new ResponseEntity("Death Record updated Successfully", HttpStatus.OK);
    }

    @PutMapping("/approve-reject/{deathId}")
    @PreAuthorize("hasAnyRole('"+Constants.ROLE_APPROVER+"', '"+Constants.ROLE_ADMIN+"', " +
            "'"+Constants.ROLE_CFC_APPROVER+"', '" + Constants.ROLE_CFC_REGISTRAR + "', '" + Constants.ROLE_PUBLIC_CREATOR + "', '" + Constants.ROLE_PUBLIC_APPROVER + "')")
    public ApiResponse updateBirthRecordStatus(
            @PathVariable("deathId") Long deathId,
            @RequestParam String status,
            @RequestParam(value = "remarks", required = false) String remarks,
            HttpServletRequest request) throws Exception{
        return deathService.updateDeathRecordStatus(deathId, status, remarks, request);

        //return new ResponseEntity("Record Status updated Successfully", HttpStatus.OK);
    }

    @RequestMapping(value= {"/print-certificate/{deathId}","/print-certificate/{deathId}/{slaId}"}, method=RequestMethod.GET)
    @PreAuthorize("hasAnyRole('"+Constants.ROLE_ADMIN+"','"+Constants.ROLE_APPROVER+"', " +
            "'"+Constants.ROLE_CREATOR+"', '"+Constants.ROLE_CFC_APPROVER+"', " +
            "'"+Constants.ROLE_CFC_CREATOR+"', '" + Constants.ROLE_CFC_REGISTRAR + "', '" + Constants.ROLE_PUBLIC + "')")
    public ResponseEntity<?> printCertificate(@PathVariable("deathId") Long deathId,
                                              @PathVariable(value="slaId",required = false) Long slaId,
                                              HttpServletRequest request) throws Exception
    {
        return certificatePrintService.printDeathCertificate(deathId, slaId, request);
    }
    @GetMapping("/qr-code/{printId}")
    @PreAuthorize("hasAnyRole('"+Constants.ROLE_ADMIN+"','" + Constants.ROLE_CHIEF_REGISTRAR + "','"+Constants.ROLE_APPROVER+"', " +
            "'"+Constants.ROLE_CREATOR+"', '"+Constants.ROLE_CFC_APPROVER+"', " +
            "'"+Constants.ROLE_CFC_CREATOR+"', '" + Constants.ROLE_CFC_REGISTRAR + "', '" + Constants.ROLE_PUBLIC + "')")
    public ResponseEntity<byte[]>  generateQrCode(@PathVariable("printId") Long printId, HttpServletRequest request)
    {
        try {
            return deathService.generateQrCode(printId, request);
        }catch(Exception e){

            logger.error("QR CODE Generation "+printId, e);
        }
        return null;
    }

    @GetMapping("/history/{deathId}")
    @PreAuthorize("hasAnyRole('"+Constants.ROLE_ADMIN+"','"+Constants.ROLE_APPROVER+"', " +
            "'"+Constants.ROLE_CFC_APPROVER+"', '"+Constants.ROLE_CFC_CREATOR+"', " +
            "'"+Constants.ROLE_CREATOR+"', '" + Constants.ROLE_CFC_REGISTRAR + "')")
    public ApiResponse getDeathHistory(@PathVariable("deathId") Long deathId, HttpServletRequest request)
    {
        try {
            return deathService.getHistoryFromBlc(deathId, request);
        }catch (Exception e){

            logger.error("Error in Birth History data fetching "+deathId, e);

        }
        return null;
    }

    @PostMapping("/cfc-filter/{filterType}")
    //@PreAuthorize("hasAnyRole('ADMIN', 'CREATOR', 'APPROVER')")
    @PreAuthorize("hasAnyRole('"+Constants.ROLE_ADMIN+"','" + Constants.ROLE_CHIEF_REGISTRAR + "','"+Constants.ROLE_CFC_APPROVER+"', " +
            "'"+Constants.ROLE_CFC_CREATOR+"','"+Constants.ROLE_CFC_REGISTRAR+"')")
    public ApiResponse getFilteredRecords(@Nullable @RequestBody CFCFilterDto cfcFilter, @PathVariable(name = "filterType") String filterType, HttpServletRequest request) throws Exception {
        logger.info("Death getFilteredRecords service call request : cfcFilter = >>>" + cfcFilter);
        if(CommonUtil.checkNullOrBlank(cfcFilter.getRegistrationNumber()+"") &&
                CommonUtil.checkNullOrBlank(cfcFilter.getApplicationNumber()+"") &&
                CommonUtil.checkNullOrBlank(cfcFilter.getRegStartDate()+"") &&
                CommonUtil.checkNullOrBlank(cfcFilter.getRegEndDate()+"") &&
                CommonUtil.checkNullOrBlank(cfcFilter.getEventStartDate()+"") &&
                CommonUtil.checkNullOrBlank(cfcFilter.getEventEndDate()+"")){
            if (filterType.equalsIgnoreCase(Constants.FILTER_LEGAL_CORRECTION_SEARCH)
                    || filterType.equalsIgnoreCase(Constants.FILTER_PRINT_SEARCH))
                throw new IllegalArgumentException("Invalid Request");
        }
        return deathService.getFilteredData(cfcFilter, filterType, request);
    }

    @PostMapping("/correction")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_CFC_APPROVER + "', '"
            + Constants.ROLE_CFC_CREATOR + "', '" + Constants.ROLE_CFC_REGISTRAR + "')")
    public ApiResponse saveDeathCorrection(@RequestBody DeathCorrectionDto stillBirthCorrectionDto,
                                           HttpServletRequest request) throws Exception {
        logger.info("saveDeathCorrection service call request : DeathCorrectionDto = >>>" + stillBirthCorrectionDto.toString());
        return slaDetailsService.saveDeathCorrection(stillBirthCorrectionDto, request);
    }

    @PostMapping("/correction/{slaId}")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_CFC_APPROVER + "', '"
            + Constants.ROLE_CFC_CREATOR + "', '" + Constants.ROLE_CFC_REGISTRAR + "')")
    public ApiResponse updateDeathCorrection(@RequestBody DeathCorrectionDto stillBirthCorrectionDto, @PathVariable("slaId") Long slaId,
                                             HttpServletRequest request) throws Exception {
        return slaDetailsService.updateDeathCorrection(slaId, stillBirthCorrectionDto, request);
    }

    @PutMapping("/correction/approve-reject/{slaDetailsId}")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_CFC_REGISTRAR + "')")
    public ApiResponse dataCorrectionApproveReject(@PathVariable("slaDetailsId") Long slaDetailsId,
                                                   @RequestParam String status,
                                                   @RequestParam(value = "remarks", required = false) String remarks,
                                                   HttpServletRequest request)
            throws Exception {
        return slaDetailsService.approveRejectDeathLegalData(slaDetailsId, status, remarks, request);
    }

    @GetMapping("/correction/details/{bndId}")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_CFC_APPROVER + "', '"
            + Constants.ROLE_CFC_CREATOR + "', '" + Constants.ROLE_CFC_REGISTRAR + "',)")
    public ApiResponse getCorrectionDetails(@PathVariable(name = "bndId") Long bndId, HttpServletRequest request)
            throws Exception {
        return slaDetailsService.getDetails(bndId, Constants.DEATH_CORRECTION, Constants.RECORD_TYPE_DEATH, request);
    }

    @GetMapping("/downloadDocument/{deathId}")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_APPROVER + "', '" + Constants.ROLE_CREATOR + "', " +
            "'" + Constants.ROLE_CFC_APPROVER + "', '" + Constants.ROLE_CFC_CREATOR + "', '" + Constants.ROLE_CFC_REGISTRAR + "')")
    public ApiResponse downloadSdmLetterImage(@PathVariable String deathId, HttpServletRequest request)
            throws FileNotFoundException {
        return deathService.loadSdmLetterFileById(deathId);

    }
    @PutMapping("/delete/{deathId}")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_CFC_CREATOR + "', '"
            + Constants.ROLE_CFC_APPROVER + "', '" + Constants.ROLE_CFC_REGISTRAR + "'," +
            "'" + Constants.USER_TYPE_HOSPITAL + "','" + Constants.USER_TYPE_CFC + "','" + Constants.ROLE_CREATOR + "')")
    public ApiResponse deleteDeath(@PathVariable("deathId") Long deathId, HttpServletRequest request) throws Exception {
        return deathService.deleteDeath(deathId, request);
    }
}
