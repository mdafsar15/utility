package com.ndmc.ndmc_record.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.ndmc.ndmc_record.dto.ApiResponse;
import com.ndmc.ndmc_record.dto.AttachmentDto;

public interface DocumentStorageService {

	Resource loadFileAsResource(String fileName);

	ApiResponse storeFile(MultipartFile file, Long slaId, AttachmentDto attachmentDto);

}
