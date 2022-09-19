package com.ndmc.ndmc_record.serviceImpl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import com.ndmc.ndmc_record.dto.*;
import com.ndmc.ndmc_record.exception.NotFoundException;
import com.ndmc.ndmc_record.model.*;
import com.ndmc.ndmc_record.repository.*;

import com.ndmc.ndmc_record.utils.CustomBeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.ndmc.ndmc_record.blockchainGatway.BlockchainGatway;
import com.ndmc.ndmc_record.config.Constants;

import com.ndmc.ndmc_record.model.BlockchainSlaDetailsResponse;
import com.ndmc.ndmc_record.model.BlockchainUpdateDeathResponse;
import com.ndmc.ndmc_record.model.SlaDetailsModel;
import com.ndmc.ndmc_record.repository.SlaDetailsRepository;
import com.ndmc.ndmc_record.service.AttachmentDetailsService;
import com.ndmc.ndmc_record.service.BirthService;
import com.ndmc.ndmc_record.service.DeathService;
import com.ndmc.ndmc_record.service.SBirthService;
import com.ndmc.ndmc_record.service.SlaDetailsService;
import com.ndmc.ndmc_record.utils.CommonUtil;
import com.ndmc.ndmc_record.utils.JwtUtil;

@Service
@Transactional
public class SlaDetailsServiceImpl implements SlaDetailsService {

	@Autowired
	LateFeeRepository lateFeeRepository;

	@Autowired
	private SlaDetailsRepository slaDetailsRepository;

	@Autowired
	BlockchainGatway blockchainGatway;

	@Autowired
	AuthRepository authRepository;
    
    @Autowired
	OrganizationRepository organizationRepository;

	@Autowired
	BirthRepository birthRepository;
	@Autowired
	BirthHistoryRepository birthHistoryRepository;

	@Autowired
	DeathRepository deathRepository;
	@Autowired
	DeathHistoryRepository deathHistoryRepository;
	@Autowired
	SBirthRepository sBirthRepository;
	@Autowired
	SBirthHistoryRepository sBirthHistoryRepository;

	@Autowired
	BirthServiceImpl birthServiceImpl;

	@Autowired
	SlaDetailsHistoryRepository slaDetailsHistoryRepository;

	@Autowired
	OnlineAppointmentRepository onlineAppointmentRepository;

	@Autowired
	BirthService birthService;

	@Autowired
	SBirthService sBirthService;

	@Autowired
	DeathService deathService;

	@Autowired
	AttachmentDetailsService attachmentService;

	@Autowired
	AuthServiceImpl authService;

	@Value("${CHANNEL_GOVT_HOSPITAL}")
	private String channelGovtHospital;

	private final Logger logger = LoggerFactory.getLogger(SlaDetailsServiceImpl.class);

	@Override
	@Transactional
	public Long saveSlaDetails(AttachmentDto attachment, HttpServletRequest request) throws Exception {
		LocalDateTime startTime = LocalDateTime.now();
		logger.info("saveSlaDetails startTime " + startTime);
		Long bndId = attachment.getBndId();
		String transactionType = attachment.getTransactionType();
		String certificateType = attachment.getRecordType();
		String status = Constants.RECORD_STATUS_UPLOADING;
		String username = authService.getUserIdFromRequest(request);

		Optional<SlaDetailsModel> slaDetails = slaDetailsRepository
				.findByBndIdAndTransactionTypeAndCertificateTypeAndStatusAndUserId(bndId, transactionType, certificateType,
						status,username);
		if (slaDetails.isPresent()) {
			SlaDetailsModel slaDetailsModel = slaDetails.get();
			return slaDetailsModel.getSlaDetailsId();
		}
		// JwtUtil jwtUtil = new JwtUtil();
		// String username = "admin";
		LocalDateTime now = LocalDateTime.now();
		SlaDetailsModel slaDetailsModel = new SlaDetailsModel();
		BeanUtils.copyProperties(attachment, slaDetailsModel);
		slaDetailsModel.setStatus(status);
		slaDetailsModel.setUserId(username);
		slaDetailsModel.setBndId(bndId);
		slaDetailsModel.setTransactionType(transactionType);
		slaDetailsModel.setCertificateType(certificateType);
		slaDetailsModel.setCreatedAt(now);
		Long organizationId = authService.getOrganizationIdFromUserId(username);
		slaDetailsModel.setSlaOrganizationId(organizationId);

		// try {
			slaDetailsModel = slaDetailsRepository.save(slaDetailsModel);
			BlockchainSlaDetailsResponse blockchainResult = blockchainGatway.insertSlaDetails(slaDetailsModel,
					channelGovtHospital);
			logger.info("After data addition in Blockchain ==== " + now);
			// Set Blockchain response
			String message = blockchainResult.getMessage();
			String txID = blockchainResult.getTxID();
			String statusBlk = blockchainResult.getStatus();
			if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(statusBlk)) {
				slaDetailsModel.setBlcMessage(message);
				slaDetailsModel.setBlcTxId(txID);
				slaDetailsModel.setBlcStatus(statusBlk);

			// } catch (Exception e) {
			// 	logger.error("===Blc Exception ===", e);
			// 	String message = e.getMessage();
			// 	String statusBlk = Constants.BLC_STATUS_FALSE;
			// 	message = CommonUtil.updateExceptionMessage(message);
			// 	slaDetailsModel.setBlcMessage(message);
			// 	slaDetailsModel.setBlcStatus(statusBlk);

			// }
				slaDetailsRepository.save(slaDetailsModel);
			} else {
				// logger.info("saveSlaDetails False response from blockchain " + blockchainResult);
				throw new Exception(Constants.INTERNAL_SERVER_ERROR);
			}
		LocalDateTime endTime = LocalDateTime.now();
		logger.info("saveSlaDetails endTime " + endTime);
		return slaDetailsModel.getSlaDetailsId();
	}

	@Override
	public Map<Long, SlaDetailsModel> findAllPendingMap(String recordType, String transactionType) {
		List<SlaDetailsModel> slaDetailList = findAll(recordType, transactionType);
		Map<Long, SlaDetailsModel> slaMap = new HashMap<>();
		for (SlaDetailsModel slaDetailsModel : slaDetailList) {
			slaMap.put(slaDetailsModel.getBndId(), slaDetailsModel);
		}
		return slaMap;
	}

	private List<SlaDetailsModel> findAll(String recordType, String transactionType) {
		return slaDetailsRepository.findAll(new Specification<SlaDetailsModel>() {
			@Override
			public Predicate toPredicate(Root<SlaDetailsModel> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicates = new ArrayList<>();
				Predicate pendingPredicate = criteriaBuilder.equal(root.get(Constants.STATUS),
						Constants.RECORD_STATUS_PENDING);
				Predicate uploadingPredicate = criteriaBuilder.equal(root.get(Constants.STATUS),
						Constants.RECORD_STATUS_UPLOADING);
				Predicate rejectedPredicate = criteriaBuilder.equal(root.get(Constants.STATUS),
						Constants.RECORD_STATUS_REJECTED);
				predicates.add(
						criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.CERTIFICATE_TYPE), recordType)));
				predicates.add(criteriaBuilder
						.and(criteriaBuilder.equal(root.get(Constants.TRANSACTION_TYPE), transactionType)));
				predicates.add(criteriaBuilder.or(pendingPredicate, uploadingPredicate, rejectedPredicate));
				return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		});
	}

	@Override
	@Transactional
	public ApiResponse saveBirthInclusion(NameIncusionDto nameIncusionDto, HttpServletRequest request) throws Exception {
		ApiResponse apiResponse = new ApiResponse();
		LocalDateTime now = LocalDateTime.now();
		Long bndId = nameIncusionDto.getBndId();

		String transactionType = nameIncusionDto.getTransactionType();
		String certificateType = nameIncusionDto.getRecordType();
		String status = Constants.RECORD_STATUS_UPLOADING;
		String username = authService.getUserIdFromRequest(request);

		Optional<SlaDetailsModel> slaDetails = slaDetailsRepository
				.findByBndIdAndTransactionTypeAndCertificateTypeAndStatusAndUserId(bndId, transactionType, certificateType,
						status,username);

		logger.info("====== slaDetails response is ===="+slaDetails);
		if (slaDetails.isPresent()) {
			SlaDetailsModel slaDetailsModel = slaDetails.get();
			slaDetailsModel.setApplDate(now);
			return saveBirthIncusion(slaDetailsModel, nameIncusionDto, request);
		} else {
			apiResponse.setMsg(Constants.PENDING_MESSAGE);
			apiResponse.setStatus(HttpStatus.BAD_REQUEST);
			return apiResponse;
		}

	}

	private ApiResponse saveBirthIncusion(SlaDetailsModel slaDetailsModel, NameIncusionDto nameIncusionDto,
			HttpServletRequest request) throws Exception {
		BeanUtils.copyProperties(nameIncusionDto, slaDetailsModel);
		LocalDateTime now = LocalDateTime.now();
		String username = authService.getUserIdFromRequest(request);
		Optional<UserModel> currentUserOp = authRepository.findById(Long.parseLong(username));

		UserModel currentUser = currentUserOp.get();
		String orgId = currentUser.getOrganizationId();
		Optional<OrganizationModel> organizationModelOp = organizationRepository.findById(Long.parseLong(orgId));
		OrganizationModel organizationModel = organizationModelOp.get();
		String orgType = organizationModel.getOrganizationType();
		// String orgCode = organizationModel.getOrganisationCode();
		// String divisionCode = organizationModel.getDivisionCode();

		slaDetailsModel.setStatus(Constants.RECORD_STATUS_PENDING);
		slaDetailsModel.setUpdatedAt(now);
		// JwtUtil jwtUtil = new JwtUtil();
		String userName = authService.getUserIdFromRequest(request);
		slaDetailsModel.setUpdatedBy(userName);
        Optional<BirthModel> birthModel = birthRepository.findById(nameIncusionDto.getBndId());

		long numberOfDays = CommonUtil.getDayBetweenDates(birthModel.get().getEventDate().toLocalDate(), LocalDate.now());

		Optional<LateFee> existedFeeData = lateFeeRepository.findById(Constants.APPLICATION_TYPE_CFC_INCLUSION);
		if (Constants.USER_TYPE_CFC.equalsIgnoreCase(organizationModel.getOrganizationType())) {
			existedFeeData = lateFeeRepository.findById(Constants.APPLICATION_TYPE_CFC_INCLUSION);
		}
		if (Constants.USER_TYPE_CFC.equalsIgnoreCase(orgType)
				&& numberOfDays > existedFeeData.get().getStartDays()) {
			slaDetailsModel.setFee(existedFeeData.get().getFee());
			slaDetailsModel.setAmount(existedFeeData.get().getFee());
		}else{
			slaDetailsModel.setFee(0.0F);
			slaDetailsModel.setAmount(0.0F);
		}

		ApiResponse apiResponse = new ApiResponse();
		apiResponse.setMsg(Constants.NAME_INCLUSION_SUCCESS_MESSAGE);
		apiResponse.setStatus(HttpStatus.OK);
		apiResponse.setData(slaDetailsModel.getApplNo());

		// try {
		BlockchainSlaDetailsResponse blockchainResult = blockchainGatway.updateSlaRecord(slaDetailsModel,
					channelGovtHospital);
			// logger.info("After data addition in Blockchain ==== " + now);
			// Set Blockchain response
			String message = blockchainResult.getMessage();
			String txID = blockchainResult.getTxID();
			String statusBlk = blockchainResult.getStatus();
			if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(statusBlk)) {
				slaDetailsModel.setBlcMessage(message);
				slaDetailsModel.setBlcTxId(txID);
				slaDetailsModel.setBlcStatus(statusBlk);

			// } catch (Exception e) {
			// 	logger.error("===Blc Exception ===", e);
			// 	String message = e.getMessage();
			// 	String statusBlk = Constants.BLC_STATUS_FALSE;
			// 	message = CommonUtil.updateExceptionMessage(message);
			// 	slaDetailsModel.setBlcMessage(message);
			// 	slaDetailsModel.setBlcStatus(statusBlk);

			// }
			// logger.info("===Saving Sla Details ===" + slaDetailsModel);
			slaDetailsRepository.save(slaDetailsModel);

			switch (slaDetailsModel.getCertificateType()) {
				case Constants.RECORD_TYPE_BIRTH:
					birthService.updateInclusionStatus(slaDetailsModel, userName);
					break;
				default:
					throw new RuntimeException("Invalid Record Type");

			}

		}else {
			// logger.info("saveBirthIncusion False response from blockchain " + blockchainResult);
			throw new Exception(Constants.INTERNAL_SERVER_ERROR);
		}
		return apiResponse;
	}

	@Override
	@Transactional
	public ApiResponse saveBirthCorrection(BirthCorrectionDto birthCorrectionDto, HttpServletRequest request) throws Exception {

		ApiResponse apiResponse = new ApiResponse();
		LocalDateTime now = LocalDateTime.now();
		Long bndId = birthCorrectionDto.getBndId();
		String transactionType = birthCorrectionDto.getTransactionType();
		String certificateType = birthCorrectionDto.getRecordType();
		String status = Constants.RECORD_STATUS_UPLOADING;
		String username = authService.getUserIdFromRequest(request);

		Optional<SlaDetailsModel> slaDetails = slaDetailsRepository.findByBndIdAndTransactionTypeAndCertificateTypeAndStatusAndUserId(bndId, transactionType, certificateType, status,username);

		logger.info("=== SLA DETAILS ==="+ slaDetails);

		if (slaDetails.isPresent()) {
			SlaDetailsModel slaDetailsModel = slaDetails.get();
			slaDetailsModel.setApplDate(now);
			return saveBirthLegalData(slaDetailsModel, birthCorrectionDto, request);
		}
		else
		{
			apiResponse.setMsg(Constants.RECORD_NOT_FOUND);
			apiResponse.setStatus(HttpStatus.NOT_FOUND);
			return apiResponse;
		}
	}

	private ApiResponse saveBirthLegalData(SlaDetailsModel slaDetailsModel, BirthCorrectionDto birthCorrectionDto,
			HttpServletRequest request) throws Exception {
		BeanUtils.copyProperties(birthCorrectionDto, slaDetailsModel);

		//CustomBeanUtils.copyBirthCorrectionToSlaDetailsModel(birthCorrectionDto, slaDetailsModel);

		slaDetailsModel.setStatus(Constants.RECORD_STATUS_PENDING);
		LocalDateTime now = LocalDateTime.now();
		slaDetailsModel.setUpdatedAt(now);
		// JwtUtil jwtUtil = new JwtUtil();
		String userName = authService.getUserIdFromRequest(request);
		slaDetailsModel.setUpdatedBy(userName);
		slaDetailsModel.setDateOfEvent(birthCorrectionDto.getDateOfEvent());
		ApiResponse apiResponse = new ApiResponse();
		apiResponse.setMsg(Constants.BIRTH_CORRECTIONS_SUCCESS_MESSAGE);
		apiResponse.setStatus(HttpStatus.OK);
		logger.info("slaDetailsModel.getApplNo() ==== " + slaDetailsModel.getApplNo());
		apiResponse.setData(slaDetailsModel.getApplNo());
		try {
		BlockchainSlaDetailsResponse blockchainResult = blockchainGatway.updateSlaRecord(slaDetailsModel,
					channelGovtHospital);
			logger.info("After data addition in Blockchain ==== " + now);
			// Set Blockchain response
			String message = blockchainResult.getMessage();
			String txID = blockchainResult.getTxID();
			String statusBlk = blockchainResult.getStatus();
			if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(statusBlk)) {
				slaDetailsModel.setBlcMessage(message);
				slaDetailsModel.setBlcTxId(txID);
				slaDetailsModel.setBlcStatus(statusBlk);

			 }} catch (Exception e) {
			// 	logger.error("===Blc Exception ===", e);
			// 	String message = e.getMessage();
			// 	String statusBlk = Constants.BLC_STATUS_FALSE;
			// 	message = CommonUtil.updateExceptionMessage(message);
			// 	slaDetailsModel.setBlcMessage(message);
			// 	slaDetailsModel.setBlcStatus(statusBlk);

			 }
		    logger.info("====SLADETAILS MODEL===="+slaDetailsModel);
			slaDetailsRepository.save(slaDetailsModel);
			SlaDetailsHistoryModel slaDetailsHistoryModel = new SlaDetailsHistoryModel();
			BeanUtils.copyProperties(slaDetailsModel, slaDetailsHistoryModel);
			slaDetailsHistoryModel = slaDetailsHistoryRepository.save(slaDetailsHistoryModel);
			birthService.updateCorrectionStatus(slaDetailsModel, userName);

		return apiResponse;
	}

	@Override
	@Transactional
	public ApiResponse saveStillBirthCorrection(StillBirthCorrectionDto stillBirthCorrectionDto,
			HttpServletRequest request) throws Exception {

		ApiResponse apiResponse = new ApiResponse();
		LocalDateTime now = LocalDateTime.now();
		Long bndId = stillBirthCorrectionDto.getBndId();
		String transactionType = stillBirthCorrectionDto.getTransactionType();
		String certificateType = stillBirthCorrectionDto.getRecordType();
		String status = Constants.RECORD_STATUS_UPLOADING;

		logger.info("====== Still birth Correction ===== transactiontype === " + transactionType
				+ " and  Certificate Type is === " + certificateType);
		String username = authService.getUserIdFromRequest(request);

		Optional<SlaDetailsModel> slaDetails = slaDetailsRepository
				.findByBndIdAndTransactionTypeAndCertificateTypeAndStatusAndUserId(bndId, transactionType, certificateType,
						status,username);

		if (slaDetails.isPresent()) {
			SlaDetailsModel slaDetailsModel = slaDetails.get();
			slaDetailsModel.setApplDate(now);
			return saveStillBirthCorrection(slaDetailsModel, stillBirthCorrectionDto, request);
		} else {
			apiResponse.setMsg(Constants.RECORD_NOT_FOUND);
			apiResponse.setStatus(HttpStatus.NOT_FOUND);
			return apiResponse;
		}
	}

	private ApiResponse saveStillBirthCorrection(SlaDetailsModel slaDetailsModel,
			StillBirthCorrectionDto stillBirthCorrectionDto, HttpServletRequest request) throws Exception {

		// logger.info("saveStillBirthCorrection ==== " + stillBirthCorrectionDto.toString());
		BeanUtils.copyProperties(stillBirthCorrectionDto, slaDetailsModel);
		slaDetailsModel.setDateOfEvent(stillBirthCorrectionDto.getDateOfEvent());
		slaDetailsModel.setStatus(Constants.RECORD_STATUS_PENDING);
		LocalDateTime now = LocalDateTime.now();
		slaDetailsModel.setUpdatedAt(now);
		// JwtUtil jwtUtil = new JwtUtil();
		String userName = authService.getUserIdFromRequest(request);
		slaDetailsModel.setUpdatedBy(userName);

		ApiResponse apiResponse = new ApiResponse();
		apiResponse.setMsg(Constants.STILL_BIRTH_CORRECTION_SUCCESS_MESSAGE);
		apiResponse.setStatus(HttpStatus.OK);
		//apiResponse.setData(slaDetailsModel.getSlaDetailsId());
		apiResponse.setData(slaDetailsModel.getApplNo());
		// try {
		BlockchainSlaDetailsResponse blockchainResult = blockchainGatway.updateSlaRecord(slaDetailsModel,
					channelGovtHospital);
			logger.info("After data addition in Blockchain ==== " + now);
			// Set Blockchain response
			String message = blockchainResult.getMessage();
			String txID = blockchainResult.getTxID();
			String statusBlk = blockchainResult.getStatus();

			if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(statusBlk)) {
				slaDetailsModel.setBlcMessage(message);
				slaDetailsModel.setBlcTxId(txID);
				slaDetailsModel.setBlcStatus(statusBlk);

			// } catch (Exception e) {
			// 	logger.error("===Blc Exception ===", e);
			// 	String message = e.getMessage();
			// 	String statusBlk = Constants.BLC_STATUS_FALSE;
			// 	message = CommonUtil.updateExceptionMessage(message);
			// 	slaDetailsModel.setBlcMessage(message);
			// 	slaDetailsModel.setBlcStatus(statusBlk);

			// }
			slaDetailsRepository.save(slaDetailsModel);
			SlaDetailsHistoryModel slaDetailsHistoryModel = new SlaDetailsHistoryModel();
			BeanUtils.copyProperties(slaDetailsModel, slaDetailsHistoryModel);
			slaDetailsHistoryModel = slaDetailsHistoryRepository.save(slaDetailsHistoryModel);
			sBirthService.updateCorrectionStatus(slaDetailsModel, userName);
		} else {
			// logger.info("saveStillBirthCorrection False response from blockchain " + blockchainResult);
			throw new Exception(Constants.INTERNAL_SERVER_ERROR);
		
		}
		return apiResponse;
	}

	@Override
	@Transactional
	public ApiResponse saveDeathCorrection(DeathCorrectionDto stillBirthCorrectionDto, HttpServletRequest request) throws Exception {
		ApiResponse apiResponse = new ApiResponse();
		LocalDateTime now = LocalDateTime.now();
		Long bndId = stillBirthCorrectionDto.getBndId();
		String transactionType = stillBirthCorrectionDto.getTransactionType();
		String certificateType = stillBirthCorrectionDto.getRecordType();
		String status = Constants.RECORD_STATUS_UPLOADING;
		String username = authService.getUserIdFromRequest(request);

		Optional<SlaDetailsModel> slaDetails = slaDetailsRepository
				.findByBndIdAndTransactionTypeAndCertificateTypeAndStatusAndUserId(bndId, transactionType, certificateType,
						status,username);

		if (slaDetails.isPresent()) {
			SlaDetailsModel slaDetailsModel = slaDetails.get();
			slaDetailsModel.setApplDate(now);
			return saveDeathCorrection(slaDetailsModel, stillBirthCorrectionDto, request);
		} else {
			apiResponse.setMsg(Constants.RECORD_NOT_FOUND);
			apiResponse.setStatus(HttpStatus.NOT_FOUND);
			return apiResponse;
		}
	}

	private ApiResponse saveDeathCorrection(SlaDetailsModel slaDetailsModel, DeathCorrectionDto deathCorrectionDto,
			HttpServletRequest request) throws Exception {

		// logger.info("saveDeathCorrection ==== " + stillBirthCorrectionDto.toString());
		BeanUtils.copyProperties(deathCorrectionDto, slaDetailsModel);
		slaDetailsModel.setDateOfEvent(deathCorrectionDto.getDateOfEvent());
		slaDetailsModel.setStatus(Constants.RECORD_STATUS_PENDING);
		LocalDateTime now = LocalDateTime.now();
		slaDetailsModel.setUpdatedAt(now);
		// JwtUtil jwtUtil = new JwtUtil();
		String userName = authService.getUserIdFromRequest(request);
		slaDetailsModel.setUpdatedBy(userName);

		ApiResponse apiResponse = new ApiResponse();
		apiResponse.setMsg(Constants.DEATH_CORRECTIONS_SUCCESS_MESSAGE);
		apiResponse.setStatus(HttpStatus.OK);
		//apiResponse.setData(slaDetailsModel.getSlaDetailsId());
		apiResponse.setData(slaDetailsModel.getApplNo());
		 try {
		BlockchainSlaDetailsResponse blockchainResult = blockchainGatway.updateSlaRecord(slaDetailsModel,
					channelGovtHospital);
			logger.info("After data addition in Blockchain ==== " + now);
			// Set Blockchain response
			String message = blockchainResult.getMessage();
			String txID = blockchainResult.getTxID();
			String statusBlk = blockchainResult.getStatus();
			if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(statusBlk)) {
				slaDetailsModel.setBlcMessage(message);
				slaDetailsModel.setBlcTxId(txID);
				slaDetailsModel.setBlcStatus(statusBlk);
			}

			} catch (Exception e) {
			// 	logger.error("===Blc Exception ===", e);
			// 	String message = e.getMessage();
			// 	String statusBlk = Constants.BLC_STATUS_FALSE;
			// 	message = CommonUtil.updateExceptionMessage(message);
			// 	slaDetailsModel.setBlcMessage(message);
			// 	slaDetailsModel.setBlcStatus(statusBlk);
              e.printStackTrace();
			 }
			slaDetailsRepository.save(slaDetailsModel);
			SlaDetailsHistoryModel slaDetailsHistoryModel = new SlaDetailsHistoryModel();
			BeanUtils.copyProperties(slaDetailsModel, slaDetailsHistoryModel);
			slaDetailsHistoryModel = slaDetailsHistoryRepository.save(slaDetailsHistoryModel);
			deathService.updateCorrectionStatus(slaDetailsModel, userName);

		return apiResponse;

	}

	@Override
	@Transactional
	public ApiResponse approveRejectBirthIncusion(Long slaDetailsId, String status, String remarks,
			HttpServletRequest request) throws Exception {

		ApiResponse res = new ApiResponse();
		Optional<SlaDetailsModel> exitedData = slaDetailsRepository.findById(slaDetailsId);
		JwtUtil jwtUtil = new JwtUtil();
		String userName = authService.getUserIdFromRequest(request);
		// UserModel currentUser = authRepository.findById(Long.parseLong(userName));
		SlaDetailsModel slaDetailsModel = exitedData.get();
		Optional<BirthModel> existedBirthData = birthRepository.findById(slaDetailsModel.getBndId());
		BirthModel birthModel = existedBirthData.get();

		logger.info("======= status is =====" + status + "==== Birth Model is" + birthModel);
		if (!exitedData.isPresent() && !existedBirthData.isPresent()) {
			res.setStatus(HttpStatus.NOT_FOUND);
			res.setMsg(Constants.RECORD_NOT_FOUND);
			logger.error("==== data not found =====" + exitedData);

		} else {

			if (Constants.RECORD_STATUS_REJECTED.equalsIgnoreCase(status)) {
				logger.error(
						" =========== Before if in else SLA details Model in rejected ============ " + slaDetailsModel);
				slaDetailsModel.setSlaDetailsId(slaDetailsId);
				slaDetailsModel.setRejectedAt(LocalDateTime.now());
				slaDetailsModel.setRejectedBy(userName);
				slaDetailsModel.setRemarks(remarks);
				slaDetailsModel.setStatus(Constants.RECORD_STATUS_REJECTED);
				slaDetailsModel = slaDetailsRepository.save(slaDetailsModel);
				logger.error(" =========== SLA details Model in rejected ============ " + slaDetailsModel);

				if (slaDetailsModel != null) {
					SlaDetailsHistoryModel slaDetailsHistoryModel = new SlaDetailsHistoryModel();

					// Update data into birth table
					// birthModel.setBirthId(slaDetailsModel.getBndId());
					birthModel.setModifiedAt(LocalDateTime.now());
					birthModel.setModifiedBy(userName);
					birthModel.setInclusionSlaId(slaDetailsModel.getSlaDetailsId());
					// birthModel.setRejectionRemark(remarks);
					birthModel.setStatus(Constants.RECORD_STATUS_INCLUSION_REJECTED);
					birthModel.setTransactionType(Constants.NAME_INCLUSION);
					birthModel = birthRepository.save(birthModel);

					// Insert new records in to birth_history table
					BirthHistoryModel birthHistoryModel = new BirthHistoryModel();
					BeanUtils.copyProperties(birthModel, birthHistoryModel);
					birthHistoryModel = birthHistoryRepository.save(birthHistoryModel);

					BeanUtils.copyProperties(slaDetailsModel, slaDetailsHistoryModel);
					slaDetailsHistoryModel = slaDetailsHistoryRepository.save(slaDetailsHistoryModel);
					res.setStatus(HttpStatus.OK);
					res.setMsg(Constants.RECORD_REJECTED_MESSAGE);
					res.setData(birthModel.getApplicationNumber());
					// ============ BLOCKCHAIN Response Handler Function =====================
					inclusionBlcResponse(slaDetailsId, slaDetailsModel, slaDetailsHistoryModel, userName, remarks,
							birthModel, birthHistoryModel);

				}

			} else if (Constants.RECORD_STATUS_APPROVED.equalsIgnoreCase(status)
					&& ((slaDetailsModel.getChildName() != null) && !slaDetailsModel.getChildName().isEmpty())) {
				logger.info(" ============ SLA details Model in Approved ========  " + slaDetailsModel);
				slaDetailsModel.setSlaDetailsId(slaDetailsId);
				slaDetailsModel.setApprovedAt(LocalDateTime.now());
				slaDetailsModel.setApprovedBy(userName);
				slaDetailsModel.setStatus(Constants.RECORD_STATUS_APPROVED);
				slaDetailsModel = slaDetailsRepository.save(slaDetailsModel);

				if (slaDetailsModel != null) {
					SlaDetailsHistoryModel slaDetailsHistoryModel = new SlaDetailsHistoryModel();
					slaDetailsHistoryModel.setApprovedAt(LocalDateTime.now());
					slaDetailsHistoryModel.setApprovedBy(userName);
					slaDetailsHistoryModel.setRemarks(remarks);
					slaDetailsHistoryModel.setStatus(Constants.RECORD_STATUS_APPROVED);
					BeanUtils.copyProperties(slaDetailsModel, slaDetailsHistoryModel);
					slaDetailsHistoryModel = slaDetailsHistoryRepository.save(slaDetailsHistoryModel);

					// Update data into birth table
					birthModel.setBirthId(slaDetailsModel.getBndId());
					birthModel.setApprovedAt(LocalDateTime.now());
					birthModel.setApprovedBy(userName);
					birthModel.setName(slaDetailsModel.getChildName());
					birthModel.setStatus(Constants.RECORD_STATUS_APPROVED);
					birthModel = birthRepository.save(birthModel);

					// Insert new records in to birth_history table
					BirthHistoryModel birthHistoryModel = new BirthHistoryModel();
					birthModel.setTransactionType(Constants.NAME_INCLUSION);
					BeanUtils.copyProperties(birthModel, birthHistoryModel);
					birthHistoryModel = birthHistoryRepository.save(birthHistoryModel);
					res.setStatus(HttpStatus.OK);
					res.setMsg(Constants.RECORD_APPROVED_MESSAGE);
					res.setData(birthModel.getApplicationNumber());
					inclusionBlcResponse(slaDetailsId, slaDetailsModel, slaDetailsHistoryModel, userName, remarks,
							birthModel, birthHistoryModel);

				}

			} else {
				res.setStatus(HttpStatus.BAD_REQUEST);
				res.setMsg(Constants.CHILD_NAME_NOT_FOUND);
			}

		}
		return res;
	}

	@Override
	@Transactional
	public ApiResponse approveRejectBirthLegalData(Long slaDetailsId, String status, String remarks,
			HttpServletRequest request) throws Exception {

		ApiResponse res = new ApiResponse();
		Optional<SlaDetailsModel> exitedData = slaDetailsRepository.findById(slaDetailsId);
		JwtUtil jwtUtil = new JwtUtil();
		String userName = authService.getUserIdFromRequest(request);
		// UserModel currentUser = authRepository.findById(Long.parseLong(userName));
		SlaDetailsModel slaDetailsModel = exitedData.get();
		Optional<BirthModel> existedBirthData = birthRepository.findById(slaDetailsModel.getBndId());
		BirthModel birthModel = existedBirthData.get();

		logger.info("======= status is =====" + status + "==== Birth Model is" + birthModel);
		if (!exitedData.isPresent() && !existedBirthData.isPresent() && (!Constants.RECORD_STATUS_CORRECTION_PENDING.equalsIgnoreCase(birthModel.getStatus()))) {
			res.setStatus(HttpStatus.NOT_FOUND);
			res.setMsg(Constants.RECORD_NOT_FOUND);
			logger.error("==== data not found =====" + exitedData);

		} else {

			if (Constants.RECORD_STATUS_REJECTED.equalsIgnoreCase(status)) {
				logger.error(
						" =========== Before if in else SLA details Model in rejected ============ " + slaDetailsModel);
				slaDetailsModel.setSlaDetailsId(slaDetailsId);
				slaDetailsModel.setRejectedAt(LocalDateTime.now());
				slaDetailsModel.setRejectedBy(userName);
				slaDetailsModel.setRemarks(remarks);
				slaDetailsModel.setStatus(Constants.RECORD_STATUS_REJECTED);
				slaDetailsModel = slaDetailsRepository.save(slaDetailsModel);
				logger.error(" =========== SLA details Model in rejected ============ " + slaDetailsModel);

				if (slaDetailsModel != null) {
					SlaDetailsHistoryModel slaDetailsHistoryModel = new SlaDetailsHistoryModel();

					// Update data into birth table
					// birthModel.setBirthId(slaDetailsModel.getBndId());
					birthModel.setModifiedAt(LocalDateTime.now());
					birthModel.setModifiedBy(userName);
					birthModel.setCorrectionSlaId(slaDetailsModel.getSlaDetailsId());
					// birthModel.setRejectionRemark(remarks);
					birthModel.setStatus(Constants.RECORD_STATUS_CORRECTION_REJECTED);
					birthModel.setTransactionType(Constants.LEGAL_CORRECTIONS);
					birthModel = birthRepository.save(birthModel);

					// Insert new records in to birth_history table
					BirthHistoryModel birthHistoryModel = new BirthHistoryModel();
					BeanUtils.copyProperties(birthModel, birthHistoryModel);
					birthHistoryModel = birthHistoryRepository.save(birthHistoryModel);

					BeanUtils.copyProperties(slaDetailsModel, slaDetailsHistoryModel);
					slaDetailsHistoryModel = slaDetailsHistoryRepository.save(slaDetailsHistoryModel);
					res.setStatus(HttpStatus.OK);
					res.setMsg(Constants.RECORD_REJECTED_MESSAGE);
					res.setData(birthModel.getApplicationNumber());

					// ============ BLOCKCHAIN Response Handler Function =====================
					birthCorrectionBlcResponse(slaDetailsId, slaDetailsModel, slaDetailsHistoryModel, userName, remarks,
							birthModel, birthHistoryModel);

				}

			} else if (Constants.RECORD_STATUS_APPROVED.equalsIgnoreCase(status)) {
				logger.info(" ============ SLA details Model in Approved ========  " + slaDetailsModel);
				slaDetailsModel.setSlaDetailsId(slaDetailsId);
				slaDetailsModel.setApprovedAt(LocalDateTime.now());
				slaDetailsModel.setApprovedBy(userName);
				slaDetailsModel.setStatus(Constants.RECORD_STATUS_APPROVED);
				slaDetailsModel = slaDetailsRepository.save(slaDetailsModel);

				if (slaDetailsModel != null) {
					SlaDetailsHistoryModel slaDetailsHistoryModel = new SlaDetailsHistoryModel();
					slaDetailsHistoryModel.setRejectedAt(LocalDateTime.now());
					slaDetailsHistoryModel.setRejectedBy(userName);
					slaDetailsHistoryModel.setRemarks(remarks);
					slaDetailsHistoryModel.setStatus(Constants.RECORD_STATUS_APPROVED);
					BeanUtils.copyProperties(slaDetailsModel, slaDetailsHistoryModel);
					slaDetailsHistoryModel = slaDetailsHistoryRepository.save(slaDetailsHistoryModel);

					// Update data into birth table

					CustomBeanUtils.copySlaDetails(slaDetailsModel, birthModel);
					birthModel.setBirthId(slaDetailsModel.getBndId());
					birthModel.setModifiedAt(LocalDateTime.now());
					birthModel.setModifiedBy(userName);
					birthModel.setStatus(Constants.RECORD_STATUS_APPROVED);
					birthModel.setTransactionType(Constants.RECORD_APPROVED);
					birthModel = birthRepository.save(birthModel);

					// Insert new records in to birth_history table
					BirthHistoryModel birthHistoryModel = new BirthHistoryModel();

					BeanUtils.copyProperties(birthModel, birthHistoryModel);
					birthHistoryModel = birthHistoryRepository.save(birthHistoryModel);
					res.setStatus(HttpStatus.OK);
					res.setMsg(Constants.RECORD_APPROVED_MESSAGE);
					res.setData(birthModel.getApplicationNumber());

					birthCorrectionBlcResponse(slaDetailsId, slaDetailsModel, slaDetailsHistoryModel, userName, remarks,
							birthModel, birthHistoryModel);

				}

			} else {
				res.setStatus(HttpStatus.BAD_REQUEST);
				res.setMsg(Constants.CHILD_NAME_NOT_FOUND);
			}

		}
		return res;
	}

	@Override
	@Transactional
	public ApiResponse approveRejectDeathLegalData(Long slaDetailsId, String status, String remarks,
			HttpServletRequest request) throws Exception {

		ApiResponse res = new ApiResponse();
		Optional<SlaDetailsModel> exitedData = slaDetailsRepository.findById(slaDetailsId);
		JwtUtil jwtUtil = new JwtUtil();
		String userName = authService.getUserIdFromRequest(request);
		// UserModel currentUser = authRepository.findById(Long.parseLong(userName));
		SlaDetailsModel slaDetailsModel = exitedData.get();
		Optional<DeathModel> existedDeathData = deathRepository.findById(slaDetailsModel.getBndId());
		DeathModel deathModel = existedDeathData.get();

		logger.info("======= status is =====" + status + "==== Birth Model is" + deathModel);
		if (!exitedData.isPresent() && !existedDeathData.isPresent()) {
			res.setStatus(HttpStatus.NOT_FOUND);
			res.setMsg(Constants.RECORD_NOT_FOUND);
			logger.error("==== data not found =====" + exitedData);

		} else {

			if (Constants.RECORD_STATUS_REJECTED.equalsIgnoreCase(status)) {
				logger.error(
						" =========== Before if in else SLA details Model in rejected ============ " + slaDetailsModel);
				slaDetailsModel.setSlaDetailsId(slaDetailsId);
				slaDetailsModel.setRejectedAt(LocalDateTime.now());
				slaDetailsModel.setRejectedBy(userName);
				slaDetailsModel.setRemarks(remarks);
				slaDetailsModel.setStatus(Constants.RECORD_STATUS_REJECTED);
				slaDetailsModel = slaDetailsRepository.save(slaDetailsModel);
				logger.error(" =========== SLA details Model in rejected ============ " + slaDetailsModel);

				if (slaDetailsModel != null) {
					SlaDetailsHistoryModel slaDetailsHistoryModel = new SlaDetailsHistoryModel();

					// Update data into death table
					// birthModel.setBirthId(slaDetailsModel.getBndId());
					CustomBeanUtils.copySlaDetailsToDeathModel(slaDetailsModel, deathModel);
					deathModel.setModifiedAt(LocalDateTime.now());
					deathModel.setModifiedBy(userName);
					deathModel.setCorrectionSlaId(slaDetailsModel.getSlaDetailsId());
					// birthModel.setRejectionRemark(remarks);
					deathModel.setStatus(Constants.RECORD_STATUS_CORRECTION_REJECTED);
					deathModel.setTransactionType(Constants.LEGAL_CORRECTIONS);
					deathModel = deathRepository.save(deathModel);

					// Insert new records in to birth_history table
					DeathHistoryModel deathHistoryModel = new DeathHistoryModel();
					BeanUtils.copyProperties(deathModel, deathHistoryModel);
					deathHistoryModel = deathHistoryRepository.save(deathHistoryModel);

					BeanUtils.copyProperties(slaDetailsModel, slaDetailsHistoryModel);
					slaDetailsHistoryModel = slaDetailsHistoryRepository.save(slaDetailsHistoryModel);
					res.setStatus(HttpStatus.OK);
					res.setMsg(Constants.RECORD_REJECTED_MESSAGE);
					res.setData(deathModel.getApplicationNumber());
					// ============ BLOCKCHAIN Response Handler Function =====================
					deathCorrectionBlcResponse(slaDetailsId, slaDetailsModel, slaDetailsHistoryModel, userName, remarks,
							deathModel, deathHistoryModel);

				}

			} else if (Constants.RECORD_STATUS_APPROVED.equalsIgnoreCase(status)) {
				logger.info(" ============ SLA details Model in Approved ========  " + slaDetailsModel);
				slaDetailsModel.setSlaDetailsId(slaDetailsId);
				slaDetailsModel.setApprovedAt(LocalDateTime.now());
				slaDetailsModel.setApprovedBy(userName);
				slaDetailsModel.setStatus(Constants.RECORD_STATUS_APPROVED);
				slaDetailsModel = slaDetailsRepository.save(slaDetailsModel);

				if (slaDetailsModel != null) {
					SlaDetailsHistoryModel slaDetailsHistoryModel = new SlaDetailsHistoryModel();
					slaDetailsHistoryModel.setRejectedAt(LocalDateTime.now());
					slaDetailsHistoryModel.setRejectedBy(userName);
					slaDetailsHistoryModel.setRemarks(remarks);
					slaDetailsHistoryModel.setStatus(Constants.RECORD_STATUS_APPROVED);
					BeanUtils.copyProperties(slaDetailsModel, slaDetailsHistoryModel);
					slaDetailsHistoryModel = slaDetailsHistoryRepository.save(slaDetailsHistoryModel);

					// Update data into death table
					CustomBeanUtils.copySlaDetailsToDeathModel(slaDetailsModel, deathModel);
					deathModel.setDeathId(slaDetailsModel.getBndId());
					deathModel.setModifiedAt(LocalDateTime.now());
					deathModel.setModifiedBy(userName);
					deathModel.setStatus(Constants.RECORD_STATUS_APPROVED);
					deathModel = deathRepository.save(deathModel);

					// Insert new records in to birth_history table
					DeathHistoryModel deathHistoryModel = new DeathHistoryModel();
					deathModel.setTransactionType(Constants.RECORD_APPROVED);
					BeanUtils.copyProperties(deathModel, deathHistoryModel);
					deathHistoryModel = deathHistoryRepository.save(deathHistoryModel);
					res.setStatus(HttpStatus.OK);
					res.setMsg(Constants.RECORD_APPROVED_MESSAGE);
					res.setData(deathModel.getApplicationNumber());
					deathCorrectionBlcResponse(slaDetailsId, slaDetailsModel, slaDetailsHistoryModel, userName, remarks,
							deathModel, deathHistoryModel);

				}

			} else {
				res.setStatus(HttpStatus.BAD_REQUEST);
				// res.setMsg(Constants.CHILD_NAME_NOT_FOUND);
			}

		}
		return res;
	}

	@Override
	@Transactional
	public ApiResponse approveRejectStillBirthLegalData(Long slaDetailsId, String status, String remarks,
			HttpServletRequest request) throws Exception {

		ApiResponse res = new ApiResponse();
		Optional<SlaDetailsModel> exitedData = slaDetailsRepository.findById(slaDetailsId);
		JwtUtil jwtUtil = new JwtUtil();
		String userName = authService.getUserIdFromRequest(request);
		// UserModel currentUser = authRepository.findById(Long.parseLong(userName));
		SlaDetailsModel slaDetailsModel = exitedData.get();
		Optional<SBirthModel> existedSBirthData = sBirthRepository.findById(slaDetailsModel.getBndId());
		SBirthModel sBirthModel = existedSBirthData.get();

		logger.info("======= status is =====" + status + "==== Birth Model is" + sBirthModel);
		if (!exitedData.isPresent() && !existedSBirthData.isPresent()) {
			res.setStatus(HttpStatus.NOT_FOUND);
			res.setMsg(Constants.RECORD_NOT_FOUND);
			logger.error("==== data not found =====" + exitedData);

		} else {

			if (Constants.RECORD_STATUS_REJECTED.equalsIgnoreCase(status)) {
				logger.error(
						" =========== Before if in else SLA details Model in rejected ============ " + slaDetailsModel);
				slaDetailsModel.setSlaDetailsId(slaDetailsId);
				slaDetailsModel.setRejectedAt(LocalDateTime.now());
				slaDetailsModel.setRejectedBy(userName);
				slaDetailsModel.setRemarks(remarks);
				slaDetailsModel.setStatus(Constants.RECORD_STATUS_REJECTED);
				slaDetailsModel = slaDetailsRepository.save(slaDetailsModel);
				logger.error(" =========== SLA details Model in rejected ============ " + slaDetailsModel);

				if (slaDetailsModel != null) {
					SlaDetailsHistoryModel slaDetailsHistoryModel = new SlaDetailsHistoryModel();

					// Update data into birth table
					// birthModel.setBirthId(slaDetailsModel.getBndId());
					CustomBeanUtils.copySlaDetailsToSbirth(slaDetailsModel, sBirthModel);
					sBirthModel.setModifiedAt(LocalDateTime.now());
					sBirthModel.setModifiedBy(userName);
					sBirthModel.setCorrectionSlaId(slaDetailsModel.getSlaDetailsId());
					// birthModel.setRejectionRemark(remarks);
					sBirthModel.setStatus(Constants.RECORD_STATUS_CORRECTION_REJECTED);
					sBirthModel.setTransactionType(Constants.LEGAL_CORRECTIONS);
					sBirthModel = sBirthRepository.save(sBirthModel);

					// Insert new records in to birth_history table
					SBirthHistoryModel birthHistoryModel = new SBirthHistoryModel();
					BeanUtils.copyProperties(sBirthModel, birthHistoryModel);
					birthHistoryModel = sBirthHistoryRepository.save(birthHistoryModel);

					BeanUtils.copyProperties(slaDetailsModel, slaDetailsHistoryModel);
					slaDetailsHistoryModel = slaDetailsHistoryRepository.save(slaDetailsHistoryModel);
					res.setStatus(HttpStatus.OK);
					res.setMsg(Constants.RECORD_REJECTED_MESSAGE);
					res.setData(sBirthModel.getApplicationNumber());
					// ============ BLOCKCHAIN Response Handler Function =====================
					sBirthCorrectionBlcResponse(slaDetailsId, slaDetailsModel, slaDetailsHistoryModel, userName,
							remarks, sBirthModel, birthHistoryModel);

				}

			} else if (Constants.RECORD_STATUS_APPROVED.equalsIgnoreCase(status)) {
				logger.info(" ============ SLA details Model in Approved ========  " + slaDetailsModel);
				slaDetailsModel.setSlaDetailsId(slaDetailsId);
				slaDetailsModel.setApprovedAt(LocalDateTime.now());
				slaDetailsModel.setApprovedBy(userName);
				slaDetailsModel.setStatus(Constants.RECORD_STATUS_APPROVED);
				slaDetailsModel = slaDetailsRepository.save(slaDetailsModel);

				if (slaDetailsModel != null) {
					SlaDetailsHistoryModel slaDetailsHistoryModel = new SlaDetailsHistoryModel();
					slaDetailsHistoryModel.setRejectedAt(LocalDateTime.now());
					slaDetailsHistoryModel.setRejectedBy(userName);
					slaDetailsHistoryModel.setRemarks(remarks);
					slaDetailsHistoryModel.setStatus(Constants.RECORD_STATUS_APPROVED);
					BeanUtils.copyProperties(slaDetailsModel, slaDetailsHistoryModel);
					slaDetailsHistoryModel = slaDetailsHistoryRepository.save(slaDetailsHistoryModel);

					// Update data into birth table
					CustomBeanUtils.copySlaDetailsToSbirth(slaDetailsModel, sBirthModel);
					sBirthModel.setSbirthId(slaDetailsModel.getBndId());
					sBirthModel.setApprovedAt(LocalDateTime.now());
					sBirthModel.setApprovedBy(userName);
					sBirthModel.setStatus(Constants.RECORD_STATUS_APPROVED);
					sBirthModel = sBirthRepository.save(sBirthModel);

					// Insert new records in to birth_history table
					SBirthHistoryModel birthHistoryModel = new SBirthHistoryModel();
					sBirthModel.setTransactionType(Constants.RECORD_APPROVED);
					BeanUtils.copyProperties(sBirthModel, birthHistoryModel);
					birthHistoryModel = sBirthHistoryRepository.save(birthHistoryModel);
					res.setStatus(HttpStatus.OK);
					res.setMsg(Constants.RECORD_APPROVED_MESSAGE);
					res.setData(sBirthModel.getApplicationNumber());
					sBirthCorrectionBlcResponse(slaDetailsId, slaDetailsModel, slaDetailsHistoryModel, userName,
							remarks, sBirthModel, birthHistoryModel);

				}

			} else {
				res.setStatus(HttpStatus.BAD_REQUEST);
				// res.setMsg(Constants.CHILD_NAME_NOT_FOUND);
			}

		}
		return res;
	}

	// =================== Blockchain response handling section start
	// ========================================
	// Name Inclusion Approval Response

	private void inclusionBlcResponse(Long slaDetailsId, SlaDetailsModel slaDetailsModel,
			SlaDetailsHistoryModel slaDetailsHistoryModel, String userName, String remarks, BirthModel birthModel,
			BirthHistoryModel birthHistoryModel) throws Exception {

		// try {
		BlockchainSlaDetailsResponse blockchainResult1 = blockchainGatway.updateSlaRecord(slaDetailsModel,
					channelGovtHospital);
			logger.info("After data addition in Blockchain ==== " + LocalDateTime.now());
			// JsonUtil.getObjectFromJson(blockchainResult, BlockchainGatway.class);

			// Set Blockchain response
			String message1 = blockchainResult1.getMessage();
			String txID1 = blockchainResult1.getTxID();
			String blcStatus1 = blockchainResult1.getStatus();
			if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(blcStatus1)) {
				// ======= SLA BLC ENTRY START ========
				slaDetailsModel.setBlcMessage(message1);
				slaDetailsModel.setBlcTxId(txID1);
				slaDetailsModel.setBlcStatus(blcStatus1);

				slaDetailsHistoryModel.setBlcMessage(message1);
				slaDetailsHistoryModel.setBlcTxId(txID1);
				slaDetailsHistoryModel.setBlcStatus(blcStatus1);

				// ============ SLA BLC ENTRY END ==========

				// if(slaDetailsModel.getStatus().equalsIgnoreCase("true")) {
				// 1. try catch
				// Update birth model
				// message = set
				// transationId set
				// }

			// } catch (Exception e) {
			// 	logger.error("===Blc Exception ===", e);
			// 	String message = e.getMessage();
			// 	String blcStatus = Constants.BLC_STATUS_FALSE;
			// 	message = message.length() > 1000 ? message.substring(0, 1000) : message;
			// 	slaDetailsModel.setBlcMessage(message);
			// 	slaDetailsModel.setBlcStatus(blcStatus);

			// 	slaDetailsHistoryModel.setBlcMessage(message);
			// 	slaDetailsHistoryModel.setBlcStatus(blcStatus);
			// }

			// ======= BLC Response updation in Main table =====================
			// try {
				BlockchainUpdateBirthResponse blockchainResult = blockchainGatway.updateBirthRecord(birthModel,
						channelGovtHospital);
				logger.info("After data addition in Blockchain ==== " + LocalDateTime.now());
				// Set Blockchain response
				String message = blockchainResult.getMessage();
				String txID = blockchainResult.getTxID();
				String blcStatus = blockchainResult.getStatus();
				if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(blcStatus)) {

					// ======= BLC Response updation in Main table Start========

					birthModel.setBlcMessage(message);
					birthModel.setBlcTxId(txID);
					birthModel.setBlcStatus(blcStatus);

					birthHistoryModel.setBlcMessage(message);
					birthHistoryModel.setBlcTxId(txID);
					birthHistoryModel.setBlcStatus(blcStatus);

					// ======= BLC Response updation in Main table Start========
				// } catch (Exception e) {
				// 	logger.error("===Blc Exception ===", e);
				// 	String message = e.getMessage();
				// 	String blcStatus = Constants.BLC_STATUS_FALSE;
				// 	message = message.length() > 1000 ? message.substring(0, 1000) : message;
				// 	birthModel.setBlcMessage(message);
				// 	birthModel.setBlcStatus(blcStatus);

				// 	birthHistoryModel.setBlcMessage(message);
				// 	birthHistoryModel.setBlcStatus(blcStatus);
				// }
				slaDetailsModel = slaDetailsRepository.save(slaDetailsModel);
				slaDetailsHistoryModel = slaDetailsHistoryRepository.save(slaDetailsHistoryModel);

				birthModel = birthRepository.save(birthModel);
				birthHistoryModel = birthHistoryRepository.save(birthHistoryModel);
			} else {
				
				// logger.info("inclusionBlcResponse False response from blockchain " + blockchainResult);
				throw new Exception(Constants.INTERNAL_SERVER_ERROR);
			}
		} else {

			// logger.info("inclusionBlcResponse False response from blockchain 1 " + blockchainResult1);
			throw new Exception(Constants.INTERNAL_SERVER_ERROR);
		}
	}

	private void birthCorrectionBlcResponse(Long slaDetailsId, SlaDetailsModel slaDetailsModel,
									  SlaDetailsHistoryModel slaDetailsHistoryModel, String userName, String remarks, BirthModel birthModel,
									  BirthHistoryModel birthHistoryModel) throws Exception {

		// try {
		BlockchainSlaDetailsResponse blockchainResult1 = blockchainGatway.updateSlaRecord(slaDetailsModel,
					channelGovtHospital);
			logger.info("After data addition in Blockchain ==== " + LocalDateTime.now());
			// JsonUtil.getObjectFromJson(blockchainResult, BlockchainGatway.class);

			// Set Blockchain response
			String message1 = blockchainResult1.getMessage();
			String txID1 = blockchainResult1.getTxID();
			String blcStatus1 = blockchainResult1.getStatus();
			if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(blcStatus1)) {
				// ======= SLA BLC ENTRY START ========
				slaDetailsModel.setBlcMessage(message1);
				slaDetailsModel.setBlcTxId(txID1);
				slaDetailsModel.setBlcStatus(blcStatus1);

				slaDetailsHistoryModel.setBlcMessage(message1);
				slaDetailsHistoryModel.setBlcTxId(txID1);
				slaDetailsHistoryModel.setBlcStatus(blcStatus1);

				// ============ SLA BLC ENTRY END ==========

				// if(slaDetailsModel.getStatus().equalsIgnoreCase("true")) {
				// 1. try catch
				// Update birth model
				// message = set
				// transationId set
				// }

			// } catch (Exception e) {
			// 	logger.error("===Blc Exception ===", e);
			// 	String message = e.getMessage();
			// 	String blcStatus = Constants.BLC_STATUS_FALSE;
			// 	message = message.length() > 1000 ? message.substring(0, 1000) : message;
			// 	slaDetailsModel.setBlcMessage(message);
			// 	slaDetailsModel.setBlcStatus(blcStatus);

			// 	slaDetailsHistoryModel.setBlcMessage(message);
			// 	slaDetailsHistoryModel.setBlcStatus(blcStatus);
			// }

			// ======= BLC Response updation in Main table =====================
			// try {
				BlockchainUpdateBirthResponse blockchainResult = blockchainGatway.updateBirthRecord(birthModel,
						channelGovtHospital);
				logger.info("After data addition in Blockchain ==== " + LocalDateTime.now());
				// Set Blockchain response
				String message = blockchainResult.getMessage();
				String txID = blockchainResult.getTxID();
				String blcStatus = blockchainResult.getStatus();
				if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(blcStatus)) {

					// ======= BLC Response updation in Main table Start========

					birthModel.setBlcMessage(message);
					birthModel.setBlcTxId(txID);
					birthModel.setBlcStatus(blcStatus);

					birthHistoryModel.setBlcMessage(message);
					birthHistoryModel.setBlcTxId(txID);
					birthHistoryModel.setBlcStatus(blcStatus);

					// ======= BLC Response updation in Main table Start========
				// } catch (Exception e) {
				// 	logger.error("===Blc Exception ===", e);
				// 	String message = e.getMessage();
				// 	String blcStatus = Constants.BLC_STATUS_FALSE;
				// 	message = message.length() > 1000 ? message.substring(0, 1000) : message;
				// 	birthModel.setBlcMessage(message);
				// 	birthModel.setBlcStatus(blcStatus);

				// 	birthHistoryModel.setBlcMessage(message);
				// 	birthHistoryModel.setBlcStatus(blcStatus);
				// }
				slaDetailsModel = slaDetailsRepository.save(slaDetailsModel);
				slaDetailsHistoryModel = slaDetailsHistoryRepository.save(slaDetailsHistoryModel);

				birthModel = birthRepository.save(birthModel);
				birthHistoryModel = birthHistoryRepository.save(birthHistoryModel);
			} else {
					
				// logger.info("birthCorrectionBlcResponse False response from blockchain " + blockchainResult);
				throw new Exception(Constants.INTERNAL_SERVER_ERROR);
			}
		} else {

			// logger.info("birthCorrectionBlcResponse False response from blockchain 1 " + blockchainResult1);
			throw new Exception(Constants.INTERNAL_SERVER_ERROR);
		}
	}

	private void deathCorrectionBlcResponse(Long slaDetailsId, SlaDetailsModel slaDetailsModel,
			SlaDetailsHistoryModel slaDetailsHistoryModel, String userName, String remarks,
			DeathModel deathModel, DeathHistoryModel deathHistoryModel) throws Exception {

		// try {
		BlockchainSlaDetailsResponse blockchainResult1 = blockchainGatway.updateSlaRecord(slaDetailsModel,
					channelGovtHospital);
			logger.info("After data addition in Blockchain ==== " + LocalDateTime.now());
			// JsonUtil.getObjectFromJson(blockchainResult, BlockchainGatway.class);

			// Set Blockchain response
			String message1 = blockchainResult1.getMessage();
			String txID1 = blockchainResult1.getTxID();
			String blcStatus1 = blockchainResult1.getStatus();
			if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(blcStatus1)) {
				// ======= SLA BLC ENTRY START ========
				slaDetailsModel.setBlcMessage(message1);
				slaDetailsModel.setBlcTxId(txID1);
				slaDetailsModel.setBlcStatus(blcStatus1);

				slaDetailsHistoryModel.setBlcMessage(message1);
				slaDetailsHistoryModel.setBlcTxId(txID1);
				slaDetailsHistoryModel.setBlcStatus(blcStatus1);

				// ============ SLA BLC ENTRY END ==========

				// if(slaDetailsModel.getStatus().equalsIgnoreCase("true")) {
				// 1. try catch
				// Update birth model
				// message = set
				// transationId set
				// }

			// } catch (Exception e) {
			// 	logger.error("===Blc Exception ===", e);
			// 	String message = e.getMessage();
			// 	String blcStatus = Constants.BLC_STATUS_FALSE;
			// 	message = message.length() > 1000 ? message.substring(0, 1000) : message;
			// 	slaDetailsModel.setBlcMessage(message);
			// 	slaDetailsModel.setBlcStatus(blcStatus);

			// 	slaDetailsHistoryModel.setBlcMessage(message);
			// 	slaDetailsHistoryModel.setBlcStatus(blcStatus);
			// }

			// ======= BLC Response updation in Main table =====================
			// try {
				BlockchainUpdateDeathResponse blockchainResult = blockchainGatway.updateDeathRecord(deathModel,
						channelGovtHospital);
				logger.info("After data addition in Blockchain ==== " + LocalDateTime.now());
				// Set Blockchain response
				String message = blockchainResult.getMessage();
				String txID = blockchainResult.getTxID();
				String blcStatus = blockchainResult.getStatus();
				if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(blcStatus)) {

					// ======= BLC Response updation in Main table Start========

					deathModel.setBlcMessage(message);
					deathModel.setBlcTxId(txID);
					deathModel.setBlcStatus(blcStatus);

					deathHistoryModel.setBlcMessage(message);
					deathHistoryModel.setBlcTxId(txID);
					deathHistoryModel.setBlcStatus(blcStatus);

				// 	// ======= BLC Response updation in Main table Start========
				// } catch (Exception e) {
				// 	logger.error("===Blc Exception ===", e);
				// 	String message = e.getMessage();
				// 	String blcStatus = Constants.BLC_STATUS_FALSE;
				// 	message = message.length() > 1000 ? message.substring(0, 1000) : message;
				// 	deathModel.setBlcMessage(message);
				// 	deathModel.setBlcStatus(blcStatus);

				// 	deathHistoryModel.setBlcMessage(message);
				// 	deathHistoryModel.setBlcStatus(blcStatus);
				// }
				slaDetailsModel = slaDetailsRepository.save(slaDetailsModel);
				slaDetailsHistoryModel = slaDetailsHistoryRepository.save(slaDetailsHistoryModel);

				deathModel = deathRepository.save(deathModel);
				deathHistoryModel = deathHistoryRepository.save(deathHistoryModel);
			} else {
					
				// logger.info("deathCorrectionBlcResponse False response from blockchain " + blockchainResult);
				throw new Exception(Constants.INTERNAL_SERVER_ERROR);
			}
		} else {

			// logger.info("deathCorrectionBlcResponse False response from blockchain 1 " + blockchainResult1);
			throw new Exception(Constants.INTERNAL_SERVER_ERROR);
		}
	}

	private void sBirthCorrectionBlcResponse(Long slaDetailsId, SlaDetailsModel slaDetailsModel,
			SlaDetailsHistoryModel slaDetailsHistoryModel, String userName, String remarks, SBirthModel birthModel,
			SBirthHistoryModel birthHistoryModel) throws Exception {

		// try {
		BlockchainSlaDetailsResponse blockchainResult1 = blockchainGatway.updateSlaRecord(slaDetailsModel,
					channelGovtHospital);
			logger.info("After data addition in Blockchain ==== " + LocalDateTime.now());
			// JsonUtil.getObjectFromJson(blockchainResult, BlockchainGatway.class);

			// Set Blockchain response
			String message1 = blockchainResult1.getMessage();
			String txID1 = blockchainResult1.getTxID();
			String blcStatus1 = blockchainResult1.getStatus();
			if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(blcStatus1)) {
				// ======= SLA BLC ENTRY START ========
				slaDetailsModel.setBlcMessage(message1);
				slaDetailsModel.setBlcTxId(txID1);
				slaDetailsModel.setBlcStatus(blcStatus1);

				slaDetailsHistoryModel.setBlcMessage(message1);
				slaDetailsHistoryModel.setBlcTxId(txID1);
				slaDetailsHistoryModel.setBlcStatus(blcStatus1);

				// ============ SLA BLC ENTRY END ==========

				// if(slaDetailsModel.getStatus().equalsIgnoreCase("true")) {
				// 1. try catch
				// Update birth model
				// message = set
				// transationId set
				// }

			// } catch (Exception e) {
			// 	logger.error("===Blc Exception ===", e);
			// 	String message = e.getMessage();
			// 	String blcStatus = Constants.BLC_STATUS_FALSE;
			// 	message = message.length() > 1000 ? message.substring(0, 1000) : message;
			// 	slaDetailsModel.setBlcMessage(message);
			// 	slaDetailsModel.setBlcStatus(blcStatus);

			// 	slaDetailsHistoryModel.setBlcMessage(message);
			// 	slaDetailsHistoryModel.setBlcStatus(blcStatus);
			// }

			// ======= BLC Response updation in Main table =====================
			// try {
				BlockchainUpdateSBirthResponse blockchainResult = blockchainGatway.updateStillBirthRecord(birthModel,
						channelGovtHospital);
				logger.info("After data addition in Blockchain ==== " + LocalDateTime.now());
				// Set Blockchain response
				String message = blockchainResult.getMessage();
				String txID = blockchainResult.getTxID();
				String blcStatus = blockchainResult.getStatus();
				if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(blcStatus)) {

					// ======= BLC Response updation in Main table Start========

					birthModel.setBlcMessage(message);
					birthModel.setBlcTxId(txID);
					birthModel.setBlcStatus(blcStatus);

					birthHistoryModel.setBlcMessage(message);
					birthHistoryModel.setBlcTxId(txID);
					birthHistoryModel.setBlcStatus(blcStatus);

					// ======= BLC Response updation in Main table Start========
				// } catch (Exception e) {
				// 	logger.error("===Blc Exception ===", e);
				// 	String message = e.getMessage();
				// 	String blcStatus = Constants.BLC_STATUS_FALSE;
				// 	message = message.length() > 1000 ? message.substring(0, 1000) : message;
				// 	birthModel.setBlcMessage(message);
				// 	birthModel.setBlcStatus(blcStatus);

				// 	birthHistoryModel.setBlcMessage(message);
				// 	birthHistoryModel.setBlcStatus(blcStatus);
				// }
				slaDetailsModel = slaDetailsRepository.save(slaDetailsModel);
				slaDetailsHistoryModel = slaDetailsHistoryRepository.save(slaDetailsHistoryModel);

				birthModel = sBirthRepository.save(birthModel);
				birthHistoryModel = sBirthHistoryRepository.save(birthHistoryModel);
			} else {
					
				// logger.info("stillBirthCorrectionBlcResponse False response from blockchain " + blockchainResult);
				throw new Exception(Constants.INTERNAL_SERVER_ERROR);
			}
		} else {

			// logger.info("stillBirthCorrectionBlcResponse False response from blockchain 1 " + blockchainResult1);
			throw new Exception(Constants.INTERNAL_SERVER_ERROR);
		}
	}

	public List<SlaDetailsModel> findByStatus(String status) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Long> recordIdsByStatus(String recordType, String transactionType, String status) {
		List<SlaDetailsModel> slaDetailsModels = slaDetailsRepository.findByCertificateTypeAndTransactionTypeAndStatus(
				recordType, CommonUtil.getTransactionType(recordType, transactionType), status);
		if (slaDetailsModels == null) {
			slaDetailsModels = new ArrayList<>();
		}
		if (status.equalsIgnoreCase(Constants.RECORD_STATUS_PENDING)) {
			List<SlaDetailsModel> uploadingList = slaDetailsRepository.findByCertificateTypeAndStatus(recordType,
					Constants.RECORD_STATUS_UPLOADING);
			if (uploadingList != null) {
				slaDetailsModels.addAll(uploadingList);
			}
		}
		return slaDetailsModels.stream().map(SlaDetailsModel::getBndId).collect(Collectors.toList());
	}

	@Override
	public List<Long> recordIdsByStatus(String recordTypeDeath, String status) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Long> findSlaOrganizationId(Long organizationId) {
		return slaDetailsRepository.findSlaOrganizationId(organizationId);
	}
	@Override
	public ApiResponse getDetails(Long bndId, String transactionType, String recordType, HttpServletRequest request) {
		ApiResponse apiResponse = new ApiResponse();
		Map<String, Object> data = new HashMap<>();
		logger.info("====== Long bndId, String transactionType, String recordType, HttpServletRequest request===bndId="+bndId +"===transactionType="+ transactionType +"===recordType="+ recordType +"===="+ request);
		Optional<SlaDetailsModel> slaDetails = findByBndIdAndRecordType(bndId, transactionType, recordType);
		logger.info("===SLADETAILS IS ==="+slaDetails);
		if (slaDetails.isPresent()) {
			SlaDetailsModel slaDetailsModel = slaDetails.get();
			data.put("slaDetails", slaDetailsModel);
			if (Constants.RECORD_TYPE_BIRTH.equals(slaDetailsModel.getCertificateType())) {
				data.put("oldData", birthService.findById(slaDetailsModel.getBndId()));
			} else if (Constants.RECORD_TYPE_DEATH.equals(slaDetailsModel.getCertificateType())) {
				data.put("oldData", deathService.findById(slaDetailsModel.getBndId()));
			} else if (Constants.RECORD_TYPE_SBIRTH.equals(slaDetailsModel.getCertificateType())) {
				data.put("oldData", sBirthService.findById(slaDetailsModel.getBndId()));
			}
			data.put("attachments", attachmentService.findBySlaDetailsId(slaDetailsModel.getSlaDetailsId()));
			apiResponse.setData(data);
			apiResponse.setStatus(HttpStatus.OK);
		} else {
			apiResponse.setMsg(Constants.RECORD_NOT_FOUND);
			apiResponse.setStatus(HttpStatus.NOT_FOUND);
		}

		return apiResponse;
	}


	@Override
	public ApiResponse getOnlineSlaDetails(Long bndId, String transactionType, String recordType, HttpServletRequest request) {
		ApiResponse apiResponse = new ApiResponse();
		Map<String, Object> data = new HashMap<>();
		logger.info("====== Long bndId, String transactionType, String recordType, HttpServletRequest request===bndId="+bndId +"===transactionType="+ transactionType +"===recordType="+ recordType +"===="+ request);
		Optional<SlaDetailsModel> slaDetails = filterByBndIdAndRecordType(bndId, transactionType, recordType);
		logger.info("===SLADETAILS IS ==="+slaDetails);
		if (slaDetails.isPresent()) {
			SlaDetailsModel slaDetailsModel = slaDetails.get();
			data.put("slaDetails", slaDetailsModel);
			if (Constants.RECORD_TYPE_BIRTH.equals(slaDetailsModel.getCertificateType())) {
				data.put("oldData", birthService.findById(slaDetailsModel.getBndId()));
			} else if (Constants.RECORD_TYPE_DEATH.equals(slaDetailsModel.getCertificateType())) {
				data.put("oldData", deathService.findById(slaDetailsModel.getBndId()));
			} else if (Constants.RECORD_TYPE_SBIRTH.equals(slaDetailsModel.getCertificateType())) {
				data.put("oldData", sBirthService.findById(slaDetailsModel.getBndId()));
			}
			data.put("attachments", attachmentService.findBySlaDetailsId(slaDetailsModel.getSlaDetailsId()));
			apiResponse.setData(data);
			apiResponse.setStatus(HttpStatus.OK);
		} else {
			apiResponse.setMsg(Constants.RECORD_NOT_FOUND);
			apiResponse.setStatus(HttpStatus.NOT_FOUND);
		}

		return apiResponse;
	}


	@Override
	@Transactional
	public ApiResponse savePrintRequest(PrintRequestDto printRequestDto, HttpServletRequest request) throws Exception {
		// Check any request is Approved in slaDetails table
		logger.info("Call savePrintRequest ==== ");
		ApiResponse apiResponse = new ApiResponse();
		Long bndId = printRequestDto.getBndId();
		String transactionType = printRequestDto.getTransactionType();
		String certificateType = printRequestDto.getRecordType();

		/*String applNo= printRequestDto.getApplNo();
		String status = Constants.RECORD_STATUS_APPROVED;
		SlaDetailsModel slaDetails = slaDetailsRepository.getApprovedRequest(bndId,applNo,certificateType, status);
		if(slaDetails == null){
			apiResponse.setMsg(Constants.RECORD_NOT_FOUND);
			apiResponse.setStatus(HttpStatus.NOT_FOUND);
			return apiResponse;
		}*/

		LocalDateTime now = LocalDateTime.now();
		SlaDetailsModel slaDetailsModel = new SlaDetailsModel();
		String status = Constants.RECORD_STATUS_PENDING;
		JwtUtil jwtUtil = new JwtUtil();
		//String username = "admin";
		String username = authService.getUserIdFromRequest(request);
		Optional<SlaDetailsModel> slaDetails = slaDetailsRepository
				.findByBndIdAndTransactionTypeAndCertificateTypeAndStatusAndUserId(bndId, transactionType, certificateType,
						status,username);

		if(slaDetails.isPresent())
			slaDetailsModel =slaDetails.get();
		Optional<LateFee> existedFeeData = null;
		if(Constants.RECORD_TYPE_BIRTH.equals(certificateType)){
			Optional<BirthModel> getBirthModel= birthRepository.findById(bndId);
			if(!getBirthModel.isPresent()){
				apiResponse.setMsg(Constants.RECORD_NOT_FOUND);
				apiResponse.setStatus(HttpStatus.BAD_REQUEST);
				return apiResponse;
			}
			BirthModel birthModel = getBirthModel.get();
			printRequestDto.setDivisionCode(birthModel.getDivisionCode());
			printRequestDto.setOrganizationCode(birthModel.getOrganizationCode());
			printRequestDto.setGenderCode(birthModel.getGenderCode());
			printRequestDto.setFatherName(birthModel.getFatherName());
			printRequestDto.setMotherName(birthModel.getMotherName());
			printRequestDto.setApplNo(birthModel.getApplicationNumber());
			slaDetailsModel.setApplNo(birthModel.getApplicationNumber());
			printRequestDto.setRegistrationNumber(birthModel.getRegistrationNumber());
			printRequestDto.setDateOfEvent(birthModel.getEventDate());
			slaDetailsModel.setDateOfEvent(birthModel.getEventDate());
			slaDetailsModel.setFatherAdharNumber(birthModel.getFatherAdharNumber());
			slaDetailsModel.setMotherAdharNumber(birthModel.getFatherAdharNumber());

			printRequestDto.setApplicationDate(birthModel.getRegistrationDatetime());
			existedFeeData = lateFeeRepository.findById(Constants.CFC_PRINT_B);
		}
		else if(Constants.RECORD_TYPE_DEATH.equals(certificateType)){
			Optional<DeathModel> getDeathModel= deathRepository.findById(bndId);
			if(!getDeathModel.isPresent()){
				apiResponse.setMsg(Constants.RECORD_NOT_FOUND+ " id "+ bndId);
				apiResponse.setStatus(HttpStatus.BAD_REQUEST);
				return apiResponse;
			}
			DeathModel deathModel = getDeathModel.get();
			printRequestDto.setDivisionCode(deathModel.getDivisionCode());
			printRequestDto.setOrganizationCode(deathModel.getOrganizationCode());
			printRequestDto.setGenderCode(deathModel.getGenderCode());
			printRequestDto.setFatherName(deathModel.getFatherName());
			printRequestDto.setMotherName(deathModel.getMotherName());
			printRequestDto.setApplNo(deathModel.getApplicationNumber());
			slaDetailsModel.setApplNo(deathModel.getApplicationNumber());
			printRequestDto.setRegistrationNumber(deathModel.getRegistrationNumber());
			printRequestDto.setDateOfEvent(deathModel.getEventDate());
			slaDetailsModel.setDateOfEvent(deathModel.getEventDate());
			slaDetailsModel.setFatherAdharNumber(deathModel.getFatherAdharNumber());
			slaDetailsModel.setMotherAdharNumber(deathModel.getFatherAdharNumber());

			printRequestDto.setApplicationDate(deathModel.getRegistrationDatetime());
			existedFeeData = lateFeeRepository.findById(Constants.CFC_PRINT_D);
		}
		else if(Constants.RECORD_TYPE_STILL_BIRTH.equals(certificateType)
		|| Constants.RECORD_TYPE_SBIRTH.equals(certificateType)){
			Optional<SBirthModel> getSBirthModel= sBirthRepository.findById(bndId);
			if(!getSBirthModel.isPresent()){
				apiResponse.setMsg(Constants.RECORD_NOT_FOUND + " id "+ bndId);
				apiResponse.setStatus(HttpStatus.BAD_REQUEST);
				return apiResponse;
			}
			SBirthModel sBirthModel = getSBirthModel.get();
			printRequestDto.setDivisionCode(sBirthModel.getDivisionCode());
			printRequestDto.setOrganizationCode(sBirthModel.getOrganizationCode());
			printRequestDto.setGenderCode(sBirthModel.getGenderCode());
			printRequestDto.setFatherName(sBirthModel.getFatherName());
			printRequestDto.setMotherName(sBirthModel.getMotherName());
			printRequestDto.setApplNo(sBirthModel.getApplicationNumber());
			slaDetailsModel.setApplNo(sBirthModel.getApplicationNumber());
			printRequestDto.setRegistrationNumber(sBirthModel.getRegistrationNumber());
			printRequestDto.setDateOfEvent(sBirthModel.getEventDate());
			slaDetailsModel.setDateOfEvent(sBirthModel.getEventDate());
			slaDetailsModel.setFatherAdharNumber(sBirthModel.getFatherAdharNumber());
			slaDetailsModel.setMotherAdharNumber(sBirthModel.getFatherAdharNumber());

			printRequestDto.setApplicationDate(sBirthModel.getRegistrationDatetime());
			existedFeeData = lateFeeRepository.findById(Constants.CFC_PRINT_S);
		}else{
			apiResponse.setMsg(Constants.NOT_PERMITTED);
			apiResponse.setStatus(HttpStatus.BAD_REQUEST);
			return apiResponse;
		}
		BeanUtils.copyProperties(printRequestDto, slaDetailsModel);

		slaDetailsModel.setStatus(Constants.RECORD_STATUS_PENDING);
		slaDetailsModel.setUserId(username);
		slaDetailsModel.setBndId(bndId);
		slaDetailsModel.setTransactionType(transactionType);
		slaDetailsModel.setCertificateType(certificateType);
		slaDetailsModel.setCreatedAt(now);

		slaDetailsModel.setDateOfEvent(printRequestDto.getDateOfEvent());
		slaDetailsModel.setApplDate(now);
		slaDetailsModel.setDueDate(printRequestDto.getDueDate());
		Long organizationId = authService.getOrganizationIdFromUserId(username);
		slaDetailsModel.setSlaOrganizationId(organizationId);
		//slaDetailsModel

		LateFee fee ;
		if(existedFeeData.isPresent()){
			fee = existedFeeData.get();
			slaDetailsModel.setFee(fee.getFee()*printRequestDto.getNoOfCopies());
			printRequestDto.setFee(slaDetailsModel.getFee());
			slaDetailsModel.setAmount(fee.getFee()*printRequestDto.getNoOfCopies());
		}else{
			printRequestDto.setFee(0.0F);
			slaDetailsModel.setFee(0.0F);
			slaDetailsModel.setAmount(0.0F);
		}

		if(Constants.INDIVIDUAL.equalsIgnoreCase(printRequestDto.getUseFor())){
			//	currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_CREATOR))
			if(CommonUtil.checkNullOrBlank(printRequestDto.getRemarks())){
				throw new RuntimeException("Invalid Request Check your request ");
			}
			slaDetailsModel.setRemarks(printRequestDto.getRemarks());
			printRequestDto.setFee(0.0F);
			slaDetailsModel.setFee(0.0F);
			slaDetailsModel.setAmount(0.0F);
		}


		// try {
		BlockchainSlaDetailsResponse blockchainResult =  null;
		if(slaDetailsModel.getSlaDetailsId() != null) {
			printRequestDto.setReceiptNumber(slaDetailsModel.getApplNo().split("/")[0].toString()+""+slaDetailsModel.getSlaDetailsId().toString()+"/"+LocalDate.now().getYear());
			slaDetailsModel.setReceiptNumber(printRequestDto.getReceiptNumber());
			blockchainResult = blockchainGatway.updateSlaRecord(slaDetailsModel, channelGovtHospital);
		}
		else {
			slaDetailsRepository.save(slaDetailsModel);
			printRequestDto.setReceiptNumber(slaDetailsModel.getApplNo().split("/")[0].toString()+""+slaDetailsModel.getSlaDetailsId().toString()+"/"+LocalDate.now().getYear());
			slaDetailsModel.setReceiptNumber(printRequestDto.getReceiptNumber());
			blockchainResult = blockchainGatway.insertSlaDetails(slaDetailsModel, channelGovtHospital);
		}

			logger.info("After data addition in Blockchain ==== " + now);
			// Set Blockchain response
			String message = blockchainResult.getMessage();
			String txID = blockchainResult.getTxID();
			String statusBlk = blockchainResult.getStatus();
			if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(statusBlk)) {
				slaDetailsModel.setBlcMessage(message);
				slaDetailsModel.setBlcTxId(txID);
				slaDetailsModel.setBlcStatus(statusBlk);
				SlaDetailsHistoryModel slaDetailsHistoryModel = new SlaDetailsHistoryModel();
				BeanUtils.copyProperties(slaDetailsModel, slaDetailsHistoryModel);
				slaDetailsHistoryModel = slaDetailsHistoryRepository.save(slaDetailsHistoryModel);

			// } catch (Exception e) {
			// 	logger.error("===Blc Exception ===", e);
			// 	String message = e.getMessage();
			// 	String statusBlk = Constants.BLC_STATUS_FALSE;
			// 	message = CommonUtil.updateExceptionMessage(message);
			// 	slaDetailsModel.setBlcMessage(message);
			// 	slaDetailsModel.setBlcStatus(statusBlk);

			// }
			slaDetailsRepository.save(slaDetailsModel);
			apiResponse.setMsg(Constants.PRINT_REQUEST_SUCCESS_MESSAGE);
			apiResponse.setStatus(HttpStatus.OK);
			printRequestDto.setUniqueId(slaDetailsModel.getSlaDetailsId());
			apiResponse.setData(printRequestDto);
		} else {
						
			// logger.info("savePrintRequest False response from blockchain " + blockchainResult);
			throw new Exception(Constants.INTERNAL_SERVER_ERROR);
		}

		return apiResponse;
	}

	@Override
	@Transactional
	public ApiResponse updatePrintRequest(Long slaId, PrintRequestDto printRequestDto, HttpServletRequest request) throws Exception {
		ApiResponse apiResponse = new ApiResponse();
		LocalDateTime now = LocalDateTime.now();

		Long bndId= printRequestDto.getBndId();
		String transactionType= printRequestDto.getTransactionType();
		String recordType=printRequestDto.getRecordType();
		String applNo= printRequestDto.getApplNo();

		Optional<SlaDetailsModel> slaDetails = slaDetailsRepository
				.findBySlaDetailsIdAndBndIdAndTransactionTypeAndRecordTypeAndApplNo(slaId,bndId,transactionType,recordType,applNo);

		if (slaDetails.isPresent()) {
			SlaDetailsModel slaDetailsModel = slaDetails.get();
			if(Constants.RECORD_STATUS_APPROVED.equalsIgnoreCase(slaDetailsModel.getStatus())){
				apiResponse.setStatus(HttpStatus.BAD_REQUEST);
				apiResponse.setMsg(Constants.RECORD_ALREADY_UPDATED);
				apiResponse.setData(slaId);
				return apiResponse;
			}
			slaDetailsModel.setStatus(Constants.RECORD_STATUS_APPROVED);
			String username = authService.getUserIdFromRequest(request);
			slaDetailsModel.setUpdatedBy(username);
			slaDetailsModel.setUpdatedAt(now);
			if(Constants.RECORD_TYPE_ONLINE_NAME_INCLUSION.equalsIgnoreCase(transactionType))
				nameInclusionCorrection(slaId,printRequestDto,request);

			// try {
			BlockchainSlaDetailsResponse blockchainResult = blockchainGatway.updateSlaRecord(slaDetailsModel,
						channelGovtHospital);
				logger.info("After data addition in Blockchain ==== " + now);
				// Set Blockchain response
				String message = blockchainResult.getMessage();
				String txID = blockchainResult.getTxID();
				String statusBlk = blockchainResult.getStatus();
				if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(statusBlk)) {
					slaDetailsModel.setBlcMessage(message);
					slaDetailsModel.setBlcTxId(txID);
					slaDetailsModel.setBlcStatus(statusBlk);
					SlaDetailsHistoryModel slaDetailsHistoryModel = new SlaDetailsHistoryModel();
					BeanUtils.copyProperties(slaDetailsModel, slaDetailsHistoryModel);
					slaDetailsHistoryModel = slaDetailsHistoryRepository.save(slaDetailsHistoryModel);
				// } catch (Exception e) {
				// 	logger.error("===Blc Exception ===", e);
				// 	String message = e.getMessage();
				// 	String statusBlk = Constants.BLC_STATUS_FALSE;
				// 	message = CommonUtil.updateExceptionMessage(message);
				// 	slaDetailsModel.setBlcMessage(message);
				// 	slaDetailsModel.setBlcStatus(statusBlk);
				// }
				apiResponse.setStatus(HttpStatus.OK);
				apiResponse.setMsg(Constants.PRINT_REQUEST_SUCCESS_MESSAGE);
				apiResponse.setData(slaId);
			} else {
						
				// logger.info("updatePrintRequest False response from blockchain " + blockchainResult);
				throw new Exception(Constants.INTERNAL_SERVER_ERROR);
			}
		}else{
			apiResponse.setStatus(HttpStatus.BAD_REQUEST);
			apiResponse.setMsg(Constants.CHECK_REQUEST_MESSAGE);
			apiResponse.setData(slaId);

		}
		// logger.info("updatePrintRequest apiResponse " + apiResponse.toString());
		return apiResponse;

	}
	@Override
	@Transactional
	public ApiResponse updatePrintRequestBySlaId(Long slaId , HttpServletRequest request) throws Exception {
		ApiResponse apiResponse = new ApiResponse();
		LocalDateTime now = LocalDateTime.now();

		Optional<SlaDetailsModel> slaDetails = slaDetailsRepository.findById(slaId);

		if (slaDetails.isPresent()) {
			SlaDetailsModel slaDetailsModel = slaDetails.get();
			String username = authService.getUserIdFromRequest(request);
			slaDetailsModel.setStatus(Constants.RECORD_STATUS_APPROVED);
			slaDetailsModel.setUpdatedBy(username);
			slaDetailsModel.setUpdatedAt(now);
			// try {
			BlockchainSlaDetailsResponse blockchainResult = blockchainGatway.updateSlaRecord(slaDetailsModel,
					channelGovtHospital);
			// logger.info("After data addition in Blockchain ==== " + now);
			// Set Blockchain response
			String message = blockchainResult.getMessage();
			String txID = blockchainResult.getTxID();
			String statusBlk = blockchainResult.getStatus();
			if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(statusBlk)) {
				slaDetailsModel.setBlcMessage(message);
				slaDetailsModel.setBlcTxId(txID);
				slaDetailsModel.setBlcStatus(statusBlk);
				SlaDetailsHistoryModel slaDetailsHistoryModel = new SlaDetailsHistoryModel();
				BeanUtils.copyProperties(slaDetailsModel, slaDetailsHistoryModel);
				slaDetailsHistoryModel = slaDetailsHistoryRepository.save(slaDetailsHistoryModel);
				// } catch (Exception e) {
				// 	logger.error("===Blc Exception ===", e);
				// 	String message = e.getMessage();
				// 	String statusBlk = Constants.BLC_STATUS_FALSE;
				// 	message = CommonUtil.updateExceptionMessage(message);
				// 	slaDetailsModel.setBlcMessage(message);
				// 	slaDetailsModel.setBlcStatus(statusBlk);
				// }
				apiResponse.setStatus(HttpStatus.OK);
				apiResponse.setMsg(Constants.PRINT_REQUEST_SUCCESS_MESSAGE);
				apiResponse.setData(slaId);
			} else {

				// logger.info("updatePrintRequest False response from blockchain " + blockchainResult);
				throw new Exception(Constants.INTERNAL_SERVER_ERROR);
			}
		}else{
			apiResponse.setStatus(HttpStatus.NOT_FOUND);
			apiResponse.setMsg(Constants.RECORD_NOT_FOUND);
			apiResponse.setData(slaId);
			logger.warn("Record not found ====  slaId " + slaId  + " ====== "+ now);
		}
		return apiResponse;

	}

	private Optional<SlaDetailsModel> findByBndIdAndRecordType(Long bndId, String transactionType, String recordType) {
			return slaDetailsRepository.findAll(new Specification<SlaDetailsModel>() {
			@Override
			public Predicate toPredicate(Root<SlaDetailsModel> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> predicates = new ArrayList<>();
				predicates.add(cb.equal(root.get("bndId"), bndId));
				predicates.add(cb.equal(root.get("transactionType"), transactionType));
				predicates.add(cb.equal(root.get("certificateType"), recordType));
				predicates.add(cb.notEqual(root.get(Constants.STATUS), Constants.RECORD_STATUS_UPLOADING));
				// orderBy slaDetailsId desc
				query.orderBy(cb.desc(root.get("slaDetailsId")));
				return cb.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		}).stream().findFirst();

	}


	private Optional<SlaDetailsModel> filterByBndIdAndRecordType(Long bndId, String transactionType, String recordType) {
		return slaDetailsRepository.findAll(new Specification<SlaDetailsModel>() {
			@Override
			public Predicate toPredicate(Root<SlaDetailsModel> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> predicates = new ArrayList<>();
				predicates.add(cb.equal(root.get("bndId"), bndId));
				predicates.add(cb.equal(root.get("transactionType"), transactionType));
				//predicates.add(cb.equal(root.get("certificateType"), recordType));
				predicates.add(cb.equal(root.get("recordType"), recordType));
				//predicates.add(cb.equal(root.get("blcStatus"), "true"));
				//predicates.add(cb.notEqual(root.get(Constants.STATUS), Constants.RECORD_STATUS_UPLOADING));
				// orderBy slaDetailsId desc
				query.orderBy(cb.desc(root.get("slaDetailsId")));
				return cb.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		}).stream().findFirst();

	}

	@Override
	public boolean verifySlaIdToUploadDocument(Long slaId, HttpServletRequest request) {
		Optional<SlaDetailsModel> slaDetails = slaDetailsRepository.findById(slaId);
		if (slaDetails.isPresent()) {
			SlaDetailsModel slaDetailsModel = slaDetails.get();
			if (Constants.RECORD_STATUS_UPLOADING.equals(slaDetailsModel.getStatus())
					|| Constants.RECORD_STATUS_REJECTED.equals(slaDetailsModel.getStatus())) {
				return true;
			}
		}
		return false;
	}

	@Override
	@Transactional
	public ApiResponse updateBirthIncusion(Long slaId, NameIncusionDto nameIncusionDto, HttpServletRequest request) throws Exception {
		SlaDetailsModel slaDetailsModel = getSlaDetailsToUpdate(slaId);
		if (slaDetailsModel == null) {
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setMsg(Constants.RECORD_NOT_FOUND + " id "+ slaId);
			apiResponse.setStatus(HttpStatus.NOT_FOUND);
			return apiResponse;
		}
		return saveBirthIncusion(slaDetailsModel, nameIncusionDto, request);
	}

	private SlaDetailsModel getSlaDetailsToUpdate(Long slaId) {
		//logger.info("SLAID ===="+slaId);
		Optional<SlaDetailsModel> slaDetails = slaDetailsRepository.findById(slaId);
		//logger.info("SLADETAILS MODEL IS ==="+slaDetails.get());
		if (slaDetails.isPresent()) {
			SlaDetailsModel slaDetailsModel = slaDetails.get();
			if (Constants.RECORD_STATUS_UPLOADING.equals(slaDetailsModel.getStatus())
					|| Constants.RECORD_STATUS_REJECTED.equals(slaDetailsModel.getStatus())) {
				return slaDetailsModel;
			}
		}
		return null;
	}

	@Override
	@Transactional
	public ApiResponse updateBirthCorrection(Long slaId, BirthCorrectionDto birthCorrectionDto,
			HttpServletRequest request) throws Exception {
		SlaDetailsModel slaDetailsModel = getSlaDetailsToUpdate(slaId);
		if (slaDetailsModel == null) {
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setMsg(Constants.RECORD_NOT_FOUND +" id "+  slaId);
			apiResponse.setStatus(HttpStatus.NOT_FOUND);
		}
		return saveBirthLegalData(slaDetailsModel, birthCorrectionDto, request);
	}

	@Override
	@Transactional
	public ApiResponse updateDeathCorrection(Long slaId, DeathCorrectionDto deathCorrectionDto,
			HttpServletRequest request) throws Exception {
		SlaDetailsModel slaDetailsModel = getSlaDetailsToUpdate(slaId);
		if (slaDetailsModel == null) {
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setMsg(Constants.RECORD_NOT_FOUND  +" id "+ slaId);
			apiResponse.setStatus(HttpStatus.NOT_FOUND);
			return apiResponse;
		}
		return saveDeathCorrection(slaDetailsModel, deathCorrectionDto, request);
	}

	@Override
	@Transactional
	public ApiResponse updateStillBirthCorrection(Long slaId, StillBirthCorrectionDto stillBirthCorrectionDto,
			HttpServletRequest request) throws Exception {
		SlaDetailsModel slaDetailsModel = getSlaDetailsToUpdate(slaId);
		if (slaDetailsModel == null) {
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setMsg(Constants.RECORD_NOT_FOUND+" id " +slaId);
			apiResponse.setStatus(HttpStatus.NOT_FOUND);
			return apiResponse;
		}
		return saveStillBirthCorrection(slaDetailsModel, stillBirthCorrectionDto, request);
	}

	@Override
	public ApiResponse getReportSearch(ReportSearchDto reportSearchDto, String type, HttpServletRequest request) {
		ApiResponse apiResponse = new ApiResponse();
		List<HashMap <String, Object>> reportSearchMapList = new ArrayList<>();
		List<SlaDetailsModel> slaDetailsModelList = new ArrayList<>();
		List<BirthHistoryModel> birthModelList = new ArrayList<>();
		List<SBirthHistoryModel> sbirthModelList = new ArrayList<>();
		List<DeathHistoryModel> deathModelList = new ArrayList<>();
		Map<Long, BirthHistoryModel> birthModelMap = new HashMap<>();
		Map<Long, DeathHistoryModel> deathModelMap = new HashMap<>();
		Map<Long, SBirthHistoryModel> odelMap = new HashMap<>();
		List<Long> slaDetailIds = new ArrayList<>();

		if(Constants.FILTER_INCLUSION_SEARCH.equalsIgnoreCase(type)) {
			birthModelList = birthService.getBirthListForReport(reportSearchDto, type);
			// logger.info("===== birthModelList ===="+birthModelList);
			//for each birth ids from birthModelList
			slaDetailIds = birthModelList.stream().map(BirthHistoryModel::getInclusionSlaId).collect(Collectors.toList());

			// logger.info("===== slaDetailIds ===="+slaDetailIds);
		} else if(Constants.FILTER_LEGAL_CORRECTIONS.equals(type)
				|| Constants.FILTER_LEGAL_CORRECTION_SEARCH.equals(type)) {
			if(CommonUtil.checkNullOrBlank(reportSearchDto.getRecordType()) || Constants.RECORD_TYPE_BIRTH.equals(reportSearchDto.getRecordType())) {
				birthModelList = birthService.getBirthListForReport(reportSearchDto, type);
				slaDetailIds.addAll(birthModelList.stream().map(BirthHistoryModel::getCorrectionSlaId).collect(Collectors.toList()));
			}
			if(CommonUtil.checkNullOrBlank(reportSearchDto.getRecordType()) || Constants.RECORD_TYPE_SBIRTH.equals(reportSearchDto.getRecordType())) {
				sbirthModelList = sBirthService.getStillBirthListForReport(reportSearchDto, type);
				slaDetailIds.addAll(sbirthModelList.stream().map(SBirthHistoryModel::getCorrectionSlaId).collect(Collectors.toList()));
			}
			if(CommonUtil.checkNullOrBlank(reportSearchDto.getRecordType()) || Constants.RECORD_TYPE_DEATH.equals(reportSearchDto.getRecordType())) {
				deathModelList = deathService.getDeathListForReport(reportSearchDto, type);
				slaDetailIds.addAll(deathModelList.stream().map(DeathHistoryModel::getCorrectionSlaId).collect(Collectors.toList()));
			}
			
		}else{
			apiResponse.setStatus(HttpStatus.BAD_REQUEST);
			apiResponse.setMsg(Constants.NOT_PERMITTED +" " +type);
			return apiResponse;
		}


		//get sla details for birth ids and birthCertificateType
		if(CommonUtil.checkNullOrBlank(reportSearchDto.getStatus())){
			// logger.info("========= Without status ======== " + slaDetailIds);
				slaDetailsModelList = slaDetailsRepository.findAllById(slaDetailIds);
				// logger.info("====== slaDetailsModelList === " + slaDetailsModelList);
		} else {
			// logger.info("========= reportSearchDto.getStatus() ======== "+reportSearchDto.getStatus() + " ==" + slaDetailIds);

				List<SlaDetailsModel> slaDetails = slaDetailsRepository.findBySlaDetailsIdInAndStatus(slaDetailIds, reportSearchDto.getStatus());
					slaDetailsModelList = slaDetails;
					// logger.info("====== slaDetailsModelList === " + slaDetailsModelList);
		}
		
		List<Long> slaIds = new ArrayList<>();
		Map<Long, SlaDetailsModel> slaMap = slaDetailsModelList.stream().collect(Collectors.toMap(SlaDetailsModel::getSlaDetailsId, slaDetailsModel -> slaDetailsModel));

		// logger.info("====== slaMap====="+slaMap);
		/*
		 *Need to dubug about null for sladetailmodel in the case of inclusion search
		 * */
		for (BirthHistoryModel birthModel : birthModelList) {
			if((Constants.FILTER_LEGAL_CORRECTIONS.equalsIgnoreCase(type) && birthModel.getCorrectionSlaId() != null  && !CommonUtil.checkNullOrBlank(birthModel.getCorrectionSlaId()+""))
					|| (Constants.FILTER_LEGAL_CORRECTION_SEARCH.equalsIgnoreCase(type) && birthModel.getCorrectionSlaId() != null && !CommonUtil.checkNullOrBlank(birthModel.getCorrectionSlaId()+"") )
					|| (Constants.FILTER_INCLUSION_SEARCH.equalsIgnoreCase(type) && birthModel.getInclusionSlaId() != null && !CommonUtil.checkNullOrBlank(birthModel.getInclusionSlaId()+"") && !slaMap.isEmpty())) {
				SlaDetailsModel slaDetailsModel = slaMap.get(
						(type.equals(Constants.FILTER_LEGAL_CORRECTIONS)
								|| type.equals(Constants.FILTER_LEGAL_CORRECTION_SEARCH))
								? birthModel.getCorrectionSlaId() : birthModel.getInclusionSlaId());
				// logger.info("======Before SlaDetailsModel====="+slaDetailsModel);


				if(slaDetailsModel !=null && !slaIds.contains(slaDetailsModel.getSlaDetailsId()) ) {

					// logger.info("====== SlaDetailsModel====="+slaDetailsModel);

						HashMap<String, Object> reportSearch = new HashMap<>();
						reportSearch.put(Constants.DATA, birthModel);
						reportSearch.put(Constants.SLA_DETAILS_MODEL, slaDetailsModel);
						reportSearchMapList.add(reportSearch);
						slaIds.add(slaDetailsModel.getSlaDetailsId());
					//}

				}
			}
		}
		
		for (SBirthHistoryModel birthModel : sbirthModelList) {
			if(birthModel.getCorrectionSlaId() != null) {
				SlaDetailsModel slaDetailsModel = slaMap.get(birthModel.getCorrectionSlaId());
				if(slaDetailsModel != null && !slaIds.contains(slaDetailsModel.getSlaDetailsId())) {
					HashMap<String, Object> reportSearch = new HashMap<>();
					reportSearch.put(Constants.DATA, birthModel);
					reportSearch.put(Constants.SLA_DETAILS_MODEL, slaDetailsModel);
					reportSearchMapList.add(reportSearch);
					slaIds.add(slaDetailsModel.getSlaDetailsId());
				}
			}
		}

		for (DeathHistoryModel deathModel : deathModelList) {
			if(deathModel.getCorrectionSlaId() != null) {
				SlaDetailsModel slaDetailsModel = slaMap.get(deathModel.getCorrectionSlaId());
				if(slaDetailsModel != null && !slaIds.contains(slaDetailsModel.getSlaDetailsId())) {
					HashMap<String, Object> reportSearch = new HashMap<>();
					reportSearch.put(Constants.DATA, deathModel);
					reportSearch.put(Constants.SLA_DETAILS_MODEL, slaDetailsModel);
					reportSearchMapList.add(reportSearch);
					slaIds.add(slaDetailsModel.getSlaDetailsId());
				}
			}
		}

		apiResponse.setStatus(HttpStatus.OK);
		apiResponse.setData(reportSearchMapList);
		return apiResponse;
	}

	@Override
	public ApiResponse saveIndividualPrintRequest(PrintRequestDto printRequestDto, String useType, HttpServletRequest request) throws Exception {

		if(Constants.INDIVIDUAL.equalsIgnoreCase(useType)) {
			printRequestDto.setUseFor(Constants.INDIVIDUAL);
			return savePrintRequest(printRequestDto, request);
		}
		throw new RuntimeException("Invalid Request");

	}


	@Override
	@Transactional
	public ApiResponse saveOnlinePrintRequest(PrintRequestDto printRequestDto, HttpServletRequest request) throws Exception {
		// Check any request is Approved in slaDetails table
		// logger.info("Call saveOnlinePrintRequest ==== ");
		ApiResponse apiResponse = new ApiResponse();
		Long bndId = printRequestDto.getBndId();
		String transactionType = printRequestDto.getTransactionType();
		String certificateType = printRequestDto.getRecordType();
		String applNo= printRequestDto.getApplNo();
		String status = Constants.RECORD_STATUS_PENDING;

		LocalDateTime now = LocalDateTime.now();
		SlaDetailsModel slaDetailsModel = new SlaDetailsModel();
		String username = authService.getUserIdFromRequest(request);

		Optional<SlaDetailsModel> slaDetails = slaDetailsRepository
				.findByBndIdAndTransactionTypeAndCertificateTypeAndStatusAndUserId(bndId, transactionType, certificateType,
						status,username);

		if(slaDetails.isPresent())
			slaDetailsModel =slaDetails.get();

		Optional<LateFee> existedFeeData = null;

		if(Constants.RECORD_TYPE_BIRTH.equals(certificateType) ||
				Constants.RECORD_NAME_INCLUSION.equals(certificateType) ){
			Optional<BirthModel> getBirthModel= birthRepository.findById(bndId);
			if(!getBirthModel.isPresent()){
				apiResponse.setMsg(Constants.RECORD_NOT_FOUND);
				apiResponse.setStatus(HttpStatus.BAD_REQUEST);
				return apiResponse;
			}
			BirthModel birthModel= getBirthModel.get();

			if (Constants.RECORD_NAME_INCLUSION.equals(certificateType)
					&& !CommonUtil.checkNullOrBlank(birthModel.getEventDate()+"")) {
				Long days= Duration.between(birthModel.getEventDate(), LocalDateTime.now()).toDays();
				if (days >= Constants.FILTER_DATE_NAME_INCLUSION) {
					apiResponse.setMsg(Constants.NAME_INCLUSION_NOT_ALLOW_MESSAGE +"" + Constants.FILTER_DATE_NAME_INCLUSION);
					apiResponse.setStatus(HttpStatus.BAD_REQUEST);
					return apiResponse;
				}else if(days < 0) {
					apiResponse.setMsg(Constants.NAME_INCLUSION_NOT_ALLOW_MESSAGE + " futures !");
					apiResponse.setStatus(HttpStatus.BAD_REQUEST);
					return apiResponse;
				}
			}
			if (Constants.RECORD_NAME_INCLUSION.equals(certificateType)
					&& !CommonUtil.checkNullOrBlank(birthModel.getName()+"")) {
				apiResponse.setMsg(Constants.NAME_ALREADY_INCLUDED_MESSAGE +"'"+ birthModel.getName() +"' ApplicationNumber '" +birthModel.getApplicationNumber()+"'");
				apiResponse.setStatus(HttpStatus.BAD_REQUEST);
				return apiResponse;
			}
			printRequestDto.setDivisionCode(birthModel.getDivisionCode());
			printRequestDto.setOrganizationCode(birthModel.getOrganizationCode());
			printRequestDto.setGenderCode(birthModel.getGenderCode());
			printRequestDto.setFatherName(birthModel.getFatherName());
			printRequestDto.setMotherName(birthModel.getMotherName());
			printRequestDto.setApplNo(birthModel.getApplicationNumber());
			slaDetailsModel.setApplNo(birthModel.getApplicationNumber());
			printRequestDto.setRegistrationNumber(birthModel.getRegistrationNumber());
			printRequestDto.setDateOfEvent(birthModel.getEventDate());
			slaDetailsModel.setDateOfEvent(birthModel.getEventDate());
			slaDetailsModel.setFatherAdharNumber(birthModel.getFatherAdharNumber());
			slaDetailsModel.setMotherAdharNumber(birthModel.getFatherAdharNumber());

			printRequestDto.setApplicationDate(birthModel.getRegistrationDatetime());


			if (Constants.RECORD_NAME_INCLUSION.equals(certificateType)) {
				existedFeeData = lateFeeRepository.findById(Constants.ONLINE_INCLUSION);
			}else{
				existedFeeData = lateFeeRepository.findById(Constants.ONLINE_PRINT_B);
			}

		}
		else if(Constants.RECORD_TYPE_DEATH.equals(certificateType)){
			Optional<DeathModel> getDeathModel= deathRepository.findById(bndId);
			if(!getDeathModel.isPresent()){
				apiResponse.setMsg(Constants.RECORD_NOT_FOUND);
				apiResponse.setStatus(HttpStatus.BAD_REQUEST);
				return apiResponse;
			}
			DeathModel deathModel =getDeathModel.get();
			printRequestDto.setDivisionCode(deathModel.getDivisionCode());
			printRequestDto.setOrganizationCode(deathModel.getOrganizationCode());
			printRequestDto.setGenderCode(deathModel.getGenderCode());
			printRequestDto.setFatherName(deathModel.getFatherName());
			printRequestDto.setMotherName(deathModel.getMotherName());
			printRequestDto.setApplNo(deathModel.getApplicationNumber());
			slaDetailsModel.setApplNo(deathModel.getApplicationNumber());
			printRequestDto.setRegistrationNumber(deathModel.getRegistrationNumber());
			printRequestDto.setDateOfEvent(deathModel.getEventDate());
			slaDetailsModel.setDateOfEvent(deathModel.getEventDate());
			printRequestDto.setApplicationDate(deathModel.getRegistrationDatetime());
			slaDetailsModel.setFatherAdharNumber(deathModel.getFatherAdharNumber());
			slaDetailsModel.setMotherAdharNumber(deathModel.getFatherAdharNumber());

			printRequestDto.setApplicationDate(deathModel.getRegistrationDatetime());
			existedFeeData = lateFeeRepository.findById(Constants.ONLINE_PRINT_D);
		}
		else if(Constants.RECORD_TYPE_STILL_BIRTH.equals(certificateType)
				|| Constants.RECORD_TYPE_SBIRTH.equals(certificateType)){
			Optional<SBirthModel> getSBirthModel= sBirthRepository.findById(bndId);
			if(!getSBirthModel.isPresent()){
				apiResponse.setMsg(Constants.RECORD_NOT_FOUND);
				apiResponse.setStatus(HttpStatus.BAD_REQUEST);
				return apiResponse;
			}
			SBirthModel sBirthModel = getSBirthModel.get();
			printRequestDto.setDivisionCode(sBirthModel.getDivisionCode());
			printRequestDto.setOrganizationCode(sBirthModel.getOrganizationCode());
			printRequestDto.setGenderCode(sBirthModel.getGenderCode());
			printRequestDto.setFatherName(sBirthModel.getFatherName());
			printRequestDto.setMotherName(sBirthModel.getMotherName());
			printRequestDto.setApplNo(sBirthModel.getApplicationNumber());
			slaDetailsModel.setApplNo(sBirthModel.getApplicationNumber());
			printRequestDto.setRegistrationNumber(sBirthModel.getRegistrationNumber());
			printRequestDto.setDateOfEvent(sBirthModel.getEventDate());
			slaDetailsModel.setDateOfEvent(sBirthModel.getEventDate());
			printRequestDto.setApplicationDate(sBirthModel.getRegistrationDatetime());
			slaDetailsModel.setFatherAdharNumber(sBirthModel.getFatherAdharNumber());
			slaDetailsModel.setMotherAdharNumber(sBirthModel.getFatherAdharNumber());

			printRequestDto.setApplicationDate(sBirthModel.getRegistrationDatetime());
			existedFeeData = lateFeeRepository.findById(Constants.ONLINE_PRINT_S);
		}else{
			apiResponse.setMsg(Constants.NOT_PERMITTED);
			apiResponse.setStatus(HttpStatus.BAD_REQUEST);
			return apiResponse;
		}

		BeanUtils.copyProperties(printRequestDto, slaDetailsModel);
		JwtUtil jwtUtil = new JwtUtil();

		slaDetailsModel.setUserId(username);
		slaDetailsModel.setStatus(Constants.RECORD_STATUS_PENDING);
		slaDetailsModel.setBndId(bndId);
		slaDetailsModel.setTransactionType(transactionType);
		slaDetailsModel.setCertificateType(certificateType);
		slaDetailsModel.setCreatedAt(now);

		slaDetailsModel.setApplDate(now);
		slaDetailsModel.setDueDate(printRequestDto.getDueDate());
		Long organizationId = authService.getOrganizationIdFromUserId(username);
		slaDetailsModel.setSlaOrganizationId(organizationId);
		//slaDetailsModel

		LateFee fee ;
		if(existedFeeData.isPresent()){
			fee = existedFeeData.get();
			slaDetailsModel.setFee(fee.getFee()*printRequestDto.getNoOfCopies());
			slaDetailsModel.setAmount(fee.getFee()*printRequestDto.getNoOfCopies());
			printRequestDto.setFee(slaDetailsModel.getFee());
		}else{
			printRequestDto.setFee(0.0F);
			slaDetailsModel.setFee(0.0F);
			slaDetailsModel.setAmount(0.0F);

		}

		// try {
		BlockchainSlaDetailsResponse blockchainResult = null;

		if(slaDetailsModel.getSlaDetailsId() != null) {
			printRequestDto.setReceiptNumber(slaDetailsModel.getApplNo().split("/")[0].toString()+""+slaDetailsModel.getSlaDetailsId().toString()+"/"+LocalDate.now().getYear());
			slaDetailsModel.setReceiptNumber(printRequestDto.getReceiptNumber());
			blockchainResult = blockchainGatway.updateSlaRecord(slaDetailsModel, channelGovtHospital);
		}
		else {
			slaDetailsRepository.save(slaDetailsModel);
			printRequestDto.setReceiptNumber(slaDetailsModel.getApplNo().split("/")[0].toString()+""+slaDetailsModel.getSlaDetailsId().toString()+"/"+LocalDate.now().getYear());
			slaDetailsModel.setReceiptNumber(printRequestDto.getReceiptNumber());
			blockchainResult = blockchainGatway.insertSlaDetails(slaDetailsModel, channelGovtHospital);
		}
			// logger.info("After data addition in Blockchain ==== " + now);
			// logger.info("Blockchain response:=== "+blockchainResult);
			// Set Blockchain response
			String message = blockchainResult.getMessage();
			String txID = blockchainResult.getTxID();
			String statusBlk = blockchainResult.getStatus();
			if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(statusBlk)) {
				slaDetailsModel.setBlcMessage(message);
				slaDetailsModel.setBlcTxId(txID);
				slaDetailsModel.setBlcStatus(statusBlk);
				SlaDetailsHistoryModel slaDetailsHistoryModel = new SlaDetailsHistoryModel();
				BeanUtils.copyProperties(slaDetailsModel, slaDetailsHistoryModel);
				slaDetailsHistoryModel = slaDetailsHistoryRepository.save(slaDetailsHistoryModel);

			// } catch (Exception e) {
			// 	logger.error("===Blc Exception ===", e);
			// 	String message = e.getMessage();
			// 	String statusBlk = Constants.BLC_STATUS_FALSE;
			// 	message = CommonUtil.updateExceptionMessage(message);
			// 	slaDetailsModel.setBlcMessage(message);
			// 	slaDetailsModel.setBlcStatus(statusBlk);

			// }
			slaDetailsRepository.save(slaDetailsModel);

			if(Constants.RECORD_NAME_INCLUSION.equals(certificateType))
					onlineInclusionRequest(slaDetailsModel.getSlaDetailsId(),printRequestDto,request);

			apiResponse.setMsg(Constants.PRINT_REQUEST_SUCCESS_MESSAGE);
			apiResponse.setStatus(HttpStatus.OK);
			printRequestDto.setUniqueId(slaDetailsModel.getSlaDetailsId());

			apiResponse.setData(printRequestDto);
		} else {
			// logger.info("saveOnlinePrintRequest False response from blockchain " + blockchainResult);
			throw new Exception(Constants.INTERNAL_SERVER_ERROR);
		}
		return apiResponse;
	}

	public ApiResponse nameInclusionCorrection(Long slaId, PrintRequestDto printRequestDto, HttpServletRequest request) throws Exception  {
		ApiResponse response = new ApiResponse();
		Long bndId= printRequestDto.getBndId();

		Optional<SlaDetailsModel> slaDetailsModel1 = slaDetailsRepository.findById(slaId);
		SlaDetailsModel slaDetailsModel = slaDetailsModel1.get();
		Optional<BirthModel> existedData = birthRepository.findById(bndId);
			if (!existedData.isPresent()) {
				response.setStatus(HttpStatus.BAD_REQUEST);
				response.setMsg(Constants.RECORD_NOT_FOUND);
			} else {
				BirthModel birthModel = existedData.get();
				if(!CommonUtil.checkNullOrBlank(birthModel.getName())) {
					response.setStatus(HttpStatus.BAD_REQUEST);
					response.setMsg(Constants.RECORD_ALREADY_UPDATED);
					return response;
				}
				String userId = authService.getUserIdFromRequest(request);

				birthModel.setName(slaDetailsModel.getChildName());
				birthModel.setInclusionSlaId(slaDetailsModel.getSlaDetailsId());
				birthModel.setModifiedAt(LocalDateTime.now());
				birthModel.setModifiedBy(userId);
				birthModel.setStatus(Constants.RECORD_STATUS_APPROVED);
				birthModel.setTransactionType(Constants.NAME_INCLUSION);

				// Add birth history
				BirthHistoryModel birthHistoryModel = new BirthHistoryModel();
				BeanUtils.copyProperties(birthModel, birthHistoryModel);

				birthModel = birthRepository.save(birthModel);
				birthHistoryModel = birthHistoryRepository.save(birthHistoryModel);

				// try {
					BlockchainUpdateBirthResponse blockchainResult = blockchainGatway.updateBirthRecord(birthModel,
							channelGovtHospital);
					// logger.info("After data addition in Blockchain ==== " + LocalDateTime.now());
					// JsonUtil.getObjectFromJson(blockchainResult, BlockchainGatway.class);

					// Set Blockchain response
					String message = blockchainResult.getMessage();
					String txID = blockchainResult.getTxID();
					String status = blockchainResult.getStatus();
					if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(status)) {
						birthModel.setBlcMessage(message);
						birthModel.setBlcTxId(txID);
						birthModel.setBlcStatus(status);

						birthHistoryModel.setBlcMessage(message);
						birthHistoryModel.setBlcTxId(txID);
						birthHistoryModel.setBlcStatus(status);
					// } catch (Exception e) {
					// 	logger.error("===Blc Exception ===", e);
					// 	String message = e.getMessage();
					// 	String status = Constants.BLC_STATUS_FALSE;
					// 	message = CommonUtil.updateExceptionMessage(message);
					// 	birthModel.setBlcMessage(message);

					// 	birthModel.setBlcStatus(status);

					// 	birthHistoryModel.setBlcMessage(message);
					// 	birthHistoryModel.setBlcStatus(status);
					// }
					// Save Blockchain response in Model

					response.setMsg(Constants.BIRTH_UPDATE_SUCCESS_MESSAGE);
					response.setStatus(HttpStatus.OK);
					response.setData(birthModel);
				} else {
					// logger.info("nameInclusionCorrection False response from blockchain"); 
					throw new Exception(Constants.INTERNAL_SERVER_ERROR);
				}
			}
		return response;
	}
	public void onlineInclusionRequest(Long slaId, PrintRequestDto printRequestDto, HttpServletRequest request) throws Exception  {
		Long bndId= printRequestDto.getBndId();
		Optional<BirthModel> existedData = birthRepository.findById(bndId);
		if (!existedData.isPresent()) {
			throw new NotFoundException(Constants.RECORD_NOT_FOUND);
		} else {
			BirthModel birthModel = existedData.get();
			String userId = authService.getUserIdFromRequest(request);
			birthModel.setInclusionSlaId(slaId);
			birthModel.setModifiedAt(LocalDateTime.now());
			birthModel.setModifiedBy(userId);
			birthModel.setStatus(Constants.RECORD_STATUS_INCLUSION_PENDING);
			birthModel.setTransactionType(Constants.NAME_INCLUSION);

			// Add birth history
			BirthHistoryModel birthHistoryModel = new BirthHistoryModel();
			BeanUtils.copyProperties(birthModel, birthHistoryModel);
			birthModel = birthRepository.save(birthModel);
			birthHistoryModel = birthHistoryRepository.save(birthHistoryModel);

			// try {
			BlockchainUpdateBirthResponse blockchainResult = blockchainGatway.updateBirthRecord(birthModel,
					channelGovtHospital);
			// logger.info("After data addition in Blockchain ==== " + LocalDateTime.now());
			// JsonUtil.getObjectFromJson(blockchainResult, BlockchainGatway.class);

			// Set Blockchain response
			String message = blockchainResult.getMessage();
			String txID = blockchainResult.getTxID();
			String status = blockchainResult.getStatus();
			if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(status)) {
				birthModel.setBlcMessage(message);
				birthModel.setBlcTxId(txID);
				birthModel.setBlcStatus(status);

				birthHistoryModel.setBlcMessage(message);
				birthHistoryModel.setBlcTxId(txID);
				birthHistoryModel.setBlcStatus(status);
				// } catch (Exception e) {
				// 	logger.error("===Blc Exception ===", e);
				// 	String message = e.getMessage();
				// 	String status = Constants.BLC_STATUS_FALSE;
				// 	message = CommonUtil.updateExceptionMessage(message);
				// 	birthModel.setBlcMessage(message);

				// 	birthModel.setBlcStatus(status);

				// 	birthHistoryModel.setBlcMessage(message);
				// 	birthHistoryModel.setBlcStatus(status);
				// }
				// Save Blockchain response in Model

			} else {
				// logger.info("nameInclusionCorrection False response from blockchain");
				throw new Exception(Constants.INTERNAL_SERVER_ERROR);
			}
		}
	}

	/*
	* Online Birth Correction
	* Date: 07-07-22
	*Author: Deepak
	* */
	@Override
	@Transactional
	public ApiResponse saveOnlineBirthCorrection(BirthCorrectionDto birthCorrectionDto, HttpServletRequest request) throws Exception {

		ApiResponse apiResponse = new ApiResponse();
		LocalDateTime now = LocalDateTime.now();
		Long bndId = birthCorrectionDto.getBndId();
		String transactionType = birthCorrectionDto.getTransactionType();
		String certificateType = birthCorrectionDto.getRecordType();
	//	String status = Constants.RECORD_STATUS_UPLOADING;
		String username = authService.getUserIdFromRequest(request);

//		Optional<SlaDetailsModel> slaDetails = slaDetailsRepository.findByBndIdAndTransactionTypeAndCertificateTypeAndStatusAndUserId(bndId, transactionType, certificateType, status,username);
//
//		logger.info("=== SLA DETAILS ==="+ slaDetails);
//
//		if (slaDetails.isPresent()) {
//			SlaDetailsModel slaDetailsModel = slaDetails.get();
//			slaDetailsModel.setApplDate(now);
//			return saveBirthLegalData(slaDetailsModel, birthCorrectionDto, request);
//		}
//		else
//		{
//			apiResponse.setMsg(Constants.RECORD_NOT_FOUND);
//			apiResponse.setStatus(HttpStatus.NOT_FOUND);
//			return apiResponse;
//		}
		SlaDetailsModel slaDetailsModel = new SlaDetailsModel();
		slaDetailsModel.setCreatedAt(LocalDateTime.now());
		slaDetailsModel.setUserId(username);
		return saveOnlineBirthLegalData(slaDetailsModel, birthCorrectionDto, request);
	}

	@Override
	@Transactional
	public ApiResponse updateOnlineBirthCorrection(Long slaId, BirthCorrectionDto birthCorrectionDto,
											 HttpServletRequest request) throws Exception {
		SlaDetailsModel slaDetailsModel = getSlaDetailsToUpdate(slaId);
		if (slaDetailsModel == null) {
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setMsg(Constants.RECORD_NOT_FOUND +" id "+  slaId);
			apiResponse.setStatus(HttpStatus.NOT_FOUND);
		}
		return saveOnlineBirthLegalData(slaDetailsModel, birthCorrectionDto, request);
	}
	private ApiResponse saveOnlineBirthLegalData(SlaDetailsModel slaDetailsModel, BirthCorrectionDto birthCorrectionDto, HttpServletRequest request) throws Exception {


		BeanUtils.copyProperties(birthCorrectionDto, slaDetailsModel);
		//CustomBeanUtils.copyBirthCorrectionToSlaDetailsModel(birthCorrectionDto, slaDetailsModel);


		slaDetailsModel.setStatus(Constants.RECORD_STATUS_PENDING);
		LocalDateTime now = LocalDateTime.now();
		slaDetailsModel.setUpdatedAt(now);
		// JwtUtil jwtUtil = new JwtUtil();
		String userName = authService.getUserIdFromRequest(request);
		slaDetailsModel.setUpdatedBy(userName);
		slaDetailsModel.setDateOfEvent(birthCorrectionDto.getDateOfEvent());
		ApiResponse apiResponse = new ApiResponse();
		apiResponse.setMsg(Constants.BIRTH_CORRECTIONS_SUCCESS_MESSAGE);
		apiResponse.setStatus(HttpStatus.OK);
		logger.info("slaDetailsModel.getApplNo() ==== " + slaDetailsModel.getApplNo());
		apiResponse.setData(slaDetailsModel.getApplNo());
		try {
			BlockchainSlaDetailsResponse blockchainResult = blockchainGatway.updateSlaRecord(slaDetailsModel,
					channelGovtHospital);
			logger.info("After data addition in Blockchain ==== " + now);
			// Set Blockchain response
			String message = blockchainResult.getMessage();
			String txID = blockchainResult.getTxID();
			String statusBlk = blockchainResult.getStatus();
			if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(statusBlk)) {
				slaDetailsModel.setBlcMessage(message);
				slaDetailsModel.setBlcTxId(txID);
				slaDetailsModel.setBlcStatus(statusBlk);

			}} catch (Exception e) {

		}
		logger.info("====SLADETAILS MODEL===="+slaDetailsModel);
		slaDetailsRepository.save(slaDetailsModel);
		SlaDetailsHistoryModel slaDetailsHistoryModel = new SlaDetailsHistoryModel();
		BeanUtils.copyProperties(slaDetailsModel, slaDetailsHistoryModel);
		slaDetailsHistoryModel = slaDetailsHistoryRepository.save(slaDetailsHistoryModel);
		birthService.updateCorrectionStatus(slaDetailsModel, userName);

		return apiResponse;
	}


	@Override
	@Transactional
	public ApiResponse saveOnlineDeathCorrection(DeathCorrectionDto deathCorrectionDto, HttpServletRequest request) throws Exception {
		ApiResponse apiResponse = new ApiResponse();
		SlaDetailsModel slaDetailsModel = new SlaDetailsModel();
		Long bndId = deathCorrectionDto.getBndId();
		String transactionType = Constants.ONLINE_DEATH_CORRECTION;
		String recordType = Constants.RECORD_TYPE_DEATH;
		//String status = Constants.RECORD_STATUS_UPLOADING;
		String username = authService.getUserIdFromRequest(request);
		slaDetailsModel.setTransactionType(transactionType);
		slaDetailsModel.setRecordType(recordType);
		slaDetailsModel.setCreatedAt(LocalDateTime.now());
		slaDetailsModel.setUserId(username);

		return saveOnlineDeathLegalData(slaDetailsModel, deathCorrectionDto, request);


	}

	private ApiResponse saveOnlineDeathLegalData(SlaDetailsModel slaDetailsModel, DeathCorrectionDto deathCorrectionDto,
											HttpServletRequest request) throws Exception {

		// logger.info("saveDeathCorrection ==== " + stillBirthCorrectionDto.toString());
		BeanUtils.copyProperties(deathCorrectionDto, slaDetailsModel);
		slaDetailsModel.setDateOfEvent(deathCorrectionDto.getDateOfEvent());
		slaDetailsModel.setStatus(Constants.RECORD_STATUS_PENDING);
		LocalDateTime now = LocalDateTime.now();
		slaDetailsModel.setUpdatedAt(now);
		// JwtUtil jwtUtil = new JwtUtil();
		String userName = authService.getUserIdFromRequest(request);
		slaDetailsModel.setUpdatedBy(userName);

		ApiResponse apiResponse = new ApiResponse();
		apiResponse.setMsg(Constants.DEATH_CORRECTIONS_SUCCESS_MESSAGE);
		apiResponse.setStatus(HttpStatus.OK);
		//apiResponse.setData(slaDetailsModel.getSlaDetailsId());
		apiResponse.setData(slaDetailsModel.getApplNo());
		 try {
		BlockchainSlaDetailsResponse blockchainResult = blockchainGatway.updateSlaRecord(slaDetailsModel,
				channelGovtHospital);
		logger.info("After data addition in Blockchain ==== " + now);
		// Set Blockchain response
		String message = blockchainResult.getMessage();
		String txID = blockchainResult.getTxID();
		String statusBlk = blockchainResult.getStatus();
		if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(statusBlk)) {
			slaDetailsModel.setBlcMessage(message);
			slaDetailsModel.setBlcTxId(txID);
			slaDetailsModel.setBlcStatus(statusBlk);

			}
		 } catch (Exception e) {
			// 	logger.error("===Blc Exception ===", e);
			// 	String message = e.getMessage();
			// 	String statusBlk = Constants.BLC_STATUS_FALSE;
			// 	message = CommonUtil.updateExceptionMessage(message);
			// 	slaDetailsModel.setBlcMessage(message);
			// 	slaDetailsModel.setBlcStatus(statusBlk);

			 }
			slaDetailsRepository.save(slaDetailsModel);
			SlaDetailsHistoryModel slaDetailsHistoryModel = new SlaDetailsHistoryModel();
			BeanUtils.copyProperties(slaDetailsModel, slaDetailsHistoryModel);
			slaDetailsHistoryModel = slaDetailsHistoryRepository.save(slaDetailsHistoryModel);
			deathService.updateCorrectionStatus(slaDetailsModel, userName);
		   return apiResponse;

	}

	@Override
	@Transactional
	public ApiResponse saveOnlineStillBirthCorrection(StillBirthCorrectionDto stillBirthCorrectionDto,
												HttpServletRequest request) throws Exception {

		ApiResponse apiResponse = new ApiResponse();
		SlaDetailsModel slaDetailsModel = new SlaDetailsModel();
		Long bndId = stillBirthCorrectionDto.getBndId();
		String transactionType = Constants.ONLINE_STILL_BIRTH_CORRECTION;
		String recordType = Constants.RECORD_TYPE_STILL_BIRTH;
		//String status = Constants.RECORD_STATUS_UPLOADING;
		String username = authService.getUserIdFromRequest(request);
		slaDetailsModel.setTransactionType(transactionType);
		slaDetailsModel.setRecordType(recordType);
		slaDetailsModel.setCreatedAt(LocalDateTime.now());
		slaDetailsModel.setUserId(username);

		return saveOnlineStillBirthLegalData(slaDetailsModel, stillBirthCorrectionDto, request);
	}



	private ApiResponse saveOnlineStillBirthLegalData(SlaDetailsModel slaDetailsModel,
												 StillBirthCorrectionDto stillBirthCorrectionDto, HttpServletRequest request) throws Exception {

		// logger.info("saveStillBirthCorrection ==== " + stillBirthCorrectionDto.toString());
		BeanUtils.copyProperties(stillBirthCorrectionDto, slaDetailsModel);
		slaDetailsModel.setDateOfEvent(stillBirthCorrectionDto.getDateOfEvent());
		slaDetailsModel.setStatus(Constants.RECORD_STATUS_PENDING);
		LocalDateTime now = LocalDateTime.now();
		slaDetailsModel.setUpdatedAt(now);
		// JwtUtil jwtUtil = new JwtUtil();
		String userName = authService.getUserIdFromRequest(request);
		slaDetailsModel.setUpdatedBy(userName);

		ApiResponse apiResponse = new ApiResponse();
		apiResponse.setMsg(Constants.STILL_BIRTH_CORRECTION_SUCCESS_MESSAGE);
		apiResponse.setStatus(HttpStatus.OK);
		//apiResponse.setData(slaDetailsModel.getSlaDetailsId());
		apiResponse.setData(slaDetailsModel.getApplNo());
		 try {
		BlockchainSlaDetailsResponse blockchainResult = blockchainGatway.updateSlaRecord(slaDetailsModel,
				channelGovtHospital);
		logger.info("After data addition in Blockchain ==== " + now);
		// Set Blockchain response
		String message = blockchainResult.getMessage();
		String txID = blockchainResult.getTxID();
		String statusBlk = blockchainResult.getStatus();

		if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(statusBlk)) {
			slaDetailsModel.setBlcMessage(message);
			slaDetailsModel.setBlcTxId(txID);
			slaDetailsModel.setBlcStatus(statusBlk);
		}

		 } catch (Exception e) {

			 e.printStackTrace();
			// 	logger.error("===Blc Exception ===", e);
			// 	String message = e.getMessage();
			// 	String statusBlk = Constants.BLC_STATUS_FALSE;
			// 	message = CommonUtil.updateExceptionMessage(message);
			// 	slaDetailsModel.setBlcMessage(message);
			// 	slaDetailsModel.setBlcStatus(statusBlk);

			}
			slaDetailsRepository.save(slaDetailsModel);
			SlaDetailsHistoryModel slaDetailsHistoryModel = new SlaDetailsHistoryModel();
			BeanUtils.copyProperties(slaDetailsModel, slaDetailsHistoryModel);
			slaDetailsHistoryModel = slaDetailsHistoryRepository.save(slaDetailsHistoryModel);
			sBirthService.updateCorrectionStatus(slaDetailsModel, userName);
//		} else {
//			// logger.info("saveStillBirthCorrection False response from blockchain " + blockchainResult);
//			throw new Exception(Constants.INTERNAL_SERVER_ERROR);
//
//		}
		return apiResponse;
	}

	@Override
	public ApiResponse onlineAppointmentBySlaId(Long slaId, AppointmentDto appointmentDto, HttpServletRequest request) {

		ApiResponse response = new ApiResponse();

		OnlineAppointment onlineAppointment = onlineAppointmentRepository.findBySlaIdOrderByAptIdDesc(slaId);

		if(onlineAppointment != null){
			if(Constants.OPEN.equalsIgnoreCase(onlineAppointment.getStatus())){
				response.setMsg(Constants.APPOINTMENT_OPEN);
			}
			else{

				SlaDetailsModel slaDetailsModel = slaDetailsRepository.findBySlaDetailsIdAndStatus(slaId, Constants.RECORD_STATUS_PENDING);

				if(slaDetailsModel != null){

					String userName = authService.getUserIdFromRequest(request);
					onlineAppointment.setAppointmentDateTime(appointmentDto.getAppointmentDateTime());
					onlineAppointment.setCreatedAt(LocalDateTime.now());
					onlineAppointment.setSlaId(slaId);
					onlineAppointment.setCreatedBy(userName);
					onlineAppointment.setStatus(Constants.OPEN);

					OnlineAppointment appointmentResponse = onlineAppointmentRepository.save(onlineAppointment);
					if(appointmentResponse != null){
						response.setStatus(HttpStatus.OK);
						response.setMsg(Constants.APPOINTMENT_CREATED);
					}
					else{
						response.setStatus(HttpStatus.BAD_REQUEST);
						response.setMsg(Constants.APPOINTMENT_NOT_CREATED);
					}
				}else{
					response.setStatus(HttpStatus.BAD_REQUEST);
					response.setMsg(Constants.RECORD_NOT_FOUND);
				}
			}
		}else{
			response.setStatus(HttpStatus.BAD_REQUEST);
			response.setMsg(Constants.INTERNAL_SERVER_ERROR);
		}

		return response;
	}

	@Override
	public ApiResponse getOnlineCorrectionList(String recordType, HttpServletRequest request) {

		ApiResponse response = new ApiResponse();
		if(Constants.RECORD_TYPE_BIRTH.equalsIgnoreCase(recordType)) {
			List<SlaDetailsModel> slaList = slaDetailsRepository.findByStatusAndTransactionType(Constants.RECORD_STATUS_PENDING, Constants.ONLINE_BIRTH_CORRECTION);

			if(slaList != null){
				response.setStatus(HttpStatus.OK);
				response.setData(slaList);
			}else{
				response.setStatus(HttpStatus.BAD_REQUEST);
				response.setMsg(Constants.INTERNAL_SERVER_ERROR);
			}
		}
		else if(Constants.RECORD_TYPE_DEATH.equalsIgnoreCase(recordType)) {
			List<SlaDetailsModel> slaList = slaDetailsRepository.findByStatusAndTransactionType(Constants.RECORD_STATUS_PENDING, Constants.ONLINE_DEATH_CORRECTION);
			if(slaList != null){
				response.setStatus(HttpStatus.OK);
				response.setData(slaList);
			}else{
				response.setStatus(HttpStatus.BAD_REQUEST);
				response.setMsg(Constants.INTERNAL_SERVER_ERROR);
			}
		}
		else if(Constants.RECORD_TYPE_SBIRTH.equalsIgnoreCase(recordType)) {
			List<SlaDetailsModel> slaList = slaDetailsRepository.findByStatusAndTransactionType(Constants.RECORD_STATUS_PENDING, Constants.ONLINE_STILL_BIRTH_CORRECTION);
			if(slaList != null){
				response.setStatus(HttpStatus.OK);
				response.setData(slaList);
			}else{
				response.setStatus(HttpStatus.BAD_REQUEST);
				response.setMsg(Constants.INTERNAL_SERVER_ERROR);
			}
		}
			return response;
	}

	@Override
	public ApiResponse updateOnlineAppointmentByAptId(Long aptId, AppointmentDto appointmentDto, HttpServletRequest request) {

		ApiResponse response = new ApiResponse();
		Optional<OnlineAppointment> appointmentDetails = onlineAppointmentRepository.findById(aptId);
		if (appointmentDetails.isPresent()) {

			String userName = authService.getUserIdFromRequest(request);
			OnlineAppointment onlineAppointment = appointmentDetails.get();

			if (Constants.CLOSED.equalsIgnoreCase(onlineAppointment.getStatus())) {

				response.setMsg(Constants.APPOINTMENT_CLOSED);
				response.setStatus(HttpStatus.NOT_ACCEPTABLE);
			}

			else {
			onlineAppointment.setSlaId(onlineAppointment.getSlaId());
			onlineAppointment.setAptId(onlineAppointment.getAptId());

			onlineAppointment.setCreatedBy(onlineAppointment.getCreatedBy());
			onlineAppointment.setCreatedAt(onlineAppointment.getCreatedAt());
			onlineAppointment.setModifiedAt(LocalDateTime.now());
			onlineAppointment.setModifiedBy(userName);

			onlineAppointment.setAppointmentDateTime(appointmentDto.getAppointmentDateTime());
			onlineAppointment.setStatus(appointmentDto.getStatus());

			onlineAppointment = onlineAppointmentRepository.save(onlineAppointment);

			if (onlineAppointment != null) {
				response.setStatus(HttpStatus.OK);
				response.setMsg(Constants.APPOINTMENT_UPDATED);
			} else {
				response.setStatus(HttpStatus.BAD_REQUEST);
				response.setMsg(Constants.INTERNAL_SERVER_ERROR);
			}

		}
	}
		else{
			response.setStatus(HttpStatus.BAD_REQUEST);
			response.setMsg(Constants.RECORD_NOT_FOUND);
		}

		return response;
	}

	@Override
	public ApiResponse getOnlineAppointmentByStatus(String status, HttpServletRequest request) {
		ApiResponse response = new ApiResponse();

		if(Constants.OPEN.equalsIgnoreCase(status)){
			List<OnlineAppointment> appointments = onlineAppointmentRepository.findByStatus(Constants.OPEN);

			logger.info("APPOINTMENTS===="+appointments);
			if(appointments != null){
				response.setStatus(HttpStatus.OK);
				response.setData(appointments);
			}
			else{
				response.setStatus(HttpStatus.BAD_REQUEST);
				response.setMsg(Constants.INTERNAL_SERVER_ERROR);
			}
		}
		else if(Constants.CLOSED.equalsIgnoreCase(status)){
			List<OnlineAppointment> appointments = onlineAppointmentRepository.findByStatus(Constants.CLOSED);

			logger.info("APPOINTMENTS===="+appointments);
			if(appointments != null){
				response.setStatus(HttpStatus.OK);
				response.setData(appointments);
			}
			else{
				response.setStatus(HttpStatus.BAD_REQUEST);
				response.setMsg(Constants.INTERNAL_SERVER_ERROR);
			}
		}
		return response;
	}


}
