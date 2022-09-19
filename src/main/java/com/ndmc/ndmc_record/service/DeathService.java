package com.ndmc.ndmc_record.service;

import com.ndmc.ndmc_record.dto.ApiResponse;
import com.ndmc.ndmc_record.dto.CFCFilterDto;
import com.ndmc.ndmc_record.dto.DeathDto;
import com.ndmc.ndmc_record.dto.ReportSearchDto;
import com.ndmc.ndmc_record.model.DeathHistoryModel;
import com.ndmc.ndmc_record.model.DeathModel;
import com.ndmc.ndmc_record.model.SlaDetailsModel;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface DeathService {
    public ApiResponse saveDeathRecords(DeathDto deathDto, HttpServletRequest request, MultipartFile sdmLetterImage) throws Exception;
    public ApiResponse updateDeathRecords(DeathDto deathDto, HttpServletRequest request, MultipartFile sdmLetterImage) throws Exception;
    public List<DeathModel> getDeathRecords(String status, String orgCode);
    public List<DeathModel> getAllDeathRecords();
    public ApiResponse getDeathDetails(Long deathId) throws Exception;
   public ApiResponse updateDeathRecordStatus(Long deathId, String status, String remarks, HttpServletRequest request) throws Exception;

   public ApiResponse getUsersDeathRecords(HttpServletRequest request) throws Exception;
    public ResponseEntity<byte[]> generateQrCode(Long printId, HttpServletRequest request) throws Exception;

    public ApiResponse getHistoryFromBlc(Long deathId, HttpServletRequest request) throws Exception;
    public ApiResponse getFilteredData(CFCFilterDto cfcFilter, String filterType, HttpServletRequest request);
    public void updateCorrectionStatus(SlaDetailsModel slaDetailsModel, String username);
    public Object findById(Long bndId);
    public List<DeathHistoryModel> getDeathListForReport(ReportSearchDto reportSearchDto, String type);

    ApiResponse loadSdmLetterFileById(String deathId);

    ApiResponse deleteDeath(Long deathId, HttpServletRequest request);
}
