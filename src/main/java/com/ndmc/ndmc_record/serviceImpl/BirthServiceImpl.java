package com.ndmc.ndmc_record.serviceImpl;

import com.ndmc.ndmc_record.blockchainGatway.BlockchainGatway;
import com.ndmc.ndmc_record.config.Constants;
import com.ndmc.ndmc_record.dto.ApiResponse;
import com.ndmc.ndmc_record.dto.BirthDto;
import com.ndmc.ndmc_record.dto.CFCFilterDto;
import com.ndmc.ndmc_record.dto.ChildDetails;
import com.ndmc.ndmc_record.dto.ReportSearchDto;
import com.ndmc.ndmc_record.exception.DateRangeException;
import com.ndmc.ndmc_record.exception.DocumentNotFoundException;
import com.ndmc.ndmc_record.model.*;
import com.ndmc.ndmc_record.property.FileStorageProperties;
import com.ndmc.ndmc_record.repository.*;
import com.ndmc.ndmc_record.service.BirthService;
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

import org.springframework.web.client.RestTemplate;
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
import java.lang.Exception;

@Service
public class BirthServiceImpl implements BirthService {

    private final Logger logger = LoggerFactory.getLogger(BirthServiceImpl.class);


    @Value("${approved_status}")
    private String approvedStatus;

    @Value("${AUTO_APPROVE_HOUR}")
    private int autoApproveHour;

    @Value("${CHANNEL_GOVT_HOSPITAL}")
    private String channelGovtHospital;

    @Value("${GOVT_HOSPITAL_USER}")
    private String userTypeGovt;

    @Value("${CERT_VERIFICATION_URL}")
    private String vertificateVerificationUrl;

    @Value("${PRIVATE_HOSPITAL_USER}")
    private String userTypePrivate;


    @Autowired
    BirthRepository birthRepository;

    @Autowired
    AuthRepository authRepository;

    @Autowired
    LateFeeRepository lateFeeRepository;

    @Autowired
    BirthHistoryRepository birthHistoryRepository;

    @Autowired
    CertificatePrintRepository certificatePrintRepository;

    @Autowired
    BlockchainGatway blockchainGatway;

    @Autowired
    QrCodeService qrCodeService;

    @Autowired
    ApplicatioNumberCounterServiceImpl applicatioNumberCounterService;

    @Autowired
    SlaDetailsService slaDetailsService;


    @Autowired
    SlaDetailsRepository slaDetailsRepository;

    @Autowired
    AuthServiceImpl authService;
    @Autowired
    OrganizationRepository organizationRepository;

   @Autowired
   FileStorageProperties fileStorageProperties;
  private Path fileStorageLocation;

  @Autowired
  CitizenBirthRepository citizenBirthRepository;

    @Override
    @Transactional
    public ApiResponse saveBirthRecords(BirthDto birthDto, HttpServletRequest request, MultipartFile sdmLetterImage) throws Exception {
        LocalDateTime startMethodTime = LocalDateTime.now();
        logger.info("saveBirthRecords startMethodTime:" + startMethodTime);


        JwtUtil jwtUtil = new JwtUtil();
        String userId = authService.getUserIdFromRequest(request);
       // long userId = Long.parseLong(username)
        logger.info("====== userName is  ===="+userId);
        ApiResponse res = new ApiResponse();
        List<ChildDetails> responseList = new ArrayList<ChildDetails>();
        for (ChildDetails childDetails : birthDto.getChildDetails()) {
            boolean inserted = insertBirthRecord(childDetails, birthDto, responseList, userId, jwtUtil, res, sdmLetterImage);
            if (!inserted) {
                return res;
            }
        }


        res.setMsg(Constants.BIRTH_SUCCESS_MESSAGE);
        res.setStatus(HttpStatus.OK);
        res.setData(responseList);
        LocalDateTime endMethodTime = LocalDateTime.now();
        logger.info("saveBirthRecords endMethodTime:" + endMethodTime);

        return res;
    }




    private boolean insertBirthRecord(ChildDetails childDetails, BirthDto birthDto, List<ChildDetails> responseList,
            String userId, JwtUtil jwtUtil, ApiResponse res, MultipartFile sdmLetterImage) throws Exception {
        LocalDateTime startMethodTime = LocalDateTime.now();
        logger.info("insertBirthRecord startMethodTime:" + startMethodTime);
        // logger.info("=======sdmLetterImage======:" + sdmLetterImage);
        BirthModel birthModel = new BirthModel();
        Optional<UserModel> currentUserOp = authRepository.findById(Long.parseLong(userId));

        UserModel currentUser = currentUserOp.get();

        // Get User type from OrganizationId
        String orgId = currentUser.getOrganizationId();
        Optional<OrganizationModel> organizationModelOp = organizationRepository.findById(Long.parseLong(orgId));
        OrganizationModel organizationModel = organizationModelOp.get();
        String orgType = organizationModel.getOrganizationType();
        String orgCode = organizationModel.getOrganisationCode();
        String divisionCode = organizationModel.getDivisionCode();
        Long organizationId = organizationModel.getOrganizationId();
        birthModel.setOrganizationId(organizationId);

        BeanUtils.copyProperties(birthDto, birthModel);
        CustomBeanUtils.copyChildDetails(childDetails, birthModel);
        LocalDateTime now = LocalDateTime.now();
        birthModel.setCreatedAt(now);

        if(CommonUtil.checkNullOrBlank(childDetails.getGenderCode())){
            throw new IllegalArgumentException("Invalid Gender");
        }
        // getDivisionCode is not coming for UI then insert divisionCode
        if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CREATOR))
                || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_APPROVER)))
        {
            birthModel.setDivisionCode(divisionCode);
        }else{
            birthModel.setDivisionCode(birthDto.getDivisionCode() == null ?  "0" : birthDto.getDivisionCode());
        }

        //SDMLetter Image upload
        if (sdmLetterImage != null && !sdmLetterImage.isEmpty()) {
            String fileNameold = StringUtils.cleanPath(sdmLetterImage.getOriginalFilename());
            String extension = fileNameold.substring(fileNameold.lastIndexOf("."));
            String fileName = "sdm_letter_" + userId + "_" + Constants.APPLICATION_TYPE_BIRTH + System.currentTimeMillis() + extension;
            //byte [] digest = new DigestUtils(DigestUtils.getSha3_224Digest()).digest(dataToDigest);
            String fileHash = new DigestUtils(DigestUtils.getMd5Digest()).digestAsHex(sdmLetterImage.getInputStream());
            logger.info("sdm leter  image name " + fileName);
            logger.info("sdm leter  image hash code " + fileHash);

            Path fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
            ;
            //String uploadDir = fileStorageLocation;
            CommonUtil.saveFile(fileStorageLocation, fileName, sdmLetterImage);

            birthModel.setSdmLetterImage(fileName);
            birthModel.setSdmLetterImageHash(fileHash);
        }
        logger.info("===== CHILD DETAILS  getRegistrationDate====" +childDetails.getRegistrationDate());

        birthModel.setRegistrationDatetime(childDetails.getRegistrationDate() == null ? now : childDetails.getRegistrationDate().atStartOfDay());

        logger.info("===== status is ====" + birthDto.getStatus());
        if (Constants.RECORD_STATUS_PENDING.equalsIgnoreCase(birthDto.getStatus())
                && ((birthModel.getApplicationNumber() == null
                || birthModel.getApplicationNumber().trim().isEmpty())
                || (birthModel.getRegistrationNumber() == null
                || birthModel.getRegistrationNumber().trim().isEmpty()))) {
            ApplicationNumberCounter counter = applicatioNumberCounterService.getRegistrationNumberCounter(
                    organizationModel.getOrganizationId(), orgCode, Constants.APPLICATION_TYPE_BIRTH,
                    birthModel.getRegistrationDatetime());
            String regNo = counter.getCount() + "";
            String applNo = applicatioNumberCounterService.generateApplicationNumber(counter);
            birthModel.setApplicationNumber(applNo);
            birthModel.setRegistrationNumber(childDetails.getRegistrationNumber() == null ? regNo : childDetails.getRegistrationNumber());
            logger.info("===== appl number is ====" + applNo);
            logger.info("===== reg number is ====" + birthModel.getRegistrationNumber());

             /*@AUTHOR DEEPAK
        Dated: 07-09-22
        * If Record is from Citizen then Update their status from PENDING  to SUBMITTED
        * */
            if(birthDto.getBirthIdTemp() != null){
                Optional<CitizenBirthModel> citizenBirthModelOp = citizenBirthRepository.findById(birthDto.getBirthIdTemp());
                CitizenBirthModel citizenBirthModel = citizenBirthModelOp.get();
                if(citizenBirthModelOp.isPresent() && birthDto.getOrganizationCode().equalsIgnoreCase(citizenBirthModel.getOrganizationCode())) {
                    updateCitizenRecords(birthDto, userId);
                }
            }
        }

        birthModel.setRegistrationNumber(childDetails.getRegistrationNumber() == null ? birthModel.getRegistrationNumber() : childDetails.getRegistrationNumber());

        birthModel.setModifiedAt(now);
        birthModel.setCreatedAt(now);
        birthModel.setModifiedBy(userId);
        // birthModel.setCreatedBy(username);
        //Added by Deepak 0n 22-04-2022
        birthModel.setUserId(userId);
        birthModel.setStatus(birthDto.getStatus().toUpperCase(Locale.ROOT));
        long numberOfDays = 0;
        if (childDetails.getEventDate() != null) {
            numberOfDays = CommonUtil.getDayBetweenDates(childDetails.getEventDate().toLocalDate(), LocalDate.now());
        }

        Optional<LateFee> existedFeeData = lateFeeRepository.findById(Constants.APPLICATION_TYPE_BIRTH);
        if (Constants.USER_TYPE_CFC.equalsIgnoreCase(organizationModel.getOrganizationType())) {
            existedFeeData = lateFeeRepository.findById(Constants.APPLICATION_TYPE_CFC_BIRTH);
        }
		logger.info("====== usertype ====" + organizationModel.getOrganizationType() + " and Record type is ===" + birthDto.getRecordType());
        if (Constants.USER_TYPE_CFC.equalsIgnoreCase(organizationModel.getOrganizationType())
                && Constants.RECORD_TYPE_OLD.equalsIgnoreCase(birthDto.getRecordType())) {
            birthModel.setLateFee(0F);

        } else if (Constants.USER_TYPE_CFC.equalsIgnoreCase(organizationModel.getOrganizationType())
                && (birthDto.getRecordType() == null ||
                !Constants.RECORD_TYPE_OLD.equalsIgnoreCase(birthDto.getRecordType().trim()))
                && numberOfDays > existedFeeData.get().getEndDays()) {
            // Check for SDM ORDER
            if (birthDto.getSdmLetterNo() != null && !birthDto.getSdmLetterNo().trim().isEmpty()) {
                birthModel.setLateFee(existedFeeData.get().getFee());
            } else {
                res.setMsg(Constants.SDM_LETTER_BLANK);
                res.setStatus(HttpStatus.NOT_FOUND);
                return false;
            }
        } else if (numberOfDays > existedFeeData.get().getEndDays()) {
            // After 365 Days Birth Record entry
            res.setMsg(Constants.VISIT_CFC);
            res.setStatus(HttpStatus.BAD_REQUEST);
            return false;
            // return res;

        } else if (numberOfDays > existedFeeData.get().getStartDays()
                && numberOfDays <= existedFeeData.get().getEndDays()) {
            // Between 21 - 365 Days Birth Record entry
            birthModel.setLateFee(existedFeeData.get().getFee());
        } else {
            // Within 21 days Birth Record entry
            birthModel.setLateFee(0F);
        }


        logger.info("Before save data in Birth table ====== " + LocalDateTime.now());
       // logger.info("Requested Body O birth model " + birthModel);
        birthModel.setCrNumber(CommonUtil.checkNullOrBlank(birthDto.getCrNumber()) ? "0" : birthDto.getCrNumber());
        birthModel.setTransactionType(Constants.RECORD_ADDED);
        birthModel.setCrNumber(CommonUtil.checkNullOrBlank(birthDto.getCrNumber()) ? "0" : birthDto.getCrNumber());
        birthModel = birthRepository.save(birthModel);
        childDetails.setBirthId(birthModel.getBirthId());
        childDetails.setApplicationNumber(birthModel.getApplicationNumber());
        childDetails.setRegistrationNumber(birthModel.getRegistrationNumber());
        childDetails.setRegistrationDate(birthModel.getRegistrationDatetime().toLocalDate());
        responseList.add(childDetails);
        logger.info("After save data in Birth table ====== " + LocalDateTime.now());


        // Add birth history
        BirthHistoryModel birthHistoryModel = new BirthHistoryModel();
        birthHistoryModel.setTransactionType(Constants.RECORD_ADDED);
        BeanUtils.copyProperties(birthModel, birthHistoryModel);
        logger.info("Before save data in Birth_history table ====== " + now);

        birthHistoryModel = birthHistoryRepository.save(birthHistoryModel);
        logger.info("After save data in Birth_history table ====== " + now);
        logger.info("Before Blockchain calling ===== " + now);
        // blockchainGatway.modifyRecord(birthModel);

        // Send Text message to approver based on organization Id 23-02-22


       // logger.info("======= BLOCKCHAIN PAYLOAD =====" + birthModel.toString());

        String channelName = authService.getChannelName(birthDto.getBirthId(), userId, birthDto.getChannelName(), Constants.RECORD_TYPE_BIRTH);
        //try {
            LocalDateTime callBLCTime = LocalDateTime.now();
            logger.info("insertBirthRecord callBLCTime:" + callBLCTime);
            BlockchainBirthResponse blockchainResult = blockchainGatway.insertBirthRecord(birthModel,
                    channelGovtHospital);
            // logger.info("After data addition in Blockchain ==== " + now);
            // JsonUtil.getObjectFromJson(blockchainResult, BlockchainGatway.class);

            LocalDateTime afterCallBLCTime = LocalDateTime.now();
            logger.info("insertBirthRecord afterCallBLCTime:" + afterCallBLCTime);
            // Set Blockchain response
            String message = blockchainResult.getMessage();
            String txID = blockchainResult.getTxID();
            String status = blockchainResult.getStatus();

            if (Constants.BLC_STATUS_TRUE.equalsIgnoreCase(status)) {
                birthModel.setBlcMessage(message);
                birthModel.setBlcTxId(txID);
                birthModel.setBlcStatus(status);

                birthHistoryModel.setBlcMessage(message);
                birthHistoryModel.setBlcTxId(txID);
                birthHistoryModel.setBlcStatus(status);
            }
      //  } catch (Exception e) {
            //     logger.error("===Blc Exception ===", e);
       // }
        // Save Blockchain response in Model
        birthModel = birthRepository.save(birthModel);
        birthHistoryModel = birthHistoryRepository.save(birthHistoryModel);

        /* Code to send new Birth record text message to parents
         * Code by Deepak
         * 29-04-2022
         * */
        if(birthModel != null && birthModel.getApplicationNumber() !=null
                && !CommonUtil.checkNullOrBlank(birthModel.getContactNumber())){

            CommonUtil commonUtil = new CommonUtil();

            logger.info("  === birth addition response ====="+birthModel);
            String encodedApplNo = Base64.getEncoder().encodeToString(birthModel.getApplicationNumber().getBytes());
            String type = "/B";
            String originalUrl = Constants.REVIEW_UAT_URL+birthModel.getApplicationNumber()+type;

            try{
            commonUtil.sendTextMessage(birthModel.getMotherName(), birthModel.getContactNumber(), birthModel.getApplicationNumber(), Constants.RECORD_TYPE_BIRTH, Constants.NEW_APPROVAL_REQUEST, "", "", originalUrl,"", "");
            }catch (Exception e){
                logger.info("===SMS EXCEPTION =="+e);
            }
            }

        return true;
    }

    @Override
    @Transactional
    public ApiResponse nameInclusionCorrection(BirthDto birthDto, HttpServletRequest request) throws Exception {
        // BirthModel birthModel = new BirthModel();
        LocalDateTime startMethodTime = LocalDateTime.now();
        logger.info("nameInclusionCorrection startMethodTime:" + startMethodTime);
        ApiResponse response = new ApiResponse();

        //  JwtUtil jwtUtil = new JwtUtil();
        String username = authService.getUserIdFromRequest(request);
       // String username = jwtUtil.getUsernameFromRequest(request);


        if (birthDto.getBirthId() != null && !birthDto.getName().isEmpty()) {
            Optional<BirthModel> existedData = birthRepository.findById(birthDto.getBirthId());
            if (!existedData.isPresent()) {
                response.setStatus(HttpStatus.BAD_REQUEST);
                response.setMsg(Constants.RECORD_NOT_FOUND);
            } else {
                BirthModel birthModel = existedData.get();
                // CustomBeanUtils.copyBirthDetailsForUpdate(birthDto, birthModel);

                birthModel.setName(birthDto.getName());
                birthModel.setModifiedBy(username);
                birthModel.setModifiedAt(LocalDateTime.now());
                birthModel.setStatus(Constants.RECORD_STATUS_PENDING);
                birthModel.setTransactionType(Constants.RECORD_NAME_INCLUSION);

                birthModel = birthRepository.save(birthModel);

                // Add birth history
                BirthHistoryModel birthHistoryModel = new BirthHistoryModel();
                // BeanUtils.copyProperties(birthModel, birthHistoryModel);
                birthHistoryModel.setTransactionType(Constants.RECORD_NAME_INCLUSION);
                birthHistoryModel = birthHistoryRepository.save(birthHistoryModel);

                // try {
                    
                    LocalDateTime callBLCTime = LocalDateTime.now();
                    logger.info("nameInclusionCorrection callBLCTime:" + callBLCTime);
                    BlockchainUpdateBirthResponse blockchainResult = blockchainGatway.updateBirthRecord(birthModel,
                            channelGovtHospital);
                    if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(blockchainResult.getStatus())) {
                    
                        // logger.info("After data addition in Blockchain ==== " + LocalDateTime.now());
                        // JsonUtil.getObjectFromJson(blockchainResult, BlockchainGatway.class);

                        LocalDateTime afterCallBLCTime = LocalDateTime.now();
                        logger.info("nameInclusionCorrection afterCallBLCTime:" + afterCallBLCTime);
                        // Set Blockchain response
                        String message = blockchainResult.getMessage();
                        String txID = blockchainResult.getTxID();
                        String status = blockchainResult.getStatus();
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
                    // Save Blockchain response in Model
                    birthModel = birthRepository.save(birthModel);
                    birthHistoryModel = birthHistoryRepository.save(birthHistoryModel);

                    response.setMsg(Constants.BIRTH_UPDATE_SUCCESS_MESSAGE);
                    response.setStatus(HttpStatus.OK);
                    response.setData(birthModel);
                } else {
                    
                    // logger.info("nameInclusionCorrection False response from blockchain " + blockchainResult);
                    throw new Exception(Constants.INTERNAL_SERVER_ERROR);
                }
            }

        } else {
            response.setStatus(HttpStatus.BAD_REQUEST);
            response.setMsg(Constants.RECORD_NOT_FOUND);
        }

        LocalDateTime endMethodTime = LocalDateTime.now();
        logger.info("nameInclusionCorrection endMethodTime:" + endMethodTime);
        return response;
    }

    @Override
    @Transactional
    public ApiResponse getHistoryFromBlc(Long birthId, HttpServletRequest request) throws Exception {

        LocalDateTime startMethodTime = LocalDateTime.now();
        logger.info("getHistoryFromBlc startMethodTime:" + startMethodTime);
        // BirthModel birthModel = new BirthModel();
        ApiResponse res = new ApiResponse();
        
        LocalDateTime callDBFindByIdTime = LocalDateTime.now();
        logger.info("getHistoryFromBlc callDBFindByIdTime:" + callDBFindByIdTime);
        Optional<BirthModel> birthModelOptional = birthRepository.findById(birthId);
        
        LocalDateTime afterCallDBFindByIdTime = LocalDateTime.now();
        logger.info("getHistoryFromBlc afterCallDBFindByIdTime:" + afterCallDBFindByIdTime);

        JwtUtil jwtUtil = new JwtUtil();
        String username = authService.getUserIdFromRequest(request);
        if (!birthModelOptional.isPresent()) {

            logger.info("Data not found for Birth history ==" + birthId);
            res.setStatus(HttpStatus.BAD_REQUEST);
            res.setMsg(Constants.RECORD_NOT_FOUND);
        } else {
            logger.info("Data found for Birth history ==" + birthId);
            // try {
                LocalDateTime callBLCTime = LocalDateTime.now();
                logger.info("getHistoryFromBlc callBLCTime:" + callBLCTime);
               
                List<BlockchainBirthHistoryResponse> blockchainBirthHistoryResponse = blockchainGatway
                        .getBirthHistoryByBirthId(birthId.toString(), channelGovtHospital);

                logger.info("Blockchai response for History ==== " + blockchainBirthHistoryResponse);
                
                LocalDateTime afterCallBLCTime = LocalDateTime.now();
                logger.info("getHistoryFromBlc afterCallBLCTime:" + afterCallBLCTime);

                // To get List of Json data from Blockchain
                // List<BirthModel> birthModels = new ArrayList<BirthModel>();
                List<FetchHistoryFromBlc> fetchHistoryFromBlcs = new ArrayList<FetchHistoryFromBlc>();
                List<BlockchainBirthHistoryResponse> fetchHistoryFromBlcsResp = new ArrayList<BlockchainBirthHistoryResponse>();
                BirthModel birthModelOld = null;

                String diff = Constants.BIRTH_HISTORY_DESCRIPTION_HEADER;

                // for (int i = 0; i < blockchainBirthHistoryResponse.size(); i++) {
                //     // logger.info(" Response iteration from birth history ==" + blockchainBirthHistoryResponse.get(i));
                //     BlockchainBirthHistoryResponse historyResponse = (BlockchainBirthHistoryResponse) blockchainBirthHistoryResponse
                //             .get(i);
                //     // logger.info("=== history Response ====" + historyResponse);

                // }

                LocalDateTime sortingHistoryStartTime = LocalDateTime.now();
                logger.info("getHistoryFromBlc sortingHistoryStartTime:" + sortingHistoryStartTime);

                for (int i = blockchainBirthHistoryResponse.size() - 1; i >= 0; i--) {

                    BlockchainBirthHistoryResponse birthModelResponse = blockchainBirthHistoryResponse.get(i);
                    BirthModel birthModel = birthModelResponse.getValue();
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
                       // fetchHistoryFromBlcs.add(fetchHistoryFromBlc);
                       // String channelName = Constants.NDMC_GOVT;
                      //  setBlcLogs(birthModelResponse.getValue(), channelGovtHospital);
                        logger.info("=== value DATA ==="+birthModelResponse.getValue());
                        fetchHistoryFromBlcsResp.add(birthModelResponse);
                    }
                }

                LocalDateTime sortingHistoryEndTime = LocalDateTime.now();
                logger.info("getHistoryFromBlc sortingHistoryEndTime:" + sortingHistoryEndTime);

                res.setStatus(HttpStatus.OK);
               // res.setData(fetchHistoryFromBlcs);
                res.setData(fetchHistoryFromBlcsResp);
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

        // logger.info("Birth history response : " + res);
        LocalDateTime endMethodTime = LocalDateTime.now();
        logger.info("getHistoryFromBlc endMethodTime:" + endMethodTime);

        return res;
    }

    @Override
    @Transactional
    public ApiResponse updateBirthRecords(BirthDto birthDto, HttpServletRequest request, MultipartFile sdmLetterImage) throws Exception {

        LocalDateTime startMethodTime = LocalDateTime.now();
        logger.info("updateBirthRecords startMethodTime:" + startMethodTime);
        JwtUtil jwtUtil = new JwtUtil();
        List<ChildDetails> responseList = new ArrayList<ChildDetails>();
        String userId = authService.getUserIdFromRequest(request);

        ApiResponse res = new ApiResponse();
        for (ChildDetails childDetails : birthDto.getChildDetails()) {
            if (childDetails.getBirthId() == null) {
                LocalDateTime callinsertBirthRecordTime = LocalDateTime.now();
                logger.info("updateBirthRecords callinsertBirthRecordTime:" + callinsertBirthRecordTime);
                boolean inserted = insertBirthRecord(childDetails, birthDto, responseList, userId, jwtUtil, res, sdmLetterImage);
                
                LocalDateTime afterCallinsertBirthRecordTime = LocalDateTime.now();
                logger.info("updateBirthRecords afterCallinsertBirthRecordTime:" + afterCallinsertBirthRecordTime);

                if (!inserted) {
                    return res;
                }

            }
            else {
                LocalDateTime callfindByIdTime = LocalDateTime.now();
                logger.info("updateBirthRecords callfindByIdTime:" + callfindByIdTime);
                
                Optional<BirthModel> existedData = birthRepository.findById(childDetails.getBirthId());
                LocalDateTime afterCallfindByIdTime = LocalDateTime.now();
                logger.info("updateBirthRecords afterCallfindByIdTime:" + afterCallfindByIdTime);
                
                // BirthModel birthModel = new BirthModel();
                BirthModel birthModel = existedData.get();
                // logger.info("Requested Data " + birthModel);

                LocalDateTime callfindByUserNameTime = LocalDateTime.now();
                logger.info("updateBirthRecords callfindByUserNameTime:" + callfindByUserNameTime);
                
                // UserModel currentUser = authRepository.findByUserName(userId);
                UserModel currentUser = authRepository.findById(Long.parseLong(userId)).get();

                // Get User type from OrganizationId
                String orgId = currentUser.getOrganizationId();
                Optional<OrganizationModel> organizationModelOp = organizationRepository.findById(Long.parseLong(orgId));
                OrganizationModel organizationModel = organizationModelOp.get();
                String orgType = organizationModel.getOrganizationType();
                String orgCode = organizationModel.getOrganisationCode();
                String divisionCode = organizationModel.getDivisionCode();
                LocalDateTime afterCallfindByUserNameTime = LocalDateTime.now();
                logger.info("updateBirthRecords afterCallfindByUserNameTime:" + afterCallfindByUserNameTime);
                // getDivisionCode is not coming for UI then insert divisionCode
                if(currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CREATOR))
                        || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_APPROVER)))
                {
                    birthModel.setDivisionCode(divisionCode);
                }else{
                    birthModel.setDivisionCode(birthDto.getDivisionCode() == null ?  "0" : birthDto.getDivisionCode());
                }


                if (!existedData.isPresent()) {
                    // bad request
                    res.setStatus(HttpStatus.BAD_REQUEST);
                    res.setMsg(Constants.RECORD_NOT_FOUND);

                } else if ((!birthModel.getOrganizationCode().equalsIgnoreCase(orgCode)
                        && !Constants.ORG_NDMC.equalsIgnoreCase(orgCode))) {
                    res.setStatus(HttpStatus.BAD_REQUEST);
                    res.setMsg(Constants.ORG_NOT_SAME_MESSAGE);
                    return res;
                } else {

                    if (birthModel.getStatus().equals(approvedStatus)) {
                        res.setMsg(Constants.NOT_PERMITTED);
                        res.setStatus(HttpStatus.BAD_REQUEST);
                        return res;

                    } else {
                        // BeanUtils.copyProperties(birthDto, birthModel);

                        CustomBeanUtils.copyBirthDetailsForUpdate(birthDto, birthModel);
                        CustomBeanUtils.copyChildDetails(childDetails, birthModel);

                        logger.info("=== REGISTRATION DATE IN DTO ==="+birthDto.getRegistrationDate());
                        logger.info("=== BIRTH MODEL IN UPDATE ==="+birthModel);
                      // birthModel.setRegistrationDatetime(birthDto.getRegistrationDate() == null ? LocalDateTime.now() : birthDto.getRegistrationDate().atStartOfDay());
                       // birthModel.setSdmLetterImage(birthModel.getSdmLetterImage());
                        if (Constants.RECORD_STATUS_PENDING.equalsIgnoreCase(birthDto.getStatus())
                                && ((birthModel.getApplicationNumber() == null
                                || birthModel.getApplicationNumber().trim().isEmpty())
                                || (birthModel.getRegistrationNumber() == null
                                || birthModel.getRegistrationNumber().trim().isEmpty()))) {
                            // This will execute when status become draft to Pending
                            LocalDateTime callgetRegistrationNumberCounterTime = LocalDateTime.now();
                            logger.info("updateBirthRecords callgenerateApplicationNumberTime:" + callgetRegistrationNumberCounterTime);
                            ApplicationNumberCounter counter = applicatioNumberCounterService
                                    .getRegistrationNumberCounter(organizationModel.getOrganizationId(), orgCode,
                                            Constants.APPLICATION_TYPE_BIRTH, birthModel.getRegistrationDatetime());
                            String regNo = counter.getCount() + "";

                            LocalDateTime callgenerateApplicationNumberTime = LocalDateTime.now();
                            logger.info("updateBirthRecords callgenerateApplicationNumberTime:" + callgenerateApplicationNumberTime);
                
                            String applNo = applicatioNumberCounterService.generateApplicationNumber(counter);
                            LocalDateTime afterCallgenerateApplicationNumberTime = LocalDateTime.now();
                            logger.info("updateBirthRecords afterCallgenerateApplicationNumberTime:" + afterCallgenerateApplicationNumberTime);
                            birthModel.setApplicationNumber(applNo);
                            // birthModel.setRegistrationNumber(regNo);
                            birthModel.setRegistrationNumber(childDetails.getRegistrationNumber() == null ? regNo : childDetails.getRegistrationNumber());

                        }else if (Constants.RECORD_STATUS_PENDING.equalsIgnoreCase(birthDto.getStatus())) {
                            birthModel.setRegistrationNumber(childDetails.getRegistrationNumber() == null ? birthModel.getRegistrationNumber() : childDetails.getRegistrationNumber());

                        }
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_WITH_TIME);
                        LocalDateTime now = LocalDateTime.now();
                        birthModel.setModifiedAt(now);
                        // birthModel.setModifiedBy(birthDto.getModifiedBy());

                        if(sdmLetterImage != null && !sdmLetterImage.isEmpty() ) {
                            String fileNameold = StringUtils.cleanPath(sdmLetterImage.getOriginalFilename());
                            String extension = fileNameold.substring(fileNameold.lastIndexOf("."));
                            String fileName = "sdm_letter_" + userId + "_" + Constants.APPLICATION_TYPE_BIRTH + System.currentTimeMillis() + extension;
                            //byte [] digest = new DigestUtils(DigestUtils.getSha3_224Digest()).digest(dataToDigest);
                            String fileHash = new DigestUtils(DigestUtils.getMd5Digest()).digestAsHex(sdmLetterImage.getInputStream());
                            logger.info("sdm leter  image name " + fileName);
                            logger.info("sdm leter  image hash code " + fileHash);

                            Path fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();;
                            //String uploadDir = fileStorageLocation;
                            CommonUtil.saveFile(fileStorageLocation, fileName, sdmLetterImage);

                            birthModel.setSdmLetterImage(fileName);
                            birthModel.setSdmLetterImageHash(fileHash);
                        }
                        long numberOfDays = 0;
                        if (childDetails.getEventDate() != null) {
                            numberOfDays = CommonUtil.getDayBetweenDates(childDetails.getEventDate().toLocalDate(), LocalDate.now());
                        }
                        Optional<LateFee> existedFeeData = lateFeeRepository.findById(Constants.APPLICATION_TYPE_BIRTH);
                        if (Constants.USER_TYPE_CFC.equalsIgnoreCase(organizationModel.getOrganizationType())) {
                            existedFeeData = lateFeeRepository.findById(Constants.APPLICATION_TYPE_CFC_BIRTH);
                        }
                        logger.info("====== usertype ====" + organizationModel.getOrganizationType() + " and Record type is ===" + birthDto.getRecordType());
                        if (Constants.USER_TYPE_CFC.equalsIgnoreCase(organizationModel.getOrganizationType())
                                && Constants.RECORD_TYPE_OLD.equalsIgnoreCase(birthDto.getRecordType())) {
                            birthModel.setLateFee(0F);

                        } else if (Constants.USER_TYPE_CFC.equalsIgnoreCase(organizationModel.getOrganizationType())
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
                        } else if (numberOfDays > existedFeeData.get().getEndDays()) {
                            // After 365 Days Birth Record entry
                            res.setMsg(Constants.VISIT_CFC);
                            res.setStatus(HttpStatus.BAD_REQUEST);
                            return res;

                        } else if (numberOfDays > existedFeeData.get().getStartDays()
                                && numberOfDays <= existedFeeData.get().getEndDays()) {
                            // Between 21 - 365 Days Birth Record entry
                            birthModel.setLateFee(existedFeeData.get().getFee());
                        } else {
                            // Within 21 days Birth Record entry
                            birthModel.setLateFee(0F);
                        }

                        birthModel.setModifiedBy(userId);
                        birthModel.setTransactionType(Constants.RECORD_UPDATED);
                        birthModel = birthRepository.save(birthModel);

                        childDetails.setApplicationNumber(birthModel.getApplicationNumber());
                        childDetails.setRegistrationNumber(birthModel.getRegistrationNumber());
                        childDetails.setRegistrationDate(birthModel.getRegistrationDatetime().toLocalDate());
                        responseList.add(childDetails);
                        // Add birth history
                        BirthHistoryModel birthHistoryModel = new BirthHistoryModel();
                        birthModel.setTransactionType(Constants.RECORD_UPDATED);
                        BeanUtils.copyProperties(birthModel, birthHistoryModel);
                        birthHistoryModel = birthHistoryRepository.save(birthHistoryModel);

                        // BLOCKCHAIN CALL
                        // BeanUtils.copyProperties(birthModel, birthDto);
                        // try {
                            LocalDateTime callBlockchainTime = LocalDateTime.now();
                            logger.info("updateBirthRecords callBlockchainTime:" + callBlockchainTime);
                            BlockchainUpdateBirthResponse blockchainResult = blockchainGatway.updateBirthRecord(birthModel,
                                    channelGovtHospital);
                            LocalDateTime afterCallBlockchainTime = LocalDateTime.now();
                            logger.info("updateBirthRecords afterCallBlockchainTime:" + afterCallBlockchainTime);
                            // logger.info("After data addition in Blockchain ==== " + now);
                            // JsonUtil.getObjectFromJson(blockchainResult, BlockchainGatway.class);

                            // Set Blockchain response
                            String message = blockchainResult.getMessage();
                            String txID = blockchainResult.getTxID();
                            String status = blockchainResult.getStatus();
                            if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(status)){
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
                            // Save Blockchain response in Model
                            birthModel = birthRepository.save(birthModel);
                            birthHistoryModel = birthHistoryRepository.save(birthHistoryModel);
                        } else {
                            
                            // logger.info("updateBirthRecords False response from blockchain " + blockchainResult);
                            throw new Exception(Constants.INTERNAL_SERVER_ERROR);
                        }

                    }
                }
            }
        //If already data exist

        }
        res.setMsg(Constants.BIRTH_UPDATE_SUCCESS_MESSAGE);
        res.setStatus(HttpStatus.OK);
        res.setData(responseList);
        LocalDateTime endMethodTime = LocalDateTime.now();
        logger.info("updateBirthRecords endMethodTime:" + endMethodTime);
        return res;
    }

    @Override
    public ApiResponse getBirthRecords(String status, String orgCode) {
        ApiResponse response = new ApiResponse();
        response.setData(birthRepository.getBirthDataByStatusAndOrganization(status, orgCode));
        response.setStatus(HttpStatus.OK);
        // return birthRepository.findAll();
        return response;
    }

    @Override
    public ApiResponse getBirthDetails(Long birthId, HttpServletRequest request) throws Exception {

        LocalDateTime startMethodTime = LocalDateTime.now();
        logger.info("getBirthDetails startMethodTime:" + startMethodTime);
        JwtUtil jwtUtil = new JwtUtil();
        String userName = authService.getUserIdFromRequest(request);
        Optional<UserModel> userModelOp = authRepository.findById(Long.parseLong(userName));
        UserModel userModel = userModelOp.get();

        String orgId = userModel.getOrganizationId();
        Optional<OrganizationModel> organizationModelOp = organizationRepository.findById(Long.parseLong(orgId));
        OrganizationModel organizationModel = organizationModelOp.get();
        String orgType = organizationModel.getOrganizationType();
        String orgCode = organizationModel.getOrganisationCode();


        logger.info("birthId ===: " + birthId.toString());
        ApiResponse res = new ApiResponse();
        LocalDateTime d1 = LocalDateTime.now();
        // logger.info("Before birthRepository Findbyid DB call ===== " + d1);
        Optional<BirthModel> existedData = birthRepository.findById(birthId);
        LocalDateTime d2 = LocalDateTime.now();
        // logger.info("After birthRepository Findbyid DB call ===== " + d2);
        boolean roleAdminOrCFC = userModel.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_ADMIN)|| r.getRoleName().equals(Constants.ROLE_CFC_APPROVER)|| r.getRoleName().equals(Constants.ROLE_CFC_CREATOR)|| r.getRoleName().equals(Constants.ROLE_CFC_REGISTRAR));

        if (!existedData.isPresent()
                || (!roleAdminOrCFC
                        && !existedData.get().getOrganizationCode().equals(orgCode))) {
            // logger.info("Record not found for birthId:" + birthId);
            res.setMsg(Constants.RECORD_NOT_FOUND);
            res.setStatus(HttpStatus.BAD_REQUEST);
        } else {
            res.setStatus(HttpStatus.OK);
            // res.setData(existedData);
            logger.info("get details of BirthId ===: " + birthId.toString());
            BirthModel blcModel = null;
            // try {
                LocalDateTime bd1 = LocalDateTime.now();
                // logger.info("Before getBirthRecord Blockchain call ===== " + bd1);
                String blcResponse = blockchainGatway.getBirthRecord(birthId, channelGovtHospital);

                LocalDateTime bd2 = LocalDateTime.now();
                // logger.info("After getBirthRecord Blockchain call ===== " + bd2);

                // logger.info("====Birth details response ===" + blcResponse);
                blcModel = JsonUtil.getObjectFromJson(blcResponse, BirthModel.class);
            // } catch (Exception e) {
            //     e.printStackTrace();
            // }
            if (blcModel == null) {
                //blcModel = existedData.get();
                // logger.info("getBirthDetails null blcModel from blockchain for birthId:" + birthId );
                throw new Exception(Constants.INTERNAL_SERVER_ERROR);
            }
            res.setData(blcModel);
        }
        LocalDateTime endMethodTime = LocalDateTime.now();
        logger.info("getBirthDetails endMethodTime:" + endMethodTime);
        return res;
    }

    @Override
    public ApiResponse getAllBirthRecords() {
        ApiResponse apiResponse = new ApiResponse();
        List<BirthModel> list = birthRepository.findAll();
        apiResponse.setStatus(HttpStatus.OK);
        apiResponse.setData(list);
        return apiResponse;
    }

    @Override
    @Transactional
    public ApiResponse updateBirthRecordStatus(Long birthId, String status, String remarks,
            HttpServletRequest request) throws Exception {
        LocalDateTime startMethodTime = LocalDateTime.now();
        UserModel userModel = new UserModel();
        logger.info("updateBirthRecordStatus startMethodTime:" + startMethodTime);
        JwtUtil jwtUtil = new JwtUtil();
        ApiResponse res = new ApiResponse();
        LocalDateTime d1 = LocalDateTime.now();
        // logger.info("Before birthRepository Findbyid DB call ===== " + d1);
        Optional<BirthModel> existedData = birthRepository.findById(birthId);
        LocalDateTime d2 = LocalDateTime.now();
        // logger.info("After birthRepository Findbyid DB call ===== " + d2);
        // boolean existedData = birthRepository.existsById(birthId);

        if (existedData.equals(Optional.empty())) {
            res.setMsg(Constants.RECORD_NOT_FOUND);
            res.setStatus(HttpStatus.BAD_REQUEST);
        }
        else {
            BirthModel birthModel = existedData.get();
            logger.info("status ----- " + birthModel.getStatus());
            String applicationNumber = birthModel.getApplicationNumber();
            String userName = authService.getUserIdFromRequest(request);

            Optional<UserModel> currentUserOp = authRepository.findById(Long.parseLong(userName));
            UserModel currentUser = currentUserOp.get();
            String orgId = currentUser.getOrganizationId();
            Optional<OrganizationModel> organizationModelOp = organizationRepository.findById(Long.parseLong(orgId));
            OrganizationModel organizationModel = organizationModelOp.get();
            String orgType = organizationModel.getOrganizationType();
            String orgCode = organizationModel.getOrganisationCode();

            String organizationCode = orgCode;
            LocalDateTime now = LocalDateTime.now();
          // Set Registration Date time during approve and reject
           // birthModel.setRegistrationDatetime(birthModel.getRegistrationDatetime());
            logger.info("CURRENT USER ORG CODE  ====" + organizationCode + ":==== DATA ORG CODE==="
                    + birthModel.getOrganizationCode());
            if (!birthModel.getStatus().equalsIgnoreCase(Constants.RECORD_STATUS_PENDING)) {
                res.setMsg(Constants.BIRTH_STATUS_NOT_PENDING);
                res.setStatus(HttpStatus.BAD_REQUEST);
            } else if (!birthModel.getOrganizationCode().equalsIgnoreCase(organizationCode)
                    && !Constants.ORG_NDMC.equalsIgnoreCase(organizationCode)) {
                res.setStatus(HttpStatus.BAD_REQUEST);
                res.setMsg(Constants.ORG_NOT_SAME_MESSAGE);
            }

            else {

                if (Constants.RECORD_STATUS_APPROVED.equalsIgnoreCase(status)) {
                    if (approveUpdateStatus(birthModel, userName, now)) {
                        res.setStatus(HttpStatus.OK);
                        res.setMsg(Constants.RECORD_APPROVED_MESSAGE);
                        res.setData(birthModel.getApplicationNumber());
                    }

                } else if (Constants.RECORD_STATUS_REJECTED.equalsIgnoreCase(status)) {
                    birthModel.setRejectedBy(userName);
                    birthModel.setRejectedAt(now);
                    birthModel.setStatus(Constants.RECORD_STATUS_REJECTED);
                    birthModel.setRejectionRemark(remarks);
                    birthModel.setTransactionType(Constants.RECORD_REJECTED);
//                    birthModel.setEventPlace(birthModel.getEventPlace());
//                    birthModel.setEventPlaceFlag(birthModel.getEventPlaceFlag());
//                    logger.info("BIRTH MODEL AFTER RJECT RECORD " + birthModel);
                    birthModel = birthRepository.save(birthModel);

                    logger.info("Birthmodel response after rejection : " + birthModel);
                    // int sqlResponse = birthRepository.rejectBirthStatusByBirthId(birthId,
                    // Constants.RECORD_STATUS_REJECTED, birthModel.getRejectedBy(),
                    // birthModel.getRejectedAt());


                    if (birthModel != null) {

                        // Add birth history
                        BirthHistoryModel birthHistoryModel = new BirthHistoryModel();
                        birthHistoryModel.setTransactionType(Constants.RECORD_REJECTED);
                        BeanUtils.copyProperties(birthModel, birthHistoryModel);
                        birthHistoryModel = birthHistoryRepository.save(birthHistoryModel);

                        res.setStatus(HttpStatus.OK);
                        res.setMsg(Constants.RECORD_REJECTED_MESSAGE);
                        res.setData(birthModel.getApplicationNumber());
                        logger.info("Birth Rejection Payload  birthId===" + birthId.toString() + "=== Rejected By "
                                + userName + "=== Rejected at " + CommonUtil.convertDateTimeFormat(now));
                        // Need to implement try catch to handle exception in blockchain method



                       // logger.info("  === birth rejection response ====="+birthModel);
                      //  commonUtil.sendTextMessage(birthModel.getMotherName(), birthModel.getContactNumber(), birthModel.getApplicationNumber(), Constants.RECORD_TYPE_BIRTH, Constants.REQUEST_REJECTED);

                      //   try {
                            LocalDateTime bd1 = LocalDateTime.now();
                            // logger.info("Before updateBirthRecordStatus Blockchain call rejectBirthRecord ===== " + bd1);
                            BlockchainRejectBirthResponse blockchainResult = blockchainGatway.rejectBirthRecord(
                                    birthId.toString(),
                                    userName, CommonUtil.convertDateTimeFormat(now), remarks, channelGovtHospital);
                                    // logger.info("After data addition in Blockchain ==== " + now);
                            LocalDateTime bd2 = LocalDateTime.now();
                            // logger.info("After updateBirthRecordStatus Blockchain call rejectBirthRecord  ===== " + bd2);
                            // JsonUtil.getObjectFromJson(blockchainResult, BlockchainGatway.class);

                            // Set Blockchain response
                            String message = blockchainResult.getMessage();
                            String txID = blockchainResult.getTxID();
                            String blcStatus = blockchainResult.getStatus();
                        logger.info("============== BLOCKCHAIN RESPONSE STATUS rejectBirthRecord======"+blcStatus);
                            if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(blcStatus)) {
                                birthModel.setBlcMessage(message);
                                birthModel.setBlcTxId(txID);
                                birthModel.setBlcStatus(blcStatus);

                                birthHistoryModel.setBlcMessage(message);
                                birthHistoryModel.setBlcTxId(txID);
                                birthHistoryModel.setBlcStatus(blcStatus);


                                 birthModel = birthRepository.save(birthModel);
                                 birthHistoryModel = birthHistoryRepository.save(birthHistoryModel);

                                /* Code to send rejection text message to parents
                                 * Code by Deepak
                                 * 29-04-2022
                                 * */
                                if(birthModel != null && birthModel.getApplicationNumber() !=null
                                        && !CommonUtil.checkNullOrBlank(birthModel.getContactNumber())){
                                    CommonUtil commonUtil = new CommonUtil();

                                    logger.info("  === birth rejection response ====="+birthModel);
                                    try{
                                    commonUtil.sendTextMessage(birthModel.getMotherName(), birthModel.getContactNumber(), birthModel.getApplicationNumber(), Constants.RECORD_TYPE_BIRTH, Constants.REQUEST_REJECTED,"", "", "", "", "");
                                    }catch (Exception e){
                                        logger.info("===SMS EXCEPTION =="+e);
                                    }
                                    }
                        }
                            else {
                            
                            // logger.info("updateBirthRecordStatus False response from blockchain " + blockchainResult);
                            throw new Exception(Constants.INTERNAL_SERVER_ERROR);
                        }
                  //  } catch (Exception e) {
                        //     logger.error("===Blc Exception ===", e);

                  //  }
                    } else {
                        res.setStatus(HttpStatus.BAD_REQUEST);
                        res.setMsg(Constants.NOT_PERMITTED);
                    }

                }
                res.setMsg(Constants.BIRTH_UPDATE_SUCCESS_MESSAGE);
                res.setStatus(HttpStatus.OK);
                res.setData(birthModel.getApplicationNumber());
            }
        }
        LocalDateTime bd3 = LocalDateTime.now();
        // logger.info("End updateBirthRecordStatus  ===== " + bd3);
        return res;
    }

    private boolean approveUpdateStatus(BirthModel birthModel, String userName, LocalDateTime now) throws Exception {
        LocalDateTime startMethodTime = LocalDateTime.now();
        UserModel userModel = new UserModel();
        logger.info("Start method approveUpdateStatus at " + startMethodTime);
        birthModel.setApprovedBy(userName);
        birthModel.setApprovedAt(now);
        birthModel.setStatus(Constants.RECORD_STATUS_APPROVED);
        birthModel.setTransactionType(Constants.RECORD_APPROVED);
//        birthModel.setEventPlace(birthModel.getEventPlace());
//        birthModel.setEventPlaceFlag(birthModel.getEventPlaceFlag());
//        logger.info("BIRTH MODEL AFTER APPROVE RECORD " + birthModel);
        birthModel = birthRepository.save(birthModel);
        // int sqlResponse = birthRepository.approveBirthStatusByBirthId(birthId,
        // Constants.RECORD_STATUS_APPROVED, birthModel.getApprovedBy(),
        // birthModel.getApprovedAt());

        if (birthModel != null) {

            logger.info("=====Contact number is ======"+birthModel.getContactNumber());

            // Add birth history
            BirthHistoryModel birthHistoryModel = new BirthHistoryModel();
            birthHistoryModel.setTransactionType(Constants.RECORD_APPROVED);
            BeanUtils.copyProperties(birthModel, birthHistoryModel);
            birthHistoryModel = birthHistoryRepository.save(birthHistoryModel);

            logger.info("Birth Approval Payload  birthId===" + birthModel.getBirthId().toString() + "=== Approved By "
                    + userName + "=== Rejected at " + now.toString());

           //  try {
                LocalDateTime bd1 = LocalDateTime.now();
                // logger.info("Before updateBirthRecordStatus Blockchain call approveBirthRecord ===== " + bd1);
                BlockchainApproveBirthResponse blockchainResult = blockchainGatway.approveBirthRecord(
                        birthModel.getBirthId().toString(),
                        userName, CommonUtil.convertDateTimeFormat(now), channelGovtHospital);
                // logger.info("After data addition in Blockchain ==== " + now);
                LocalDateTime bd2 = LocalDateTime.now();
                // logger.info("After updateBirthRecordStatus Blockchain call approveBirthRecord  ===== " + bd2);
                // JsonUtil.getObjectFromJson(blockchainResult, BlockchainGatway.class);

                // Set Blockchain response
                String message = blockchainResult.getMessage();
                String txID = blockchainResult.getTxID();
                String blcStatus = blockchainResult.getStatus();
                logger.info("============== BLOCKCHAIN RESPONSE STATUS approveBirthRecord======"+blcStatus);
                if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(blcStatus)) {
                    birthModel.setBlcMessage(message);
                    birthModel.setBlcTxId(txID);
                    birthModel.setBlcStatus(blcStatus);

                    birthHistoryModel.setBlcMessage(message);
                    birthHistoryModel.setBlcTxId(txID);
                    birthHistoryModel.setBlcStatus(blcStatus);
                   birthModel = birthRepository.save(birthModel);

                    /* Code to send Approval text message to parents
                     * Code by Deepak
                     * 29-04-2022
                     * */
                    if(birthModel != null && birthModel.getApplicationNumber() !=null
                            && !CommonUtil.checkNullOrBlank(birthModel.getContactNumber())){
                        CommonUtil commonUtil = new CommonUtil();

                        logger.info("  === birth addition response ====="+birthModel);
                        try{
                        commonUtil.sendTextMessage(birthModel.getMotherName(), birthModel.getContactNumber(), birthModel.getApplicationNumber(), Constants.RECORD_TYPE_BIRTH, Constants.REQUEST_APPROVED, "", "","", "", "");
                        }catch (Exception e){
                            logger.info("===SMS EXCEPTION =="+e);
                        }
                        }
                birthHistoryModel = birthHistoryRepository.save(birthHistoryModel);
            }

                else {

                // logger.info("approveUpdateStatus False response from blockchain " + blockchainResult);
                throw new Exception(Constants.INTERNAL_SERVER_ERROR);
            }
           // } catch (Exception e) {
            //     logger.error("===Blc Exception ===", e);

           // }
            LocalDateTime bd3 = LocalDateTime.now();
            // logger.info("End approveUpdateStatus  ===== " + bd3);
            return true;
        }
        LocalDateTime bd4DateTime = LocalDateTime.now();
        // logger.info("End approveUpdateStatus  ===== " + bd4DateTime);
        return false;
    }

    @Override
    public ApiResponse getUsersBirthRecords(HttpServletRequest request) throws Exception {
        // check user role
        // if creater or approvar fetch all pending of his org and his draft record
        // org = xyz and (status = pending or ( status = draft and user=abc))
        // if admin fetch pending of all orgs and his drafs records
        ApiResponse response = new ApiResponse();
        String userName = authService.getUserIdFromRequest(request);
        logger.info("");
        Optional<UserModel> currentUserOp = authRepository.findById(Long.parseLong(userName));
        UserModel currentUser = currentUserOp.get();
        String orgId = currentUser.getOrganizationId();
        Optional<OrganizationModel> organizationModelOp = organizationRepository.findById(Long.parseLong(orgId));
        OrganizationModel organizationModel = organizationModelOp.get();
        String orgType = organizationModel.getOrganizationType();
        String orgCode = organizationModel.getOrganisationCode();

        String organizationCode = orgCode;
        Long userId = currentUser.getUserId();

        // response.setData(currentUser);
        // response.setStatus(HttpStatus.OK);

        if (currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CREATOR))
                || currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_CREATOR))) {
            logger.info("Current USER IS CREATOR ===== " + currentUser);

            // First time Data will be fetched which is PENDING
//            List<BirthModel> data = birthRepository.getCreatorRecords(userId, organizationCode,
//                    Constants.RECORD_STATUS_PENDING, Constants.RECORD_STATUS_REJECTED,
//                    Constants.RECORD_STATUS_DRAFT);

            List<BirthModel> data = birthRepository.getApproverRecords(organizationCode,
                    Constants.RECORD_STATUS_PENDING);
            // logger.info("RESPONSE FROM getCreatorRecords ===== " + data);
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
            logger.info("Current USER IS Approver ===== " + currentUser);
            LocalDateTime d1 = LocalDateTime.now();
            // logger.info("Before getCreatorRecords method DB call ===== " + d1);
            List<BirthModel> data = birthRepository.getApproverRecords(organizationCode,
                    Constants.RECORD_STATUS_PENDING);
            // logger.info("RESPONSE FROM getApproverRecords ===== " + data);
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
            logger.info("Current USER IS Approver ===== " + currentUser);
            List<BirthModel> data = birthRepository.getAdminRecords(userId, Constants.RECORD_STATUS_PENDING,
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
        logger.info("Current USER  ===== " + currentUser.getRoles().stream());
        return response;
    }

    /*
     * This method return Number of days and hours for approval
     * In case of Pending status
     */
    private List<BirthModel> recordApprovalTime(List<BirthModel> data) throws Exception {

        if (data != null) {

            for (BirthModel bModel : data) {

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
        logger.info("*** CRON START*****");
        //String userName = "Cron";
        //LocalDateTime now = LocalDateTime.now();
        List<BirthModel> birthModel = birthRepository.getByStatusWithHour(Constants.RECORD_STATUS_PENDING,
                autoApproveHour);

        for (BirthModel bm : birthModel) {
            cronApproveUpdateStatus(bm);

        }
        logger.info("*** CRON END*****");
    }

    @Transactional
    private void cronApproveUpdateStatus(BirthModel bm) throws Exception {
        logger.info("updating for : " + bm.getBirthId());
        approveUpdateStatus(bm, Constants.CRON, LocalDateTime.now());
        logger.info("updation completed : " + bm.getBirthId());
    }

    @Override
    public ApiResponse getFilteredData(CFCFilterDto filterDto, String filterType, HttpServletRequest request) throws Exception {
        ApiResponse response = new ApiResponse();
        JwtUtil jwtUtil = new JwtUtil();
        logger.info("==== CFCFILTERDTO ====="+filterDto);
        String userName = authService.getUserIdFromRequest(request);
        // String userName = jwtUtil.getUsernameFromRequest(request);

        List<BirthModel> birthRecords = findAll(filterDto, filterType, userName);

        // logger.info("==== BirthRecords from findAll method ====="+birthRecords);

        if (Constants.FILTER_NAME_REJECTION.equals(filterType)
                || Constants.FILTER_LEGAL_CORRECTION_REJECTION.equals(filterType)) {
            if (birthRecords == null) {
                birthRecords = new ArrayList<>();
            }
        }

        updateViewDocuments(birthRecords, filterType);
        response.setStatus(HttpStatus.OK);
        response.setData(birthRecords);
        return response;
    }

    private void updateViewDocuments(List<BirthModel> birthRecords, String filterType) {
        Map<Long, SlaDetailsModel> slaDetailsMap = new HashMap<>();
        if(Constants.FILTER_NAME_INCLUSION.equals(filterType) || Constants.FILTER_NAME_APPROVAL.equals(filterType) || 
        Constants.FILTER_NAME_REJECTION.equals(filterType)) {
            //slaDetailsMap = slaDetailsService.findAllPendingMap(Constants.RECORD_TYPE_BIRTH, Constants.RECORD_STATUS_PENDING, Constants.RECORD_STATUS_UPLOADED, Constants.RECORD_STATUS_REJECTED, Constants.NAME_INCLUSION);
            slaDetailsMap = slaDetailsService.findAllPendingMap(Constants.RECORD_TYPE_BIRTH, Constants.RECORD_NAME_INCLUSION);
        
        } else if(Constants.FILTER_LEGAL_CORRECTIONS.equals(filterType) || Constants.FILTER_LEGAL_CORRECTIONS_APPROVAL.equals(filterType) || 
        Constants.FILTER_LEGAL_CORRECTION_REJECTION.equals(filterType)) {
            //slaDetailsMap = slaDetailsService.findAllPendingMap(Constants.RECORD_TYPE_BIRTH, Constants.RECORD_STATUS_PENDING, Constants.RECORD_STATUS_UPLOADED, Constants.RECORD_STATUS_REJECTED, Constants.LEGAL_CORRECTIONS);
            slaDetailsMap = slaDetailsService.findAllPendingMap(Constants.RECORD_TYPE_BIRTH, Constants.LEGAL_CORRECTIONS);
        }

        for (BirthModel birthModel : birthRecords) {
                birthModel.setViewDocuments(slaDetailsMap.containsKey(birthModel.getBirthId())); 
        }

    }

    private List<BirthModel> getRejectedRecords(CFCFilterDto filterDto, String filterType,
            UserModel currentUser) {
        // fetch rejected records from SAL table, on that basis fetch from birth table
        return new ArrayList<BirthModel>();
    }

    private List<BirthModel> findAll(CFCFilterDto filterDto1, String filterType, String userName){
        Long organizationId = authService.getOrganizationIdFromUserId(userName);
        return birthRepository.findAll(new Specification<BirthModel>() {
            @Override
            public Predicate toPredicate(Root<BirthModel> root, CriteriaQuery<?> query,
                    CriteriaBuilder criteriaBuilder) {
                try{
                List<Predicate> predicates = new ArrayList<>();
                // logger.info("filterDto1:" + filterDto1);
                CFCFilterDto filterDto = filterDto1;
                boolean isJoinCorrection = false;
                boolean isJoinInclusion = false;
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

                // Search with Event Place flag

                if (!CommonUtil.checkNullOrBlank(filterDto.getEventPlaceFlag())) {
                    // eventPlace
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.equal(root.get(Constants.EVENT_PLACE_FLAG), filterDto.getEventPlaceFlag())));
                }

                if (!CommonUtil.checkNullOrBlank(filterDto.getEventPlace())) {
                    // eventPlace
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.equal(root.get(Constants.EVENT_PLACE), filterDto.getEventPlace())));
                }

                //Division Code

                if (!CommonUtil.checkNullOrBlank(filterDto.getDivisionCode())) {
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
                Predicate predicateApproved = criteriaBuilder.equal(root.get(Constants.STATUS), Constants.RECORD_STATUS_APPROVED);
                
                Predicate approvedPredicate = criteriaBuilder.and(
                    criteriaBuilder.equal(root.get(Constants.STATUS), Constants.RECORD_STATUS_APPROVED));
                    if(!CommonUtil.checkNullOrBlank(filterDto.getStatus())) {

                        String filterStatus = filterDto.getStatus();

                       if (filterType.equalsIgnoreCase(Constants.FILTER_INCLUSION_SEARCH)) {
                           if(!"".equalsIgnoreCase(filterStatus)) {
                               /*  Join<Object, Object> joinParent = root.join(Constants.INCLUSION_SLA_ID, JoinType.INNER);
                               javax.persistence.criteria.Path expression = joinParent.get(Constants.SLA_ORGID);
                               CommonUtil.addPredicate(predicates, criteriaBuilder, expression, organizationId);
                          */
                               predicates.add(root.get(Constants.INCLUSION_SLA_ID).isNotNull());
                               isJoinInclusion =true;
                           }

                            if (Constants.RECORD_STATUS_PENDING.equalsIgnoreCase(filterStatus)) {
                                predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.STATUS),
                                        Constants.RECORD_STATUS_INCLUSION_PENDING)));
                            } else if (Constants.RECORD_STATUS_REJECTED.equalsIgnoreCase(filterStatus)) {
                                predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.STATUS),
                                        Constants.RECORD_STATUS_INCLUSION_REJECTED)));
                            }
                            else if(Constants.RECORD_STATUS_APPROVED.equalsIgnoreCase(filterStatus)){
                                List<Long> birthIds = slaDetailsService.recordIdsByStatus(Constants.RECORD_TYPE_BIRTH, filterType,
                                        filterDto.getStatus());
                                if(!birthIds.isEmpty()) {
                                    predicates.add(criteriaBuilder.and(
                                            root.get(Constants.BIRTH_ID).in(birthIds)));
                                    predicates.add(approvedPredicate);
                                }
                            }
                        }
                       else if (filterType.equalsIgnoreCase(Constants.FILTER_LEGAL_CORRECTION_SEARCH)) {
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
                                Predicate predicateNameNull = criteriaBuilder.isTrue(root.get(Constants.CHILD_NAME).isNotNull());
                                Predicate predicateNameEmpty = criteriaBuilder.notEqual(root.get(Constants.CHILD_NAME), "");
                                Predicate predicateName = criteriaBuilder.and(predicateNameNull, predicateNameEmpty);
                                Predicate approvePredicate = criteriaBuilder.and(approvedPredicate, predicateName);
                                predicates.add(approvePredicate);
                            }
                        }
                       // For approved


                        // ===== Closed approved predicates ======
                    } else if (!CommonUtil.checkNullOrBlank(filterType)) {
                    if (filterType.equalsIgnoreCase(Constants.FILTER_NAME_INCLUSION)) {
                        Predicate rejectedPredicate = criteriaBuilder.and(
                            criteriaBuilder.equal(root.get(Constants.STATUS), Constants.RECORD_STATUS_INCLUSION_REJECTED));    

                        Predicate predicateSlaIdNotNull = criteriaBuilder.isTrue(root.get(Constants.INCLUSION_SLA_ID).isNotNull());
                        Predicate approvePredicate = criteriaBuilder.and(approvedPredicate, predicateSlaIdNotNull);
                        Predicate predicateInclusionRejected = criteriaBuilder.and(rejectedPredicate);
                        predicates.add(criteriaBuilder.or(approvePredicate, predicateInclusionRejected));
                    } else if (filterType.equalsIgnoreCase(Constants.FILTER_NAME_APPROVAL)) {
                        predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.STATUS),
                                Constants.RECORD_STATUS_INCLUSION_PENDING)));
                    } else if (filterType.equalsIgnoreCase(Constants.FILTER_LEGAL_CORRECTIONS)) {

                        Predicate rejectedPredicate = criteriaBuilder.and(
                            criteriaBuilder.equal(root.get(Constants.STATUS), Constants.RECORD_STATUS_CORRECTION_REJECTED));
                            Predicate predicateNameNull = criteriaBuilder.isTrue(root.get(Constants.CHILD_NAME).isNotNull());
                            Predicate predicateNameEmpty = criteriaBuilder.notEqual(root.get(Constants.CHILD_NAME), "");
                            Predicate predicateName = criteriaBuilder.and(predicateNameNull, predicateNameEmpty); 
                            Predicate approvePredicate = criteriaBuilder.and(approvedPredicate, predicateName);
                            //Predicate userPredicate = criteriaBuilder.equal(root.get(Constants.USER_NAME), userName);
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
                    } else{
                        throw new IllegalArgumentException("Invalid Request");
                    }
                   if (filterType.equalsIgnoreCase(Constants.FILTER_LEGAL_CORRECTIONS_APPROVAL)
                        || filterType.equalsIgnoreCase(Constants.FILTER_LEGAL_CORRECTION_REJECTION)
                        || filterType.equalsIgnoreCase(Constants.FILTER_LEGAL_CORRECTIONS)) {

                       /*   Join<Object, Object> joinParent = root.join(Constants.CORRECTION_SLA_ID, JoinType.INNER);
                        javax.persistence.criteria.Path expression = joinParent.get(Constants.SLA_ORGID);
                        CommonUtil.addPredicate(predicates, criteriaBuilder, expression, organizationId);
                   */
                       predicates.add(root.get(Constants.CORRECTION_SLA_ID).isNotNull());
                       isJoinCorrection=true;
                    }
                   if (filterType.equalsIgnoreCase(Constants.FILTER_NAME_REJECTION)
                            || filterType.equalsIgnoreCase(Constants.FILTER_NAME_APPROVAL)) {
                       /*  Join<Object, Object> joinParent = root.join(Constants.INCLUSION_SLA_ID, JoinType.INNER);
                            javax.persistence.criteria.Path expression = joinParent.get(Constants.SLA_ORGID);
                            CommonUtil.addPredicate(predicates, criteriaBuilder, expression, organizationId);
                    */
                       predicates.add(root.get(Constants.INCLUSION_SLA_ID).isNotNull());
                       isJoinInclusion =true;
                   }
                    Optional<UserModel> currentUserOp = authRepository.findById(Long.parseLong(userName));
                    UserModel currentUser = currentUserOp.get();

                   if(isJoinInclusion){
                     if(!currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_REGISTRAR)))  {
                           List<Long> slaDetailsModel= slaDetailsService.findSlaOrganizationId(organizationId);
                           if(!slaDetailsModel.isEmpty()) {
                               predicates.add(criteriaBuilder.and(
                                       root.get(Constants.INCLUSION_SLA_ID).in(slaDetailsModel)));
                               predicates.add(approvedPredicate);
                           }
                       }

                   }
                   if(isJoinCorrection){
                       if(!currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_REGISTRAR)))
                       {
                           List<Long> slaDetailsModel = slaDetailsService.findSlaOrganizationId(organizationId);
                           if (!slaDetailsModel.isEmpty()) {
                               predicates.add(criteriaBuilder.and(
                                       root.get(Constants.CORRECTION_SLA_ID).in(slaDetailsModel)));
                               predicates.add(approvedPredicate);
                           }
                       }
                   }


                }
                query.orderBy(criteriaBuilder.desc(root.get("registrationDatetime")));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }catch(Exception e) {
                logger.error("Exception {}... ", e);
                throw e;
             }
            }
        });
    }

    @Override
    @Transactional
    public void updateInclusionStatus(SlaDetailsModel slaDetailsModel, String userName) {
        BirthModel birthModel = birthRepository.findById(slaDetailsModel.getBndId()).get();  
        birthModel.setStatus(CommonUtil.getInclusionStatus(slaDetailsModel.getStatus()));
        birthModel.setTransactionType(Constants.NAME_INCLUSION);

        //Added by Deepak at 26th Jan 2022
       // birthModel.setName(slaDetailsModel.getChildName());

        birthModel.setModifiedAt(LocalDateTime.now());
        birthModel.setModifiedBy(userName);
        // birthModel.setName(slaDetailsModel.getChildName());
        birthModel.setInclusionSlaId(slaDetailsModel.getSlaDetailsId());
        birthRepository.save(birthModel);
        BirthHistoryModel birthHistoryModel = new BirthHistoryModel();
        BeanUtils.copyProperties(birthModel, birthHistoryModel);
        birthHistoryModel = birthHistoryRepository.save(birthHistoryModel);
        birthHistoryRepository.save(birthHistoryModel);
        
    }

    @Override
    @Transactional
    public void updateCorrectionStatus(SlaDetailsModel slaDetailsModel, String userName) {
        BirthModel birthModel = birthRepository.findById(slaDetailsModel.getBndId()).get();  
        birthModel.setStatus(CommonUtil.getCorrectionStatus(slaDetailsModel.getStatus()));

        //Set Sladetails model into Birth model
        if(Constants.RECORD_STATUS_APPROVED.equalsIgnoreCase(birthModel.getStatus())) {
            CustomBeanUtils.copySlaDetails(slaDetailsModel, birthModel);
        }

        birthModel.setTransactionType(Constants.LEGAL_CORRECTIONS);
        birthModel.setModifiedAt(LocalDateTime.now());
        birthModel.setModifiedBy(userName);
        birthModel.setCorrectionSlaId(slaDetailsModel.getSlaDetailsId());
        //Added by Deepak on 26th jan 2022
       // birthModel.setName(slaDetailsModel.getChildName());

        //
        birthModel.setEventPlaceFlag(slaDetailsModel.getEventPlaceFlag());
        birthModel.setEventPlace(slaDetailsModel.getEventPlace());

        birthRepository.save(birthModel);
        BirthHistoryModel birthHistoryModel = new BirthHistoryModel();
        BeanUtils.copyProperties(birthModel, birthHistoryModel);
        birthHistoryModel = birthHistoryRepository.save(birthHistoryModel);
        birthHistoryRepository.save(birthHistoryModel);
    }

    @Override
    public BirthModel findById(Long bndId) {
        return birthRepository.findById(bndId).get();
    }

    @Override
    public List<BirthHistoryModel> getBirthListForReport(ReportSearchDto reportSearchDto, String type)  throws DateRangeException{

            return birthHistoryRepository.findAll(new Specification<BirthHistoryModel>() {
                @Override
                public Predicate toPredicate(Root<BirthHistoryModel> root, CriteriaQuery<?> query,
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

                //    logger.info("=== division code "+reportSearchDto.getDivisionCode()+"-"+CommonUtil.checkNullOrBlank(reportSearchDto.getDivisionCode()));
                    if (!CommonUtil.checkNullOrBlank(reportSearchDto.getDivisionCode())) {
                        predicates.add(criteriaBuilder
                                .and(criteriaBuilder.equal(root.get(Constants.DIVISION_CODE), reportSearchDto.getDivisionCode())));
                    }
                        // FILTER BY Registeration date
                    if (!CommonUtil.checkNullOrBlank(reportSearchDto.getRegStartDate()+"") && !CommonUtil.checkNullOrBlank(reportSearchDto.getRegEndDate()+"")) {
                        // registrationDatetime
                        CommonUtil.betweenDates(reportSearchDto.getRegStartDate().atTime(00,00,00),reportSearchDto.getRegEndDate().atTime(23,59,59));
                        predicates
                                .add(criteriaBuilder.and(criteriaBuilder.between(root.get(Constants.REGISTRATION_DATE_TIME),
                                        reportSearchDto.getRegStartDate().atTime(00,00,00),reportSearchDto.getRegEndDate().atTime(23,59,59))));
                    }

                    // FILTER BY event date
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
    public Resource loadSdmLetterFileById(String birthId) {

        ApiResponse res = new ApiResponse();
        Optional<BirthModel> birthModelOp = birthRepository.findById(Long.parseLong(birthId));
        BirthModel birthRecord = birthModelOp.get();
        // logger.info("==== Birth Record is "+birthRecord);
//        if( birthRecord.getSdmLetterImage() != null && !birthRecord.getSdmLetterImage().isEmpty()){
            try {
                Path filePath = this.fileStorageLocation.resolve(birthRecord.getSdmLetterImage()).normalize();
                Resource resource = new UrlResource(filePath.toUri());

                logger.info("=== Resource======"+resource);
                if (resource.exists()) {
                    return resource;
                } else {
                    throw new DocumentNotFoundException("File not found " + birthRecord.getSdmLetterImage());
                }
            } catch (Exception ex) {
                throw new DocumentNotFoundException("File not found " + birthRecord.getSdmLetterImage());
            }
        }

    @Override
    @Transactional
    public ApiResponse deleteBirth(Long birthId, HttpServletRequest request) {
        logger.info("Before deleteBirth ====== "+birthId);

        ApiResponse res = new ApiResponse();
        Optional<BirthModel> birthModelDtl = birthRepository.findById(birthId);
        String userId = authService.getUserIdFromRequest(request);
        Optional<UserModel> currentUserOp = authRepository.findById(Long.parseLong(userId));
        UserModel currentUser = currentUserOp.get();
        if(!birthModelDtl.isPresent()){
            res.setStatus(HttpStatus.BAD_REQUEST);
            res.setMsg(Constants.RECORD_WRONG_ERROR);
        }else{
            BirthModel birthModel = birthModelDtl.get();
            if(!Constants.RECORD_STATUS_DRAFT.equalsIgnoreCase(birthModel.getStatus())){
                res.setStatus(HttpStatus.BAD_REQUEST);
                res.setMsg(Constants.RECORD_STATUS_DRAFT_ERROR);
                return res;
            }
            if(!userId.equalsIgnoreCase(birthModel.getUserId())
                    && !currentUser.getRoles().stream().anyMatch(r -> r.getRoleName().equals(Constants.ROLE_CFC_REGISTRAR)) ){
                res.setStatus(HttpStatus.BAD_REQUEST);
                res.setMsg(Constants.RECORD_USER_ERROR);
                return res;
            }
            if(Constants.RECORD_Y.equalsIgnoreCase(birthModel.getIsDeleted())){
                res.setStatus(HttpStatus.BAD_REQUEST);
                res.setMsg(Constants.RECORD_ALREADY_DELETED_ERROR);
                return res;
            }
            birthModel.setModifiedAt(LocalDateTime.now());
            birthModel.setModifiedBy(userId);
            birthModel.setIsDeleted(Constants.RECORD_Y);
            birthModel.setTransactionType(Constants.RECORD_DELETE);
            birthRepository.save(birthModel);

            BirthHistoryModel birthHistoryModel = new BirthHistoryModel();
            birthHistoryModel.setTransactionType(Constants.RECORD_DELETE);
            BeanUtils.copyProperties(birthModel, birthHistoryModel);
            birthHistoryModel = birthHistoryRepository.save(birthHistoryModel);
            res.setMsg(Constants.BIRTH_DELETED_SUCCESS_MESSAGE);
            res.setData(birthModel.getRegistrationNumber());
            res.setStatus(HttpStatus.OK);
        }
        return res;
    }

    private void updateCitizenRecords(BirthDto birthDto, String userId) {
        CitizenBirthModel citizenBirthModel = new CitizenBirthModel();
        BeanUtils.copyProperties(birthDto, citizenBirthModel);
        citizenBirthModel.setModifiedAt(LocalDateTime.now());
        citizenBirthModel.setModifiedBy(userId);
        citizenBirthModel.setStatus(Constants.RECORD_SUBMITTED);
        citizenBirthRepository.save(citizenBirthModel);
    }

}
