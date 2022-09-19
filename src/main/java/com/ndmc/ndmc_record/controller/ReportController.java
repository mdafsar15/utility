package com.ndmc.ndmc_record.controller;

import com.ndmc.ndmc_record.config.Constants;
import com.ndmc.ndmc_record.dto.*;
import com.ndmc.ndmc_record.service.ReportService;
import com.ndmc.ndmc_record.serviceImpl.AuthServiceImpl;
import com.ndmc.ndmc_record.utils.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("api/v1/reports")
public class ReportController {

    @Autowired
    ReportService reportService;
    @Autowired
    AuthServiceImpl authService;

    @PostMapping("/monthly/{recordType}")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','"+ Constants.ROLE_CHIEF_REGISTRAR + "')")
    public ApiResponse getReportSearch(@RequestBody ReportSearchDto reportSearchDto, @PathVariable(name = "recordType") String recordType,
                                       HttpServletRequest request) throws Exception {
        return reportService.getReprtSearchData(reportSearchDto, recordType, request);
    }

    @PostMapping("/monthly/state-wise")
    @PreAuthorize("hasAnyRole('"+ Constants.ROLE_CHIEF_REGISTRAR + "')")
    public ApiResponse getVitalEventReports(@RequestBody ReportSearchDto reportSearchDto,
                                       HttpServletRequest request) throws Exception {
        return reportService.getVitalEventReportsData(reportSearchDto, request);
    }

    @PostMapping("/monthly/vital-events")
    @PreAuthorize("hasAnyRole('"+ Constants.ROLE_CHIEF_REGISTRAR + "')")
    public ApiResponse getEventReports(@RequestBody ReportSearchDto reportSearchDto,
                                            HttpServletRequest request) throws Exception {
        return reportService.getEventReportsData(reportSearchDto, request);
    }

    @PostMapping("/certificate/count")
    @PreAuthorize("hasAnyRole('"+ Constants.ROLE_CHIEF_REGISTRAR + "', '"+ Constants.ROLE_CFC_REGISTRAR + "')")
    public ApiResponse getCertificateCount(@RequestBody PrintCountDto printCountDto,
                                       HttpServletRequest request) throws Exception {
        return reportService.getCountOfCertificate(printCountDto, request);
    }


    @PostMapping("/daily/user")
    @PreAuthorize("hasAnyRole('"+ Constants.ROLE_CFC_CREATOR + "', '"+ Constants.ROLE_CFC_APPROVER + "')")
    public ApiResponse getUserReports(@RequestBody DailyReportDto dailyReportDto,
                                      HttpServletRequest request) throws Exception {
        String userId = authService.getUserIdFromRequest(request);
        dailyReportDto.setUserId(userId);
        dailyReportDto.setOrgId(null);
        return reportService.getUserReports(dailyReportDto);
    }

    @PostMapping("/cod")
    @PreAuthorize("hasAnyRole('"+ Constants.ROLE_CFC_REGISTRAR + "', '"+ Constants.ROLE_CHIEF_REGISTRAR + "', '"+ Constants.ROLE_ADMIN + "')")
    public ApiResponse getCauseOfDeathReports(@RequestBody FilterDto filterDto,
                                      HttpServletRequest request) throws Exception {
       // String userId = authService.getUserIdFromRequest(request);
        //dailyReportDto.setUserId(userId);
        //dailyReportDto.setOrgId(null);
        return reportService.getCauseOfDeathReports(filterDto, request);
    }


    @PostMapping("/daily/cfc")
    @PreAuthorize("hasAnyRole('"+ Constants.ROLE_CFC_REGISTRAR + "', '"+ Constants.ROLE_CHIEF_REGISTRAR + "', " +
            "'"+ Constants.ROLE_ADMIN + "', '"+ Constants.ROLE_CFC_APPROVER + "')")
    public ApiResponse getCfcReports(@RequestBody DailyReportDto dailyReportDto,
                                      HttpServletRequest request) throws Exception {
        //dailyReportDto.setUserId(null);
        ApiResponse res = new ApiResponse();

        if (CommonUtil.checkNullOrBlank(dailyReportDto.getRegStartDate() + "") &&
                CommonUtil.checkNullOrBlank(dailyReportDto.getRegEndDate() + "") &&
                CommonUtil.checkNullOrBlank(dailyReportDto.getUserId())) {
            res.setStatus(HttpStatus.BAD_REQUEST);
            res.setMsg(Constants.ALL_FIELDS_REQUIRED);
            return res;
        }
        if (CommonUtil.checkNullOrBlank(dailyReportDto.getOrgId())) {
            res.setStatus(HttpStatus.BAD_REQUEST);
            res.setMsg(Constants.SELECT_CFC);
            return res;
        }
        return reportService.getCfcReports(dailyReportDto);
    }

    @PostMapping("/mis/{eventType}/{dateType}")
    @PreAuthorize("hasAnyRole('"+ Constants.ROLE_CHIEF_REGISTRAR + "', '"+ Constants.ROLE_CFC_REGISTRAR + "')")
    public ApiResponse getMisReport(@RequestBody ReportSearchDto reportSearchDto, @PathVariable(name = "eventType") 
            String eventType, @PathVariable(name = "dateType") 
            String dateType, HttpServletRequest request) throws Exception {
        return reportService.getMisReport(reportSearchDto, request, dateType, eventType);

    }

}
