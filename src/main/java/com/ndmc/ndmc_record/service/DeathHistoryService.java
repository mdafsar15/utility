package com.ndmc.ndmc_record.service;

import com.ndmc.ndmc_record.dto.ApiResponse;
import com.ndmc.ndmc_record.dto.DeathDto;
import com.ndmc.ndmc_record.model.DeathModel;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface DeathHistoryService {
    public ApiResponse saveDeathRecords(DeathDto deathDto, HttpServletRequest request) throws Exception;
    public ApiResponse updateDeathRecords(DeathDto deathDto, HttpServletRequest request) throws Exception;
    public List<DeathModel> getDeathRecords(String status, String orgCode);
    public List<DeathModel> getAllDeathRecords();
    public ApiResponse getDeathDetails(Long deathId) throws Exception;
   public ApiResponse updateDeathRecordStatus(Long deathId, String status, HttpServletRequest request) throws Exception;
}
