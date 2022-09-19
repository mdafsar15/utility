package com.ndmc.ndmc_record.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.ndmc.ndmc_record.dto.ApiResponse;
import com.ndmc.ndmc_record.dto.AttachmentDto;
import com.ndmc.ndmc_record.model.AttachmentModel;

public interface AttachmentDetailsService {
	Long saveAttachmentDetails(AttachmentDto attachmentDto, Long slaId, HttpServletRequest request,
			ApiResponse apiResponse, String fileNameByUser) throws Exception;

    List<AttachmentModel> findBySlaDetailsId(Long slaDetailsId);
}
