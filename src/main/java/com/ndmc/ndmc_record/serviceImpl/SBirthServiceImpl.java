package com.ndmc.ndmc_record.serviceImpl;

import com.ndmc.ndmc_record.blockchainGatway.BlockchainGatway;
import com.ndmc.ndmc_record.config.Constants;
import com.ndmc.ndmc_record.dto.*;
import com.ndmc.ndmc_record.exception.DateRangeException;
import com.ndmc.ndmc_record.model.*;
import com.ndmc.ndmc_record.property.FileStorageProperties;
import com.ndmc.ndmc_record.repository.*;
import com.ndmc.ndmc_record.service.QrCodeService;
import com.ndmc.ndmc_record.service.SBirthService;
import com.ndmc.ndmc_record.service.SlaDetailsService;
import com.ndmc.ndmc_record.utils.CommonUtil;
import com.ndmc.ndmc_record.utils.CustomBeanUtils;
import com.ndmc.ndmc_record.utils.JsonUtil;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.*;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
public class SBirthServiceImpl implements SBirthService {

    private final Logger logger = LoggerFactory.getLogger(SBirthServiceImpl.class);

    @Value("${initial.record.status}")
    private String recordStatus;

    @Value("${CHANNEL_GOVT_HOSPITAL}")
    private String channelGovtHospital;

    @Value("${AUTO_APPROVE_HOUR}")
    private int autoApproveHour;

    @Value("${approved_status}")
    private String approvedStatus;

    @Value("${CERT_VERIFICATION_URL}")
    private String vertificateVerificationUrl;

    @Autowired
    SBirthRepository birthRepository;

    @Autowired
    SBirthHistoryRepository birthHistoryRepository;

    @Autowired
    AuthRepository authRepository;

    @Autowired
    ApplicatioNumberCounterServiceImpl applicatioNumberCounterService;

    @Autowired
    LateFeeRepository lateFeeRepository;

    @Autowired
    BlockchainGatway blockchainGatway;

    @Autowired
    QrCodeService qrCodeService;

    @Autowired
    CertificatePrintRepository certificatePrintRepository;

    @Autowired
    SlaDetailsService slaDetailsService;

    @Autowired
    AuthServiceImpl authService;

    @Autowired
    OrganizationRepository organizationRepository;

    @Autowired
    FileStorageProperties fileStorageProperties;

    private Path fileStorageLocation;

    @Autowired
    CitizenSBirthRepository citizenSbirthRepository;

    @Override
    @Transactional
    public ApiResponse saveSBirthRecords(SBirthDto birthDto, HttpServletRequest request, MultipartFile sdmLetterImage) throws Exception {
        LocalDateTime startTime = LocalDateTime.now();
        logger.info("saveSBirthRecords startTime " + startTime);
        // JwtUtil jwtUtil = new JwtUtil();
        String username = authService.getUserIdFromRequest(request);
        ApiResponse res = new ApiResponse();
        SBirthModel birthModel = new SBirthModel();
        Optional<UserModel> currentUserOp = authRepository.findById(Long.parseLong(username));
        UserModel currentUser = currentUserOp.get();

        //Get Organization detail from user organization Id
        OrganizationModel orgModel = getOrganizationFromOrgId(currentUser.getOrganizationId());
        String orgType = orgModel.getOrganizationType();
        String orgCode = orgModel.getOrganisationCode();
        String divisionCode = orgModel.getDivisionCode();

        //BeanUtils.copyProperties(birthDto, birthModel);
        CustomBeanUtils.copySBirthDetailsForUpdate(birthDto, birthModel);
        LocalDateTime now = LocalDateTime.now();
        birthModel.setRegistrationDatetime(
                birthDto.getRegistrationDate() == null ? now : birthDto.getRegistrationDate().atStartOfDay());
        birthModel.setCreatedAt(now);
        // getDivisionCode is not coming for UI then insert divisionCode
        if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CREATOR))
                || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_APPROVER)))
        {
            birthModel.setDivisionCode(divisionCode);
        }else{
            birthModel.setDivisionCode(birthDto.getDivisionCode() == null ?  "0" : birthDto.getDivisionCode());
        }

        if (Constants.RECORD_STATUS_PENDING.equals(birthDto.getStatus())
                && ((birthModel.getApplicationNumber() == null
                || birthModel.getApplicationNumber().trim().isEmpty())
                || (birthModel.getRegistrationNumber() == null
                || birthModel.getRegistrationNumber().trim().isEmpty()))) {
            ApplicationNumberCounter counter = applicatioNumberCounterService.getRegistrationNumberCounter(
                orgModel.getOrganizationId(),orgModel.getOrganisationCode(), Constants.APPLICATION_TYPE_STILL_BIRTH,
                    birthModel.getRegistrationDatetime());
            String regNo = counter.getCount() + "";
            String applNo = applicatioNumberCounterService.generateApplicationNumber(counter);
            birthModel.setApplicationNumber(applNo);
            // birthModel.setRegistrationNumber(regNo);
            //birthModel.setRegistrationNumber(birthDto.getRegistrationNumber());
            birthModel.setRegistrationNumber(birthDto.getRegistrationNumber() == null ? regNo : birthDto.getRegistrationNumber());


                /*@AUTHOR DEEPAK
        Dated: 07-09-22
        * If Record is from Citizen then Update their status from PENDING  to SUBMITTED
        * */
            if(birthDto.getSbirthIdTemp() != null){
                Optional<CitizenSBirthModel> citizenBirthModelOp = citizenSbirthRepository.findById(birthDto.getSbirthIdTemp());
                CitizenSBirthModel citizenBirthModel = citizenBirthModelOp.get();
                if(citizenBirthModelOp.isPresent() && birthDto.getOrganizationCode().equalsIgnoreCase(citizenBirthModel.getOrganizationCode())) {
                    updateCitizenRecords(birthDto, username);
                }
            }
        }

            birthModel.setRegistrationNumber(birthDto.getRegistrationNumber() == null ? birthModel.getRegistrationNumber() : birthDto.getRegistrationNumber());

        birthModel.setModifiedAt(now);
        birthModel.setCreatedAt(now);
        birthModel.setModifiedBy(username);
        birthModel.setUserId(username);
        Long organizationId = authService.getOrganizationIdFromUserId(username);
        birthModel.setOrganizationId(organizationId);
        birthModel.setStatus(birthDto.getStatus().toUpperCase(Locale.ROOT));
        long numberOfDays = 0;
        if (birthDto.getEventDate() != null) {
            numberOfDays = CommonUtil.getDayBetweenDates(birthDto.getEventDate().toLocalDate(), LocalDate.now());
            // logger.info("===== numberOfDays ======"+numberOfDays);
        }

        Optional<LateFee> existedFeeData = lateFeeRepository.findById(Constants.APPLICATION_TYPE_STILL_BIRTH);
        if (Constants.USER_TYPE_CFC.equalsIgnoreCase(orgModel.getOrganizationType())) {
            existedFeeData = lateFeeRepository.findById(Constants.APPLICATION_TYPE_CFC_STILL_BIRTH);
        }
        if (Constants.USER_TYPE_CFC.equalsIgnoreCase(orgModel.getOrganizationType())
                && Constants.RECORD_TYPE_OLD.equalsIgnoreCase(birthDto.getRecordType())) {
            birthModel.setLateFee(0F);

        } else if (Constants.USER_TYPE_CFC.equalsIgnoreCase(orgModel.getOrganizationType())
                && (birthDto.getRecordType() == null ||
                        !Constants.RECORD_TYPE_OLD.equalsIgnoreCase(birthDto.getRecordType().trim()))
                && numberOfDays > existedFeeData.get().getEndDays()) {
            // Check for SDM ORDER
            if (birthDto.getSdmLetterNo() != null && !birthDto.getSdmLetterNo().trim().isEmpty()) {
                birthModel.setLateFee(existedFeeData.get().getFee());
            } else {
                res.setMsg(Constants.SDM_LETTER_BLANK);
                res.setStatus(HttpStatus.NOT_FOUND);
                return res;
            }
        }
       else if (numberOfDays > existedFeeData.get().getEndDays()) {
            // After 365 Days Birth Record entry
            res.setMsg(Constants.VISIT_CFC);
            res.setStatus(HttpStatus.BAD_REQUEST);

            return res;

        }

        else if (numberOfDays > existedFeeData.get().getStartDays()
                && numberOfDays <= existedFeeData.get().getEndDays()) {
            // Between 21 - 365 Days Birth Record entry
            birthModel.setLateFee(existedFeeData.get().getFee());
        } else {
            // Within 21 days Birth Record entry
            birthModel.setLateFee(0F);
        }

        if(sdmLetterImage != null && !sdmLetterImage.isEmpty()) {
            String fileNameold = StringUtils.cleanPath(sdmLetterImage.getOriginalFilename());
            String extension = fileNameold.substring(fileNameold.lastIndexOf("."));
            String fileName = "sdm_letter_" + username + "_" + Constants.APPLICATION_TYPE_STILL_BIRTH + System.currentTimeMillis() + extension;
            //byte [] digest = new DigestUtils(DigestUtils.getSha3_224Digest()).digest(dataToDigest);
            String fileHash = new DigestUtils(DigestUtils.getMd5Digest()).digestAsHex(sdmLetterImage.getInputStream());
            // logger.info("sdm leter  image name " + fileName);
            // logger.info("sdm leter  image hash code " + fileHash);

            Path fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();;
            //String uploadDir = fileStorageLocation;
            CommonUtil.saveFile(fileStorageLocation, fileName, sdmLetterImage);

            birthModel.setSdmLetterImage(fileName);
            birthModel.setSdmLetterImageHash(fileHash);
        }

        // logger.info("Before save data in SBirth table ====== " + LocalDateTime.now());
        // logger.info("Requested Body O Sbirth model " + birthModel);
        birthModel.setTransactionType(Constants.RECORD_ADDED);
        birthModel = birthRepository.save(birthModel);
        // logger.info("After save data in SBirth table ====== " + LocalDateTime.now());


        // Add birth history
        SBirthHistoryModel birthHistoryModel = new SBirthHistoryModel();
        birthHistoryModel.setTransactionType(Constants.RECORD_ADDED);
        BeanUtils.copyProperties(birthModel, birthHistoryModel);
        // logger.info("Before save data in SBirth_history table ====== " + now);
        birthHistoryModel = birthHistoryRepository.save(birthHistoryModel);
        // logger.info("After save data in SBirth_history table ====== " + now);
        // logger.info("Before Blockchain calling ===== " + now);
        // blockchainGatway.modifyRecord(birthModel);
        // try {
            LocalDateTime callinsertStillBirthRecordTime = LocalDateTime.now();
            logger.info("Before calling insertStillBirthRecord method ===== " + callinsertStillBirthRecordTime);
            BlockchainStillBirthResponse blockchainResult = blockchainGatway.insertStillBirthRecord(birthModel,
                    channelGovtHospital);
            // logger.info("After calling insertStillBirthRecord method ===== " + LocalDateTime.now());

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


                try{
                    // Send Text message to Approver for particular Organization
                    String applNumber = birthModel.getApplicationNumber();
                    List<UserModel> approverUsers = authService.findApproverUserDetailsUserId(currentUser.getUserId().toString());
                    approverUsers.stream().forEach( e -> {
                        CommonUtil.sendEventMessage(e.getFirstName(), applNumber, e.getContactNo(), Constants.NEW_APPROVAL_REQUEST);
                    });
                }catch (Exception e){
                   // logger.info(e.getMessage()) ;
                }
            // } catch (Exception e) {
            //     logger.error("===Blc Exception ===", e);
            //     String message = e.getMessage();
            //     String status = Constants.BLC_STATUS_FALSE;
            //     message = message.length() > 1000 ? message.substring(0, 1000) : message;
            //     birthModel.setBlcMessage(message);

            //     birthModel.setBlcStatus(status);

            //     birthHistoryModel.setBlcMessage(message);
            //     birthHistoryModel.setBlcStatus(status);
            // }

            birthModel = birthRepository.save(birthModel);
            birthHistoryModel = birthHistoryRepository.save(birthHistoryModel);

                /* Code to send new Record text message to parents
                 * Code by Deepak
                 * 29-04-2022
                 * */
                if(birthModel != null && birthModel.getApplicationNumber() !=null
                        && !CommonUtil.checkNullOrBlank(birthModel.getContactNumber())){
                    CommonUtil commonUtil = new CommonUtil();
                    String type = "/S";
                    String originalUrl = Constants.REVIEW_UAT_URL+birthModel.getApplicationNumber()+type;
                    logger.info("  === Sbirth addition response ====="+birthModel);
                    try{
                    commonUtil.sendTextMessage(birthModel.getMotherName(), birthModel.getContactNumber(), birthModel.getApplicationNumber(), Constants.RECORD_TYPE_SBIRTH, Constants.NEW_APPROVAL_REQUEST, "", "", originalUrl, "", "");
                    }catch (Exception e){
                        logger.info("===SMS EXCEPTION =="+e);
                    }
                }
            res.setMsg(Constants.SBIRTH_SUCCESS_MESSAGE);
            res.setStatus(HttpStatus.OK);
            res.setData(birthModel);
        } else {
            
            // logger.info("insertStillBirthRecord False response from blockchain " + blockchainResult);
            throw new Exception(Constants.INTERNAL_SERVER_ERROR);
        }

        LocalDateTime endTime = LocalDateTime.now();
        logger.info("End time of insertStillBirthRecord method ===== " + endTime);
        return res;

    }

    private OrganizationModel getOrganizationFromOrgId(String orgId) {

        Optional<OrganizationModel> organizationModelOp = organizationRepository.findById(Long.parseLong(orgId));
        OrganizationModel organizationModel = organizationModelOp.get();
        return organizationModel;
    }

    @Override
    @Transactional
    public ApiResponse updateSBirthRecords(SBirthDto birthDto, HttpServletRequest request, MultipartFile sdmLetterImage) throws Exception {
        LocalDateTime startTime = LocalDateTime.now();
        logger.info("Start time of updateSBirthRecords method ===== " + startTime);
        Optional<SBirthModel> existedData = birthRepository.findById(birthDto.getSbirthId());
        ApiResponse res = new ApiResponse();

        // JwtUtil jwtUtil = new JwtUtil();
        String username = authService.getUserIdFromRequest(request);
        Optional<UserModel> currentUserOp = authRepository.findById(Long.parseLong(username));
        UserModel currentUser = currentUserOp.get();
        // BirthModel birthModel = new BirthModel();
        SBirthModel birthModel = existedData.get();
        // logger.info("Requested Body " + birthModel);

        //Get Organization detail from user organization Id
        OrganizationModel orgModel = getOrganizationFromOrgId(currentUser.getOrganizationId());
        String divisionCode = orgModel.getDivisionCode();

        if (existedData.equals(Optional.empty())) {
            res.setMsg(Constants.RECORD_NOT_FOUND);
            res.setStatus(HttpStatus.BAD_REQUEST);
            // logger.info("Existed Data -------------------------------------------++++ " + existedData);
        } else {

            if (birthModel.getStatus().equals(approvedStatus)) {
                res.setMsg(Constants.NOT_PERMITTED);
                res.setStatus(HttpStatus.BAD_REQUEST);

            } else {
                // BeanUtils.copyProperties(birthDto, birthModel);
                CustomBeanUtils.copySBirthDetailsForUpdate(birthDto, birthModel);

                LocalDateTime now = LocalDateTime.now();
                birthModel.setRegistrationDatetime(
                        birthDto.getRegistrationDate() == null ? now : birthDto.getRegistrationDate().atStartOfDay());
                birthModel.setCreatedAt(now);

                // getDivisionCode is not coming for UI then insert divisionCode
                if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CREATOR))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_APPROVER)))
                {
                    birthModel.setDivisionCode(divisionCode);
                }else{
                    birthModel.setDivisionCode(birthDto.getDivisionCode() == null ?  "0" : birthDto.getDivisionCode());
                }
                if (Constants.RECORD_STATUS_PENDING.equals(birthDto.getStatus())
                        && ((birthModel.getApplicationNumber() == null
                        || birthModel.getApplicationNumber().trim().isEmpty())
                        || (birthModel.getRegistrationNumber() == null
                        || birthModel.getRegistrationNumber().trim().isEmpty()))) {
                    ApplicationNumberCounter counter = applicatioNumberCounterService.getRegistrationNumberCounter(
                        orgModel.getOrganizationId(),orgModel.getOrganisationCode(), Constants.APPLICATION_TYPE_STILL_BIRTH,
                            birthModel.getRegistrationDatetime());
                    String regNo = counter.getCount() + "";
                    String applNo = applicatioNumberCounterService.generateApplicationNumber(counter) ;
                    birthModel.setApplicationNumber(applNo);
                    // birthModel.setRegistrationNumber(regNo);
                    birthModel.setRegistrationNumber(birthDto.getRegistrationNumber() == null ? regNo : birthDto.getRegistrationNumber());

                    //birthModel.setRegistrationNumber(birthDto.getRegistrationNumber());
                }else if (Constants.RECORD_STATUS_PENDING.equalsIgnoreCase(birthDto.getStatus())) {
                    birthModel.setRegistrationNumber(birthDto.getRegistrationNumber() == null ? birthModel.getRegistrationNumber() : birthDto.getRegistrationNumber());
                }
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_WITH_TIME);
                birthModel.setModifiedAt(now);
                birthModel.setModifiedBy(username);
                long numberOfDays =0;
                if(birthDto.getEventDate() != null) {
                    numberOfDays = CommonUtil.getDayBetweenDates(birthDto.getEventDate().toLocalDate(), LocalDate.now());
                }

                Optional<LateFee> existedFeeData = lateFeeRepository.findById(Constants.APPLICATION_TYPE_STILL_BIRTH);
                if (Constants.USER_TYPE_CFC.equalsIgnoreCase(orgModel.getOrganizationType())) {
                    existedFeeData = lateFeeRepository.findById(Constants.APPLICATION_TYPE_CFC_STILL_BIRTH);
                }
                if (Constants.USER_TYPE_CFC.equalsIgnoreCase(orgModel.getOrganizationType())
                        && Constants.RECORD_TYPE_OLD.equalsIgnoreCase(birthDto.getRecordType())) {
                    birthModel.setLateFee(0F);

                } else if (Constants.USER_TYPE_CFC.equalsIgnoreCase(orgModel.getOrganizationType())
                        && (birthDto.getRecordType() == null ||
                        !Constants.RECORD_TYPE_OLD.equalsIgnoreCase(birthDto.getRecordType().trim()))
                        && numberOfDays > existedFeeData.get().getEndDays()) {
                    // Check for SDM ORDER
                    if (birthDto.getSdmLetterNo() != null && !birthDto.getSdmLetterNo().trim().isEmpty()) {
                        birthModel.setLateFee(existedFeeData.get().getFee());
                    } else {
                        res.setMsg(Constants.SDM_LETTER_BLANK);
                        res.setStatus(HttpStatus.NOT_FOUND);
                        return res;
                    }
                }
                else if (numberOfDays > existedFeeData.get().getEndDays()) {
                    // After 365 Days Birth Record entry
                    res.setMsg(Constants.VISIT_CFC);
                    res.setStatus(HttpStatus.BAD_REQUEST);

                    return res;

                }

                else if (numberOfDays > existedFeeData.get().getStartDays()
                        && numberOfDays <= existedFeeData.get().getEndDays()) {
                    // Between 21 - 365 Days Birth Record entry
                    birthModel.setLateFee(existedFeeData.get().getFee());
                } else {
                    // Within 21 days Birth Record entry
                    birthModel.setLateFee(0F);
                }


                if(sdmLetterImage != null && !sdmLetterImage.isEmpty()) {
                    String fileNameold = StringUtils.cleanPath(sdmLetterImage.getOriginalFilename());
                    String extension = fileNameold.substring(fileNameold.lastIndexOf("."));
                    String fileName = "sdm_letter_" + username + "_" + Constants.APPLICATION_TYPE_STILL_BIRTH + System.currentTimeMillis() + extension;
                    //byte [] digest = new DigestUtils(DigestUtils.getSha3_224Digest()).digest(dataToDigest);
                    String fileHash = new DigestUtils(DigestUtils.getMd5Digest()).digestAsHex(sdmLetterImage.getInputStream());
                    // logger.info("sdm leter  image name " + fileName);
                    // logger.info("sdm leter  image hash code " + fileHash);

                    Path fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();;
                    //String uploadDir = fileStorageLocation;
                    CommonUtil.saveFile(fileStorageLocation, fileName, sdmLetterImage);

                    birthModel.setSdmLetterImage(fileName);
                    birthModel.setSdmLetterImageHash(fileHash);
                }

                // birthModel.setModifiedBy(birthDto.getModifiedBy());
                birthModel.setModifiedBy(username);
                birthModel.setTransactionType(Constants.RECORD_UPDATED);
                birthModel = birthRepository.save(birthModel);

                // Add birth history
                SBirthHistoryModel birthHistoryModel = new SBirthHistoryModel();
                birthHistoryModel.setTransactionType(Constants.RECORD_UPDATED);
                BeanUtils.copyProperties(birthModel, birthHistoryModel);
                birthHistoryModel = birthHistoryRepository.save(birthHistoryModel);

                // BLOCKCHAIN CALL
                // blockchainGatway.updateStillBirthRecord(birthModel, channelGovtHospital);

                // try {

                    LocalDateTime callupdateStillBirthRecordTime = LocalDateTime.now();
                    logger.info("Call updateStillBirthRecord method ===== " + callupdateStillBirthRecordTime);
                    BlockchainUpdateSBirthResponse blockchainResult = blockchainGatway.updateStillBirthRecord(birthModel,
                            channelGovtHospital);
                    // logger.info("after updateStillBirthRecord method ===== " + LocalDateTime.now());
                    // logger.info("After data addition in Blockchain ==== " + now);

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
                    //     logger.error("===Blc Exception ===", e);
                    //     String message = e.getMessage();
                    //     String status = Constants.BLC_STATUS_FALSE;
                    //     message = CommonUtil.updateExceptionMessage(message);
                    //     birthModel.setBlcMessage(message);

                    //     birthModel.setBlcStatus(status);

                    //     birthHistoryModel.setBlcMessage(message);
                    //     birthHistoryModel.setBlcStatus(status);
                    // }
                    // BeanUtils.copyProperties(birthModel, birthDto);
                    birthModel = birthRepository.save(birthModel);
                    birthHistoryModel = birthHistoryRepository.save(birthHistoryModel);

                    res.setMsg(Constants.SBIRTH_UPDATE_SUCCESS_MESSAGE);
                    res.setStatus(HttpStatus.OK);
                    res.setData(birthModel.getApplicationNumber());
                } else {
                    // logger.info("updateStillBirthRecord False response from blockchain " + blockchainResult);
                    throw new Exception(Constants.INTERNAL_SERVER_ERROR);
                }
            }
        }
        LocalDateTime endTime = LocalDateTime.now();
        logger.info("updateStillBirthRecord endTime " + endTime);
        return res;

    }



    @Override
    public List<SBirthModel> getBirthRecords(String status, String orgCode) {
        return birthRepository.getSBirthDataByStatusAndOrganization(status, orgCode);
    }

    @Override
    public ApiResponse getSBirthDetails(Long sbirthId) throws Exception {
        LocalDateTime startTime = LocalDateTime.now();
        logger.info("getSBirthDetails startTime " + startTime);
        ApiResponse res = new ApiResponse();
        Optional<SBirthModel> existedData = birthRepository.findById(sbirthId);
        if (existedData.equals(Optional.empty())) {
            res.setMsg(Constants.RECORD_NOT_FOUND);
            res.setStatus(HttpStatus.BAD_REQUEST);
        } else {
            res.setStatus(HttpStatus.OK);
            // res.setData(existedData);
            // blockchainGatway.getStillBirthRecord(sbirthId.toString(),
            // channelGovtHospital);
            // logger.info("get details of BirthId ===: " + sbirthId.toString());
            SBirthModel blcModel = null;
            // try {
                LocalDateTime callgetStillBirthRecordTime = LocalDateTime.now();
                 logger.info("Call getStillBirthRecord method ===== " + callgetStillBirthRecordTime);
                String blcResponse = blockchainGatway.getStillBirthRecord(sbirthId.toString(), channelGovtHospital);

                // logger.info("====Birth details response ===" + blcResponse);
                // logger.info("after getStillBirthRecord method ===== " + LocalDateTime.now());
                blcModel = JsonUtil.getObjectFromJson(blcResponse, SBirthModel.class);
            // } catch (Exception e) {
            //     e.printStackTrace();
            // }
            if (blcModel == null) {
                // logger.info("getStillBirthRecord blcModel is null ===" + blcResponse);
                throw new Exception(Constants.INTERNAL_SERVER_ERROR);
            }
            res.setData(blcModel);
        }
        LocalDateTime endTime = LocalDateTime.now();
         logger.info("getSBirthDetails endTime " + endTime);
        return res;

    }

    @Override
    public ApiResponse getAllSBirthRecords() {
        ApiResponse apiResponse = new ApiResponse();
        List<SBirthModel> list = birthRepository.findAll();
        apiResponse.setStatus(HttpStatus.OK);
        apiResponse.setData(list);
        return apiResponse;
    }

    @Override
    @Transactional
    public ApiResponse updateSBirthRecordStatus(Long sbirthId, String status, String remarks,
            HttpServletRequest request) throws Exception {
        LocalDateTime startTime = LocalDateTime.now();
        logger.info("updateSBirthRecordStatus startTime " + startTime);
        // JwtUtil jwtUtil = new JwtUtil();
        ApiResponse res = new ApiResponse();
        Optional<SBirthModel> existedData = birthRepository.findById(sbirthId);
        // boolean existedData = birthRepository.existsById(birthId);

        if (existedData.equals(Optional.empty())) {
            res.setMsg(Constants.RECORD_NOT_FOUND);
            res.setStatus(HttpStatus.BAD_REQUEST);
        } else {
            SBirthModel sbirthModel = existedData.get();
            // logger.info("status ----- " + sbirthModel.getStatus());
            String applicationNumber = sbirthModel.getApplicationNumber();
            String userName = authService.getUserIdFromRequest(request);

            Optional<UserModel> currentUserOp = authRepository.findById(Long.parseLong(userName));
            UserModel currentUser = currentUserOp.get();

            //Get Organization detail from user organization Id
            OrganizationModel orgModel = getOrganizationFromOrgId(currentUser.getOrganizationId());

            String organizationCode = orgModel.getOrganisationCode();
            LocalDateTime now = LocalDateTime.now();

             logger.info("CURRENT USER ORG CODE  ====" + organizationCode + ":==== DATA ORG CODE==="
                    + sbirthModel.getOrganizationCode());
            if (!sbirthModel.getStatus().equalsIgnoreCase(Constants.RECORD_STATUS_PENDING)) {
                res.setMsg(Constants.BIRTH_STATUS_NOT_PENDING);
                res.setStatus(HttpStatus.BAD_REQUEST);
            } else if (!sbirthModel.getOrganizationCode().equalsIgnoreCase(organizationCode)
                    && !Constants.ORG_NDMC.equalsIgnoreCase(organizationCode)) {
                res.setStatus(HttpStatus.BAD_REQUEST);
                res.setMsg("YouR ORGANISATION IS NOT SAME");
            }

            else {

                if (Constants.RECORD_STATUS_APPROVED.equalsIgnoreCase(status)) {

                    if (approveUpdateStatus(sbirthModel, userName, now)) {
                        res.setStatus(HttpStatus.OK);
                        res.setMsg(Constants.RECORD_APPROVED_MESSAGE);
                        res.setData(sbirthModel.getApplicationNumber());
                    }

                } else if (Constants.RECORD_STATUS_REJECTED.equalsIgnoreCase(status)) {
                    sbirthModel.setRejectedBy(userName);
                    sbirthModel.setRejectedAt(now);
                    sbirthModel.setStatus(Constants.RECORD_STATUS_REJECTED);
                    sbirthModel.setRejectionRemark(remarks);
//                    sbirthModel.setEventPlace(sbirthModel.getEventPlace());
//                    sbirthModel.setEventPlaceFlag(sbirthModel.getEventPlaceFlag());
//                    logger.info("SBIRTH MODEL AFTER RJECT RECORD " + sbirthModel);
                    sbirthModel.setTransactionType(Constants.RECORD_REJECTED);
                    sbirthModel = birthRepository.save(sbirthModel);

                    // int sqlResponse = birthRepository.rejectBirthStatusByBirthId(birthId,
                    // Constants.RECORD_STATUS_REJECTED, birthModel.getRejectedBy(),
                    // birthModel.getRejectedAt());
                    if (sbirthModel != null) {

                        // Add birth history
                        SBirthHistoryModel sBirthHistoryModel = new SBirthHistoryModel();
                        sBirthHistoryModel.setTransactionType(Constants.RECORD_REJECTED);
                        BeanUtils.copyProperties(sbirthModel, sBirthHistoryModel);
                        sBirthHistoryModel = birthHistoryRepository.save(sBirthHistoryModel);
                        res.setStatus(HttpStatus.OK);
                        res.setMsg(Constants.RECORD_REJECTED_MESSAGE);
                        res.setData(sbirthModel.getApplicationNumber());
                        logger.info("Birth Rejection Payload  birthId===" + sbirthId.toString() + "=== Rejected By "
                                + userName + "=== Rejected at " + CommonUtil.convertDateTimeFormat(now));
                        // Need to implement try catch to handle exception in blockchain method

                        // try {
                            LocalDateTime callrejectStillBirthRecordTime = LocalDateTime.now();
                            logger.info("Call rejectStillBirthRecord method ===== " + callrejectStillBirthRecordTime);
                            BlockchainRejectBirthResponse blockchainResult = blockchainGatway.rejectStillBirthRecord(
                                    sbirthId.toString(), userName, CommonUtil.convertDateTimeFormat(now), remarks, channelGovtHospital);
                            // logger.info("After data addition in Blockchain ==== " + now);
                            // JsonUtil.getObjectFromJson(blockchainResult, BlockchainGatway.class);
                            // logger.info("after rejectStillBirthRecord method ===== " + LocalDateTime.now());
                            // Set Blockchain response
                            String message = blockchainResult.getMessage();
                            String txID = blockchainResult.getTxID();
                            String blcStatus = blockchainResult.getStatus();
                            if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(blcStatus)) {
                                sbirthModel.setBlcMessage(message);
                                sbirthModel.setBlcTxId(txID);
                                sbirthModel.setBlcStatus(blcStatus);

                                sBirthHistoryModel.setBlcMessage(message);
                                sBirthHistoryModel.setBlcTxId(txID);
                                sBirthHistoryModel.setBlcStatus(blcStatus);


                                // } catch (Exception e) {
                            //     logger.error("===Blc Exception ===", e);

                            // }
                            sbirthModel = birthRepository.save(sbirthModel);
                            sBirthHistoryModel = birthHistoryRepository.save(sBirthHistoryModel);

                                /* Code to send Rejection text message to parents
                                 * Code by Deepak
                                 * 29-04-2022
                                 * */
                                if(sbirthModel != null && sbirthModel.getApplicationNumber() !=null
                                        && !CommonUtil.checkNullOrBlank(sbirthModel.getContactNumber())){
                                    CommonUtil commonUtil = new CommonUtil();

                                    logger.info("  === birth addition response ====="+sbirthModel);
                                    try{
                                    commonUtil.sendTextMessage(sbirthModel.getMotherName(), sbirthModel.getContactNumber(), sbirthModel.getApplicationNumber(), Constants.RECORD_TYPE_SBIRTH, Constants.REQUEST_REJECTED, "", "","", "", "");
                                    }catch (Exception e){
                                        logger.info("===SMS EXCEPTION =="+e);
                                    }
                                    }
                        } else {
                            logger.error("updateSBirthRecordStatus Flase response from blockchain ", blockchainResult);
                            throw new Exception(Constants.INTERNAL_SERVER_ERROR);
                        } 
                    }

                }
                res.setMsg(Constants.SBIRTH_UPDATE_SUCCESS_MESSAGE);
                res.setStatus(HttpStatus.OK);
                res.setData(sbirthModel.getApplicationNumber());
            }
        }
        LocalDateTime endTime = LocalDateTime.now();
        logger.info("updateSBirthRecordStatus End Time ===== " + endTime);
        return res;

    }

    private boolean approveUpdateStatus(SBirthModel sbirthModel, String userName, LocalDateTime now) throws Exception {
        LocalDateTime startTime = LocalDateTime.now();
        logger.info("approveUpdateStatus Start Time ===== " + startTime);
        sbirthModel.setApprovedBy(userName);
        sbirthModel.setApprovedAt(now);
        sbirthModel.setStatus(Constants.RECORD_STATUS_APPROVED);
        sbirthModel.setTransactionType(Constants.RECORD_APPROVED);
//        sbirthModel.setEventPlace(sbirthModel.getEventPlace());
//        sbirthModel.setEventPlaceFlag(sbirthModel.getEventPlaceFlag());
//        logger.info("SBIRTH MODEL AFTER APPROVED RECORD " + sbirthModel);
        sbirthModel = birthRepository.save(sbirthModel);
        // int sqlResponse = birthRepository.approveBirthStatusByBirthId(birthId,
        // Constants.RECORD_STATUS_APPROVED, birthModel.getApprovedBy(),
        // birthModel.getApprovedAt());

        if (sbirthModel != null) {


            // Add birth history
            SBirthHistoryModel sBirthHistoryModel = new SBirthHistoryModel();
            sBirthHistoryModel.setTransactionType(Constants.RECORD_APPROVED);
            BeanUtils.copyProperties(sbirthModel, sBirthHistoryModel);
            sBirthHistoryModel = birthHistoryRepository.save(sBirthHistoryModel);
             logger.info("Birth Approval Payload  birthId===" + sbirthModel.getSbirthId().toString() + "=== Approved By "
                    + userName + "=== Rejected at " + now.toString());

            // try {
                LocalDateTime callApproveStillBirthRecordTime = LocalDateTime.now();
                logger.info("Call approveStillBirthRecord method ===== " + callApproveStillBirthRecordTime);
                BlockchainApproveBirthResponse blockchainResult = blockchainGatway.approveStillBirthRecord(
                        sbirthModel.getSbirthId().toString(), userName, CommonUtil.convertDateTimeFormat(now), channelGovtHospital);
                // logger.info("After data addition in Blockchain ==== " + now);
                // JsonUtil.getObjectFromJson(blockchainResult, BlockchainGatway.class);
                // logger.info("after approveStillBirthRecord method ===== " + LocalDateTime.now());

                // Set Blockchain response
                String message = blockchainResult.getMessage();
                String txID = blockchainResult.getTxID();
                String blcStatus = blockchainResult.getStatus();
                if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(blcStatus)) {
                    sbirthModel.setBlcMessage(message);
                    sbirthModel.setBlcTxId(txID);
                    sbirthModel.setBlcStatus(blcStatus);

                    sBirthHistoryModel.setBlcMessage(message);
                    sBirthHistoryModel.setBlcTxId(txID);
                    sBirthHistoryModel.setBlcStatus(blcStatus);

                    // Send Approved request to the Guardian registered Mobile number
                    try{
                        CommonUtil.sendEventMessage(sbirthModel.getMotherName(), sbirthModel.getApplicationNumber(), sbirthModel.getContactNumber(), Constants.APPROVED_REQ_MSG);
                    }catch (Exception e){
                        // logger.info(e.getMessage()) ;
                    }
                // } catch (Exception e) {
                //     logger.error("===Blc Exception ===", e);
                //     String message = e.getMessage();
                //     String blcStatus = Constants.BLC_STATUS_FALSE;
                //     message = CommonUtil.updateExceptionMessage(message);
                //     sbirthModel.setBlcMessage(message);

                //     sbirthModel.setBlcStatus(blcStatus);

                //     sBirthHistoryModel.setBlcMessage(message);
                //     sBirthHistoryModel.setBlcStatus(blcStatus);
                // }

                sbirthModel = birthRepository.save(sbirthModel);
                sBirthHistoryModel = birthHistoryRepository.save(sBirthHistoryModel);

                    /* Code to send Approval text message to parents
                     * Code by Deepak
                     * 29-04-2022
                     * */
                    if(sbirthModel != null && sbirthModel.getApplicationNumber() !=null
                            && !CommonUtil.checkNullOrBlank(sbirthModel.getContactNumber())){
                        CommonUtil commonUtil = new CommonUtil();

                        logger.info("  === birth addition response ====="+sbirthModel);
                        try{
                        commonUtil.sendTextMessage(sbirthModel.getMotherName(), sbirthModel.getContactNumber(), sbirthModel.getApplicationNumber(), Constants.RECORD_TYPE_SBIRTH, Constants.REQUEST_APPROVED, "", "","", "", "");
                        }catch (Exception e){
                            logger.info("===SMS EXCEPTION =="+e);
                        }
                        }
            } else {
                logger.error("updateSBirthRecordStatus Flase response from blockchain ", blockchainResult);
                throw new Exception(Constants.INTERNAL_SERVER_ERROR);
            }
            
            LocalDateTime endTime1 = LocalDateTime.now();
            // logger.info("approveUpdateStatus End Time ===== " + endTime1);
            return true;

        }
        LocalDateTime endTime = LocalDateTime.now();
        logger.info("approveUpdateStatus End Time ===== " + endTime);
        return false;
    }

    @Override
    public ApiResponse getUsersSBirthRecords(HttpServletRequest request) throws Exception {
        LocalDateTime startTime = LocalDateTime.now();
        logger.info("getUsersSBirthRecords Start Time ===== " + startTime);
        ApiResponse response = new ApiResponse();
        // JwtUtil jwtUtil = new JwtUtil();
        String userName = authService.getUserIdFromRequest(request);
        Optional<UserModel> currentUserOp = authRepository.findById(Long.parseLong(userName));
        UserModel currentUser = currentUserOp.get();
        //Get Organization detail from user organization Id
        OrganizationModel orgModel = getOrganizationFromOrgId(currentUser.getOrganizationId());

        String organizationCode = orgModel.getOrganisationCode();
        Long userId = currentUser.getUserId();

        if (currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CREATOR))
                || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_CREATOR))) {
            // logger.info("Current USER IS CREATOR ===== " + currentUser);
//            List<SBirthModel> data = birthRepository.getCreatorRecords(userId, organizationCode,
//                    Constants.RECORD_STATUS_PENDING, Constants.RECORD_STATUS_REJECTED,
//                    Constants.RECORD_STATUS_DRAFT);

            List<SBirthModel> data = birthRepository.getApproverRecords(organizationCode,
                    Constants.RECORD_STATUS_PENDING);
            if (data == null) {
                response.setStatus(HttpStatus.NOT_FOUND);
                response.setMsg(Constants.RECORD_NOT_FOUND);
            } else {
                data = recordApprovalTime(data);
                response.setStatus(HttpStatus.OK);
                response.setData(data);
            }
        }
        if (currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_APPROVER))
                || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_APPROVER))) {
            // logger.info("Current USER IS Approver ===== " + currentUser);
            List<SBirthModel> data = birthRepository.getApproverRecords(organizationCode,
                    Constants.RECORD_STATUS_PENDING);
            if (data == null) {
                response.setStatus(HttpStatus.NOT_FOUND);
                response.setMsg(Constants.RECORD_NOT_FOUND);
            } else {
                data = recordApprovalTime(data);
                response.setStatus(HttpStatus.OK);
                response.setData(data);
            }
        }
        if (currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_ADMIN))) {
            // logger.info("Current USER IS Approver ===== " + currentUser);
            List<SBirthModel> data = birthRepository.getAdminRecords(userId, Constants.RECORD_STATUS_PENDING,
                    Constants.RECORD_STATUS_REJECTED, Constants.RECORD_STATUS_DRAFT);
            if (data == null) {
                response.setStatus(HttpStatus.NOT_FOUND);
                response.setMsg(Constants.RECORD_NOT_FOUND);
            } else {
                data = recordApprovalTime(data);
                response.setStatus(HttpStatus.OK);
                response.setData(data);
            }
        }
        // logger.info("Current USER  ===== " + currentUser.getRoles().stream());
        LocalDateTime endTime = LocalDateTime.now();
        logger.info("getUsersSBirthRecords End Time ===== " + endTime);
        return response;
    }

    /*
     * This method return Number of days and hours for approval
     * In case of Pending status
     */
    private List<SBirthModel> recordApprovalTime(List<SBirthModel> data) throws Exception {

        if (data != null) {

            for (SBirthModel bModel : data) {

                if (Constants.RECORD_STATUS_PENDING.equalsIgnoreCase(bModel.getStatus())) {
                    String approvalTimeLeft = CommonUtil.getApprovalTimeLeft(bModel.getModifiedAt(),
                            LocalDateTime.now());
                    bModel.setApprovalTimeLeft(approvalTimeLeft);
                }
            }

        }
        return data;
    }

    @Override
    public ResponseEntity<byte[]> generateQrCode(Long printId, HttpServletRequest request) throws Exception {

        // ApiResponse response = new ApiResponse();
        Optional<CertificatePrintModel> existedData = certificatePrintRepository.findById(printId);
        if (!existedData.isPresent()) {
            return ResponseEntity.badRequest().body(null);
        } else {

            String url = vertificateVerificationUrl;
            // String qrCodeText = url +
            // URLEncoder.encode(Base64.getEncoder().encode(existedData.get().getApplicationNumber().getBytes()).toString(),
            // "UTF-8");
            String hexString = Hex.encodeHexString(existedData.get().getPrintApplicationNumber().getBytes());
            String qrCodeText = url + URLEncoder.encode(hexString, "UTF-8");

            byte[] qrImage = qrCodeService.createQrCode(qrCodeText, 200, 200);
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qrImage);

        }

    }

    @Scheduled(cron = "${scheduler.cronSync}", zone = "Asia/Kolkata")
    public void updateBirthRecordStatusCronSync() throws Exception {
        // logger.info("*** CRON START*****");
        String userName = "Cron";
        LocalDateTime now = LocalDateTime.now();
        List<SBirthModel> sBirthModels = birthRepository.getByStatusWithHour(Constants.RECORD_STATUS_PENDING,
                autoApproveHour);

        for (SBirthModel bm : sBirthModels) {
            cronApproveUpdateStatus(bm);

        }
        // logger.info("*** CRON END*****");
    }

    @Transactional
    private void cronApproveUpdateStatus(SBirthModel bm) throws Exception {
        // logger.info("updating for : " + bm.getSbirthId());
        approveUpdateStatus(bm, Constants.CRON, LocalDateTime.now());
        // logger.info("updation completed : " + bm.getSbirthId());
        
    }

    @Override
    public ApiResponse getHistoryFromBlc(Long birthId, HttpServletRequest request) throws Exception {
        LocalDateTime startTime = LocalDateTime.now();
        logger.info("getHistoryFromBlc Start Time ===== " + startTime);
        // BirthModel birthModel = new BirthModel();
        ApiResponse res = new ApiResponse();
        Optional<SBirthModel> birthModelOptional = birthRepository.findById(birthId);
        // JwtUtil jwtUtil = new JwtUtil();
        String username = authService.getUserIdFromRequest(request);
        if (!birthModelOptional.isPresent()) {

            // logger.info("Data not found for Birth history ==" + birthId);
            res.setStatus(HttpStatus.BAD_REQUEST);
            res.setMsg(Constants.RECORD_NOT_FOUND);
        } else {
            // logger.info("Data found for Birth history ==" + birthId);
            // try {
                LocalDateTime callgetStillBirthHistoryBySBirthIdTime = LocalDateTime.now();
                logger.info("callgetStillBirthHistoryBySBirthId Start Time ===== " + callgetStillBirthHistoryBySBirthIdTime);
                List<BlockchainStillBirthHistoryResponse> blockchainSBirthHistoryResponse = blockchainGatway
                        .getStillBirthHistoryBySBirthId(birthId.toString(), channelGovtHospital);

                // logger.info("Blockchai response for History ==== " + blockchainSBirthHistoryResponse);
                LocalDateTime callgetStillBirthHistoryBySBirthIdEndTime = LocalDateTime.now();
                logger.info("callgetStillBirthHistoryBySBirthId End Time ===== " + callgetStillBirthHistoryBySBirthIdEndTime);
                // To get List of Json data from Blockchain
                // List<BirthModel> birthModels = new ArrayList<BirthModel>();
                List<FetchHistoryFromBlc> fetchHistoryFromBlcs = new ArrayList<FetchHistoryFromBlc>();
                SBirthModel birthModelOld = null;

                String diff = Constants.BIRTH_HISTORY_DESCRIPTION_HEADER;

                for (int i = 0; i < blockchainSBirthHistoryResponse.size(); i++) {
                    // logger.info(" Response iteration from birth history ==" + blockchainSBirthHistoryResponse.get(i));
                    BlockchainStillBirthHistoryResponse historyResponse = (BlockchainStillBirthHistoryResponse) blockchainSBirthHistoryResponse
                            .get(i);
                    logger.info("=== history Response ====" + historyResponse);

                }

                for (BlockchainStillBirthHistoryResponse birthModelResponse : blockchainSBirthHistoryResponse) {
                    SBirthModel birthModel = birthModelResponse.getValue();
                    if (!Constants.RECORD_STATUS_DRAFT.equalsIgnoreCase(birthModel.getStatus())) {
                        FetchHistoryFromBlc fetchHistoryFromBlc = new FetchHistoryFromBlc();
                        fetchHistoryFromBlc.setDate(birthModel.getModifiedAt());
                        fetchHistoryFromBlc.setUserName(birthModel.getModifiedBy());
                        fetchHistoryFromBlc.setTransactionType(birthModel.getTransactionType());
                        if (Constants.RECORD_STATUS_PENDING.equalsIgnoreCase(birthModel.getStatus())) {
                            fetchHistoryFromBlc
                                    .setDescription(CommonUtil.getDifference(birthModelOld, birthModel, diff));
                        } else if (Constants.RECORD_STATUS_APPROVED.equalsIgnoreCase(birthModel.getStatus())) {
                            fetchHistoryFromBlc.setDescription(Constants.HISTORY_APPROVED_TEXT);

                            fetchHistoryFromBlc.setUserName(birthModel.getApprovedBy());
                            fetchHistoryFromBlc.setDate(birthModel.getApprovedAt());
                        } else if (Constants.RECORD_STATUS_REJECTED.equalsIgnoreCase(birthModel.getStatus())) {
                            fetchHistoryFromBlc
                                    .setDescription(Constants.HISTORY_REJECTION_TEXT + birthModel.getRejectionRemark());

                            fetchHistoryFromBlc.setUserName(birthModel.getRejectedBy());
                            fetchHistoryFromBlc.setDate(birthModel.getRejectedAt());
                        }
                        birthModelOld = birthModel;
                        fetchHistoryFromBlcs.add(fetchHistoryFromBlc);
                    }
                }

                res.setStatus(HttpStatus.OK);
                res.setData(fetchHistoryFromBlcs);
            // } catch (Exception e) {
            //     // Optional<BirthHistoryModel> birthHistoryModel =
            //     // birthHistoryRepository.findById(birthId);
            //     // if(birthHistoryModel.isPresent()){
            //     // logger.info(" ======= Birth History data in Exception ====
            //     // "+birthHistoryModel.get());
            //     // }

            //     FetchHistoryFromBlc fetchHistoryFromBlc = new FetchHistoryFromBlc();
            //     FetchHistoryFromBlc fetchHistoryFromBlc2 = new FetchHistoryFromBlc();

            //     fetchHistoryFromBlc.setDescription("Name added Tushar");
            //     fetchHistoryFromBlc.setDate(LocalDateTime.now());
            //     fetchHistoryFromBlc.setUserName(username);
            //     fetchHistoryFromBlc.setTransactionType("Record Addition");

            //     fetchHistoryFromBlc2.setDescription("Record Updated and name inclusion or correction");
            //     fetchHistoryFromBlc2.setDate(LocalDateTime.now());
            //     fetchHistoryFromBlc2.setUserName(username);
            //     fetchHistoryFromBlc2.setTransactionType("Name Inclusion");
            //     logger.error("======= Birth history exception ========= : ", e);

            //     List list = new ArrayList();
            //     list.add(fetchHistoryFromBlc);
            //     list.add(fetchHistoryFromBlc2);

            //     res.setStatus(HttpStatus.OK);
            //     res.setData(list);
            // }

        }

        logger.error("Birth history response : " + res);
        LocalDateTime endTime = LocalDateTime.now();
        logger.info("getHistoryFromBlc End Time ===== " + endTime);
        return res;
    }

    @Override
    public ApiResponse getFilteredData(CFCFilterDto filterDto, String filterType, HttpServletRequest request) {
        ApiResponse response = new ApiResponse();
        // JwtUtil jwtUtil = new JwtUtil();
        // DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // String startDate = null;
        // if(filterDto.getRegStartDate() != null) {
        // startDate = filterDto.getRegStartDate().format(dtf);
        // }
        // String endDate = null;
        // if(filterDto.getRegEndDate() != null) {
        // endDate = filterDto.getRegEndDate().format(dtf);
        // }
        // String eventStartDate = null;
        // if(filterDto.getEventStartDate() != null) {
        // eventStartDate = filterDto.getEventStartDate().format(dtf);
        // }
        // String eventEndDate = null;
        // if(filterDto.getEventEndDate() != null) {
        // eventEndDate = filterDto.getEventEndDate().format(dtf);
        // }
        //String userName = jwtUtil.getUsernameFromRequest(request);
        String userId = authService.getUserIdFromRequest(request);
        Optional<UserModel> currentUserOp = authRepository.findById(Long.parseLong(userId));
        UserModel currentUser = currentUserOp.get();

        // System.out.println("======
        // startDate===="+startDate+"=======Enddate========"+endDate);
        // logger.info("======
        // startDate===="+startDate+"=======Enddate========"+endDate);
        // logger.info("======
        // eventStartDate===="+eventStartDate+"=======eventEndDate========"+eventEndDate);
        // if((startDate ==null || startDate.trim().isEmpty() || endDate == null ||
        // endDate.trim().isEmpty()) && (eventStartDate ==null ||
        // eventStartDate.trim().isEmpty() || eventEndDate == null ||
        // eventEndDate.trim().isEmpty()))
        // {
        // logger.info("inside if");
        // response.setStatus(HttpStatus.BAD_REQUEST);
        // response.setMsg(Constants.START_END_DATE_MANDATORY);
        // return response;
        // }
        // else
        // {

        List<SBirthModel> birthRecords = findAll(filterDto, filterType, userId);
        if (Constants.FILTER_NAME_REJECTION.equals(filterType)
                || Constants.FILTER_LEGAL_CORRECTION_REJECTION.equals(filterType)) {
            if (birthRecords == null) {
                birthRecords = new ArrayList<>();
            }
            birthRecords.addAll(getRejectedRecords(filterDto, filterType, currentUser));
        }
        updateViewDocuments(birthRecords, filterType);

        response.setStatus(HttpStatus.OK);
        response.setData(birthRecords);
        // }
        return response;
    }

    private void updateViewDocuments(List<SBirthModel> birthRecords, String filterType) {
        Map<Long, SlaDetailsModel> slaDetailsMap = new HashMap<>();
        if(Constants.FILTER_NAME_INCLUSION.equals(filterType) || Constants.FILTER_NAME_APPROVAL.equals(filterType) || 
        Constants.FILTER_NAME_REJECTION.equals(filterType)) {
            //slaDetailsMap = slaDetailsService.findAllPendingMap(Constants.RECORD_TYPE_BIRTH, Constants.RECORD_STATUS_PENDING, Constants.RECORD_STATUS_UPLOADED, Constants.RECORD_STATUS_REJECTED, Constants.NAME_INCLUSION);
            slaDetailsMap = slaDetailsService.findAllPendingMap(Constants.RECORD_TYPE_SBIRTH, Constants.RECORD_NAME_INCLUSION);
        
        } else if(Constants.FILTER_LEGAL_CORRECTIONS.equals(filterType) || Constants.FILTER_LEGAL_CORRECTIONS_APPROVAL.equals(filterType) || 
        Constants.FILTER_LEGAL_CORRECTION_REJECTION.equals(filterType)) {
            //slaDetailsMap = slaDetailsService.findAllPendingMap(Constants.RECORD_TYPE_BIRTH, Constants.RECORD_STATUS_PENDING, Constants.RECORD_STATUS_UPLOADED, Constants.RECORD_STATUS_REJECTED, Constants.LEGAL_CORRECTIONS);
            slaDetailsMap = slaDetailsService.findAllPendingMap(Constants.RECORD_TYPE_SBIRTH, Constants.LEGAL_CORRECTIONS);
        }

        for (SBirthModel birthModel : birthRecords) {
                birthModel.setViewDocuments(slaDetailsMap.containsKey(birthModel.getSbirthId())); 
        }

    }

    private List<SBirthModel> getRejectedRecords(CFCFilterDto filterDto, String filterType,
            UserModel currentUser) {
        // fetch rejected records from SAL table, on that basis fetch from birth table
        return new ArrayList<SBirthModel>();
    }

    private List<SBirthModel> findAll(CFCFilterDto filterDto1, String filterType, String userName) throws DateRangeException {
        Long organizationId = authService.getOrganizationIdFromUserId(userName);

        return birthRepository.findAll(new Specification<SBirthModel>() {
            @Override
            public Predicate toPredicate(Root<SBirthModel> root, CriteriaQuery<?> query,
                    CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                // logger.info("filterDto1:" + filterDto1);
                CFCFilterDto filterDto = filterDto1;
                boolean isJoinCorrection = false;
                if (filterDto == null) {
                    filterDto = new CFCFilterDto();
                    filterDto.setRegEndDate(LocalDate.now());
                    filterDto.setRegStartDate(LocalDate.now().minusDays(Constants.DEFAULT_DAYS));
                } 
//				if (!CommonUtil.checkNullOrBlank(filterDto.getRegistrationNumber())
//                        ||!CommonUtil.checkNullOrBlank(filterDto.getApplicationNumber())){
//                    filterDto.setRegEndDate(null);
//                    filterDto.setRegStartDate(null);
//                    filterDto.setEventStartDate(null);
//                    filterDto.setEventEndDate(null);
//                }
                // logger.info("filterDto:" + filterDto);
                predicates.add(criteriaBuilder.and(criteriaBuilder.notEqual(root.get(Constants.STATUS),
                        Constants.RECORD_STATUS_DRAFT)));
                predicates.add(criteriaBuilder.and(criteriaBuilder.notEqual(root.get(Constants.STATUS),
                        Constants.RECORD_STATUS_REJECTED)));
                predicates.add(criteriaBuilder.and(criteriaBuilder.notEqual(root.get(Constants.STATUS),
                        Constants.RECORD_STATUS_PENDING)));
                if (!CommonUtil.checkNullOrBlank(filterDto.getRegistrationNumber())) {
                    // registrationNumber
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.REGISTRATION_NUMBER),
                            filterDto.getRegistrationNumber())));
                }
                if (!CommonUtil.checkNullOrBlank(filterDto.getApplicationNumber())) {
                    // applicationNumber
                    predicates.add(criteriaBuilder.or(criteriaBuilder.equal(root.get(Constants.APPLICATION_NUMBER), filterDto.getApplicationNumber()),
                            criteriaBuilder.equal(root.get(Constants.ORIGINAL_APPLICATION_NUMBER), filterDto.getApplicationNumber())));
                }

                if (!CommonUtil.checkNullOrBlank(filterDto.getMobileNumber())) {
                    // contactNumber
                    predicates.add(criteriaBuilder.and(
                            criteriaBuilder.equal(root.get(Constants.CONTACT_NUMBER), filterDto.getMobileNumber())));
                }
                if (!CommonUtil.checkNullOrBlank(filterDto.getMotherName())) {
                    // motherName
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.like(root.get(Constants.MOTHER_NAME), filterDto.getMotherName()+"%")));
                }
                if (!CommonUtil.checkNullOrBlank(filterDto.getFatherName())) {
                    // fatherName
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.like(root.get(Constants.FATHER_NAME), filterDto.getFatherName()+"%")));
                }

                if (!CommonUtil.checkNullOrBlank(filterDto.getName())) {
                    // name
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.like(root.get(Constants.CHILD_NAME), filterDto.getName()+"%")));
                }
                if (!CommonUtil.checkNullOrBlank(filterDto.getEventPlace())) {
                    // eventPlace
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.equal(root.get(Constants.EVENT_PLACE), filterDto.getEventPlace())));
                }
                //Event Place Flag

                if (!CommonUtil.checkNullOrBlank(filterDto.getEventPlaceFlag())) {
                    // eventPlace
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.equal(root.get(Constants.EVENT_PLACE_FLAG), filterDto.getEventPlaceFlag())));
                }

                //Division Code

                if (!CommonUtil.checkNullOrBlank(filterDto.getDivisionCode())) {
                    // eventPlace
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.equal(root.get(Constants.DIVISION_CODE), filterDto.getDivisionCode())));
                }

                // Filter by Gender
                if (!CommonUtil.checkNullOrBlank(filterDto.getGenderCode())) {
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.equal(root.get(Constants.GENDER_CODE), filterDto.getGenderCode())));
                }
                // Filter by SPOUSE NAME
                if (!CommonUtil.checkNullOrBlank(filterDto.getHusbandWifeName())) {
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.equal(root.get(Constants.HUSBAND_WIFE_NAME), filterDto.getHusbandWifeName())));
                }

                if (!CommonUtil.checkNullOrBlank(filterDto.getRegStartDate()+"") && !CommonUtil.checkNullOrBlank(filterDto.getRegEndDate()+"")) {
                    // registrationDatetime
                    CommonUtil.betweenDates(filterDto.getRegStartDate().atTime(00,00,00),filterDto.getRegEndDate().atTime(23,59,59));
                    predicates
                            .add(criteriaBuilder.and(criteriaBuilder.between(root.get(Constants.REGISTRATION_DATE_TIME),
                                    filterDto.getRegStartDate().atTime(00,00,00),filterDto.getRegEndDate().atTime(23,59,59))));
                }
                if (!CommonUtil.checkNullOrBlank(filterDto.getEventStartDate()+"") && !CommonUtil.checkNullOrBlank(filterDto.getEventEndDate()+"")) {
                    // eventDate
                    CommonUtil.betweenDates(filterDto.getEventStartDate().atTime(00,00,00),filterDto.getEventEndDate().atTime(23,59,59));

                    predicates.add(criteriaBuilder.and(criteriaBuilder.between(root.get(Constants.EVENT_DATE),
                            filterDto.getEventStartDate().atTime(00,00,00),filterDto.getEventEndDate().atTime(23,59,59))));
                }
                // status
                
                Predicate approvedPredicate = criteriaBuilder.and(
                    criteriaBuilder.equal(root.get(Constants.STATUS), Constants.RECORD_STATUS_APPROVED));
                    if(!CommonUtil.checkNullOrBlank(filterDto.getStatus())){


                        String filterStatus = filterDto.getStatus();


                         if (filterType.equalsIgnoreCase(Constants.FILTER_LEGAL_CORRECTION_SEARCH)) {

                             if(!"".equalsIgnoreCase(filterStatus)) {
                                 /* Join<Object, Object> joinParent = root.join(Constants.CORRECTION_SLA_ID, JoinType.INNER);
                                 javax.persistence.criteria.Path expression = joinParent.get(Constants.SLA_ORGID);
                                 CommonUtil.addPredicate(predicates, criteriaBuilder, expression, organizationId);

                                  */
                                 predicates.add(root.get(Constants.CORRECTION_SLA_ID).isNotNull());
                                 isJoinCorrection =true;
                             }
                            if (Constants.RECORD_STATUS_PENDING.equalsIgnoreCase(filterStatus)) {
                                predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.STATUS),
                                        Constants.RECORD_STATUS_CORRECTION_PENDING)));
                            } else if (Constants.RECORD_STATUS_REJECTED.equalsIgnoreCase(filterStatus)) {
                                predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.STATUS),
                                        Constants.RECORD_STATUS_CORRECTION_REJECTED)));
                            }
                            else if (Constants.RECORD_STATUS_APPROVED.equalsIgnoreCase(filterStatus)) {
                                predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.STATUS),
                                        Constants.RECORD_STATUS_APPROVED)));
                                predicates.add(criteriaBuilder.and(root.get(Constants.CORRECTION_SLA_ID).isNotNull()));

                            }
                        }



                    } else if (!CommonUtil.checkNullOrBlank(filterType)) {
                    if (filterType.equalsIgnoreCase(Constants.FILTER_NAME_INCLUSION)) {
                        Predicate rejectedPredicate = criteriaBuilder.and(
                            criteriaBuilder.equal(root.get(Constants.STATUS), Constants.RECORD_STATUS_INCLUSION_REJECTED));    
                        Predicate predicateNameNull = criteriaBuilder.isTrue(root.get(Constants.CHILD_NAME).isNull());
                        Predicate predicateNameEmpty = criteriaBuilder.equal(root.get(Constants.CHILD_NAME), "");
                        Predicate predicateName = criteriaBuilder.or(predicateNameNull, predicateNameEmpty);
                        Predicate approvePredicate = criteriaBuilder.and(approvedPredicate, predicateName);
                        Predicate userPredicate = criteriaBuilder.equal(root.get(Constants.USER_NAME), userName);
                        Predicate predicateInclusionRejected = criteriaBuilder.and(rejectedPredicate, userPredicate);
                        predicates.add(criteriaBuilder.or(approvePredicate, predicateInclusionRejected));
                    } else if (filterType.equalsIgnoreCase(Constants.FILTER_NAME_APPROVAL)) {
                        predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.STATUS),
                                Constants.RECORD_STATUS_INCLUSION_PENDING)));
                    } else if (filterType.equalsIgnoreCase(Constants.FILTER_LEGAL_CORRECTIONS)) {
                        
                        Predicate rejectedPredicate = criteriaBuilder.and(
                            criteriaBuilder.equal(root.get(Constants.STATUS), Constants.RECORD_STATUS_CORRECTION_REJECTED));
                           // Predicate predicateNameNull = criteriaBuilder.isTrue(root.get(Constants.CHILD_NAME).isNotNull());
                           // Predicate predicateNameEmpty = criteriaBuilder.notEqual(root.get(Constants.CHILD_NAME), "");
                           // Predicate predicateName = criteriaBuilder.and(predicateNameNull, predicateNameEmpty); 
                            Predicate approvePredicate = criteriaBuilder.and(approvedPredicate);
                           // Predicate userPredicate = criteriaBuilder.equal(root.get(Constants.USER_NAME), userName);
                            Predicate predicateInclusionRejected = criteriaBuilder.and(rejectedPredicate);
                            predicates.add(criteriaBuilder.or(approvePredicate, predicateInclusionRejected));
                    } else if (filterType.equalsIgnoreCase(Constants.FILTER_LEGAL_CORRECTIONS_APPROVAL)) {
                        predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.STATUS),
                                Constants.RECORD_STATUS_CORRECTION_PENDING)));
                    } else if (filterType.equalsIgnoreCase(Constants.FILTER_NAME_REJECTION)) {
                        predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.STATUS),
                                Constants.RECORD_STATUS_INCLUSION_REJECTED)));
                    } else if (filterType.equalsIgnoreCase(Constants.FILTER_LEGAL_CORRECTION_REJECTION)) {
                        predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.STATUS),
                                Constants.RECORD_STATUS_CORRECTION_REJECTED)));
                    }
                    else if (filterType.equalsIgnoreCase(Constants.FILTER_LEGAL_CORRECTION_SEARCH)) {
                        predicates.add(criteriaBuilder.and(criteriaBuilder.notEqual(root.get(Constants.STATUS),
                                Constants.RECORD_STATUS_INCLUSION_REJECTED)));

                        predicates.add(criteriaBuilder.and(criteriaBuilder.notEqual(root.get(Constants.STATUS),
                                Constants.RECORD_STATUS_INCLUSION_PENDING)));

                    }
                    else if (filterType.equalsIgnoreCase(Constants.FILTER_INCLUSION_SEARCH)) {
                        predicates.add(criteriaBuilder.and(criteriaBuilder.notEqual(root.get(Constants.STATUS),
                                Constants.RECORD_STATUS_CORRECTION_REJECTED)));

                        predicates.add(criteriaBuilder.and(criteriaBuilder.notEqual(root.get(Constants.STATUS),
                                Constants.RECORD_STATUS_CORRECTION_PENDING)));

                    }
                    else if (filterType.equalsIgnoreCase(Constants.FILTER_PRINT_SEARCH)) {
                        //NOT REQUIRED ANY CONDITION
                        //ALL COVERED ABOVE
                    }
                    else {
                        //predicates.add(criteriaBuilder.and(
                          //      criteriaBuilder.notEqual(root.get(Constants.STATUS), Constants.RECORD_STATUS_DRAFT)));
                        throw new IllegalArgumentException("Invalid Request");

                    }
                    if (filterType.equalsIgnoreCase(Constants.FILTER_LEGAL_CORRECTIONS_APPROVAL)
                            || filterType.equalsIgnoreCase(Constants.FILTER_LEGAL_CORRECTION_REJECTION)
                            || filterType.equalsIgnoreCase(Constants.FILTER_LEGAL_CORRECTIONS)) {

                       /*  Join<Object, Object> joinParent = root.join(Constants.CORRECTION_SLA_ID, JoinType.INNER);
                        javax.persistence.criteria.Path expression = joinParent.get(Constants.SLA_ORGID);
                        CommonUtil.addPredicate(predicates, criteriaBuilder, expression, organizationId);
                        */
                        predicates.add(root.get(Constants.CORRECTION_SLA_ID).isNotNull());
                        isJoinCorrection =true;
                    }
                    if (filterType.equalsIgnoreCase(Constants.FILTER_NAME_REJECTION)
                            || filterType.equalsIgnoreCase(Constants.FILTER_NAME_APPROVAL)) {
                    /*     Join<Object, Object> joinParent = root.join(Constants.INCLUSION_SLA_ID, JoinType.INNER);
                        javax.persistence.criteria.Path expression = joinParent.get(Constants.SLA_ORGID);
                        CommonUtil.addPredicate(predicates, criteriaBuilder, expression, organizationId);
                     */
                        predicates.add(root.get(Constants.CORRECTION_SLA_ID).isNotNull());
                        isJoinCorrection =true;
                    }

                    Optional<UserModel> currentUserOp = authRepository.findById(Long.parseLong(userName));
                    UserModel currentUser = currentUserOp.get();


                    if(isJoinCorrection) {
                        if (!currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_REGISTRAR))) {
                            List<Long> slaDetailsModel = slaDetailsService.findSlaOrganizationId(organizationId);
                            if (!slaDetailsModel.isEmpty()) {
                                predicates.add(criteriaBuilder.and(
                                        root.get(Constants.CORRECTION_SLA_ID).in(slaDetailsModel)));
                                predicates.add(approvedPredicate);
                            }
                        }
                    }

                    }
                // predicates.add(criteriaBuilder
                //         .and(criteriaBuilder.equal(root.get(Constants.STATUS), Constants.RECORD_STATUS_APPROVED)));
                query.orderBy(criteriaBuilder.desc(root.get("registrationDatetime")));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        });
    }

    
    @Override
    @Transactional
    public void updateCorrectionStatus(SlaDetailsModel slaDetailsModel, String userName) {
        SBirthModel sbirthModel = birthRepository.findById(slaDetailsModel.getBndId()).get();  
        sbirthModel.setStatus(CommonUtil.getCorrectionStatus(slaDetailsModel.getStatus()));
        if(Constants.RECORD_STATUS_APPROVED.equalsIgnoreCase(sbirthModel.getStatus())) {
            CustomBeanUtils.copySlaDetailsToSbirth(slaDetailsModel, sbirthModel);
        }

        sbirthModel.setTransactionType(Constants.LEGAL_CORRECTIONS);
        sbirthModel.setModifiedAt(LocalDateTime.now());
        sbirthModel.setModifiedBy(userName);
        birthRepository.save(sbirthModel);
        SBirthHistoryModel birthHistoryModel = new SBirthHistoryModel();
        BeanUtils.copyProperties(sbirthModel, birthHistoryModel);
        birthHistoryModel = birthHistoryRepository.save(birthHistoryModel);
        birthHistoryRepository.save(birthHistoryModel);
    }

    @Override
    public SBirthModel findById(Long bndId) {
        return birthRepository.findById(bndId).get();
    }

    @Override
    public List<SBirthHistoryModel> getStillBirthListForReport(ReportSearchDto reportSearchDto, String type)  throws DateRangeException {
        return birthHistoryRepository.findAll(new Specification<SBirthHistoryModel>() {
            @Override
            public Predicate toPredicate(Root<SBirthHistoryModel> root, CriteriaQuery<?> query,
                    CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    if(Constants.FILTER_INCLUSION_SEARCH.equalsIgnoreCase(type)) {
                        predicates.add(criteriaBuilder.equal(root.get(Constants.STATUS), Constants.RECORD_STATUS_INCLUSION_PENDING));
                    }
                    else if(Constants.FILTER_LEGAL_CORRECTION_SEARCH.equalsIgnoreCase(type)) {
                        predicates.add(criteriaBuilder.equal(root.get(Constants.STATUS), Constants.RECORD_STATUS_CORRECTION_PENDING));
                    } else {
                        throw new IllegalArgumentException("Invalid Request");
                    }
                    if (!CommonUtil.checkNullOrBlank(reportSearchDto.getRegistrationNumber())) {
                        // registrationNumber
                        predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.REGISTRATION_NUMBER),
                        reportSearchDto.getRegistrationNumber())));
                    }
                    if (!CommonUtil.checkNullOrBlank(reportSearchDto.getApplicationNumber())) {
                        // applicationNumber
                        predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.APPLICATION_NUMBER),
                        reportSearchDto.getApplicationNumber())));
                    }
    
                    if (!CommonUtil.checkNullOrBlank(reportSearchDto.getMobileNumber())) {
                        // contactNumber
                        predicates.add(criteriaBuilder.and(
                                criteriaBuilder.equal(root.get(Constants.CONTACT_NUMBER), reportSearchDto.getMobileNumber())));
                    }
                    if (!CommonUtil.checkNullOrBlank(reportSearchDto.getMotherName())) {
                        // motherName
                        predicates.add(criteriaBuilder
                                .and(criteriaBuilder.like(root.get(Constants.MOTHER_NAME), reportSearchDto.getMotherName()+"%")));
                    }
                    if (!CommonUtil.checkNullOrBlank(reportSearchDto.getFatherName())) {
                        // fatherName
                        predicates.add(criteriaBuilder
                                .and(criteriaBuilder.like(root.get(Constants.FATHER_NAME), reportSearchDto.getFatherName()+"%")));
                    }
                    if (!CommonUtil.checkNullOrBlank(reportSearchDto.getName())) {
                        // name
                        predicates.add(criteriaBuilder
                                .and(criteriaBuilder.like(root.get(Constants.CHILD_NAME), reportSearchDto.getName()+"%")));
                    }
                    if (!CommonUtil.checkNullOrBlank(reportSearchDto.getEventPlace())) {
                        // eventPlace
                        predicates.add(criteriaBuilder
                                .and(criteriaBuilder.equal(root.get(Constants.EVENT_PLACE), reportSearchDto.getEventPlace())));
                    }

                /*
                 * Date: 06-05-22
                 * Description: CFC User can search particular Hospital through their division code
                 * Author: Deepak Patel
                 * */

                logger.info("=== division code "+reportSearchDto.getDivisionCode()+"-"+CommonUtil.checkNullOrBlank(reportSearchDto.getDivisionCode()));
                if (!CommonUtil.checkNullOrBlank(reportSearchDto.getDivisionCode())) {
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.equal(root.get(Constants.DIVISION_CODE), reportSearchDto.getDivisionCode())));
                }

     /*              DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    String startDate = null;
                    if (reportSearchDto.getRegStartDate() != null) {
                        startDate = reportSearchDto.getRegStartDate().format(dtf) + " 00:00:00";
                    }
                    String endDate = null;
                    if (reportSearchDto.getRegEndDate() != null) {
                        endDate = reportSearchDto.getRegEndDate().format(dtf) + " 23:59:59";
                    }
                    String eventStartDate = null;
                    if (reportSearchDto.getEventStartDate() != null) {
                        eventStartDate = reportSearchDto.getEventStartDate().format(dtf);
                    }
                    String eventEndDate = null;
                    if (reportSearchDto.getEventEndDate() != null) {
                        eventEndDate = reportSearchDto.getEventEndDate().format(dtf);
                    }
    
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    // LocalDateTime dateTime = LocalDateTime.parse(str, formatter);
        */
                if (!CommonUtil.checkNullOrBlank(reportSearchDto.getRegStartDate()+"") && !CommonUtil.checkNullOrBlank(reportSearchDto.getRegEndDate()+"")) {
                    // registrationDatetime
                    CommonUtil.betweenDates(reportSearchDto.getRegStartDate().atTime(00,00,00),reportSearchDto.getRegEndDate().atTime(23,59,59));
                    predicates
                            .add(criteriaBuilder.and(criteriaBuilder.between(root.get(Constants.REGISTRATION_DATE_TIME),
                                    reportSearchDto.getRegStartDate().atTime(00,00,00),reportSearchDto.getRegEndDate().atTime(23,59,59))));
                }
                if (!CommonUtil.checkNullOrBlank(reportSearchDto.getEventStartDate()+"") && !CommonUtil.checkNullOrBlank(reportSearchDto.getEventEndDate()+"")) {
                    // eventDate
                    CommonUtil.betweenDates(reportSearchDto.getEventStartDate().atTime(00,00,00),reportSearchDto.getEventEndDate().atTime(23,59,59));

                    predicates.add(criteriaBuilder.and(criteriaBuilder.between(root.get(Constants.EVENT_DATE),
                            reportSearchDto.getEventStartDate().atTime(00,00,00),reportSearchDto.getEventEndDate().atTime(23,59,59))));
                }
                    // status
                query.orderBy(criteriaBuilder.desc(root.get(Constants.EVENT_DATE)));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
                }
            });
    }

    @Override
    public ApiResponse loadSdmLetterFileById(String birthId) {
        ApiResponse res = new ApiResponse();
        Optional<SBirthModel> birthModelOp = birthRepository.findById(Long.parseLong(birthId));
        SBirthModel birthRecord = birthModelOp.get();
        // logger.info("==== Birth Record is "+birthRecord);
        if(!birthRecord.getSdmLetterImage().isEmpty()){
            try {
                fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
                Path filePath = this.fileStorageLocation.resolve(birthRecord.getSdmLetterImage()).normalize();
                Resource resource = new UrlResource(filePath.toUri());
                res.setStatus(HttpStatus.OK);
                res.setData(resource);

            }catch (Exception e)
            {
                res.setStatus(HttpStatus.NOT_FOUND);
                res.setMsg(Constants.FILE_NOT_FOUND+" "+birthRecord.getSdmLetterImage());
            }
        }
        else{
            res.setStatus(HttpStatus.NOT_FOUND);
            res.setMsg(Constants.FILE_NOT_FOUND);
        }

        return res;
    }

    @Override
    @Transactional
    public ApiResponse deleteSbirth(Long birthId, HttpServletRequest request) {
        // logger.info("Before deleteBirth ====== "+birthId);

        ApiResponse res = new ApiResponse();
        Optional<SBirthModel> SBirthModelDtl = birthRepository.findById(birthId);
        String userId = authService.getUserIdFromRequest(request);
        Optional<UserModel> currentUserOp = authRepository.findById(Long.parseLong(userId));
        UserModel currentUser = currentUserOp.get();

        if(!SBirthModelDtl.isPresent()){
            res.setStatus(HttpStatus.BAD_REQUEST);
            res.setMsg(Constants.RECORD_WRONG_ERROR);
        }else{
            SBirthModel sBirthModel = SBirthModelDtl.get();
            if(!Constants.RECORD_STATUS_DRAFT.equalsIgnoreCase(sBirthModel.getStatus())){
                res.setStatus(HttpStatus.BAD_REQUEST);
                res.setMsg(Constants.RECORD_STATUS_DRAFT_ERROR);
                return res;
            }
            if(!userId.equalsIgnoreCase(sBirthModel.getUserId())
                    && !currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_REGISTRAR))){
                res.setStatus(HttpStatus.BAD_REQUEST);
                res.setMsg(Constants.RECORD_USER_ERROR);
                return res;
            }
            if(Constants.RECORD_Y.equalsIgnoreCase(sBirthModel.getIsDeleted())){
                res.setStatus(HttpStatus.BAD_REQUEST);
                res.setMsg(Constants.RECORD_ALREADY_DELETED_ERROR);
                return res;
            }
            sBirthModel.setModifiedAt(LocalDateTime.now());
            sBirthModel.setModifiedBy(userId);
            sBirthModel.setIsDeleted(Constants.RECORD_Y);
            sBirthModel.setTransactionType(Constants.RECORD_DELETE);
            birthRepository.save(sBirthModel);

            SBirthHistoryModel birthHistoryModel = new SBirthHistoryModel();
            birthHistoryModel.setTransactionType(Constants.RECORD_DELETE);
            BeanUtils.copyProperties(sBirthModel, birthHistoryModel);
            birthHistoryModel = birthHistoryRepository.save(birthHistoryModel);
            res.setData(sBirthModel.getApplicationNumber());
            res.setMsg(Constants.SBIRTH_DELETED_SUCCESS_MESSAGE);
            res.setStatus(HttpStatus.OK);
        }
        return res;
    }

    private void updateCitizenRecords(SBirthDto birthDto, String userId) {
        CitizenSBirthModel citizenBirthModel = new CitizenSBirthModel();
        BeanUtils.copyProperties(birthDto, citizenBirthModel);
        citizenBirthModel.setModifiedAt(LocalDateTime.now());
        citizenBirthModel.setModifiedBy(userId);
        citizenBirthModel.setStatus(Constants.RECORD_SUBMITTED);
        citizenSbirthRepository.save(citizenBirthModel);
    }
}
