package com.ndmc.ndmc_record.service;

import com.ndmc.ndmc_record.dto.ApiResponse;
import com.ndmc.ndmc_record.dto.BirthDto;
import com.ndmc.ndmc_record.dto.CFCFilterDto;
import com.ndmc.ndmc_record.dto.ReportSearchDto;
import com.ndmc.ndmc_record.dto.SBirthDto;
import com.ndmc.ndmc_record.dto.StillBirthCorrectionDto;
import com.ndmc.ndmc_record.model.BirthModel;
import com.ndmc.ndmc_record.model.SBirthHistoryModel;
import com.ndmc.ndmc_record.model.SBirthModel;
import com.ndmc.ndmc_record.model.SlaDetailsModel;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

public interface SBirthService {

    public ApiResponse saveSBirthRecords(SBirthDto birthDto, HttpServletRequest request, MultipartFile sdmLetterImage) throws Exception;
    public ApiResponse updateSBirthRecords(SBirthDto birthDto, HttpServletRequest request, MultipartFile sdmLetterImage) throws Exception;
    public List<SBirthModel> getBirthRecords(String status, String orgCode);
    public ApiResponse getSBirthDetails(Long sbirthId) throws Exception;
    public ApiResponse getAllSBirthRecords();
    public ApiResponse updateSBirthRecordStatus(Long sbirthId, String status, String remarks, HttpServletRequest request) throws Exception;

    public ApiResponse getUsersSBirthRecords(HttpServletRequest request) throws Exception;

    public ResponseEntity<byte[]> generateQrCode(Long printId, HttpServletRequest request) throws Exception;

    public ApiResponse getHistoryFromBlc(Long birthId, HttpServletRequest request) throws Exception;
    public ApiResponse getFilteredData(CFCFilterDto cfcFilter, String filterType, HttpServletRequest request);
    public void updateCorrectionStatus(SlaDetailsModel slaDetailsModel, String username);
    public Object findById(Long bndId);
    public List<SBirthHistoryModel> getStillBirthListForReport(ReportSearchDto reportSearchDto, String type);

    ApiResponse loadSdmLetterFileById(String birthId);

    ApiResponse deleteSbirth(Long sBirthId, HttpServletRequest request);

}
