package com.ndmc.ndmc_record.service;

import com.ndmc.ndmc_record.dto.ApiResponse;
import com.ndmc.ndmc_record.dto.FilterDto;
import javax.servlet.http.HttpServletRequest;

public interface DashboardService {
    public ApiResponse getAllRecords(HttpServletRequest request);
    public ApiResponse getFilteredData(FilterDto filterDto,String type, HttpServletRequest request);

    public ApiResponse dashboardFilteredData(FilterDto filterDto, String type, HttpServletRequest request) throws Exception;
}
