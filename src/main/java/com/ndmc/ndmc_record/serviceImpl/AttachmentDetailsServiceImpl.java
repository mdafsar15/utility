package com.ndmc.ndmc_record.serviceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ndmc.ndmc_record.blockchainGatway.BlockchainGatway;
import com.ndmc.ndmc_record.config.Constants;
import com.ndmc.ndmc_record.dto.ApiResponse;
import com.ndmc.ndmc_record.dto.AttachmentDto;
import com.ndmc.ndmc_record.dto.UploadFileResponse;
import com.ndmc.ndmc_record.model.AttachmentModel;
import com.ndmc.ndmc_record.model.BlockchainAttachmentResponse;
import com.ndmc.ndmc_record.model.BlockchainUpdateDeathResponse;
import com.ndmc.ndmc_record.repository.AttachmentRepository;
import com.ndmc.ndmc_record.service.AttachmentDetailsService;
import com.ndmc.ndmc_record.utils.CommonUtil;
import com.ndmc.ndmc_record.utils.JwtUtil;

@Service
public class AttachmentDetailsServiceImpl implements AttachmentDetailsService {

	private final Logger logger = LoggerFactory.getLogger(AttachmentDetailsServiceImpl.class);

	@Autowired
	private AttachmentRepository attachmentRepository;

	@Autowired
	BlockchainGatway blockchainGatway;

	@Autowired
	AuthServiceImpl authService;

	@Value("${CHANNEL_GOVT_HOSPITAL}")
	private String channelGovtHospital;

	@Override
	@Transactional
	public Long saveAttachmentDetails(AttachmentDto attachment, Long slaId, HttpServletRequest request,
			ApiResponse apiResponse, String fileNameByUser) throws Exception {
		LocalDateTime startMethodTime = LocalDateTime.now();
		logger.info("saveAttachmentDetails startMethodTime: "+ startMethodTime);
		Long bndId = attachment.getBndId();
		String status = Constants.RECORD_STATUS_ACTIVE;
		String fileType = attachment.getDocumentName();
		Optional<AttachmentModel> attachmentDetails = attachmentRepository.findByBndIdAndSlaDetailsIdAndFileType(bndId,
				slaId, fileType);
				
		UploadFileResponse uploadFileResponse = (UploadFileResponse) apiResponse.getData();
		LocalDateTime now = LocalDateTime.now();
		JwtUtil jwtUtil = new JwtUtil();
//		String username = "admin";
		String username = authService.getUserIdFromRequest(request);

		if (attachmentDetails.isPresent()) {
			AttachmentModel attachmentModel = attachmentDetails.get();
			BeanUtils.copyProperties(attachment, attachmentModel);
			attachmentModel.setBndId(bndId);
			attachmentModel.setFileName(fileNameByUser);
			attachmentModel.setFileHash(uploadFileResponse.getFileHash() + "");
			attachmentModel.setFilePath(uploadFileResponse.getFileDownloadUri());
			attachmentModel.setFileSize(uploadFileResponse.getSize() + "");
			attachmentModel.setSavedFileName(uploadFileResponse.getFileName());
			attachmentModel.setSlaDetailsId(slaId);
			attachmentModel.setStatus(status);
			attachmentModel.setFileType(fileType);
			attachmentModel.setUserId(username);
			attachmentModel.setCreatedAt(now);
			// try {
				attachmentModel = attachmentRepository.save(attachmentModel);
				LocalDateTime callBLCTime = LocalDateTime.now();
				logger.info("saveAttachmentDetails callBLCTime: "+ callBLCTime);
			BlockchainAttachmentResponse blockchainResult = blockchainGatway
						.updateAttachmentRecord(attachmentModel, channelGovtHospital);
				
				LocalDateTime afterCallBLCTime = LocalDateTime.now();
				logger.info("saveAttachmentDetails afterCallBLCTime: "+ afterCallBLCTime);
				// Set Blockchain response
				String message = blockchainResult.getMessage();
				String txID = blockchainResult.getTxID();
				String statusBlk = blockchainResult.getStatus();
				if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(statusBlk)) {
						attachmentModel.setBlcMessage(message);
						attachmentModel.setBlcTxId(txID);
						attachmentModel.setBlcStatus(statusBlk);

					// } catch (Exception e) {
					// 	logger.error("===Blc Exception ===", e);
					// 	String message = e.getMessage();
					// 	String statusBlk = Constants.BLC_STATUS_FALSE;
					// 	message = CommonUtil.updateExceptionMessage(message);
					// 	attachmentModel.setBlcMessage(message);
					// 	attachmentModel.setBlcStatus(statusBlk);

					// }

					attachmentRepository.save(attachmentModel);
				} else {
					
					logger.debug("saveAttachmentDetails False response from insertAttachmentDetails blockchain " + blockchainResult);
					throw new Exception(Constants.INTERNAL_SERVER_ERROR);
				}
				LocalDateTime endMethodTime = LocalDateTime.now();
				logger.debug("saveAttachmentDetails endMethodTime: "+ endMethodTime);
				
			return attachmentModel.getFileId();
		}
		AttachmentModel attachmentModel = new AttachmentModel();
		BeanUtils.copyProperties(attachment, attachmentModel);
		attachmentModel.setBndId(bndId);
		attachmentModel.setFileName(fileNameByUser);
		attachmentModel.setFileHash(uploadFileResponse.getFileHash() + "");
		attachmentModel.setFilePath(uploadFileResponse.getFileDownloadUri());
		attachmentModel.setFileSize(uploadFileResponse.getSize() + "");
		attachmentModel.setSavedFileName(uploadFileResponse.getFileName());
		attachmentModel.setSlaDetailsId(slaId);
		attachmentModel.setStatus(status);
		attachmentModel.setFileType(fileType);
		attachmentModel.setUserId(username);
		attachmentModel.setCreatedAt(now);
		// try {
			attachmentModel = attachmentRepository.save(attachmentModel);
			BlockchainAttachmentResponse blockchainResult = blockchainGatway.insertAttachmentDetails(attachmentModel,
					channelGovtHospital);
			logger.info("After data addition in Blockchain ==== " + LocalDateTime.now());
			// JsonUtil.getObjectFromJson(blockchainResult, BlockchainGatway.class);

			// Set Blockchain response
			String message = blockchainResult.getMessage();
			String txID = blockchainResult.getTxID();
			String statusBlk = blockchainResult.getStatus();
			if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(statusBlk)) {
				attachmentModel.setBlcMessage(message);
				attachmentModel.setBlcTxId(txID);
				attachmentModel.setBlcStatus(statusBlk);
			// } catch (Exception e) {
			// 	logger.error("===Blc Exception ===", e);
			// 	String message = e.getMessage();
			// 	String statusBlk = Constants.BLC_STATUS_FALSE;
			// 	message = CommonUtil.updateExceptionMessage(message);
			// 	attachmentModel.setBlcMessage(message);
			// 	attachmentModel.setBlcStatus(statusBlk);
			// }
			attachmentRepository.save(attachmentModel);
		} else {
					
			logger.debug("saveAttachmentDetails False response from insertAttachmentDetails blockchain " + blockchainResult);
			throw new Exception(Constants.INTERNAL_SERVER_ERROR);
		}
		return attachmentModel.getFileId();
	}

	@Override
	public List<AttachmentModel> findBySlaDetailsId(Long slaDetailsId) {
		return attachmentRepository.findBySlaDetailsId(slaDetailsId);
	}

}
