package com.ndmc.ndmc_record.service;

import com.ndmc.ndmc_record.dto.ApiResponse;
import com.ndmc.ndmc_record.dto.SBirthDto;
import com.ndmc.ndmc_record.model.SBirthModel;

import java.util.List;

public interface SBirthHistoryService {

    public ApiResponse saveSBirthRecords(SBirthDto birthDto);
    public ApiResponse updateSBirthRecords(SBirthDto birthDto);
    public List<SBirthModel> getBirthRecords(String status, String orgCode);
    public List<SBirthModel> getAllSBirthRecords();
    public ApiResponse updateBirthRecordStatus(Long sbirthId, String status);
}
