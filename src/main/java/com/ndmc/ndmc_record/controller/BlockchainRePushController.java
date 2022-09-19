package com.ndmc.ndmc_record.controller;
import com.ndmc.ndmc_record.config.Constants;
import com.ndmc.ndmc_record.dto.ApiResponse;
import com.ndmc.ndmc_record.model.BlockchainRePushSummary;
import com.ndmc.ndmc_record.service.BlockchainRePushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("api/v1/blockchain")
public class BlockchainRePushController {
    private final Logger logger = LoggerFactory.getLogger(BlockchainRePushController.class);

    @Autowired
    BlockchainRePushService blockchainRePushService;
     /*
      This API Using to Re Push The data in blockchain
     */

    @GetMapping("/re-push")
    @PreAuthorize("hasRole('" + Constants.ROLE_ADMIN + "')")
    public List<BlockchainRePushSummary> blockchainRePush(HttpServletRequest request) throws Exception {
        logger.info("CALLING blockchain Re Push<<<<>>>>>>>");
        return blockchainRePushService.getAllRecords(request);
    }
//    @GetMapping("/re-correct-date")
//    @PreAuthorize("hasRole('" + Constants.ROLE_ADMIN + "')")
//    public String blockchainReCorrectDate(HttpServletRequest request) throws Exception {
//        logger.info("CALLING blockchain blockchainReCorrectDate<<<<>>>>>>>");
//        return blockchainRePushService.blockchainReCorrectDate(request);
//    }


    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_APPROVER + "', '"
            + Constants.ROLE_CFC_APPROVER + "', '" + Constants.ROLE_CFC_CREATOR + "', '" + Constants.ROLE_CFC_REGISTRAR + "','" + Constants.ROLE_CREATOR
            + "')")
    public ApiResponse getEventHistory(HttpServletRequest request) {
          //  return blockchainRePushService.fetchBirthHistoryByTypeAndInsertInBlc(recordType);

        return null;

    }
}
