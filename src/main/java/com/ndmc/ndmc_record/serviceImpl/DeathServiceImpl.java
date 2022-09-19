package com.ndmc.ndmc_record.serviceImpl;

import com.ndmc.ndmc_record.blockchainGatway.BlockchainGatway;
import com.ndmc.ndmc_record.config.Constants;
import com.ndmc.ndmc_record.dto.*;
import com.ndmc.ndmc_record.exception.DateRangeException;
import com.ndmc.ndmc_record.model.*;
import com.ndmc.ndmc_record.property.FileStorageProperties;
import com.ndmc.ndmc_record.repository.*;
import com.ndmc.ndmc_record.service.DeathService;
import com.ndmc.ndmc_record.service.QrCodeService;
import com.ndmc.ndmc_record.service.SlaDetailsService;
import com.ndmc.ndmc_record.utils.CommonUtil;
import com.ndmc.ndmc_record.utils.CustomBeanUtils;
import com.ndmc.ndmc_record.utils.JsonUtil;
import com.ndmc.ndmc_record.utils.JwtUtil;
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
public class DeathServiceImpl implements DeathService {

    private final Logger logger = LoggerFactory.getLogger(DeathServiceImpl.class);

    @Value("${approved_status}")
    private String approvedStatus;

    @Value("${AUTO_APPROVE_HOUR}")
    private int autoApproveHour;

    @Value("${CHANNEL_GOVT_HOSPITAL}")
    private String channelGovtHospital;

    @Value("${GOVT_HOSPITAL_USER}")
    private String userTypeGovt;

    @Value("${PRIVATE_HOSPITAL_USER}")
    private String userTypePrivate;

    @Value("${CERT_VERIFICATION_URL}")
    private String vertificateVerificationUrl;

    @Autowired
    DeathRepository deathRepository;

    @Autowired
    ApplicatioNumberCounterServiceImpl applicatioNumberCounterService;

    @Autowired
    DeathHistoryRepository deathHistoryRepository;

    @Autowired
    BlockchainGatway blockchainGatway;

    @Autowired
    AuthRepository authRepository;
    @Autowired
    LateFeeRepository lateFeeRepository;

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
    CitizenDeathRepository citizenDeathRepository;

    @Override
    @Transactional
    public ApiResponse saveDeathRecords(DeathDto deathDto, HttpServletRequest request, MultipartFile sdmLetterImage) throws Exception {

        LocalDateTime startMethodTime = LocalDateTime.now();
        logger.info("saveDeathRecords startMethodTime " + startMethodTime);
        //  JwtUtil jwtUtil = new JwtUtil();
        String username = authService.getUserIdFromRequest(request);
        Optional<UserModel> currentUserOp = authRepository.findById(Long.parseLong(username));

        UserModel currentUser = currentUserOp.get();
        String orgId = currentUser.getOrganizationId();
        Optional<OrganizationModel> organizationModelOp = organizationRepository.findById(Long.parseLong(orgId));
        OrganizationModel organizationModel = organizationModelOp.get();
        String orgType = organizationModel.getOrganizationType();
        String orgCode = organizationModel.getOrganisationCode();
        String divisionCode = organizationModel.getDivisionCode();
        ApiResponse res = new ApiResponse();
        DeathModel deathModel = new DeathModel();
        Optional<UserModel> userModelOp = authRepository.findById(Long.parseLong(username));
        UserModel userModel = userModelOp.get();
        //BeanUtils.copyProperties(deathDto, deathModel);
        CustomBeanUtils.copyDeathDetailsForUpdate(deathDto, deathModel);
        LocalDateTime now = LocalDateTime.now();
        deathModel.setCreatedAt(now);
        // getDivisionCode is not coming for UI then insert divisionCode
        if (currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CREATOR))
                || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_APPROVER))) {
            deathModel.setDivisionCode(divisionCode);
        } else {
            deathModel.setDivisionCode(deathDto.getDivisionCode() == null ? "0" : deathDto.getDivisionCode());
        }
        deathModel.setRegistrationDatetime(
                deathDto.getRegistrationDate() == null ? now : deathDto.getRegistrationDate().atStartOfDay());
        // DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // String dateOfEvent = birthDto.getEventDate().format(dtf);
        if (Constants.RECORD_STATUS_PENDING.equals(deathDto.getStatus())
                && ((deathModel.getApplicationNumber() == null
                || deathModel.getApplicationNumber().trim().isEmpty())
                || (deathModel.getRegistrationNumber() == null
                || deathModel.getRegistrationNumber().trim().isEmpty()))) {
            // logger.info("===== application status is PENDING ========");
            ApplicationNumberCounter counter = applicatioNumberCounterService.getRegistrationNumberCounter(
                    organizationModel.getOrganizationId(), orgCode, Constants.APPLICATION_TYPE_DEATH,
                    deathModel.getRegistrationDatetime());
            String regNo = counter.getCount() + "";
            String applNo = applicatioNumberCounterService.generateApplicationNumber(counter);
            deathModel.setApplicationNumber(applNo);
            // deathModel.setRegistrationNumber(regNo);
            //deathModel.setRegistrationNumber(deathDto.getRegistrationNumber());
            deathModel.setRegistrationNumber(deathDto.getRegistrationNumber() == null ? regNo : deathDto.getRegistrationNumber());


               /*@AUTHOR DEEPAK
        Dated: 07-09-22
        * If Record is from Citizen then Update their status from PENDING  to SUBMITTED
        * */
            if(deathDto.getDeathIdTemp() != null){
                Optional<CitizenDeathModel> citizenDeathModelOp = citizenDeathRepository.findById(deathDto.getDeathIdTemp());
                CitizenDeathModel citizenDeathModel = citizenDeathModelOp.get();
                if(citizenDeathModelOp.isPresent() && deathDto.getOrganizationCode().equalsIgnoreCase(citizenDeathModel.getOrganizationCode())) {
                    updateCitizenRecords(deathDto, username);
                }
            }
        }
        deathModel.setRegistrationNumber(deathDto.getRegistrationNumber() == null ? deathModel.getRegistrationNumber() : deathDto.getRegistrationNumber());
        deathModel.setRegistrationDatetime(deathDto.getRegistrationDate() == null ? now : deathDto.getRegistrationDate().atStartOfDay());

        deathModel.setModifiedAt(now);
        deathModel.setModifiedBy(username);
        deathModel.setCreatedAt(now);

        deathModel.setUserId(username);
        //deathModel.setEventDate(LocalDateTime.from(deathDto.getEventDate()));
        deathModel.setStatus(deathDto.getStatus().toUpperCase(Locale.ROOT));
        Long organizationId = authService.getOrganizationIdFromUserId(username);
        deathModel.setOrganizationId(organizationId);

        // Deepak: We are not calculating late fee charges for unknown cases
        if (deathDto.getEventDate() != null && ( CommonUtil.checkNullOrBlank(deathDto.getIsUnkownCase()) || !Constants.IS_UNKNOWN_CASE.equalsIgnoreCase(deathDto.getIsUnkownCase())))
        {
            long numberOfDays = CommonUtil.getDayBetweenDates(deathDto.getEventDate().toLocalDate(), LocalDate.now());

            Optional<LateFee> existedFeeData = lateFeeRepository.findById(Constants.APPLICATION_TYPE_DEATH);
            if (Constants.USER_TYPE_CFC.equalsIgnoreCase(organizationModel.getOrganizationType())) {
                existedFeeData = lateFeeRepository.findById(Constants.APPLICATION_TYPE_CFC_DEATH);
            }
            if (Constants.USER_TYPE_CFC.equalsIgnoreCase(orgType)
                    && Constants.RECORD_TYPE_OLD.equalsIgnoreCase(deathDto.getRecordType())) {
                deathModel.setLateFee(0F);

            } else if (Constants.USER_TYPE_CFC.equalsIgnoreCase(orgType)
                    && (deathDto.getRecordType() == null ||
                    !Constants.RECORD_TYPE_OLD.equalsIgnoreCase(deathDto.getRecordType().trim()))
                    && numberOfDays > existedFeeData.get().getEndDays()) {
                // Check for SDM ORDER
                if (deathDto.getSdmLetterNo() != null && !deathDto.getSdmLetterNo().trim().isEmpty()) {
                    deathModel.setLateFee(existedFeeData.get().getFee());
                } else {
                    res.setMsg(Constants.SDM_LETTER_BLANK);
                    res.setStatus(HttpStatus.NOT_FOUND);
                    return res;
                }
            } else if (numberOfDays > existedFeeData.get().getEndDays()) {
                // After 365 Days Birth Record entry
                res.setMsg(Constants.VISIT_CFC);
                res.setStatus(HttpStatus.BAD_REQUEST);
                return res;

            } else if (numberOfDays > existedFeeData.get().getStartDays()
                    && numberOfDays <= existedFeeData.get().getEndDays()) {
                // Between 21 - 365 Days Birth Record entry
                deathModel.setLateFee(existedFeeData.get().getFee());
            } else {
                // Within 21 days Birth Record entry
                deathModel.setLateFee(0F);
            }
        }else{
            deathModel.setLateFee(0F);
        }

        if(sdmLetterImage != null && !sdmLetterImage.isEmpty()) {
            String fileNameold = StringUtils.cleanPath(sdmLetterImage.getOriginalFilename());
            String extension = fileNameold.substring(fileNameold.lastIndexOf("."));
            String fileName = "sdm_letter_" + username + "_" + Constants.APPLICATION_TYPE_DEATH + System.currentTimeMillis() + extension;
            //byte [] digest = new DigestUtils(DigestUtils.getSha3_224Digest()).digest(dataToDigest);
            String fileHash = new DigestUtils(DigestUtils.getMd5Digest()).digestAsHex(sdmLetterImage.getInputStream());
            // logger.info("sdm leter  image name " + fileName);
            // logger.info("sdm leter  image hash code " + fileHash);

            Path fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();;
            //String uploadDir = fileStorageLocation;
            CommonUtil.saveFile(fileStorageLocation, fileName, sdmLetterImage);

            deathModel.setSdmLetterImage(fileName);
            deathModel.setSdmLetterImageHash(fileHash);
        }

        // logger.info("Before save data in Death table ====== " + LocalDateTime.now());
        // logger.info("Requested Body O Death model " + deathModel);
        deathModel.setTransactionType(Constants.RECORD_ADDED);
        deathModel.setCrNumber(CommonUtil.checkNullOrBlank(deathDto.getCrNumber()) ? "0" : deathDto.getCrNumber());
        deathModel = deathRepository.save(deathModel);
        // logger.info("After save data in Birth table ====== " + LocalDateTime.now());



        // Add Death history
        DeathHistoryModel deathHistoryModel = new DeathHistoryModel();
        deathHistoryModel.setTransactionType(Constants.RECORD_ADDED);
        BeanUtils.copyProperties(deathModel, deathHistoryModel);
        // logger.info("Before save data in Death_history table ====== " + now);
        deathHistoryModel = deathHistoryRepository.save(deathHistoryModel);
        // logger.info("After save data in Death_history table ====== " + now);
        // logger.info("Before Blockchain calling ===== " + now);
        // blockchainGatway.modifyRecord(birthModel);
        // blockchainGatway.insertDeathRecord(deathModel, channelGovtHospital);
        // logger.info("After data addition in Blockchain ==== " + now);



        // try {
        LocalDateTime callBLCTime = LocalDateTime.now();
        logger.info("saveDeathRecords callBLCTime " + callBLCTime);
        BlockchainDeathResponse blockchainResult = blockchainGatway.insertDeathRecord(deathModel,
                channelGovtHospital);
        LocalDateTime afterBLCTime = LocalDateTime.now();
        logger.info("saveDeathRecords afterBLCTime " + afterBLCTime);
        // logger.info("After data addition in Blockchain ==== " + now);
        // JsonUtil.getObjectFromJson(blockchainResult, BlockchainGatway.class);

        // Set Blockchain response
        String message = blockchainResult.getMessage().trim();
        String txID = blockchainResult.getTxID();
        String status = blockchainResult.getStatus();
        if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(status)) {
            deathModel.setBlcMessage(message);
            deathModel.setBlcTxId(txID);
            deathModel.setBlcStatus(status);

            deathHistoryModel.setBlcMessage(message);
            deathHistoryModel.setBlcTxId(txID);
            deathHistoryModel.setBlcStatus(status);

            try{
                // Send Text message to Approver for particular Organization
                String applNumber = deathModel.getApplicationNumber();
                List<UserModel> approverUsers = authService.findApproverUserDetailsUserId(currentUser.getUserId().toString());
                approverUsers.stream().forEach( e -> {
                    CommonUtil.sendEventMessage(e.getFirstName(), applNumber, e.getContactNo(), Constants.NEW_APPROVAL_REQUEST);
                });
            }
            catch(Exception e){
                // logger.info(e.getMessage());
            }


            deathModel = deathRepository.save(deathModel);
            deathHistoryRepository.save(deathHistoryModel);


                if(deathModel != null && deathModel.getApplicationNumber() !=null
                        && !CommonUtil.checkNullOrBlank(deathModel.getContactNumber())){
                    CommonUtil commonUtil = new CommonUtil();
                    String type = "/D";
                    String originalUrl = Constants.REVIEW_UAT_URL+deathModel.getApplicationNumber()+type;

                logger.info("  === Death addition response ====="+deathModel);
                try {
                    commonUtil.sendTextMessage(Constants.APPLICANT, deathModel.getContactNumber(), deathModel.getApplicationNumber(), Constants.RECORD_TYPE_DEATH, Constants.NEW_APPROVAL_REQUEST, "", "",originalUrl, "", "");
                }catch (Exception e){
                logger.info("===SMS EXCEPTION =="+e);
            }
                }

            res.setMsg(Constants.DEATH_SUCCESS_MESSAGE);
            res.setStatus(HttpStatus.OK);
            res.setData(deathModel);
        } else {

            // logger.info("saveDeathRecords False response from blockchain " + blockchainResult);
            throw new Exception(Constants.INTERNAL_SERVER_ERROR);
        }
        // } catch (Exception e) {
        //    logger.error("===Blc Exception ===", e);
        //     String message = e.getMessage();
        //     String status = Constants.BLC_STATUS_FALSE;
        //     message = CommonUtil.updateExceptionMessage(message);
        //     deathModel.setBlcMessage(message);

        //     deathModel.setBlcStatus(status);

        //     deathHistoryModel.setBlcMessage(message);
        //     deathHistoryModel.setBlcStatus(status);
        //   }
        LocalDateTime endTime = LocalDateTime.now();
        logger.info("saveDeathRecords endTime " + endTime);
        return res;

    }

    @Override
    @Transactional
    public ApiResponse updateDeathRecords(DeathDto deathDto, HttpServletRequest request, MultipartFile sdmLetterImage) throws Exception {

        LocalDateTime startTime = LocalDateTime.now();
        logger.info("updateDeathRecords startTime " + startTime);
        Optional<DeathModel> existedData = deathRepository.findById(deathDto.getDeathId());
        // JwtUtil jwtUtil = new JwtUtil();
        String username = authService.getUserIdFromRequest(request);
        Optional<UserModel> currentUserOp = authRepository.findById(Long.parseLong(username));

        UserModel currentUser = currentUserOp.get();
        String orgId = currentUser.getOrganizationId();
        Optional<OrganizationModel> organizationModelOp = organizationRepository.findById(Long.parseLong(orgId));
        OrganizationModel organizationModel = organizationModelOp.get();
        String orgType = organizationModel.getOrganizationType();
        String orgCode = organizationModel.getOrganisationCode();
        String divisionCode = organizationModel.getDivisionCode();
        //UserModel currentUser = currentUserOp.get();
        ApiResponse res = new ApiResponse();
        // BirthModel birthModel = new BirthModel();
        // logger.info("Requested Body " + deathDto);
        if (existedData.equals(Optional.empty())) {
            res.setMsg(Constants.RECORD_NOT_FOUND);
            res.setStatus(HttpStatus.BAD_REQUEST);
            // logger.info("Existed Data -------------------------------------------++++ " + existedData);
        } else {
            DeathModel deathModel = existedData.get();
            // getDivisionCode is not coming for UI then insert divisionCode
            if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CREATOR))
                    || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_APPROVER)))
            {
                deathModel.setDivisionCode(divisionCode);
            }else{
                deathModel.setDivisionCode(deathDto.getDivisionCode() == null ?  "0" : deathDto.getDivisionCode());
            }
            if (deathModel.getStatus().equals(approvedStatus)) {
                res.setMsg(Constants.NOT_PERMITTED);
                res.setStatus(HttpStatus.BAD_REQUEST);

            } else {

                CustomBeanUtils.copyDeathDetailsForUpdate(deathDto, deathModel);
                
                if (Constants.RECORD_STATUS_PENDING.equals(deathDto.getStatus())
                        && ((deathModel.getApplicationNumber() == null
                        || deathModel.getApplicationNumber().trim().isEmpty())
                        || (deathModel.getRegistrationNumber() == null
                        || deathModel.getRegistrationNumber().trim().isEmpty()))) {
                    // This will execute when status become draft to Pending
                    ApplicationNumberCounter counter = applicatioNumberCounterService.getRegistrationNumberCounter(
                            organizationModel.getOrganizationId(), orgCode, Constants.APPLICATION_TYPE_DEATH,
                            deathModel.getRegistrationDatetime());
                    String regNo = counter.getCount() + "";
                    String applNo = applicatioNumberCounterService.generateApplicationNumber(counter) ;
                    deathModel.setApplicationNumber(applNo);
                    // deathModel.setRegistrationNumber(regNo);
                    deathModel.setRegistrationNumber(deathDto.getRegistrationNumber() == null ? regNo : deathDto.getRegistrationNumber());

                    //deathModel.setRegistrationNumber(deathDto.getRegistrationNumber());
                }else if (Constants.RECORD_STATUS_PENDING.equalsIgnoreCase(deathDto.getStatus())) {
                    deathModel.setRegistrationNumber(deathDto.getRegistrationNumber() == null ? deathModel.getRegistrationNumber() : deathDto.getRegistrationNumber());
                }

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_WITH_TIME);
                LocalDateTime now = LocalDateTime.now();
                deathModel.setModifiedAt(now);
                // deathModel.setModifiedBy(deathDto.getModifiedBy());
                deathModel.setModifiedBy(username);
                //Added to Update Registration date time
               // deathModel.setRegistrationDatetime(deathDto.getRegistrationDate().atStartOfDay());

                if (deathDto.getEventDate() != null && ( CommonUtil.checkNullOrBlank(deathDto.getIsUnkownCase()) ||
                        !Constants.IS_UNKNOWN_CASE.equalsIgnoreCase(deathDto.getIsUnkownCase()))) {
                    long numberOfDays = CommonUtil.getDayBetweenDates(deathDto.getEventDate().toLocalDate(), LocalDate.now());

                    Optional<LateFee> existedFeeData = lateFeeRepository.findById(Constants.APPLICATION_TYPE_DEATH);
                    if (Constants.USER_TYPE_CFC.equalsIgnoreCase(organizationModel.getOrganizationType())) {
                        existedFeeData = lateFeeRepository.findById(Constants.APPLICATION_TYPE_CFC_DEATH);
                    }
                    if (Constants.USER_TYPE_CFC.equalsIgnoreCase(orgType)
                            && Constants.RECORD_TYPE_OLD.equalsIgnoreCase(deathDto.getRecordType())) {
                        deathModel.setLateFee(0F);

                    } else if (Constants.USER_TYPE_CFC.equalsIgnoreCase(orgType)
                            && (deathDto.getRecordType() == null ||
                            !Constants.RECORD_TYPE_OLD.equalsIgnoreCase(deathDto.getRecordType().trim()))
                            && numberOfDays > existedFeeData.get().getEndDays()) {
                        // Check for SDM ORDER
                        if (deathDto.getSdmLetterNo() != null && !deathDto.getSdmLetterNo().trim().isEmpty()) {
                            deathModel.setLateFee(existedFeeData.get().getFee());
                        } else {
                            res.setMsg(Constants.SDM_LETTER_BLANK);
                            res.setStatus(HttpStatus.NOT_FOUND);
                            return res;
                        }
                    } else if (numberOfDays > existedFeeData.get().getEndDays()) {
                        // After 365 Days Birth Record entry
                        res.setMsg(Constants.VISIT_CFC);
                        res.setStatus(HttpStatus.BAD_REQUEST);
                        return res;

                    } else if (numberOfDays > existedFeeData.get().getStartDays()
                            && numberOfDays <= existedFeeData.get().getEndDays()) {
                        // Between 21 - 365 Days Birth Record entry
                        deathModel.setLateFee(existedFeeData.get().getFee());
                    } else {
                        // Within 21 days Birth Record entry
                        deathModel.setLateFee(0F);
                    }
                }else{
                    deathModel.setLateFee(0F);
                }


                if(sdmLetterImage != null && !sdmLetterImage.isEmpty()) {
                    String fileNameold = StringUtils.cleanPath(sdmLetterImage.getOriginalFilename());
                    String extension = fileNameold.substring(fileNameold.lastIndexOf("."));
                    String fileName = "sdm_letter_" + username + "_" + Constants.APPLICATION_TYPE_DEATH + System.currentTimeMillis() + extension;
                    //byte [] digest = new DigestUtils(DigestUtils.getSha3_224Digest()).digest(dataToDigest);
                    String fileHash = new DigestUtils(DigestUtils.getMd5Digest()).digestAsHex(sdmLetterImage.getInputStream());
                    // logger.info("sdm leter  image name " + fileName);
                    // logger.info("sdm leter  image hash code " + fileHash);

                    Path fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
                    //String uploadDir = fileStorageLocation;
                    CommonUtil.saveFile(fileStorageLocation, fileName, sdmLetterImage);

                    deathModel.setSdmLetterImage(fileName);
                    deathModel.setSdmLetterImageHash(fileHash);
                }

                deathModel.setTransactionType(Constants.RECORD_UPDATED);
                deathModel.setIsUnkownCase(deathDto.getIsUnkownCase());
                deathModel = deathRepository.save(deathModel);

                // Add Death history
                DeathHistoryModel deathHistoryModel = new DeathHistoryModel();
                deathHistoryModel.setTransactionType(Constants.RECORD_UPDATED);
                BeanUtils.copyProperties(deathModel, deathHistoryModel);
                deathHistoryModel = deathHistoryRepository.save(deathHistoryModel);

                // BLOCKCHAIN CALL
                // blockchainGatway.updateDeathRecord(deathModel, channelGovtHospital);

                // try {
                LocalDateTime callupdateDeathRecordTime = LocalDateTime.now();
                logger.info("updateDeathRecords callupdateDeathRecordCTime " + callupdateDeathRecordTime);
                BlockchainUpdateDeathResponse blockchainResult = blockchainGatway.updateDeathRecord(deathModel,
                        channelGovtHospital);
                LocalDateTime afterCallupdateDeathRecordTime = LocalDateTime.now();
                logger.info("updateDeathRecords afterCallupdateDeathRecordTime " + afterCallupdateDeathRecordTime);
                // logger.info("After data addition in Blockchain ==== " + now);
                // JsonUtil.getObjectFromJson(blockchainResult, BlockchainGatway.class);

                // Set Blockchain response
                String message = blockchainResult.getMessage().trim();
                String txID = blockchainResult.getTxID();
                String status = blockchainResult.getStatus();
                if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(status)){
                    deathModel.setBlcMessage(message);
                    deathModel.setBlcTxId(txID);
                    deathModel.setBlcStatus(status);

                    deathHistoryModel.setBlcMessage(message);
                    deathHistoryModel.setBlcTxId(txID);
                    deathHistoryModel.setBlcStatus(status);
                    // } catch (Exception e) {
                    //     logger.error("===Blc Exception ===", e);
                    //     String message = e.getMessage();
                    //     String status = Constants.BLC_STATUS_FALSE;
                    //     message = CommonUtil.updateExceptionMessage(message);
                    //     deathModel.setBlcMessage(message);

                    //     deathModel.setBlcStatus(status);

                    //     deathHistoryModel.setBlcMessage(message);
                    //     deathHistoryModel.setBlcStatus(status);
                    // }

                    deathModel = deathRepository.save(deathModel);
                    deathHistoryRepository.save(deathHistoryModel);
                    // BeanUtils.copyProperties(birthModel, birthDto);
                    res.setMsg(Constants.DEATH_UPDATE_SUCCESS_MESSAGE);
                    res.setStatus(HttpStatus.OK);
                    res.setData(deathModel.getApplicationNumber());
                } else {
                    // logger.info("updateDeathRecords False response from blockchain " + blockchainResult);
                    throw new Exception(Constants.INTERNAL_SERVER_ERROR);
                }

            }
        }
        LocalDateTime endTime = LocalDateTime.now();
        logger.info("updateDeathRecords endTime " + endTime);
        return res;

    }

    @Override
    public List<DeathModel> getDeathRecords(String status, String orgCode) {
        return deathRepository.getDeathDataByStatusAndOrganization(status, orgCode);
    }

    @Override
    public ApiResponse getDeathDetails(Long deathId) throws Exception {
        LocalDateTime startTime = LocalDateTime.now();
        logger.info("getDeathDetails startTime " + startTime);
        ApiResponse res = new ApiResponse();
        Optional<DeathModel> existedData = deathRepository.findById(deathId);
        if (existedData.equals(Optional.empty())) {
            res.setMsg(Constants.RECORD_NOT_FOUND);
            res.setStatus(HttpStatus.BAD_REQUEST);
        } else {
            res.setStatus(HttpStatus.OK);
            DeathModel blcModel = null;
            // try {
            String blcResponse = blockchainGatway.getDeathRecord(deathId.toString(), channelGovtHospital);

            // logger.info("====Birth details response ===" + blcResponse);
            blcModel = JsonUtil.getObjectFromJson(blcResponse, DeathModel.class);
            // } catch (Exception e) {
            //     e.printStackTrace();
            // }
            if (blcModel == null) {
                // logger.info("getDeathDetails blcModel is null from blockchain " + blcResponse);
                throw new Exception(Constants.INTERNAL_SERVER_ERROR);
            }
            res.setData(blcModel);
        }
        LocalDateTime endTime = LocalDateTime.now();
        logger.info("getDeathDetails endTime " + endTime);
        return res;
    }

    @Override
    public List<DeathModel> getAllDeathRecords() {
        return deathRepository.findAll();
    }

    @Override
    @Transactional
    public ApiResponse updateDeathRecordStatus(Long deathId, String status, String remarks, HttpServletRequest request)
            throws Exception {
        LocalDateTime startTime = LocalDateTime.now();
        logger.info("updateDeathRecordStatus startTime " + startTime);
        // JwtUtil jwtUtil = new JwtUtil();
        ApiResponse res = new ApiResponse();
        Optional<DeathModel> existedData = deathRepository.findById(deathId);
        // boolean existedData = birthRepository.existsById(birthId);

        if (existedData.equals(Optional.empty())) {
            res.setMsg(Constants.RECORD_NOT_FOUND);
            res.setStatus(HttpStatus.BAD_REQUEST);
        } else {
            DeathModel deathModel = existedData.get();
            logger.info("status ----- " + deathModel.getStatus());
            String applicationNumber = deathModel.getApplicationNumber();
            String userName = authService.getUserIdFromRequest(request);

            Optional<UserModel> currentUserOp = authRepository.findById(Long.parseLong(userName));
            UserModel currentUser = currentUserOp.get();
            String orgId = currentUser.getOrganizationId();
            Optional<OrganizationModel> organizationModelOp =organizationRepository.findById(Long.parseLong(orgId));
            OrganizationModel organizationModel = organizationModelOp.get();
            String organizationCode = organizationModel.getOrganisationCode();
            LocalDateTime now = LocalDateTime.now();

            logger.info("CURRENT USER ORG CODE  ====" + organizationCode + ":==== DATA ORG CODE==="
                    + deathModel.getOrganizationCode());
            if (!deathModel.getStatus().equalsIgnoreCase(Constants.RECORD_STATUS_PENDING)) {
                res.setMsg(Constants.BIRTH_STATUS_NOT_PENDING);
                res.setStatus(HttpStatus.BAD_REQUEST);
            } else if (!deathModel.getOrganizationCode().equalsIgnoreCase(organizationCode)
                    && !Constants.ORG_NDMC.equalsIgnoreCase(organizationCode)) {
                res.setStatus(HttpStatus.BAD_REQUEST);
                res.setMsg("YouR ORGANISATION IS NOT SAME");
            }

            else {

                if (Constants.RECORD_STATUS_APPROVED.equalsIgnoreCase(status)) {
                    if (approveUpdateStatus(deathModel, userName, now)) {
                        res.setStatus(HttpStatus.OK);
                        res.setMsg(Constants.RECORD_APPROVED_MESSAGE);
                        res.setData(deathModel.getApplicationNumber());
                    }

                } else if (Constants.RECORD_STATUS_REJECTED.equalsIgnoreCase(status)) {
                    deathModel.setRejectedBy(userName);
                    deathModel.setRejectedAt(now);
                    deathModel.setStatus(Constants.RECORD_STATUS_REJECTED);
                    deathModel.setRejectionRemark(remarks);

                    deathModel.setTransactionType(Constants.RECORD_REJECTED);
//                    deathModel.setEventPlace(deathModel.getEventPlace());
//                    deathModel.setEventPlaceFlag(deathModel.getEventPlaceFlag());
//                    logger.info("DEATH MODEL AFTER RJECT RECORD " + deathModel);
                    deathModel = deathRepository.save(deathModel);

                    // int sqlResponse = birthRepository.rejectBirthStatusByBirthId(birthId,
                    // Constants.RECORD_STATUS_REJECTED, birthModel.getRejectedBy(),
                    // birthModel.getRejectedAt());
                    if (deathModel != null) {


                        // Add birth history
                        DeathHistoryModel deathHistoryModel = new DeathHistoryModel();
                        deathHistoryModel.setTransactionType(Constants.RECORD_REJECTED);
                        BeanUtils.copyProperties(deathModel, deathHistoryModel);
                        deathHistoryModel = deathHistoryRepository.save(deathHistoryModel);
                        res.setStatus(HttpStatus.OK);
                        res.setMsg(Constants.RECORD_REJECTED_MESSAGE);
                        res.setData(deathModel.getApplicationNumber());
                        logger.info("Birth Rejection Payload  birthId===" + deathId.toString() + "=== Rejected By "
                                + userName + "=== Rejected at " + CommonUtil.convertDateTimeFormat(now));
                        // Need to implement try catch to handle exception in blockchain method

                        // try {
                        LocalDateTime callrejectDeathRecordTime = LocalDateTime.now();
                        logger.info("updateDeathRecordStatus callrejectDeathRecordTime " + callrejectDeathRecordTime);
                        BlockchainRejectDeathResponse blockchainResult = blockchainGatway.rejectDeathRecord(
                                deathId.toString(), userName, CommonUtil.convertDateTimeFormat(now), remarks, channelGovtHospital);
                        // logger.info("After data addition in Blockchain ==== " + now);
                        // JsonUtil.getObjectFromJson(blockchainResult, BlockchainGatway.class);
                        LocalDateTime callrejectDeathRecordEndTime = LocalDateTime.now();
                        logger.info("updateDeathRecordStatus callrejectDeathRecordEndTime " + callrejectDeathRecordEndTime);
                        // Set Blockchain response
                        String message = blockchainResult.getMessage();
                        String txID = blockchainResult.getTxID();
                        String blcStatus = blockchainResult.getStatus();
                        if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(blcStatus)) {
                            deathModel.setBlcMessage(message);
                            deathModel.setBlcTxId(txID);
                            deathModel.setBlcStatus(blcStatus);

                            deathHistoryModel.setBlcMessage(message);
                            deathHistoryModel.setBlcTxId(txID);
                            deathHistoryModel.setBlcStatus(blcStatus);


                            // } catch (Exception e) {
                            //     logger.error("===Blc Exception ===", e);

                            // }
                            deathModel = deathRepository.save(deathModel);
                            deathHistoryModel = deathHistoryRepository.save(deathHistoryModel);


                                /* Code to send Application REJECTION text message
                                 * Code by Deepak
                                 * 29-04-2022
                                 * */
                                if(deathModel != null && deathModel.getApplicationNumber() !=null
                                        && !CommonUtil.checkNullOrBlank(deathModel.getContactNumber())){
                                    CommonUtil commonUtil = new CommonUtil();


                                logger.info("  === Death addition response ====="+deathModel);
                                try{
                                commonUtil.sendTextMessage(Constants.APPLICANT, deathModel.getContactNumber(), deathModel.getApplicationNumber(), Constants.RECORD_TYPE_DEATH, Constants.REQUEST_REJECTED, "", "", "", "", "");
                                }catch (Exception e){
                                    logger.info("===SMS EXCEPTION =="+e);
                                }
                            }
                        } else {
                            // logger.info("updateDeathRecords False response from blockchain " + blockchainResult);
                            throw new Exception(Constants.INTERNAL_SERVER_ERROR);
                        }
                    }

                }
                res.setMsg(Constants.DEATH_UPDATE_SUCCESS_MESSAGE);
                res.setStatus(HttpStatus.OK);
                res.setData(deathModel.getApplicationNumber());
            }
        }
        LocalDateTime endTime = LocalDateTime.now();
        logger.info("updateDeathRecordStatus endTime " + endTime);
        return res;
    }

    private boolean approveUpdateStatus(DeathModel deathModel, String userName, LocalDateTime now) throws Exception {
        LocalDateTime startTime = LocalDateTime.now();
        logger.info("approveUpdateStatus startTime " + startTime);
        deathModel.setApprovedBy(userName);
        deathModel.setApprovedAt(now);
        deathModel.setStatus(Constants.RECORD_STATUS_APPROVED);
        deathModel.setTransactionType(Constants.RECORD_APPROVED);
//        deathModel.setEventPlace(deathModel.getEventPlace());
//        deathModel.setEventPlaceFlag(deathModel.getEventPlaceFlag());
//        logger.info("DEATH MODEL AFTER APPROVED RECORD " + deathModel);
        deathModel = deathRepository.save(deathModel);
        // int sqlResponse = birthRepository.approveBirthStatusByBirthId(birthId,
        // Constants.RECORD_STATUS_APPROVED, birthModel.getApprovedBy(),
        // birthModel.getApprovedAt());

        if (deathModel != null) {


            // Add birth history
            DeathHistoryModel deathHistoryModel = new DeathHistoryModel();
            deathHistoryModel.setTransactionType(Constants.RECORD_APPROVED);
            BeanUtils.copyProperties(deathModel, deathHistoryModel);
            deathHistoryModel = deathHistoryRepository.save(deathHistoryModel);

            logger.info("Birth Approval Payload  birthId===" + deathModel.getDeathId().toString() + "=== Approved By "
                    + userName + "=== Rejected at " + CommonUtil.convertDateTimeFormat(now));

            // try {
            LocalDateTime callapproveDeathRecordTime = LocalDateTime.now();
            logger.info("approveUpdateStatus callapproveDeathRecordTime " + callapproveDeathRecordTime);
            BlockchainApproveDeathResponse blockchainResult = blockchainGatway.approveDeathRecord(
                    deathModel.getDeathId().toString(), userName, CommonUtil.convertDateTimeFormat(now), channelGovtHospital);
            // logger.info("After data addition in Blockchain ==== " + now);
            // JsonUtil.getObjectFromJson(blockchainResult, BlockchainGatway.class);
            LocalDateTime callapproveDeathRecordEndTime = LocalDateTime.now();
            logger.info("approveUpdateStatus callapproveDeathRecordEndTime " + callapproveDeathRecordEndTime);
            // Set Blockchain response
            String message = blockchainResult.getMessage();
            String txID = blockchainResult.getTxID();
            String blcStatus = blockchainResult.getStatus();
            if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(blcStatus)) {
                deathModel.setBlcMessage(message);
                deathModel.setBlcTxId(txID);
                deathModel.setBlcStatus(blcStatus);

                deathHistoryModel.setBlcMessage(message);
                deathHistoryModel.setBlcTxId(txID);
                deathHistoryModel.setBlcStatus(blcStatus);


                // } catch (Exception e) {
                //     logger.error("===Blc Exception ===", e);

                // }
                deathModel = deathRepository.save(deathModel);
                deathHistoryModel = deathHistoryRepository.save(deathHistoryModel);

                     /* Code to send Application Approval text message
                     * Code by Deepak
                     * 29-04-2022
                     * */
                    if(deathModel != null && deathModel.getApplicationNumber() !=null
                            && !CommonUtil.checkNullOrBlank(deathModel.getContactNumber())){
                        CommonUtil commonUtil = new CommonUtil();


                    logger.info("  === Death addition response ====="+deathModel);
                    try{
                    commonUtil.sendTextMessage(Constants.APPLICANT, deathModel.getContactNumber(), deathModel.getApplicationNumber(), Constants.RECORD_TYPE_DEATH, Constants.REQUEST_APPROVED, "", "","", "", "");
                    }catch (Exception e){
                        logger.info("===SMS EXCEPTION =="+e);
                    }
                    }
            } else {
                // logger.info("approveUpdateStatus False response from blockchain " + blockchainResult);
                throw new Exception(Constants.INTERNAL_SERVER_ERROR);
            }

            return true;
        }
        return false;

    }

    @Override
    public ApiResponse getUsersDeathRecords(HttpServletRequest request) throws Exception {
        // check user role
        // if creater or approvar fetch all pending of his org and his draft record
        // org = xyz and (status = pending or ( status = draft and user=abc))
        // if admin fetch pending of all orgs and his drafs records
        ApiResponse response = new ApiResponse();
        JwtUtil jwtUtil = new JwtUtil();
        String userName = authService.getUserIdFromRequest(request);
        Optional<UserModel> currentUserOp = authRepository.findById(Long.parseLong(userName));
        UserModel currentUser = currentUserOp.get();
        String orgId = currentUser.getOrganizationId();
        Optional<OrganizationModel> organizationModelOp =organizationRepository.findById(Long.parseLong(orgId));
        OrganizationModel organizationModel = organizationModelOp.get();
        String organizationCode = organizationModel.getOrganisationCode();
        Long userId = currentUser.getUserId();

        if (currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CREATOR))
                || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_CREATOR))) {
            // logger.info("Current USER IS CREATOR ===== " + currentUser);
//            List<DeathModel> data = deathRepository.getCreatorRecords(userId, organizationCode,
//                    Constants.RECORD_STATUS_PENDING, Constants.RECORD_STATUS_REJECTED,
//                    Constants.RECORD_STATUS_DRAFT);

            List<DeathModel> data = deathRepository.getApproverRecords(organizationCode,
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
            List<DeathModel> data = deathRepository.getApproverRecords(organizationCode,
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
            List<DeathModel> data = deathRepository.getAdminRecords(userId, Constants.RECORD_STATUS_PENDING,
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
        return response;
    }

    /*
     * This method return Number of days and hours for approval
     * In case of Pending status
     */
    private List<DeathModel> recordApprovalTime(List<DeathModel> data) throws Exception {

        if (data != null) {

            for (DeathModel bModel : data) {

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
        // String userName = "Cron";
        // LocalDateTime now = LocalDateTime.now();
        List<DeathModel> deathModels = deathRepository.getByStatusWithHour(Constants.RECORD_STATUS_PENDING,
                autoApproveHour);

        for (DeathModel dm : deathModels) {
            cronApproveUpdateStatus(dm);

        }
        // logger.info("*** CRON END*****");
    }

    @Transactional
    private void cronApproveUpdateStatus(DeathModel dm) throws Exception {
        // logger.info("updating for : " + dm.getDeathId());
        approveUpdateStatus(dm, Constants.CRON, LocalDateTime.now());
        // logger.info("updation completed : " + dm.getDeathId());
    }

    @Override
    public ApiResponse getHistoryFromBlc(Long deathId, HttpServletRequest request) throws Exception {
        LocalDateTime startTime = LocalDateTime.now();
        logger.info("getHistoryFromBlc startTime : " + startTime);
        // BirthModel birthModel = new BirthModel();
        ApiResponse res = new ApiResponse();
        Optional<DeathModel> deathModelOptional = deathRepository.findById(deathId);
        if (!deathModelOptional.isPresent()) {

            logger.info("Data not found for Birth history ==" + deathId);
            res.setStatus(HttpStatus.BAD_REQUEST);
            res.setMsg(Constants.RECORD_NOT_FOUND);
        } else {
            logger.info("Data found for Birth history ==" + deathId);
            // try {
            LocalDateTime callgetgetDeathHistoryByDeathIdTime = LocalDateTime.now();
            logger.info("getHistoryFromBlc callgetgetDeathHistoryByDeathIdTime : " + callgetgetDeathHistoryByDeathIdTime);
            List<BlockchainDeathHistoryResponse> blockchainDeathHistoryResponse = blockchainGatway
                    .getDeathHistoryByDeathId(deathId.toString(), channelGovtHospital);

            // logger.info("Blockchai response for History ==== " + blockchainDeathHistoryResponse);
            // To get List of Json data from Blockchain
            // List<BirthModel> birthModels = new ArrayList<BirthModel>();
            LocalDateTime afterCallgetHistoryFromBlcTime = LocalDateTime.now();
            logger.info("getHistoryFromBlc afterCallgetHistoryFromBlcTime : " + afterCallgetHistoryFromBlcTime);
            List<FetchHistoryFromBlc> fetchHistoryFromBlcs = new ArrayList<FetchHistoryFromBlc>();
            DeathModel deathModelOld = null;

            String diff = Constants.DEATH_HISTORY_DESCRIPTION_HEADER;

            for (int i = 0; i < blockchainDeathHistoryResponse.size(); i++) {
                // logger.info(" Response iteration from birth history ==" + blockchainDeathHistoryResponse.get(i));
                BlockchainDeathHistoryResponse historyResponse = (BlockchainDeathHistoryResponse) blockchainDeathHistoryResponse
                        .get(i);
                // logger.info("=== history Response ====" + historyResponse);

            }

            for (BlockchainDeathHistoryResponse deathModelResponse : blockchainDeathHistoryResponse) {
                DeathModel deathModel = deathModelResponse.getValue();
                if (!Constants.RECORD_STATUS_DRAFT.equalsIgnoreCase(deathModel.getStatus())) {
                    FetchHistoryFromBlc fetchHistoryFromBlc = new FetchHistoryFromBlc();
                    fetchHistoryFromBlc.setDate(deathModel.getModifiedAt());
                    fetchHistoryFromBlc.setUserName(deathModel.getModifiedBy());
                    fetchHistoryFromBlc.setTransactionType(deathModel.getTransactionType());
                    if (Constants.RECORD_STATUS_PENDING.equalsIgnoreCase(deathModel.getStatus())) {
                        fetchHistoryFromBlc
                                .setDescription(CommonUtil.getDifference(deathModelOld, deathModel, diff));
                    } else if (Constants.RECORD_STATUS_APPROVED.equalsIgnoreCase(deathModel.getStatus())) {
                        fetchHistoryFromBlc.setDescription(Constants.HISTORY_APPROVED_TEXT);

                        fetchHistoryFromBlc.setUserName(deathModel.getApprovedBy());
                        fetchHistoryFromBlc.setDate(deathModel.getApprovedAt());
                    } else if (Constants.RECORD_STATUS_REJECTED.equalsIgnoreCase(deathModel.getStatus())) {
                        fetchHistoryFromBlc
                                .setDescription(Constants.HISTORY_REJECTION_TEXT + deathModel.getRejectionRemark());

                        fetchHistoryFromBlc.setUserName(deathModel.getRejectedBy());
                        fetchHistoryFromBlc.setDate(deathModel.getRejectedAt());
                    }
                    deathModelOld = deathModel;
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
        logger.info("getHistoryFromBlc endTime : " + endTime);

        return res;
    }

    @Override
    public ApiResponse getFilteredData(CFCFilterDto filterDto, String filterType, HttpServletRequest request) {

        ApiResponse response = new ApiResponse();
        JwtUtil jwtUtil = new JwtUtil();
        String userName = authService.getUserIdFromRequest(request);
        // String userName = jwtUtil.getUsernameFromRequest(request);


        List<DeathModel> deathRecords = findAll(filterDto, filterType, userName);
        if (Constants.FILTER_NAME_REJECTION.equals(filterType)
                || Constants.FILTER_LEGAL_CORRECTION_REJECTION.equals(filterType)) {
            if (deathRecords == null) {
                deathRecords = new ArrayList<>();
            }
        }
        updateViewDocuments(deathRecords, filterType);

        response.setStatus(HttpStatus.OK);
        response.setData(deathRecords);
        // }
        return response;
    }

    private void updateViewDocuments(List<DeathModel> deathRecords, String filterType) {
        Map<Long, SlaDetailsModel> slaDetailsMap = new HashMap<>();
        if (Constants.FILTER_NAME_INCLUSION.equals(filterType) || Constants.FILTER_NAME_APPROVAL.equals(filterType) ||
                Constants.FILTER_NAME_REJECTION.equals(filterType)) {
            // slaDetailsMap =
            // slaDetailsService.findAllPendingMap(Constants.RECORD_TYPE_BIRTH,
            // Constants.RECORD_STATUS_PENDING, Constants.RECORD_STATUS_UPLOADED,
            // Constants.RECORD_STATUS_REJECTED, Constants.NAME_INCLUSION);
            slaDetailsMap = slaDetailsService.findAllPendingMap(Constants.RECORD_TYPE_DEATH, Constants.RECORD_NAME_INCLUSION);

        } else if (Constants.FILTER_LEGAL_CORRECTIONS.equals(filterType)
                || Constants.FILTER_LEGAL_CORRECTIONS_APPROVAL.equals(filterType) ||
                Constants.FILTER_LEGAL_CORRECTION_REJECTION.equals(filterType)) {
            // slaDetailsMap =
            // slaDetailsService.findAllPendingMap(Constants.RECORD_TYPE_BIRTH,
            // Constants.RECORD_STATUS_PENDING, Constants.RECORD_STATUS_UPLOADED,
            // Constants.RECORD_STATUS_REJECTED, Constants.LEGAL_CORRECTIONS);
            slaDetailsMap = slaDetailsService.findAllPendingMap(Constants.RECORD_TYPE_DEATH,
                    Constants.LEGAL_CORRECTIONS);
        }

        for (DeathModel deathModel : deathRecords) {
            deathModel.setViewDocuments(slaDetailsMap.containsKey(deathModel.getDeathId()));
        }

    }

    private List<DeathModel> findAll(CFCFilterDto filterDto1, String filterType, String userName)  throws DateRangeException {

        Long organizationId = authService.getOrganizationIdFromUserId(userName);

        return deathRepository.findAll(new Specification<DeathModel>() {
            @Override
            public Predicate toPredicate(Root<DeathModel> root, CriteriaQuery<?> query,
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
//                if (!CommonUtil.checkNullOrBlank(filterDto.getRegistrationNumber())
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
                         /*   Join<Object, Object> joinParent = root.join(Constants.CORRECTION_SLA_ID, JoinType.INNER);
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
                                criteriaBuilder.equal(root.get(Constants.STATUS),
                                        Constants.RECORD_STATUS_INCLUSION_REJECTED));
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
                                criteriaBuilder.equal(root.get(Constants.STATUS),
                                        Constants.RECORD_STATUS_CORRECTION_REJECTED));
                        // Predicate predicateNameNull =
                        // criteriaBuilder.isTrue(root.get(Constants.CHILD_NAME).isNotNull());
                        // Predicate predicateNameEmpty =
                        // criteriaBuilder.notEqual(root.get(Constants.CHILD_NAME), "");
                        // Predicate predicateName = criteriaBuilder.and(predicateNameNull,
                        // predicateNameEmpty);
                        Predicate approvePredicate = criteriaBuilder.and(approvedPredicate);
//                        Predicate userPredicate = criteriaBuilder.equal(root.get(Constants.USER_NAME), userName);
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

                        throw new IllegalArgumentException("Invalid Request");
                        // predicates.add(criteriaBuilder.and(
                        //       criteriaBuilder.notEqual(root.get(Constants.STATUS), Constants.RECORD_STATUS_DRAFT)));
                    }
                    if (filterType.equalsIgnoreCase(Constants.FILTER_LEGAL_CORRECTIONS_APPROVAL)
                            || filterType.equalsIgnoreCase(Constants.FILTER_LEGAL_CORRECTION_REJECTION)
                            || filterType.equalsIgnoreCase(Constants.FILTER_LEGAL_CORRECTIONS)) {

                     /*   Join<Object, Object> joinParent = root.join(Constants.CORRECTION_SLA_ID, JoinType.INNER);
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

                    if(isJoinCorrection){
                        if(!currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_REGISTRAR))) {
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
                // .and(criteriaBuilder.equal(root.get(Constants.STATUS),
                // Constants.RECORD_STATUS_APPROVED)));
                query.orderBy(criteriaBuilder.desc(root.get("registrationDatetime")));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }

        });
    }

    @Override
    @Transactional
    public void updateCorrectionStatus(SlaDetailsModel slaDetailsModel, String userName) {
        LocalDateTime startTime = LocalDateTime.now();
        logger.info("updateCorrectionStatus startTime " + startTime);
        DeathModel deathModel = deathRepository.findById(slaDetailsModel.getBndId()).get();
        deathModel.setStatus(CommonUtil.getCorrectionStatus(slaDetailsModel.getStatus()));

        if(Constants.RECORD_STATUS_APPROVED.equalsIgnoreCase(deathModel.getStatus())) {
            CustomBeanUtils.copySlaDetailsToDeathModel(slaDetailsModel, deathModel);
        }
        deathModel.setTransactionType(Constants.LEGAL_CORRECTIONS);
        deathModel.setModifiedAt(LocalDateTime.now());
        deathModel.setModifiedBy(userName);
        deathModel.setCorrectionSlaId(slaDetailsModel.getSlaDetailsId());
        deathRepository.save(deathModel);
        DeathHistoryModel deathHistoryModel = new DeathHistoryModel();
        BeanUtils.copyProperties(deathModel, deathHistoryModel);
        deathHistoryModel = deathHistoryRepository.save(deathHistoryModel);
        deathHistoryRepository.save(deathHistoryModel);
        LocalDateTime endTime = LocalDateTime.now();
        logger.info("updateCorrectionStatus endTime " + endTime);
    }

    @Override
    public DeathModel findById(Long bndId) {
        return deathRepository.findById(bndId).get();
    }

    @Override
    public List<DeathHistoryModel> getDeathListForReport(ReportSearchDto reportSearchDto, String type)  throws DateRangeException {
        return deathHistoryRepository.findAll(new Specification<DeathHistoryModel>() {
            @Override
            public Predicate toPredicate(Root<DeathHistoryModel> root, CriteriaQuery<?> query,
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
                /*    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
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
    public ApiResponse loadSdmLetterFileById(String deathId) {
        ApiResponse res = new ApiResponse();
        Optional<DeathModel> deathModelOp = deathRepository.findById(Long.parseLong(deathId));
        DeathModel deathRecord = deathModelOp.get();
        // logger.info("==== Birth Record is "+deathRecord);
        if(!deathRecord.getSdmLetterImage().isEmpty()){
            try {
                fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
                Path filePath = this.fileStorageLocation.resolve(deathRecord.getSdmLetterImage()).normalize();
                Resource resource = new UrlResource(filePath.toUri());
                res.setStatus(HttpStatus.OK);
                res.setData(resource);

            }catch (Exception e)
            {
                res.setStatus(HttpStatus.NOT_FOUND);
                res.setMsg(Constants.FILE_NOT_FOUND+" "+deathRecord.getSdmLetterImage());
            }
        }
        else{
            res.setStatus(HttpStatus.NOT_FOUND);
            res.setMsg(Constants.FILE_NOT_FOUND);
        }

        return res;
    }

    @Override
    public ApiResponse deleteDeath(Long deathId, HttpServletRequest request) {

        // logger.info("Before deleteDeath ====== "+deathId);

        ApiResponse res = new ApiResponse();
        Optional<DeathModel> deathModelDtl = deathRepository.findById(deathId);
        String userId = authService.getUserIdFromRequest(request);
        Optional<UserModel> currentUserOp = authRepository.findById(Long.parseLong(userId));
        UserModel currentUser = currentUserOp.get();
        if(!deathModelDtl.isPresent()){
            res.setStatus(HttpStatus.BAD_REQUEST);
            res.setMsg(Constants.RECORD_WRONG_ERROR);
        }else{
            DeathModel deathModel = deathModelDtl.get();
            if(!Constants.RECORD_STATUS_DRAFT.equalsIgnoreCase(deathModel.getStatus())){
                res.setStatus(HttpStatus.BAD_REQUEST);
                res.setMsg(Constants.RECORD_STATUS_DRAFT_ERROR);
                return res;
            }
            if(!userId.equalsIgnoreCase(deathModel.getUserId())
                    && !currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_REGISTRAR))){
                res.setStatus(HttpStatus.BAD_REQUEST);
                res.setMsg(Constants.RECORD_USER_ERROR);
                return res;
            }
            if(Constants.RECORD_Y.equalsIgnoreCase(deathModel.getIsDeleted())){
                res.setStatus(HttpStatus.BAD_REQUEST);
                res.setMsg(Constants.RECORD_ALREADY_DELETED_ERROR);
                return res;
            }
            deathModel.setModifiedAt(LocalDateTime.now());
            deathModel.setModifiedBy(userId);
            deathModel.setIsDeleted(Constants.RECORD_Y);
            deathModel.setTransactionType(Constants.RECORD_DELETE);
            deathRepository.save(deathModel);

            DeathHistoryModel deathHistoryModel = new DeathHistoryModel();
            deathHistoryModel.setTransactionType(Constants.RECORD_DELETE);
            BeanUtils.copyProperties(deathModel, deathHistoryModel);
            deathHistoryModel = deathHistoryRepository.save(deathHistoryModel);
            res.setData(deathModel.getApplicationNumber());
            res.setMsg(Constants.DEATH_DELETED_SUCCESS_MESSAGE);
            res.setStatus(HttpStatus.OK);
        }
        return res;

    }

    private void updateCitizenRecords(DeathDto deathDto, String userId) {
        CitizenDeathModel citizenDeathModel = new CitizenDeathModel();
        BeanUtils.copyProperties(deathDto, citizenDeathModel);
        citizenDeathModel.setModifiedAt(LocalDateTime.now());
        citizenDeathModel.setModifiedBy(userId);
        citizenDeathModel.setStatus(Constants.RECORD_SUBMITTED);
        citizenDeathRepository.save(citizenDeathModel);
    }
}
