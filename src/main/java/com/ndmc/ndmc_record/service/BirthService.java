package com.ndmc.ndmc_record.service;

import com.ndmc.ndmc_record.dto.ApiResponse;
import com.ndmc.ndmc_record.dto.BirthDto;
import com.ndmc.ndmc_record.dto.CFCFilterDto;
import com.ndmc.ndmc_record.dto.NameIncusionDto;
import com.ndmc.ndmc_record.dto.ReportSearchDto;
import com.ndmc.ndmc_record.dto.Response;
import com.ndmc.ndmc_record.model.BirthHistoryModel;
import com.ndmc.ndmc_record.model.BirthModel;
import com.ndmc.ndmc_record.model.SlaDetailsModel;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.lang.Exception;

public interface BirthService {

    public  ApiResponse saveBirthRecords(BirthDto birthDto, HttpServletRequest request, MultipartFile sdmLetterImage) throws Exception;
    public ApiResponse updateBirthRecords(BirthDto birthDto, HttpServletRequest request, MultipartFile sdmLetterImage) throws Exception;
    public ApiResponse getBirthRecords(String status, String orgCode);
    public ApiResponse getBirthDetails(Long birthId, HttpServletRequest request) throws Exception;
    public ApiResponse getAllBirthRecords();
    public ApiResponse updateBirthRecordStatus(Long birthId, String status, String remarks, HttpServletRequest request) throws Exception;

    public ApiResponse getUsersBirthRecords(HttpServletRequest request) throws Exception;

    public ResponseEntity<byte[]> generateQrCode(Long birthId, HttpServletRequest request) throws Exception;

    public ApiResponse nameInclusionCorrection(BirthDto birthDto, HttpServletRequest request) throws Exception;

    public ApiResponse getHistoryFromBlc(Long birthId, HttpServletRequest request) throws Exception;
    
    public ApiResponse getFilteredData(CFCFilterDto cfcFilter, String filterType, HttpServletRequest request) throws Exception;

    public void updateInclusionStatus(SlaDetailsModel slaDetailsModel, String username);

    public void updateCorrectionStatus(SlaDetailsModel slaDetailsModel, String username);

    public Object findById(Long bndId);
    
    public List<BirthHistoryModel> getBirthListForReport(ReportSearchDto reportSearchDto, String type);

    Resource loadSdmLetterFileById(String birthId);

    ApiResponse deleteBirth(Long birthDto, HttpServletRequest request);
}
