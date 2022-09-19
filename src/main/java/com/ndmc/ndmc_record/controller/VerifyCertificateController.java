package com.ndmc.ndmc_record.controller;

import com.ndmc.ndmc_record.config.Constants;
import com.ndmc.ndmc_record.dto.ApiResponse;
import com.ndmc.ndmc_record.dto.PrintRequestDto;
import com.ndmc.ndmc_record.service.CertificatePrintService;
import com.ndmc.ndmc_record.service.SlaDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("api/v1/certificate")
//@CrossOrigin(origins = "*")
public class VerifyCertificateController {

    @Autowired
    CertificatePrintService certificatePrintService;

    @Autowired
    SlaDetailsService slaDetailsService;


    @CrossOrigin(origins = "/**")
    @GetMapping("/verify/{printId}")
    public ApiResponse verifyCertificate(@PathVariable("printId") String printId) throws Exception {

        //String url = "https://www.bing.com/search?q=";
        //String qrCodeText = URLEncoder.encode(Base64.getEncoder().encode(printId, "UTF-8");
       // String qrCodeText =  URLEncoder.encode(printId), "UTF-8");
        return certificatePrintService.verifyCertificate(printId);
    }

    // After correction Print request

    @PostMapping("/print/request")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_CFC_APPROVER + "', '"
            + Constants.ROLE_CFC_CREATOR + "', '" + Constants.ROLE_CFC_REGISTRAR + "','" + Constants.ROLE_CFC_REGISTRAR + "')")
    public ApiResponse savePrintRequest(@RequestBody PrintRequestDto printRequestDto,
                                           HttpServletRequest request) throws Exception {
        return slaDetailsService.savePrintRequest(printRequestDto, request);
    }
    @PostMapping("/print/{useType}/request")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_CFC_REGISTRAR + "')")
    public ApiResponse saveIndividualPrintRequest(@PathVariable(value = "useType") String useType,@RequestBody PrintRequestDto printRequestDto,
                                        HttpServletRequest request) throws Exception {

        return slaDetailsService.saveIndividualPrintRequest(printRequestDto,useType, request);
    }
// Status Change 
    @PutMapping("/print/{id}")
    public ApiResponse updatePrintRequest(@PathVariable(value = "id") Long slaId,
                                          @RequestBody PrintRequestDto printRequestDto,
                                          HttpServletRequest request) throws Exception {
        return slaDetailsService.updatePrintRequest(slaId, printRequestDto, request);
    }
}
