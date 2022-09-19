package com.ndmc.ndmc_record.controller;

import com.ndmc.ndmc_record.config.Constants;
import com.ndmc.ndmc_record.dto.ApiResponse;
import com.ndmc.ndmc_record.dto.FilterDto;
import com.ndmc.ndmc_record.service.DashboardService;
import com.ndmc.ndmc_record.serviceImpl.AuthServiceImpl;
import com.ndmc.ndmc_record.utils.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("api/v1/dashboard")
public class DashboardController {
    private final Logger logger = LoggerFactory.getLogger(DashboardController.class);

 @Autowired
    DashboardService dashboardService;
 @Autowired
    AuthServiceImpl authService;

    @GetMapping("/get-all-data")
   // @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR', 'APPROVER')")
    @PreAuthorize("hasAnyRole('"+Constants.ROLE_ADMIN+"','"+Constants.ROLE_APPROVER+"', " +
            "'"+Constants.ROLE_CREATOR+"', '"+Constants.ROLE_CFC_APPROVER+"', " +
            "'"+Constants.ROLE_CFC_CREATOR+"', '" + Constants.ROLE_CFC_REGISTRAR + "','" + Constants.ROLE_CHIEF_REGISTRAR + "',)")
    public ApiResponse getData(HttpServletRequest request) throws Exception {
        return dashboardService.getAllRecords(request);
    }

    //type = BIRTH, SBIRTH, DEATH
    @PostMapping("/filter-record/{type}")
    //@PreAuthorize("hasAnyRole('ADMIN', 'CREATOR', 'APPROVER')")
     @PreAuthorize("hasAnyRole('"+Constants.ROLE_ADMIN+"','" + Constants.ROLE_CHIEF_REGISTRAR + "','"+Constants.ROLE_APPROVER+"', " +
             "'"+Constants.ROLE_CREATOR+"', '"+Constants.ROLE_CFC_APPROVER+"', " +
             "'"+Constants.ROLE_CFC_CREATOR+"', '" + Constants.ROLE_CFC_REGISTRAR + "')")
    public ApiResponse getFilteredRecords(@RequestBody FilterDto filterDto, @PathVariable("type") String type, HttpServletRequest request) throws Exception {
       // return dashboardService.getFilteredData(filterDto, type, request);
        logger.info("Birth getFilteredRecords service call request : FilterDto = >>>" + filterDto);

        if(CommonUtil.checkNullOrBlank(filterDto.getRegistrationNumber()+"") &&
                CommonUtil.checkNullOrBlank(filterDto.getApplicationNumber()+"") &&
                CommonUtil.checkNullOrBlank(filterDto.getRegStartDate()+"") &&
                CommonUtil.checkNullOrBlank(filterDto.getRegEndDate()+"") &&
                CommonUtil.checkNullOrBlank(filterDto.getEventStartDate()+"") &&
                CommonUtil.checkNullOrBlank(filterDto.getEventEndDate()+"") &&
                CommonUtil.checkNullOrBlank(filterDto.getStartDate()+"") &&
                CommonUtil.checkNullOrBlank(filterDto.getEndDate()+"")){
                throw new IllegalArgumentException("Invalid Request");
        }
//         if(CommonUtil.checkNullOrBlank(filterDto.getUserId()+"")){
//            String userId = authService.getUserIdFromRequest(request);
//            filterDto.setUserId(Long.parseLong(userId));
//        }
         return dashboardService.dashboardFilteredData(filterDto, type, request);
    }

}
