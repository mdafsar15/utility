package com.ndmc.ndmc_record.serviceImpl;

import com.ndmc.ndmc_record.exception.NullPointerException;
import com.ndmc.ndmc_record.blockchainGatway.BlockchainGatway;
import com.ndmc.ndmc_record.config.Constants;
import com.ndmc.ndmc_record.dto.ApiResponse;
import com.ndmc.ndmc_record.model.*;
import com.ndmc.ndmc_record.repository.*;
import com.ndmc.ndmc_record.service.CertificatePrintService;
import com.ndmc.ndmc_record.service.SlaDetailsService;
import com.ndmc.ndmc_record.utils.JwtUtil;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CertificatePrintServiceImpl implements CertificatePrintService {

    private final Logger logger = LoggerFactory.getLogger(CertificatePrintServiceImpl.class);
    @Value("${CHANNEL_GOVT_HOSPITAL}")
    private String channelGovtHospital;
    @Autowired
    CertificatePrintRepository certificatePrintRepository;

    @Autowired
    CertificateHistoryRepository certificateHistoryRepository;

    @Autowired
    BirthHistoryRepository birthHistoryRepository;

    @Autowired
    SBirthHistoryRepository sBirthHistoryRepository;

    @Autowired
    DeathHistoryRepository deathHistoryRepository;

    @Autowired
    BirthRepository birthRepository;

    @Autowired
    DeathRepository deathRepository;

    @Autowired
    SBirthRepository sBirthRepository;

    @Autowired
    AuthRepository authRepository;
    @Autowired
    AuthServiceImpl authService;

    @Autowired
    OrganizationRepository organizationRepository;
    @Autowired
    BlockchainGatway blockchainGatway;

    @Autowired
    SlaDetailsService slaDetailsService;

    @Override
    @Transactional
    public ResponseEntity<?> printBirthCertificate(Long recordId, Long slaId, HttpServletRequest request) throws Exception {

        // logger.info("printBirthCertificate recordId : "+recordId);
        String username = authService.getUserIdFromRequest(request);
        Optional<UserModel> currentUserOp = authRepository.findById(Long.parseLong(username));
        UserModel currentUser = currentUserOp.get();
        CertificatePrintModel result = certificatePrintRepository.findLatestPrint(recordId, Constants.RECORD_TYPE_BIRTH);
        int printSeqNo = 1;
        if(result != null && result.getPrintSequenceNo() > 0) {
            printSeqNo = result.getPrintSequenceNo() + 1;
        }
        BirthHistoryModel birthHistoryModel = birthHistoryRepository.findLatestBirthHistory(recordId);
        Optional<BirthModel> birthModel = birthRepository.findById(recordId);

        CertificatePrintModel certificatePrintModel = new CertificatePrintModel();
        certificatePrintModel.setPrintedAt(LocalDateTime.now());
        certificatePrintModel.setPrintedBy(username);
        certificatePrintModel.setApplicationNumber(birthHistoryModel.getApplicationNumber());
        certificatePrintModel.setPrintSequenceNo(printSeqNo);

        certificatePrintModel.setRecordHistoryId(birthHistoryModel.getBirthHistoryId());
        certificatePrintModel.setPrintApplicationNumber(generatePrintApplicationNumber(birthHistoryModel.getRegistrationNumber(),
                birthHistoryModel.getApplicationNumber(), printSeqNo, currentUser));
        certificatePrintModel.setQrImage("");
        certificatePrintModel.setRecordId(birthHistoryModel.getBirthId());
        certificatePrintModel.setRecordType(Constants.RECORD_TYPE_BIRTH);


        certificatePrintModel = certificatePrintRepository.save(certificatePrintModel);
        BirthModel bModel = birthModel.get();
        if(certificatePrintModel != null) {
            bModel.setPrintedAt(LocalDateTime.now());
            bModel.setPrintedBy(username);
            bModel.setPrintId(certificatePrintModel.getPrintId().toString());
        }

        if(!Constants.YES_PRINTED.equalsIgnoreCase(bModel.getIsPrinted())
                || !Constants.YES_PRINTED.equalsIgnoreCase(birthHistoryModel.getIsPrinted())){
            bModel.setIsPrinted(Constants.YES_PRINTED);
            birthHistoryModel.setIsPrinted(Constants.YES_PRINTED);
        }

        BlockchainUpdateBirthResponse blockchainResult = blockchainGatway.updateBirthRecord(bModel,
                channelGovtHospital);
        // Set Blockchain response
        // logger.info("blockchainResult.getData() : "+blockchainResult.getData());
        certificatePrintModel.setData(blockchainResult.getData());
        String message = blockchainResult.getMessage();
        String txID = blockchainResult.getTxID();
        String statusBlk = blockchainResult.getStatus();
        if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(statusBlk)) {
            bModel.setBlcMessage(message);
            bModel.setBlcTxId(txID);
            bModel.setBlcStatus(statusBlk);
            certificatePrintModel.setBlcTransactionId(txID);

        } else {
            // logger.info("saveBirthCorrection False response from blockchain " + blockchainResult);
            throw new Exception(Constants.INTERNAL_SERVER_ERROR);
        }
        birthRepository.save(bModel);
        birthHistoryRepository.save(birthHistoryModel);
        certificatePrintRepository.save(certificatePrintModel);
        if(slaId != null) {
            ApiResponse apiResponse = slaDetailsService.updatePrintRequestBySlaId(slaId, request);
            if(apiResponse.getStatus() == HttpStatus.NOT_FOUND )
                throw new NullPointerException(Constants.RECORD_NOT_FOUND +" {slaId}  "+ slaId);
        }
        return new ResponseEntity<>(certificatePrintModel, HttpStatus.OK);
    }


    @Override
    @Transactional
    public ResponseEntity<?> printDeathCertificate(Long recordId,Long slaId, HttpServletRequest request) throws Exception{

        String username = authService.getUserIdFromRequest(request);
        Optional<UserModel> currentUserOp = authRepository.findById(Long.parseLong(username));
        UserModel currentUser = currentUserOp.get();
        CertificatePrintModel result = certificatePrintRepository.findLatestPrint(recordId, Constants.RECORD_TYPE_DEATH);
        int printSeqNo = 1;
        if(result != null && result.getPrintSequenceNo() > 0) {
            printSeqNo = result.getPrintSequenceNo() + 1;
        }
        DeathHistoryModel deathHistoryModel = deathHistoryRepository.findLatestDeathHistory(recordId);
        Optional<DeathModel> deathModel = deathRepository.findById(recordId);

        CertificatePrintModel certificatePrintModel = new CertificatePrintModel();
        certificatePrintModel.setPrintedAt(LocalDateTime.now());
        certificatePrintModel.setPrintedBy(username);
        certificatePrintModel.setApplicationNumber(deathHistoryModel.getApplicationNumber());
        certificatePrintModel.setPrintSequenceNo(printSeqNo);
        certificatePrintModel.setData(deathHistoryModel);
        certificatePrintModel.setRecordHistoryId(deathHistoryModel.getDeathHistoryId());
        certificatePrintModel.setPrintApplicationNumber(generatePrintApplicationNumber(deathHistoryModel.getRegistrationNumber(),
                deathHistoryModel.getApplicationNumber(), printSeqNo, currentUser));
        certificatePrintModel.setQrImage("");
        certificatePrintModel.setRecordId(deathHistoryModel.getDeathId());
        certificatePrintModel.setRecordType(Constants.RECORD_TYPE_DEATH);

        certificatePrintModel = certificatePrintRepository.save(certificatePrintModel);
        DeathModel dModel = deathModel.get();
        if(certificatePrintModel != null) {
            dModel.setPrintedAt(LocalDateTime.now());
            dModel.setPrintedBy(username);
            dModel.setPrintId(certificatePrintModel.getPrintId().toString());
        }


        if(!Constants.YES_PRINTED.equalsIgnoreCase(dModel.getIsPrinted()) || !Constants.YES_PRINTED.equalsIgnoreCase(deathHistoryModel.getIsPrinted())){
            dModel.setIsPrinted(Constants.YES_PRINTED);
            deathHistoryModel.setIsPrinted(Constants.YES_PRINTED);

        }

        BlockchainUpdateDeathResponse blockchainResult = blockchainGatway.updateDeathRecord(dModel, channelGovtHospital);
        // Set Blockchain response
        certificatePrintModel.setData(blockchainResult.getData());
        String message = blockchainResult.getMessage();
        String txID = blockchainResult.getTxID();
        String statusBlk = blockchainResult.getStatus();
        if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(statusBlk)) {
            dModel.setBlcMessage(message);
            dModel.setBlcTxId(txID);
            dModel.setBlcStatus(statusBlk);
            certificatePrintModel.setBlcTransactionId(txID);

        } else {
            // logger.info("saveBirthCorrection False response from blockchain " + blockchainResult);
            throw new Exception(Constants.INTERNAL_SERVER_ERROR);
        }
        deathRepository.save(dModel);
        logger.info("===deathHistoryModel=="+deathHistoryModel);
        deathHistoryModel = deathHistoryRepository.save(deathHistoryModel);
        logger.info("===deathHistoryModel response after saving=="+deathHistoryModel);
        certificatePrintRepository.save(certificatePrintModel);
        if(slaId != null) {
            ApiResponse apiResponse = slaDetailsService.updatePrintRequestBySlaId(slaId, request);
            if(apiResponse.getStatus() == HttpStatus.NOT_FOUND )
                throw new NullPointerException(Constants.RECORD_NOT_FOUND +" {slaId}  "+ slaId);
        }
        return new ResponseEntity<>(certificatePrintModel, HttpStatus.OK);

    }

    @Override
    @Transactional
    public ResponseEntity<?> printSBirthCertificate(Long recordId,Long slaId, HttpServletRequest request) throws Exception {
        String username = authService.getUserIdFromRequest(request);
        Optional<UserModel> currentUserOp = authRepository.findById(Long.parseLong(username));
        UserModel currentUser = currentUserOp.get();
        CertificatePrintModel result = certificatePrintRepository.findLatestPrint(recordId, Constants.RECORD_TYPE_SBIRTH);
        int printSeqNo = 1;
        if(result != null && result.getPrintSequenceNo() > 0) {
            printSeqNo = result.getPrintSequenceNo() + 1;
        }
        SBirthHistoryModel sBirthHistoryModel = sBirthHistoryRepository.findLatestSBirthHistory(recordId);
        Optional<SBirthModel> sBirthModel = sBirthRepository.findById(recordId);

        CertificatePrintModel certificatePrintModel = new CertificatePrintModel();
        certificatePrintModel.setPrintedAt(LocalDateTime.now());
        certificatePrintModel.setPrintedBy(username);
        certificatePrintModel.setApplicationNumber(sBirthHistoryModel.getApplicationNumber());
        certificatePrintModel.setPrintSequenceNo(printSeqNo);
        certificatePrintModel.setData(sBirthHistoryModel);
        certificatePrintModel.setRecordHistoryId(sBirthHistoryModel.getSBirthHistoryId());
        certificatePrintModel.setPrintApplicationNumber(generatePrintApplicationNumber(sBirthHistoryModel.getRegistrationNumber(),
                sBirthHistoryModel.getApplicationNumber(), printSeqNo, currentUser));
        certificatePrintModel.setQrImage("");
        certificatePrintModel.setRecordId(sBirthHistoryModel.getSbirthId());
        certificatePrintModel.setRecordType(Constants.RECORD_TYPE_SBIRTH);

        certificatePrintModel = certificatePrintRepository.save(certificatePrintModel);
        SBirthModel sbModel = sBirthModel.get();
        if(certificatePrintModel != null) {
            sbModel.setPrintedAt(LocalDateTime.now());
            sbModel.setPrintedBy(username);
            sbModel.setPrintId(certificatePrintModel.getPrintId().toString());
        }

        if(!Constants.YES_PRINTED.equalsIgnoreCase(sbModel.getIsPrinted()) || !Constants.YES_PRINTED.equalsIgnoreCase(sBirthHistoryModel.getIsPrinted())){
            sbModel.setIsPrinted(Constants.YES_PRINTED);
            sBirthHistoryModel.setIsPrinted(Constants.YES_PRINTED);
        }

        BlockchainUpdateSBirthResponse blockchainResult = blockchainGatway.updateStillBirthRecord(sbModel, channelGovtHospital);
        // Set Blockchain response
        certificatePrintModel.setData(blockchainResult.getData());
        String message = blockchainResult.getMessage();
        String txID = blockchainResult.getTxID();
        String statusBlk = blockchainResult.getStatus();
        if(Constants.BLC_STATUS_TRUE.equalsIgnoreCase(statusBlk)) {
            sbModel.setBlcMessage(message);
            sbModel.setBlcTxId(txID);
            sbModel.setBlcStatus(statusBlk);
            certificatePrintModel.setBlcTransactionId(txID);

        } else {
            // logger.info("saveBirthCorrection False response from blockchain " + blockchainResult);
            throw new Exception(Constants.INTERNAL_SERVER_ERROR);
        }
        sBirthRepository.save(sbModel);
        sBirthHistoryRepository.save(sBirthHistoryModel);
        certificatePrintRepository.save(certificatePrintModel);
        if(slaId != null) {
            ApiResponse apiResponse = slaDetailsService.updatePrintRequestBySlaId(slaId, request);
            if(apiResponse.getStatus() == HttpStatus.NOT_FOUND )
                throw new NullPointerException(Constants.RECORD_NOT_FOUND +" {slaId}  "+ slaId);
        }

        return new ResponseEntity<>(certificatePrintModel, HttpStatus.OK);
    }

    @Override
    public ApiResponse verifyCertificate(String strPrintId) throws Exception {

        ApiResponse res = new ApiResponse();

        String printApplicationNumber =new String(Hex.decodeHex(strPrintId.toCharArray()), "UTF-8");
        logger.info("==== printApplicationNumber ==== Decoded =="+printApplicationNumber);
        // Long printId = Long.parseLong(strPrintId, 16);
        // logger.info("Hex String print Application Number : "+strPrintId+" Long print Application Number is : "+printApplicationNumber);
        //Optional<CertificatePrintModel> certificatePrintModel = certificatePrintRepository.findByPrintApplicationNumber(printApplicationNumber);
        List<CertificatePrintModel> certificatePrintModel = certificatePrintRepository.findByPrintApplicationNumber(printApplicationNumber);
        Long recordCount = certificatePrintModel.stream().count();
        logger.info("==== Record count is =="+recordCount);
        if(!certificatePrintModel.isEmpty() && recordCount == 1){
            CertificatePrintModel existedData = certificatePrintModel.get(0);
            logger.info("existedData =====>>>>>> : "+existedData);
            if(Constants.DATA_TYPE_MIGRATED.equalsIgnoreCase(existedData.getDataType())) {
                 logger.info("DATA_TYPE_MIGRATED =====>>>>>> : "+printApplicationNumber);
                Optional<CertificateHistoryModel> certificateHistoryModel = certificateHistoryRepository.findByUniquenum(printApplicationNumber);
                if (Constants.RECORD_TYPE_BIRTH.equalsIgnoreCase(existedData.getRecordType())) {
                    //If Data exist set birthHistoryModel in data and return exiting data
                    if (certificateHistoryModel.isPresent()) {
                        existedData.setData(copyPropertiesBirthModel(certificateHistoryModel.get()));
                       // return new ResponseEntity<>(existedData, HttpStatus.OK);
                        res.setData(existedData);
                        res.setStatus(HttpStatus.OK);
                    }
                } else if (Constants.RECORD_TYPE_DEATH.equalsIgnoreCase(existedData.getRecordType())) {
                    //If Data exist set birthHistoryModel in data and return exiting data
                    if (certificateHistoryModel.isPresent()) {
                        existedData.setData(copyPropertiesDeathhModel(certificateHistoryModel.get()));
                        //return new ResponseEntity<>(existedData, HttpStatus.OK);
                        res.setData(existedData);
                        res.setStatus(HttpStatus.OK);
                    }
                } else if (Constants.RECORD_TYPE_SBIRTH.equalsIgnoreCase(existedData.getRecordType())) {
                    //If Data exist set birthHistoryModel in data and return exiting data
                    if (certificateHistoryModel.isPresent()) {
                        existedData.setData(copyPropertiesSBirthModel(certificateHistoryModel.get()));
                       // return new ResponseEntity<>(existedData, HttpStatus.OK);
                        res.setData(existedData);
                        res.setStatus(HttpStatus.OK);
                    }
                }
            }
            else{
                // logger.info("NEW =====>>>>>> : "+printApplicationNumber);
                if (Constants.RECORD_TYPE_BIRTH.equalsIgnoreCase(existedData.getRecordType())) {
                    //If Data exist set birthHistoryModel in data and return exiting data
                    Optional<BirthHistoryModel> birthHistoryModel = birthHistoryRepository.findById(existedData.getRecordHistoryId());

                    if (birthHistoryModel.isPresent()) {
                        existedData.setData(birthHistoryModel.get());
                       // return new ResponseEntity<>(existedData, HttpStatus.OK);
                        res.setData(existedData);
                        res.setStatus(HttpStatus.OK);
                    }
                }
                else if (Constants.RECORD_TYPE_DEATH.equalsIgnoreCase(existedData.getRecordType())) {
                    //If Data exist set birthHistoryModel in data and return exiting data
                    Optional<DeathHistoryModel> deathHistoryModel = deathHistoryRepository.findById(existedData.getRecordHistoryId());

                    if (deathHistoryModel.isPresent()) {
                        existedData.setData(deathHistoryModel.get());
                       // return new ResponseEntity<>(existedData, HttpStatus.OK);
                        res.setData(existedData);
                        res.setStatus(HttpStatus.OK);
                    }
                }
                else if (Constants.RECORD_TYPE_SBIRTH.equalsIgnoreCase(existedData.getRecordType())) {
                    //If Data exist set birthHistoryModel in data and return exiting data
                    Optional<SBirthHistoryModel> sBirthHistoryModel = sBirthHistoryRepository.findById(existedData.getRecordHistoryId());

                    if (sBirthHistoryModel.isPresent()) {
                        existedData.setData(sBirthHistoryModel.get());
                        //return new ResponseEntity<>(existedData, HttpStatus.OK);
                        res.setData(existedData);
                        res.setStatus(HttpStatus.OK);
                    }
                }
            }
            //Same for Death and Still birth
        }else if(!certificatePrintModel.isEmpty() && recordCount > 1){
            //return new ResponseEntity<>(Constants.REPRINT_CERTIFICATE, HttpStatus.OK);
            res.setStatus(HttpStatus.CONFLICT);
            res.setMsg(Constants.REPRINT_CERTIFICATE);
        }else{
            res.setStatus(HttpStatus.NOT_FOUND);
            res.setMsg(Constants.REPRINT_CERTIFICATE);
            //res.setMsg(Constants.RECORD_NOT_FOUND);
        }
        return  res;
        //return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);


    }


    private String generatePrintApplicationNumber(String registrationNumber, String applicationNumber, int printSeqNo, UserModel currentUser) {

        String printResult = "";
        String prefix = Constants.ONLINE_TYPE_ONLINE;
        String orgId = currentUser.getOrganizationId();
        Optional<OrganizationModel> organizationModelOp = organizationRepository.findById(Long.parseLong(orgId));
        OrganizationModel organizationModel = organizationModelOp.get();
        String orgType = organizationModel.getOrganizationType();

        if((Constants.USER_TYPE_CFC).equalsIgnoreCase(orgType)){
            prefix = Constants.PRINT_TYPE_CFC;
        }
        else if((Constants.USER_TYPE_HOSPITAL).equalsIgnoreCase(orgType)){
            prefix = Constants.PRINT_TYPE_HOSPITAL;
        }
        printResult = prefix + registrationNumber + applicationNumber + Constants.PRINT_CERT_SEPARATOR + printSeqNo;

        return printResult;
    }
    private BirthHistoryModel copyPropertiesBirthModel(CertificateHistoryModel certificateHistoryModel ) {

        BirthHistoryModel birthHistoryModel = new BirthHistoryModel();
        birthHistoryModel.setApplicationNumber(certificateHistoryModel.getApplNo());
        birthHistoryModel.setRegistrationNumber(certificateHistoryModel.getRegno());
        birthHistoryModel.setName(certificateHistoryModel.getName());
        birthHistoryModel.setGenderCode(certificateHistoryModel.getGender());
        birthHistoryModel.setEventDate(certificateHistoryModel.getDoe());
        birthHistoryModel.setEventPlace(certificateHistoryModel.getPoe());
        birthHistoryModel.setMotherName(certificateHistoryModel.getMotherName());
        birthHistoryModel.setMotherAdharNumber(certificateHistoryModel.getMotherUid());
        birthHistoryModel.setFatherName(certificateHistoryModel.getFatherName());
        birthHistoryModel.setFatherAdharNumber(certificateHistoryModel.getFatherUid());
        birthHistoryModel.setAddressAtBirth(certificateHistoryModel.getEvenAddOfBnD());
        birthHistoryModel.setPermanentAddress(certificateHistoryModel.getPermanentaddressofBnD());
        birthHistoryModel.setRegistrationDatetime(certificateHistoryModel.getDtRegn());
        birthHistoryModel.setCreatedAt(certificateHistoryModel.getIssuedate());
        birthHistoryModel.setOrganizationCode(certificateHistoryModel.getLocation());
        birthHistoryModel.setRecordType(certificateHistoryModel.getRecordType());
        birthHistoryModel.setUserId(certificateHistoryModel.getUserId());
        birthHistoryModel.setBirthId(certificateHistoryModel.getRecordId());
        // birthHistoryModel.setHusbandWifeName(certificateHistoryModel.getHusbWifeName());
        // birthHistoryModel.setHusbandWifeUID(certificateHistoryModel.getHusbWifeUidNo());
        // birthHistoryModel.setDeceasedAdharNumber(certificateHistoryModel.getDeceasedUidNo());

        return birthHistoryModel;
    }

    private DeathHistoryModel copyPropertiesDeathhModel(CertificateHistoryModel certificateHistoryModel ) {

        DeathHistoryModel deathHistoryModel = new DeathHistoryModel();
        deathHistoryModel.setApplicationNumber(certificateHistoryModel.getApplNo());
        deathHistoryModel.setRegistrationNumber(certificateHistoryModel.getRegno());
        deathHistoryModel.setName(certificateHistoryModel.getName());
        deathHistoryModel.setGenderCode(certificateHistoryModel.getGender());
        deathHistoryModel.setEventDate(certificateHistoryModel.getDoe());
        deathHistoryModel.setEventPlace(certificateHistoryModel.getPoe());
        deathHistoryModel.setMotherName(certificateHistoryModel.getMotherName());
        deathHistoryModel.setMotherAdharNumber(certificateHistoryModel.getMotherUid());
        deathHistoryModel.setFatherName(certificateHistoryModel.getFatherName());
        deathHistoryModel.setFatherAdharNumber(certificateHistoryModel.getFatherUid());
        deathHistoryModel.setAddressAtDeath(certificateHistoryModel.getEvenAddOfBnD());
        deathHistoryModel.setPermanentAddress(certificateHistoryModel.getPermanentaddressofBnD());
        deathHistoryModel.setRegistrationDatetime(certificateHistoryModel.getDtRegn());
        deathHistoryModel.setCreatedAt(certificateHistoryModel.getIssuedate());
        deathHistoryModel.setOrganizationCode(certificateHistoryModel.getLocation());
        deathHistoryModel.setRecordType(certificateHistoryModel.getRecordType());
        deathHistoryModel.setUserId(certificateHistoryModel.getUserId());
        deathHistoryModel.setDeathId(certificateHistoryModel.getRecordId());
        deathHistoryModel.setHusbandWifeName(certificateHistoryModel.getHusbWifeName());
        deathHistoryModel.setHusbandWifeUID(certificateHistoryModel.getHusbWifeUidNo());
        deathHistoryModel.setDeceasedAdharNumber(certificateHistoryModel.getDeceasedUidNo());
        return deathHistoryModel;
    }

    private SBirthHistoryModel copyPropertiesSBirthModel(CertificateHistoryModel certificateHistoryModel ) {
        SBirthHistoryModel cBirthHistoryModel = new SBirthHistoryModel();
        cBirthHistoryModel.setApplicationNumber(certificateHistoryModel.getApplNo());
        cBirthHistoryModel.setRegistrationNumber(certificateHistoryModel.getRegno());
        cBirthHistoryModel.setName(certificateHistoryModel.getName());
        cBirthHistoryModel.setGenderCode(certificateHistoryModel.getGender());
        cBirthHistoryModel.setEventDate(certificateHistoryModel.getDoe());
        cBirthHistoryModel.setEventPlace(certificateHistoryModel.getPoe());
        cBirthHistoryModel.setMotherName(certificateHistoryModel.getMotherName());
        cBirthHistoryModel.setMotherAdharNumber(certificateHistoryModel.getMotherUid());
        cBirthHistoryModel.setFatherName(certificateHistoryModel.getFatherName());
        cBirthHistoryModel.setFatherAdharNumber(certificateHistoryModel.getFatherUid());
        cBirthHistoryModel.setAddressAtBirth(certificateHistoryModel.getEvenAddOfBnD());
        cBirthHistoryModel.setPermanentAddress(certificateHistoryModel.getPermanentaddressofBnD());
        cBirthHistoryModel.setRegistrationDatetime(certificateHistoryModel.getDtRegn());
        cBirthHistoryModel.setCreatedAt(certificateHistoryModel.getIssuedate());
        cBirthHistoryModel.setOrganizationCode(certificateHistoryModel.getLocation());
        cBirthHistoryModel.setRecordType(certificateHistoryModel.getRecordType());
        cBirthHistoryModel.setUserId(certificateHistoryModel.getUserId());
        cBirthHistoryModel.setSbirthId(certificateHistoryModel.getRecordId());
        // cBirthHistoryModel.setHusbandWifeName(certificateHistoryModel.getHusbWifeName());
        // cBirthHistoryModel.setHusbandWifeUID(certificateHistoryModel.getHusbWifeUidNo());
        // cBirthHistoryModel.setDeceasedAdharNumber(certificateHistoryModel.getDeceasedUidNo());
        return cBirthHistoryModel;
    }
}
