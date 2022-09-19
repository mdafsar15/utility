package com.ndmc.ndmc_record.service;

import com.ndmc.ndmc_record.dto.ApiResponse;
import com.ndmc.ndmc_record.dto.FilterDto;
import com.ndmc.ndmc_record.model.BlockchainRePushSummary;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface BlockchainRePushService {

    public List<BlockchainRePushSummary> getAllRecords(HttpServletRequest request) throws Exception;

    String blockchainReCorrectDate(HttpServletRequest request) throws Exception;

}
