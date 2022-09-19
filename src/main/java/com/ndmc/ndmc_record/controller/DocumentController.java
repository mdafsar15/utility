package com.ndmc.ndmc_record.controller;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;
import com.ndmc.ndmc_record.config.Constants;
import com.ndmc.ndmc_record.dto.ApiResponse;
import com.ndmc.ndmc_record.dto.AttachmentDto;
import com.ndmc.ndmc_record.dto.ReportSearchDto;
import com.ndmc.ndmc_record.service.AttachmentDetailsService;
import com.ndmc.ndmc_record.service.DocumentStorageService;
import com.ndmc.ndmc_record.service.SlaDetailsService;

@RestController
@RequestMapping("api/v1/document")
//@CrossOrigin(origins = "*")
public class DocumentController {

	private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

	@Autowired
	private DocumentStorageService documentStorageService;
	@Autowired
	private SlaDetailsService slaDetailsService;

	@Autowired
	private AttachmentDetailsService attachmentDetailsService;

	@GetMapping("/downloadDocument/{fileName:.+}")
    @PreAuthorize("hasAnyRole('"+Constants.ROLE_ADMIN+"','"+Constants.ROLE_APPROVER+"', '"+Constants.ROLE_CREATOR+"', " +
			"'"+Constants.ROLE_CFC_APPROVER+"', '"+Constants.ROLE_CFC_CREATOR+"', '" + Constants.ROLE_CFC_REGISTRAR + "')")
	public ResponseEntity<Resource> downloadDocument(@PathVariable String fileName, HttpServletRequest request)
			throws FileNotFoundException {

		Resource resource = documentStorageService.loadFileAsResource(fileName);
		String contentType = null;
		try {
			contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
		} catch (IOException ex) {
			logger.error("Could not determine file type." + ex);
		}

		if (contentType == null) {
			contentType = "application/octet-stream";
		}

		return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				.body(resource);
	}

	@PostMapping("/uploadDocument")
    @PreAuthorize("hasAnyRole('"+Constants.ROLE_ADMIN+"','"+Constants.ROLE_APPROVER+"', " +
			"'"+Constants.ROLE_CREATOR+"', '"+Constants.ROLE_CFC_APPROVER+"', '"+Constants.ROLE_CFC_CREATOR+"','" + Constants.ROLE_CFC_REGISTRAR + "')")
	public ApiResponse uploadDocument(@RequestParam("file") MultipartFile file,
			@RequestPart("attachment") String attachment, HttpServletRequest request) throws Exception {
		logger.info("inside uploadDocument Extrafield :" + attachment + ",file object :" + file);

		Gson gson = new Gson();
		AttachmentDto attachmentDto = gson.fromJson(attachment, AttachmentDto.class);

		Long slaId = slaDetailsService.saveSlaDetails(attachmentDto,request);
		ApiResponse apiResponse = documentStorageService.storeFile(file, slaId ,attachmentDto);
		String fileNameByUser = StringUtils.cleanPath(file.getOriginalFilename());
		
		attachmentDetailsService.saveAttachmentDetails(attachmentDto, slaId ,request,apiResponse, fileNameByUser);
		return apiResponse;
	}

	@PostMapping("/uploadDocument/{slaId}")
    @PreAuthorize("hasAnyRole('"+Constants.ROLE_ADMIN+"','"+Constants.ROLE_APPROVER+"', '"+Constants.ROLE_CREATOR+"', '"+Constants.ROLE_CFC_APPROVER+"', '"+Constants.ROLE_CFC_CREATOR+"','" + Constants.ROLE_CFC_REGISTRAR + "')")
	public ApiResponse uploadDocumentForRejectedSlas(@RequestParam("file") MultipartFile file,
			@RequestPart("attachment") String attachment, @PathVariable(name = "slaId") Long slaId, HttpServletRequest request) throws Exception {
		logger.info("inside uploadDocument Extrafield :" + attachment + ",file object :" + file);

		Gson gson = new Gson();
		AttachmentDto attachmentDto = gson.fromJson(attachment, AttachmentDto.class);

		if(!slaDetailsService.verifySlaIdToUploadDocument(slaId,request)){
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setMsg(Constants.RECORD_NOT_FOUND);
			apiResponse.setStatus(HttpStatus.NOT_FOUND);
			return apiResponse;
		}
		ApiResponse apiResponse = documentStorageService.storeFile(file, slaId ,attachmentDto);
		String fileNameByUser = StringUtils.cleanPath(file.getOriginalFilename());
		
		attachmentDetailsService.saveAttachmentDetails(attachmentDto, slaId ,request,apiResponse, fileNameByUser);
		return apiResponse;
	}

	
	@PostMapping("/reports/{type}")
	@PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_CFC_APPROVER + "', '"
			+ Constants.ROLE_CFC_CREATOR + "', '" + Constants.ROLE_CFC_REGISTRAR + "','" + Constants.ROLE_CHIEF_REGISTRAR + "',)")
	public ApiResponse getReportSearch(@RequestBody ReportSearchDto reportSearchDto, @PathVariable(name = "type") String type,
			HttpServletRequest request) throws Exception {
		return slaDetailsService.getReportSearch(reportSearchDto, type, request);
	}
	
}