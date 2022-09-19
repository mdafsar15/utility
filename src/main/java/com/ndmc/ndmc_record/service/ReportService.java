package com.ndmc.ndmc_record.service;

import com.ndmc.ndmc_record.dto.*;

import javax.servlet.http.HttpServletRequest;

public interface ReportService {
    ApiResponse getReprtSearchData(ReportSearchDto reportSearchDto, String recordType, HttpServletRequest request);

    ApiResponse getVitalEventReportsData(ReportSearchDto reportSearchDto, HttpServletRequest request);

    ApiResponse getEventReportsData(ReportSearchDto reportSearchDto, HttpServletRequest request);

    ApiResponse getCountOfCertificate(PrintCountDto printCountDto, HttpServletRequest request);


    ApiResponse getUserReports(DailyReportDto dailyReportDto);

    ApiResponse getCfcReports(DailyReportDto dailyReportDto);

    ApiResponse getOnlineNameInclusionReports(DailyReportDto dailyReportDto);

    ApiResponse getCauseOfDeathReports(FilterDto filterDto, HttpServletRequest request) throws Exception;

    ApiResponse getMisReport(ReportSearchDto reportSearchDto, HttpServletRequest request, String dateType,
            String eventType) throws Exception;

}
