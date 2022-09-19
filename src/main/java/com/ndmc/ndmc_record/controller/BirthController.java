package com.ndmc.ndmc_record.controller;

import com.ndmc.ndmc_record.config.Constants;
import com.ndmc.ndmc_record.dto.ApiResponse;
import com.ndmc.ndmc_record.dto.BirthCorrectionDto;
import com.ndmc.ndmc_record.dto.BirthDto;
import com.ndmc.ndmc_record.dto.CFCFilterDto;
import com.ndmc.ndmc_record.dto.NameIncusionDto;
import com.ndmc.ndmc_record.service.*;
import com.ndmc.ndmc_record.serviceImpl.CertificatePrintServiceImpl;
import com.ndmc.ndmc_record.utils.CommonUtil;
import com.ndmc.ndmc_record.utils.JsonUtil;
import com.ndmc.ndmc_record.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@RequestMapping("api/v1/birth")
//@CrossOrigin(origins = "*")
public class BirthController {

	private final Logger logger = LoggerFactory.getLogger(BirthController.class);

	@Autowired
	BirthService birthService;

	@Autowired
	ExcelService excelService;

	@Autowired
	SlaDetailsService slaDetailsService;

	@Autowired
	CertificatePrintServiceImpl certificatePrintService;

	@Autowired
	JwtUtil jwtUtil;

	@Autowired
	private DocumentStorageService documentStorageService;

	@GetMapping("/records/{status}")
	@PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_CHIEF_REGISTRAR + "','" + Constants.ROLE_APPROVER + "', '"
			+ Constants.ROLE_CREATOR + "', '" + Constants.ROLE_CFC_APPROVER + "', '" + Constants.ROLE_CFC_CREATOR
			+ "', '" + Constants.ROLE_CFC_REGISTRAR + "', '" + Constants.ROLE_PUBLIC_CREATOR + "', '" + Constants.ROLE_PUBLIC_APPROVER + "')")
	public ApiResponse birthRecords(@PathVariable("status") String status, @RequestParam String orgCode,
									HttpServletRequest request) {
		return birthService.getBirthRecords(status, orgCode);
	}

	@GetMapping("/records")
	@PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_CHIEF_REGISTRAR + "','" + Constants.ROLE_APPROVER + "', '"
			+ Constants.ROLE_CREATOR + "', '" + Constants.ROLE_CFC_APPROVER + "', '" + Constants.ROLE_CFC_CREATOR
			+ "', '" + Constants.ROLE_CFC_REGISTRAR + "', '" + Constants.ROLE_PUBLIC_CREATOR + "', '" + Constants.ROLE_PUBLIC_APPROVER + "')")
	public ApiResponse birthRecordsForUser(HttpServletRequest request) throws Exception {
		LocalDateTime d1 = LocalDateTime.now();
		logger.debug("Request received in Birth Controller " + d1);
		ApiResponse response = birthService.getUsersBirthRecords(request);
		LocalDateTime d2 = LocalDateTime.now();
		logger.debug("Response received in Birth Controller " + d2);
		return response;
	}

	@GetMapping("/details/{birthId}")
	@PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_APPROVER + "', '"
			+ Constants.ROLE_CREATOR + "', '" + Constants.ROLE_CFC_APPROVER + "', '" + Constants.ROLE_CFC_CREATOR
			+ "', '" + Constants.ROLE_CFC_REGISTRAR + "', '" + Constants.ROLE_PUBLIC_CREATOR + "', '" + Constants.ROLE_PUBLIC_APPROVER + "')")
	public ApiResponse birthRecords(@PathVariable("birthId") Long birthId, HttpServletRequest request)
			throws Exception {
		LocalDateTime d1 = LocalDateTime.now();
		logger.debug("Before getBirthDetails call in Controller " + d1);
		ApiResponse response = birthService.getBirthDetails(birthId, request);
		LocalDateTime d2 = LocalDateTime.now();
		logger.debug("After getBirthDetails call in Controller " + d2);
		return response;
	}

	@GetMapping("/all-records")
	@PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_CHIEF_REGISTRAR + "','" + Constants.ROLE_APPROVER + "', '"
			+ Constants.ROLE_CREATOR + "', '" + Constants.ROLE_CFC_APPROVER + "', '" + Constants.ROLE_CFC_CREATOR
			+ "', '" + Constants.ROLE_CFC_REGISTRAR + "')")
	public ApiResponse birthRecords(HttpServletRequest request) {
		// UserModel existedUser = new UserModel();
		if (request != null) {
			System.out.println("Http request is ============== " + request);
		}
		return birthService.getAllBirthRecords();
	}

//	@PostMapping("/register")
//	@PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_CREATOR + "', '"
//			+ Constants.ROLE_CFC_CREATOR + "', '" + Constants.ROLE_CFC_REGISTRAR + "')")
//	public ApiResponse addBirthRecord(@Valid @RequestBody BirthDto birthDto, HttpServletRequest request)
//			throws Exception {
//
//		logger.info("Before birth register service call : " + LocalDateTime.now());
//		ApiResponse response = birthService.saveBirthRecords(birthDto, request);
//		logger.info("After birth register service call : " + LocalDateTime.now());
//		return response;
//
//	}

	@PostMapping("/register")
	@PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_CREATOR + "', '"
			+ Constants.ROLE_CFC_CREATOR + "', '" + Constants.ROLE_CFC_REGISTRAR + "', '" + Constants.ROLE_PUBLIC_CREATOR + "')")
	public ApiResponse addBirthRecord(@RequestPart("data") String data, HttpServletRequest request, @RequestParam(value = "sdmLetterImage", required = false) MultipartFile sdmLetterImage)
			throws Exception {
		logger.info("addBirthRecord service call request : data = >>>" + data);
		BirthDto birthDto = JsonUtil.getObjectFromJson(data, BirthDto.class);
		logger.info("Before birth register service call : " + sdmLetterImage);
		ApiResponse response = birthService.saveBirthRecords(birthDto, request, sdmLetterImage);
		logger.info("After birth register service call : " + LocalDateTime.now());
		return response;
	}

	@PutMapping("/update")
	@PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_APPROVER + "', '"
			+ Constants.ROLE_CREATOR + "', '" + Constants.ROLE_CFC_CREATOR + "', '" + Constants.ROLE_CFC_APPROVER
			+ "', '" + Constants.ROLE_CFC_REGISTRAR + "', '" + Constants.ROLE_PUBLIC_CREATOR + "', '" + Constants.ROLE_PUBLIC_APPROVER + "')")
	public ApiResponse updateBirthRecord(@RequestPart("data") String data, HttpServletRequest request, @RequestParam(value="sdmLetterImage", required = false) MultipartFile sdmLetterImage) throws Exception {

		logger.info("updateBirthRecord service call request : data = >>>" + data);
		BirthDto birthDto = JsonUtil.getObjectFromJson(data, BirthDto.class);
		return birthService.updateBirthRecords(birthDto, request, sdmLetterImage);
		// return new ResponseEntity("Birth Record updated Successfully",
		// HttpStatus.OK);
	}

	@PutMapping("/name/inclusion-correction") // need to change birth dto to NameInclusio DTO
	@PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_CFC_CREATOR + "', '"
			+ Constants.ROLE_CFC_APPROVER + "', '" + Constants.ROLE_CFC_REGISTRAR + "')")
	public ApiResponse includeName(@RequestBody BirthDto birthDto, HttpServletRequest request) throws Exception {

		logger.info("includeName service call request : data = >>>" + birthDto.toString());
		return birthService.nameInclusionCorrection(birthDto, request);
		// return new ResponseEntity("Birth Record updated Successfully",
		// HttpStatus.OK);
	}

	@PutMapping("/approve-reject/{birthId}")
	@PreAuthorize("hasAnyRole('" + Constants.ROLE_APPROVER + "', '" + Constants.ROLE_CFC_APPROVER + "', '"
			+ Constants.ROLE_ADMIN + "', '" + Constants.ROLE_CFC_REGISTRAR + "', '" + Constants.ROLE_PUBLIC_CREATOR + "', '" + Constants.ROLE_PUBLIC_APPROVER + "')")
	public ApiResponse updateBirthRecordStatus(@PathVariable("birthId") Long birthId, @RequestParam String status,
											   @RequestParam(value = "remarks", required = false) String remarks, HttpServletRequest request)
			throws Exception {
		logger.info("updateBirthRecordStatus service call request : birthId = >>>" + birthId+" , Status="+status+" , remarks="+remarks);
		return birthService.updateBirthRecordStatus(birthId, status, remarks, request);
		// return new ResponseEntity("Record Status updated Successfully",
		// HttpStatus.OK);
	}
	@RequestMapping(value= {"/print-certificate/{birthId}","/print-certificate/{birthId}/{slaId}"}, method=RequestMethod.GET)
	@PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_APPROVER + "', '"
			+ Constants.ROLE_CFC_APPROVER + "', '" + Constants.ROLE_CREATOR + "', '" + Constants.ROLE_CFC_CREATOR
			+ "', '" + Constants.ROLE_CFC_REGISTRAR + "', '" + Constants.ROLE_PUBLIC + "')")
	public ResponseEntity<?> printCertificate(@PathVariable("birthId") Long birthId,
											  @PathVariable(value="slaId",required = false) Long slaId,
	HttpServletRequest request) throws Exception {
		logger.info("printCertificate service call request : birthId = >>>" + birthId+" , slaId="+slaId);
		return certificatePrintService.printBirthCertificate(birthId,slaId, request);
	}


	@GetMapping("/qr-code/{printId}")
	@PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_CHIEF_REGISTRAR + "','" + Constants.ROLE_APPROVER + "', '"
			+ Constants.ROLE_CFC_APPROVER + "', '" + Constants.ROLE_CFC_CREATOR + "', '" + Constants.ROLE_CREATOR
			+ "','" + Constants.ROLE_CFC_REGISTRAR + "', '" + Constants.ROLE_PUBLIC + "')")
	public ResponseEntity<byte[]> generateQrCode(@PathVariable("printId") Long printId, HttpServletRequest request) {
		try {
			logger.info("generateQrCode service call request : printId = >>>" + printId);
			return birthService.generateQrCode(printId, request);
		} catch (Exception e) {

			logger.error("QR CODE Generation " + printId, e);

		}
		return null;
	}

	// Birth history from blockchain

	@GetMapping("/history/{birthId}")
	@PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_APPROVER + "', '"
			+ Constants.ROLE_CFC_APPROVER + "', '" + Constants.ROLE_CFC_CREATOR + "', '" + Constants.ROLE_CFC_REGISTRAR + "','" + Constants.ROLE_CREATOR
			+ "')")
	public ApiResponse getBirthHistory(@PathVariable("birthId") Long birthId, HttpServletRequest request) {
		try {
			return birthService.getHistoryFromBlc(birthId, request);
		} catch (Exception e) {

			logger.error("Error in Birth History data fetching " + birthId, e);

		}
		return null;
	}

	@PostMapping("/cfc-filter/{filterType}")
	// @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR', 'APPROVER')")
	@PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_CHIEF_REGISTRAR + "','" + Constants.ROLE_CFC_APPROVER + "', '"
			+ Constants.ROLE_CFC_CREATOR + "', '" + Constants.ROLE_CFC_REGISTRAR + "',)")
	public ApiResponse getFilteredRecords(@Nullable @RequestBody CFCFilterDto cfcFilter,
										  @PathVariable(name = "filterType") String filterType, HttpServletRequest request) throws Exception {
		logger.info("Birth getFilteredRecords service call request : cfcFilter = >>>" + cfcFilter);
		if( CommonUtil.checkNullOrBlank(cfcFilter.getRegistrationNumber()+"") &&
			CommonUtil.checkNullOrBlank(cfcFilter.getApplicationNumber()+"") &&
			CommonUtil.checkNullOrBlank(cfcFilter.getRegStartDate()+"") &&
			CommonUtil.checkNullOrBlank(cfcFilter.getRegEndDate()+"") &&
			CommonUtil.checkNullOrBlank(cfcFilter.getEventStartDate()+"") &&
			CommonUtil.checkNullOrBlank(cfcFilter.getEventEndDate()+"")){
			if (filterType.equalsIgnoreCase(Constants.FILTER_LEGAL_CORRECTION_SEARCH)
			   || filterType.equalsIgnoreCase(Constants.FILTER_INCLUSION_SEARCH)
				|| filterType.equalsIgnoreCase(Constants.FILTER_PRINT_SEARCH))
				throw new IllegalArgumentException("Invalid Request");
		}
		return birthService.getFilteredData(cfcFilter, filterType, request);
	}

	@PostMapping("/inclusion")
	@PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_CFC_APPROVER + "', '"
			+ Constants.ROLE_CFC_CREATOR + "', '" + Constants.ROLE_CFC_REGISTRAR + "',)")
	public ApiResponse saveBirthIncusion(@RequestBody NameIncusionDto nameIncusionDto, HttpServletRequest request)
			throws Exception {
		logger.info("saveBirthIncusion service call request : nameIncusionDto = >>>" + nameIncusionDto.toString());
		return slaDetailsService.saveBirthInclusion(nameIncusionDto, request);
	}

	@PostMapping("/inclusion/{slaId}")
	@PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_CFC_APPROVER + "', '"
			+ Constants.ROLE_CFC_CREATOR + "', '" + Constants.ROLE_CFC_REGISTRAR + "',)")
	public ApiResponse updateBirthIncusion(@RequestBody NameIncusionDto nameIncusionDto, @PathVariable(name = "slaId") Long slaId, HttpServletRequest request)
			throws Exception {
		logger.info("updateBirthIncusion service call request : nameIncusionDto = >>>" + nameIncusionDto.toString());

		return slaDetailsService.updateBirthIncusion(slaId, nameIncusionDto, request);
	}

	// Birth Correction

	@PostMapping("/correction")
	@PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_CFC_APPROVER + "', '"
			+ Constants.ROLE_CFC_CREATOR + "', '" + Constants.ROLE_CFC_REGISTRAR + "',)")
	public ApiResponse saveBirthCorrection(@RequestBody BirthCorrectionDto birthCorrectionDto,
										   HttpServletRequest request) throws Exception {
		logger.info("saveBirthCorrection service call request : birthCorrectionDto = >>>" + birthCorrectionDto.toString());

		return slaDetailsService.saveBirthCorrection(birthCorrectionDto, request);
	}

	@PostMapping("/correction/{slaId}")
	@PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_CFC_APPROVER + "', '"
			+ Constants.ROLE_CFC_CREATOR + "', '" + Constants.ROLE_CFC_REGISTRAR + "',)")
	public ApiResponse updateBirthCorrection(@RequestBody BirthCorrectionDto birthCorrectionDto, @PathVariable(name = "slaId") Long slaId,
											 HttpServletRequest request) throws Exception {
		logger.info("updateBirthCorrection service call request : birthCorrectionDto = >>>" + birthCorrectionDto.toString());

		return slaDetailsService.updateBirthCorrection(slaId, birthCorrectionDto, request);
	}

	// Approve and Rejection of Name inclusion
	// BndId, certificateType & slaDetailsId

	@PutMapping("/inclusion/approve-reject/{slaDetailsId}")
	@PreAuthorize("hasAnyRole('" + Constants.ROLE_CFC_APPROVER + "')")
	public ApiResponse nameInclusionApproveReject(@PathVariable("slaDetailsId") Long slaDetailsId,
												  @RequestParam String status,
												  @RequestParam(value = "remarks", required = false) String remarks,
												  HttpServletRequest request)
			throws Exception {
		return slaDetailsService.approveRejectBirthIncusion(slaDetailsId, status, remarks, request);
	}

	@PutMapping("/correction/approve-reject/{slaDetailsId}")
	@PreAuthorize("hasAnyRole('" + Constants.ROLE_CFC_REGISTRAR + "')")
	public ApiResponse dataCorrectionApproveReject(@PathVariable("slaDetailsId") Long slaDetailsId,
												   @RequestParam String status,
												   @RequestParam(value = "remarks", required = false) String remarks,
												   HttpServletRequest request)
			throws Exception {
		return slaDetailsService.approveRejectBirthLegalData(slaDetailsId, status, remarks, request);
	}


	@GetMapping("/correction/details/{bndId}")
	@PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_CFC_APPROVER + "', '"
			+ Constants.ROLE_CFC_CREATOR + "', '" + Constants.ROLE_CFC_REGISTRAR + "')")
	public ApiResponse getCorrectionDetails(@PathVariable(name = "bndId") Long bndId, HttpServletRequest request)
			throws Exception {
		return slaDetailsService.getDetails(bndId, Constants.BIRTH_CORRECTION, Constants.RECORD_TYPE_BIRTH, request);
	}

	@GetMapping("/inclusion/details/{bndId}")
	@PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_CFC_APPROVER + "', '"
			+ Constants.ROLE_CFC_CREATOR + "', '" + Constants.ROLE_CFC_REGISTRAR + "')")
	public ApiResponse getIncusionDetails(@PathVariable(name = "bndId") Long bndId, HttpServletRequest request)
			throws Exception {
		return slaDetailsService.getDetails(bndId, Constants.RECORD_NAME_INCLUSION, Constants.RECORD_TYPE_BIRTH, request);
	}


	@GetMapping("/downloadDocument/{birthId}")
	@PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_APPROVER + "', '" + Constants.ROLE_CREATOR + "', " +
			"'" + Constants.ROLE_CFC_APPROVER + "', '" + Constants.ROLE_CFC_CREATOR + "', '" + Constants.ROLE_CFC_REGISTRAR + "')")
	public ResponseEntity<Resource> downloadSdmLetterImage(@PathVariable String birthId, HttpServletRequest request)
			throws FileNotFoundException {
		Resource resource = birthService.loadSdmLetterFileById(birthId);

		String contentType = null;
		try {
			contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
		} catch (IOException ex) {
			logger.info("Could not determine file type." + ex);
		}

		if (contentType == null) {
			contentType = "application/octet-stream";
		}

		return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				.body(resource);

	}

	@PutMapping("/delete/{birthId}")
	@PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_CFC_CREATOR + "', '"
			+ Constants.ROLE_CFC_APPROVER + "', '" + Constants.ROLE_CFC_REGISTRAR + "'," +
			"'" + Constants.USER_TYPE_HOSPITAL + "','" + Constants.USER_TYPE_CFC + "','" + Constants.ROLE_CREATOR + "')")
	public ApiResponse deleteBirth(@PathVariable("birthId") Long birthId, HttpServletRequest request) throws Exception {
		return birthService.deleteBirth(birthId, request);
	}
}
