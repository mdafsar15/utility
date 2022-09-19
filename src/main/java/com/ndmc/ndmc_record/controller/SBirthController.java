package com.ndmc.ndmc_record.controller;

import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.ndmc.ndmc_record.utils.CommonUtil;
import com.ndmc.ndmc_record.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.beust.jcommander.internal.Nullable;
import com.ndmc.ndmc_record.config.Constants;
import com.ndmc.ndmc_record.dto.ApiResponse;
import com.ndmc.ndmc_record.dto.CFCFilterDto;
import com.ndmc.ndmc_record.dto.SBirthDto;
import com.ndmc.ndmc_record.dto.StillBirthCorrectionDto;
import com.ndmc.ndmc_record.model.SBirthModel;
import com.ndmc.ndmc_record.service.CertificatePrintService;
import com.ndmc.ndmc_record.service.SBirthService;
import com.ndmc.ndmc_record.service.SlaDetailsService;
import org.springframework.web.multipart.MultipartFile;

@RestController
//@CrossOrigin("*")
@RequestMapping("api/v1/sbirth")
public class SBirthController {

    private final Logger logger = LoggerFactory.getLogger(SBirthController.class);

    @Autowired
    SBirthService birthService;

    @Autowired
	SlaDetailsService slaDetailsService;
    
    @Autowired
    CertificatePrintService certificatePrintService;

    @GetMapping("/records/{status}")
    @PreAuthorize("hasAnyRole('"+Constants.ROLE_ADMIN+"', '"+Constants.ROLE_CREATOR+"', '"+Constants.ROLE_APPROVER+"'," +
            " '"+Constants.ROLE_CFC_APPROVER+"', '"+Constants.ROLE_CFC_CREATOR+"', '" + Constants.ROLE_CFC_REGISTRAR + "')")
    public List<SBirthModel> birthRecords(@PathVariable("status") String status, @RequestParam String orgCode, HttpServletRequest request)
    {
        return birthService.getBirthRecords(status, orgCode);
    }

    @GetMapping("/records")
    @PreAuthorize("hasAnyRole('"+Constants.ROLE_ADMIN+"','"+Constants.ROLE_APPROVER+"', '"+Constants.ROLE_CREATOR+"', " +
            "'"+Constants.ROLE_CFC_APPROVER+"', '"+Constants.ROLE_CFC_CREATOR+"', '" + Constants.ROLE_CFC_REGISTRAR + "','" + Constants.ROLE_CHIEF_REGISTRAR + "',)")
    public ApiResponse sbirthRecordsForUser(HttpServletRequest request) throws Exception
    {
        return birthService.getUsersSBirthRecords(request);
    }

    @GetMapping("/details/{sbirthId}")
    @PreAuthorize("hasAnyRole('"+Constants.ROLE_ADMIN+"','"+Constants.ROLE_APPROVER+"', '"+Constants.ROLE_CREATOR+"'," +
            " '"+Constants.ROLE_CFC_APPROVER+"', '"+Constants.ROLE_CFC_CREATOR+"', '" + Constants.ROLE_CFC_REGISTRAR + "')")
    public ApiResponse birthRecords(@PathVariable("sbirthId") Long sbirthId) throws Exception
    {
        return birthService.getSBirthDetails(sbirthId);
    }

    @GetMapping("/all-records")
    @PreAuthorize("hasAnyRole('"+Constants.ROLE_ADMIN+"','"+Constants.ROLE_APPROVER+"', " +
            "'"+Constants.ROLE_CREATOR+"', '"+Constants.ROLE_CFC_APPROVER+"'," +
            " '"+Constants.ROLE_CFC_CREATOR+"', '" + Constants.ROLE_CFC_REGISTRAR + "')")
    public ApiResponse birthRecords(HttpServletRequest request)
    {
        // UserModel existedUser = new UserModel();
        if(request != null){
            System.out.println("Http request is ============== "+request);
        }
        return birthService.getAllSBirthRecords();
    }



    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('"+Constants.ROLE_ADMIN+"', '"+Constants.ROLE_CREATOR+"',  " +
            "'"+Constants.ROLE_CFC_CREATOR+"', '" + Constants.ROLE_CFC_REGISTRAR + "', '" + Constants.ROLE_PUBLIC_CREATOR + "')")
    public ApiResponse addBirthRecord(@RequestPart("data") String data, HttpServletRequest request, @RequestParam(value = "sdmLetterImage", required = false) MultipartFile sdmLetterImage) throws Exception{

        SBirthDto birthDto = JsonUtil.getObjectFromJson(data, SBirthDto.class);
        logger.info("Before Sbirth register service call : "+ LocalDateTime.now());
        ApiResponse response = birthService.saveSBirthRecords(birthDto, request, sdmLetterImage);
        logger.info("After Sbirth register service call : "+ LocalDateTime.now());
        return response;
        //return birthService.saveSBirthRecords(birthDto);
    }

    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('"+Constants.ROLE_ADMIN+"','"+Constants.ROLE_APPROVER+"', " +
            "'"+Constants.ROLE_CREATOR+"', '"+Constants.ROLE_CFC_APPROVER+"', " +
            "'"+Constants.ROLE_CFC_CREATOR+"', '" + Constants.ROLE_CFC_REGISTRAR + "', '" + Constants.ROLE_PUBLIC_CREATOR + "', '" + Constants.ROLE_PUBLIC_APPROVER + "')")
    public ApiResponse updateBirthRecord(@RequestPart("data") String data, HttpServletRequest request, @RequestParam(value = "sdmLetterImage", required = false) MultipartFile sdmLetterImage) throws Exception{
        SBirthDto birthDto = JsonUtil.getObjectFromJson(data, SBirthDto.class);
        return birthService.updateSBirthRecords(birthDto, request, sdmLetterImage);
        //return new ResponseEntity("Birth Record updated Successfully", HttpStatus.OK);
    }

    @PutMapping("/approve-reject/{sbirthId}")
    @PreAuthorize("hasAnyRole('"+Constants.ROLE_APPROVER+"', '"+Constants.ROLE_ADMIN+"', " +
            "'"+Constants.ROLE_CFC_APPROVER+"', '" + Constants.ROLE_CFC_REGISTRAR + "', '" + Constants.ROLE_PUBLIC_CREATOR + "', '" + Constants.ROLE_PUBLIC_APPROVER + "')")
    public ApiResponse updateSBirthRecordStatus(
            @PathVariable("sbirthId") Long sbirthId,
            @RequestParam String status,
            @RequestParam(value = "remarks", required = false) String remarks,
            HttpServletRequest request) throws Exception{
        return birthService.updateSBirthRecordStatus(sbirthId, status, remarks, request);
        // return new ResponseEntity("Record Status updated Successfully", HttpStatus.OK);
    }



    @RequestMapping(value= {"/print-certificate/{sbirthId}","/print-certificate/{sbirthId}/{slaId}"}, method=RequestMethod.GET)
    @PreAuthorize("hasAnyRole('"+Constants.ROLE_ADMIN+"','"+Constants.ROLE_APPROVER+"', " +
            "'"+Constants.ROLE_CREATOR+"', '"+Constants.ROLE_CFC_APPROVER+"', " +
            "'"+Constants.ROLE_CFC_CREATOR+"', '" + Constants.ROLE_CFC_REGISTRAR + "', '" + Constants.ROLE_PUBLIC + "')")
    public ResponseEntity<?> printCertificate(@PathVariable("sbirthId") Long sbirthId,
                                              @PathVariable(value="slaId",required = false) Long slaId,
                                                      HttpServletRequest request) throws Exception
    {
        return certificatePrintService.printSBirthCertificate(sbirthId,slaId, request);
    }
    @GetMapping("/qr-code/{printId}")
    @PreAuthorize("hasAnyRole('"+Constants.ROLE_ADMIN+"','"+Constants.ROLE_APPROVER+"', " +
            "'"+Constants.ROLE_CREATOR+"', '"+Constants.ROLE_CFC_APPROVER+"', " +
            "'"+Constants.ROLE_CFC_CREATOR+"', '" + Constants.ROLE_CFC_REGISTRAR + "', '" + Constants.ROLE_PUBLIC + "','" + Constants.ROLE_CHIEF_REGISTRAR + "',)")
    public ResponseEntity<byte[]>  generateQrCode(@PathVariable("printId") Long printId, HttpServletRequest request)
    {
        try {
            return birthService.generateQrCode(printId, request);
        }catch(Exception e){
            logger.error("QR CODE Generation "+printId, e);
        }
        return null;
    }
    @GetMapping("/history/{birthId}")
    @PreAuthorize("hasAnyRole('"+Constants.ROLE_ADMIN+"','"+Constants.ROLE_APPROVER+"'," +
            " '"+Constants.ROLE_CFC_APPROVER+"', '"+Constants.ROLE_CFC_CREATOR+"', " +
            "'"+Constants.ROLE_CREATOR+"', '" + Constants.ROLE_CFC_REGISTRAR +"','" + Constants.ROLE_CHIEF_REGISTRAR + "',)")
    public ApiResponse getBirthHistory(@PathVariable("birthId") Long birthId, HttpServletRequest request)
    {
        try {
            return birthService.getHistoryFromBlc(birthId, request);
        }catch (Exception e){

            logger.error("Error in Birth History data fetching "+birthId, e);

        }
        return null;
    }

    @PostMapping("/cfc-filter/{filterType}")
    //@PreAuthorize("hasAnyRole('ADMIN', 'CREATOR', 'APPROVER')")
    @PreAuthorize("hasAnyRole('"+Constants.ROLE_ADMIN+"','"+Constants.ROLE_CFC_APPROVER+"', " +
            "'"+Constants.ROLE_CFC_CREATOR+"','"+Constants.ROLE_CFC_REGISTRAR+"','" + Constants.ROLE_CFC_REGISTRAR +"')")
    public ApiResponse getFilteredRecords(@Nullable @RequestBody CFCFilterDto cfcFilter, @PathVariable(name = "filterType") String filterType, HttpServletRequest request) throws Exception {
        logger.info("SBirth getFilteredRecords service call request : cfcFilter = >>>" + cfcFilter);
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
        return birthService.getFilteredData(cfcFilter, filterType, request);
    }


//    @PostMapping("orgCode-filter/{filterType}")
//    //@PreAuthorize("hasAnyRole('ADMIN', 'CREATOR', 'APPROVER')")
//     @PreAuthorize("hasAnyRole('"+Constants.ROLE_ADMIN+"','"+Constants.ROLE_CFC_APPROVER+"', " +
//             "'"+Constants.ROLE_CFC_CREATOR+"','"+Constants.ROLE_CFC_REGISTRAR+"','" + Constants.ROLE_CFC_REGISTRAR +"','" + Constants.ROLE_CHIEF_REGISTRAR + "',)")
//    public ApiResponse getFilteredRecords(@Nullable @RequestBody CFCFilterDto cfcFilter, @PathVariable(name = "filterType") String filterType, HttpServletRequest request) throws Exception {
//        return birthService.getFilteredData(cfcFilter, filterType, request);
//    }
    @PostMapping("/correction")
	@PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_CFC_APPROVER + "', '"
			+ Constants.ROLE_CFC_CREATOR + "', '" + Constants.ROLE_CFC_REGISTRAR + "')")
	public ApiResponse saveStillBirthCorrection(@RequestBody StillBirthCorrectionDto stillBirthCorrectionDto,
			HttpServletRequest request) throws Exception {
        logger.info("saveStillBirthCorrection service call request : StillBirthCorrectionDto = >>>" + stillBirthCorrectionDto.toString());
		return slaDetailsService.saveStillBirthCorrection(stillBirthCorrectionDto, request);
	}

    @PostMapping("/correction/{slaId}")
	@PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_CFC_APPROVER + "', '"
			+ Constants.ROLE_CFC_CREATOR + "', '" + Constants.ROLE_CFC_REGISTRAR + "')")
	public ApiResponse updateStillBirthCorrection(@RequestBody StillBirthCorrectionDto stillBirthCorrectionDto, @PathVariable("slaId") Long slaId,
			HttpServletRequest request) throws Exception {
		return slaDetailsService.updateStillBirthCorrection(slaId, stillBirthCorrectionDto, request);
	}

    @PutMapping("/correction/approve-reject/{slaDetailsId}")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_CFC_REGISTRAR + "')")
    public ApiResponse dataCorrectionApproveReject(@PathVariable("slaDetailsId") Long slaDetailsId,
                                                   @RequestParam String status,
                                                   @RequestParam(value = "remarks", required = false) String remarks,
                                                   HttpServletRequest request)
            throws Exception {
        return slaDetailsService.approveRejectStillBirthLegalData(slaDetailsId, status, remarks, request);
    }

    @GetMapping("/correction/details/{bndId}")
	@PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_CFC_APPROVER + "', '"
			+ Constants.ROLE_CFC_CREATOR + "', '" + Constants.ROLE_CFC_REGISTRAR + "')")
	public ApiResponse getCorrectionDetails(@PathVariable(name = "bndId") Long bndId, HttpServletRequest request)
			throws Exception {
		return slaDetailsService.getDetails(bndId, Constants.STILL_BIRTH_CORRECTION, Constants.RECORD_TYPE_SBIRTH, request);
	}

    @GetMapping("/downloadDocument/{birthId}")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_APPROVER + "', '" + Constants.ROLE_CREATOR + "', " +
            "'" + Constants.ROLE_CFC_APPROVER + "', '" + Constants.ROLE_CFC_CREATOR + "', '" + Constants.ROLE_CFC_REGISTRAR + "')")
    public ApiResponse downloadSdmLetterImage(@PathVariable String birthId, HttpServletRequest request)
            throws FileNotFoundException {
        return birthService.loadSdmLetterFileById(birthId);

    }



    @PutMapping("/delete/{sBirthId}")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_CFC_CREATOR + "', '"
            + Constants.ROLE_CFC_APPROVER + "', '" + Constants.ROLE_CFC_REGISTRAR + "'," +
            "'" + Constants.USER_TYPE_HOSPITAL + "','" + Constants.USER_TYPE_CFC + "','" + Constants.ROLE_CREATOR + "')")
    public ApiResponse deleteSbirth(@PathVariable("sBirthId") Long sBirthId, HttpServletRequest request) throws Exception {
        return birthService.deleteSbirth(sBirthId, request);
    }
}
