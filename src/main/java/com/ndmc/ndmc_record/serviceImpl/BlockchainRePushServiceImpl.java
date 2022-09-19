package com.ndmc.ndmc_record.serviceImpl;

import com.ndmc.ndmc_record.blockchainGatway.BlockchainGatway;
import com.ndmc.ndmc_record.config.Constants;
import com.ndmc.ndmc_record.model.*;
import com.ndmc.ndmc_record.repository.*;
import com.ndmc.ndmc_record.service.BlockchainRePushService;
import com.ndmc.ndmc_record.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class BlockchainRePushServiceImpl implements BlockchainRePushService {

    private final Logger logger = LoggerFactory.getLogger(BlockchainRePushServiceImpl.class);

    @Autowired
    BlockchainRePushRepository blockchainRePushRepository;

    @Autowired
    SBirthRepository sBirthRepository;

    @Autowired
    SBirthHistoryRepository sBirthHistoryRepository;

    @Autowired
    BirthRepository birthRepository;

    @Autowired
    BirthHistoryRepository birthHistoryRepository;

    @Autowired
    DeathHistoryRepository deathHistoryRepository;

    @Autowired
    DeathRepository deathRepository;

    @Autowired
    SlaDetailsRepository slaDetailsRepository;

    @Autowired
    SlaDetailsHistoryRepository slaDetailsHistoryRepository;

    @Autowired
    BlockchainGatway blockchainGatway;

    @Value("${CHANNEL_GOVT_HOSPITAL}")
    private String channelGovtHospital;

    @Autowired
    AuthServiceImpl authService;

    Map<String,Integer> recordDetails= new HashMap<>();

    @Override
    @Transactional
    public  List<BlockchainRePushSummary> getAllRecords(HttpServletRequest request) throws Exception {
        recordDetails = null;
        recordDetails= new HashMap<>();
        List<Map<String,Integer>> totalRecordDetails= new ArrayList<>();
        List<BlockchainRePushSummary> blockchainRePushSummary =  blockchainRePushRepository.findAll();
        if(!blockchainRePushSummary.isEmpty()) {
            blockchainRePushSummary.forEach( bs -> {
                logger.info(bs.getId());
                logger.info(bs.getIdTable()+"");
                logger.info(bs.getTableName());
                logger.info(bs.getBlcAction());
                logger.info(bs.getBlcStatus());
                if(Constants.RECORD_TYPE_BIRTH.equals(bs.getTableName())) {
                    try {
                        Map<String, Integer> insertUpdateBlockchainBirth = insertUpdateBlockchainBirth(bs,request);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else if(Constants.RECORD_TYPE_DEATH.equals(bs.getTableName())) {
                    try {
                        Map<String, Integer> insertUpdateBlockchainDeath= insertUpdateBlockchainDeath(bs,request);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else if(Constants.RECORD_TYPE_STILL_BIRTH.equals(bs.getTableName())) {
                    try {
                        Map<String, Integer> insertUpdateBlockchainStillBirth=insertUpdateBlockchainStillBirth(bs,request);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else if(Constants.RECORD_TYPE_SLA.equals(bs.getTableName())) {
                    try {
                        Map<String, Integer> insertUpdateBlockchainSla= insertUpdateBlockchainSla(bs,request);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            } );

        }
        // logger.info(String.valueOf(recordDetails));
        // logger.info(blockchainRePushSummary.toString());
        return blockchainRePushSummary;
    }

    @Override
    public String blockchainReCorrectDate(HttpServletRequest request) throws Exception {
        List<BirthModel> birthModelList =  birthRepository.getBlockchainReCorrectDate();
        if(!birthModelList.isEmpty()){
            logger.info("birthModelList ==>>"+ birthModelList.size());
            for(BirthModel birthModel : birthModelList ){
                try{
                boolean flg = false;
                logger.info("birthModel ==>>" + birthModel.getBirthId() + " " +birthModel.getEventDate()+ " " +birthModel.getRegistrationDatetime() + " " +birthModel.getApplicationNumber());
                String blcResponse = blockchainGatway.getBirthRecord(birthModel.getBirthId(), channelGovtHospital);

                BirthModel blcModel = JsonUtil.getObjectFromJson(blcResponse, BirthModel.class);
                logger.info("blcModel ==>>" +blcModel.getBirthId() + " " +blcModel.getEventDate()+ " " +blcModel.getRegistrationDatetime() + " " +blcModel.getApplicationNumber());

                if(birthModel.getEventDate()!=blcModel.getEventDate()) {
                    flg= true;
                    birthModel.setEventDate(blcModel.getEventDate());
                }
                if(birthModel.getRegistrationDatetime() != blcModel.getRegistrationDatetime()) {
                    flg= true;
                    birthModel.setRegistrationDatetime(blcModel.getRegistrationDatetime());
                }
                if(flg) {
                    logger.info("flg in BirthModel ==>>"+ flg);
                    birthModel.setBlcMessage("Record Update corrected date ");
                    birthRepository.save(birthModel);
                }
                }catch (Exception e){
                    e.printStackTrace();
                    logger.error("Exception in birthMode ==>>"+ birthModel ,e);
                }
            }
        }
        List<DeathModel> deathModelList =  deathRepository.getBlockchainReCorrectDate();
        if(!deathModelList.isEmpty()){
            logger.info("deathModelList ==>>"+ deathModelList.size());
            for(DeathModel deathMode : deathModelList ){
                try{
                boolean flg = false;
                logger.info("deathMode ==>>" + deathMode.getDeathId() + " " +deathMode.getEventDate()+ " " +deathMode.getRegistrationDatetime() + " " +deathMode.getApplicationNumber());
                String blcResponse = blockchainGatway.getDeathRecord(deathMode.getDeathId().toString(), channelGovtHospital);
                DeathModel blcModel = JsonUtil.getObjectFromJson(blcResponse, DeathModel.class);
                logger.info("blcModel ==>>" +blcModel.getDeathId() + " " +blcModel.getEventDate()+ " " +blcModel.getRegistrationDatetime() + " " +blcModel.getApplicationNumber());

                if(deathMode.getEventDate()!=blcModel.getEventDate()) {
                    flg= true;
                    deathMode.setEventDate(blcModel.getEventDate());
                }
                if(deathMode.getRegistrationDatetime() != blcModel.getRegistrationDatetime()) {
                    flg= true;
                    deathMode.setRegistrationDatetime(blcModel.getRegistrationDatetime());
                }
                if(flg) {
                    logger.info("flg in DeathModel ==>>"+ flg);
                    deathMode.setBlcMessage("Record Update corrected date ");
                    deathRepository.save(deathMode);
                }
                }catch (Exception e){
                    e.printStackTrace();
                    logger.error("Exception in DeathModel ==>>"+ deathMode,e);
                }
            }
        }
        List<SBirthModel> sBirthlList =  sBirthRepository.getBlockchainReCorrectDate();
        if(!deathModelList.isEmpty()){
            logger.info("sBirthlList ==>>"+ sBirthlList.size());
            for(SBirthModel sBirthModel : sBirthlList ){
                try{
                boolean flg = false;
                logger.info("sBirthModel ==>>" + sBirthModel.getSbirthId() + " " +sBirthModel.getEventDate()+ " " +sBirthModel.getRegistrationDatetime() + " " +sBirthModel.getApplicationNumber());
                String blcResponse = blockchainGatway.getStillBirthRecord(sBirthModel.getSbirthId().toString(), channelGovtHospital);
                SBirthModel blcModel = JsonUtil.getObjectFromJson(blcResponse, SBirthModel.class);
                logger.info("blcModel ==>>" +blcModel.getSbirthId() + " " +blcModel.getEventDate()+ " " +blcModel.getRegistrationDatetime() + " " +blcModel.getApplicationNumber());

                if(sBirthModel.getEventDate()!=blcModel.getEventDate()) {
                    flg= true;
                    sBirthModel.setEventDate(blcModel.getEventDate());
                }
                if(sBirthModel.getRegistrationDatetime() != blcModel.getRegistrationDatetime()) {
                    flg= true;
                    sBirthModel.setRegistrationDatetime(blcModel.getRegistrationDatetime());
                }
                if(flg) {
                    logger.info("flg in sBirthModel ==>>"+ flg);
                    sBirthModel.setBlcMessage("Record Update corrected date ");
                    sBirthRepository.save(sBirthModel);
                }}catch (Exception e){
                e.printStackTrace();
                logger.error("Exception in sBirthModel ==>>"+ sBirthModel,e);
            }
            }
        }

        return "Record Update corrected date ";
    }

    private Map<String, Integer> insertUpdateBlockchainSla(BlockchainRePushSummary bc, HttpServletRequest request) throws Exception {
        Optional<SlaDetailsModel> slaDetailsModelDetails= slaDetailsRepository.findById(bc.getIdTable());

        if(slaDetailsModelDetails.isPresent()) {
            BlockchainSlaDetailsResponse blockchainResult = null;
            BlockchainSlaDetailsResponse blockchainResultUpdate = null;
            String message = null;
            String txID = null;
            String status = null;
            SlaDetailsModel slaDetailsModel = slaDetailsModelDetails.get();
            // logger.info(slaDetailsModelDetails.toString());
            // logger.info("insertUpdateBlockchainBirth beforeCallBLCTime:" + LocalDateTime.now());
            // Set Blockchain response
            if("INSERT".equals(bc.getBlcAction())) {
                blockchainResult = blockchainGatway.insertSlaDetails(slaDetailsModel, channelGovtHospital);
                message = blockchainResult.getMessage();
                txID = blockchainResult.getTxID();
                status = blockchainResult.getStatus();
                if (recordDetails.containsKey("INSERT_SLA")) {
                    recordDetails.put("INSERT_SLA", recordDetails.get("INSERT_SLA") + 1);
                } else {
                    recordDetails.put("INSERT_SLA", 1);
                }
            }else if("UPDATE".equals(bc.getBlcAction())) {
                blockchainResultUpdate = blockchainGatway.updateSlaRecord(slaDetailsModel, channelGovtHospital);
                message = blockchainResultUpdate.getMessage();
                txID = blockchainResultUpdate.getTxID();
                status = blockchainResultUpdate.getStatus();
                if (recordDetails.containsKey("UPDATE_SLA")) {
                    recordDetails.put("UPDATE_SLA", recordDetails.get("UPDATE_SLA") + 1);
                } else {
                    recordDetails.put("UPDATE_SLA", 1);
                }
            }

            if (Constants.BLC_STATUS_TRUE.equalsIgnoreCase(status)) {
                slaDetailsModel.setBlcMessage(message);
                slaDetailsModel.setBlcTxId(txID);
                slaDetailsModel.setBlcStatus(status);
            }else{
                if (recordDetails.containsKey("ERROR_SLA")) {
                    recordDetails.put("ERROR_SLA", recordDetails.get("ERROR_SLA") + 1);
                } else {
                    recordDetails.put("ERROR_SLA", 1);
                }
            }
            slaDetailsModel = slaDetailsRepository.save(slaDetailsModel);
            SlaDetailsHistoryModel slaHistoryModel = new SlaDetailsHistoryModel();
            BeanUtils.copyProperties(slaDetailsModel, slaHistoryModel);
            slaHistoryModel = slaDetailsHistoryRepository.save(slaHistoryModel);
        }
        // logger.info("insertUpdateBlockchainBirth afterCallBLCTime:" + LocalDateTime.now());
        // logger.info("insertUpdateBlockchainBirth recordDetails:" + recordDetails);

        return recordDetails;
    }

    private Map<String,Integer> insertUpdateBlockchainStillBirth(BlockchainRePushSummary bc,HttpServletRequest request) throws Exception {
        Optional<SBirthModel> sBirthModelDetails= sBirthRepository.findById(bc.getIdTable());
        if(sBirthModelDetails.isPresent()) {
            BlockchainStillBirthResponse blockchainResult = null;
            BlockchainUpdateSBirthResponse blockchainResultUpdate = null;
            String message = null;
            String txID = null;
            String status = null;
            SBirthModel sBirthModel = sBirthModelDetails.get();
            // logger.info(sBirthModelDetails.toString());
            // logger.info("insertUpdateBlockchainStillBirth beforeCallBLCTime:" + LocalDateTime.now());
            // Set Blockchain response
            if("INSERT".equals(bc.getBlcAction())) {
                blockchainResult = blockchainGatway.insertStillBirthRecord(sBirthModel, channelGovtHospital);
                message = blockchainResult.getMessage();
                txID = blockchainResult.getTxID();
                status = blockchainResult.getStatus();
                if (recordDetails.containsKey("INSERT_STILL_BIRTH")) {
                    recordDetails.put("INSERT_STILL_BIRTH", recordDetails.get("INSERT_STILL_BIRTH") + 1);
                } else {
                    recordDetails.put("INSERT_STILL_BIRTH", 1);
                }
            }else if("UPDATE".equals(bc.getBlcAction())) {
                blockchainResultUpdate = blockchainGatway.updateStillBirthRecord(sBirthModel, channelGovtHospital);
                message = blockchainResultUpdate.getMessage();
                txID = blockchainResultUpdate.getTxID();
                status = blockchainResultUpdate.getStatus();
                if (recordDetails.containsKey("UPDATE_STILL_BIRTH")) {
                    recordDetails.put("UPDATE_STILL_BIRTH", recordDetails.get("UPDATE_STILL_BIRTH") + 1);
                } else {
                    recordDetails.put("ERROR_STILL_BIRTH", 1);
                }
            }
            if (Constants.BLC_STATUS_TRUE.equalsIgnoreCase(status)) {
                sBirthModel.setBlcMessage(message);
                sBirthModel.setBlcTxId(txID);
                sBirthModel.setBlcStatus(status);
            }else {
                if (recordDetails.containsKey("ERROR_STILL_BIRTH")) {
                    recordDetails.put("ERROR_STILL_BIRTH", recordDetails.get("ERROR_STILL_BIRTH") + 1);
                } else {
                    recordDetails.put("ERROR_STILL_BIRTH", 1);
                }
            }
            sBirthModel = sBirthRepository.save(sBirthModel);
            SBirthHistoryModel sBirthHistoryModel = new SBirthHistoryModel();
            BeanUtils.copyProperties(sBirthModel, sBirthHistoryModel);
            sBirthHistoryModel = sBirthHistoryRepository.save(sBirthHistoryModel);
        }
        // logger.info("insertUpdateBlockchainStillBirth afterCallBLCTime:" + LocalDateTime.now());
        // logger.info("insertUpdateBlockchainStillBirth recordDetails:" + recordDetails);

        return recordDetails;
    }

    private Map<String, Integer> insertUpdateBlockchainDeath(BlockchainRePushSummary bc,HttpServletRequest request) throws Exception {
        Optional<DeathModel> deathModelDetails= deathRepository.findById(bc.getIdTable());
        if(deathModelDetails.isPresent()) {
            BlockchainDeathResponse blockchainResult = null;
            BlockchainUpdateDeathResponse blockchainResultUpdate = null;
            String message = null;
            String txID = null;
            String status = null;
            DeathModel deathModel = deathModelDetails.get();
            // logger.info(deathModelDetails.toString());
            // logger.info("insertUpdateBlockchainDeath beforeCallBLCTime:" + LocalDateTime.now());
            // Set Blockchain response
            if("INSERT".equals(bc.getBlcAction())) {
                blockchainResult = blockchainGatway.insertDeathRecord(deathModel, channelGovtHospital);
                message = blockchainResult.getMessage();
                txID = blockchainResult.getTxID();
                status = blockchainResult.getStatus();
                if(recordDetails.containsKey("INSERT_DEATH")){
                    recordDetails.put("INSERT_DEATH",recordDetails.get("INSERT_DEATH")+1);
                }else{
                    recordDetails.put("INSERT_DEATH",1);
                }
            }else if("UPDATE".equals(bc.getBlcAction())) {
                blockchainResultUpdate = blockchainGatway.updateDeathRecord(deathModel, channelGovtHospital);
                message = blockchainResultUpdate.getMessage();
                txID = blockchainResultUpdate.getTxID();
                status = blockchainResultUpdate.getStatus();
                if(recordDetails.containsKey("UPDATE_DEATH")){
                    recordDetails.put("UPDATE_DEATH",recordDetails.get("UPDATE_DEATH")+1);
                }else{
                    recordDetails.put("UPDATE_DEATH",1);
                }
            }

            if (Constants.BLC_STATUS_TRUE.equalsIgnoreCase(status)) {
                deathModel.setBlcMessage(message);
                deathModel.setBlcTxId(txID);
                deathModel.setBlcStatus(status);
            }else {
                if (recordDetails.containsKey("ERROR_DEATH")) {
                    recordDetails.put("ERROR_DEATH", recordDetails.get("ERROR_DEATH") + 1);
                } else {
                    recordDetails.put("ERROR_DEATH", 1);
                }
            }
            deathModel = deathRepository.save(deathModel);
            DeathHistoryModel deathHistoryModel = new DeathHistoryModel();
            BeanUtils.copyProperties(deathModel, deathHistoryModel);
            deathHistoryModel = deathHistoryRepository.save(deathHistoryModel);
        }
        // logger.info("insertUpdateBlockchainDeath afterCallBLCTime:" + LocalDateTime.now());
        // logger.info("insertUpdateBlockchainDeath recordDetails:" + recordDetails);

        return recordDetails;
    }

    public Map<String, Integer> insertUpdateBlockchainBirth(BlockchainRePushSummary bc, HttpServletRequest request) throws Exception {
        Optional<BirthModel> birthModelDetails= birthRepository.findById(bc.getIdTable());
        if(birthModelDetails.isPresent()) {
            BlockchainBirthResponse blockchainResult = null;
            BlockchainUpdateBirthResponse blockchainResultUpdate = null;
            String message = null;
            String txID = null;
            String status = null;
            BirthModel birthModel = birthModelDetails.get();
            // logger.info(birthModelDetails.toString());
            // logger.info("insertUpdateBlockchainBirth beforeCallBLCTime:" + LocalDateTime.now());
            // Set Blockchain response
            if("INSERT".equals(bc.getBlcAction())) {
                blockchainResult = blockchainGatway.insertBirthRecord(birthModel, channelGovtHospital);
                message = blockchainResult.getMessage();
                txID = blockchainResult.getTxID();
                status = blockchainResult.getStatus();
                if(recordDetails.containsKey("INSERT_BIRTH")){
                    recordDetails.put("INSERT_BIRTH",recordDetails.get("INSERT_BIRTH")+1);
                }else{
                    recordDetails.put("INSERT_BIRTH",1);
                }

            }else if("UPDATE".equals(bc.getBlcAction())) {
                blockchainResultUpdate = blockchainGatway.updateBirthRecord(birthModel, channelGovtHospital);
                message = blockchainResultUpdate.getMessage();
                txID = blockchainResultUpdate.getTxID();
                status = blockchainResultUpdate.getStatus();
                if(recordDetails.containsKey("UPDATE_BIRTH")){
                    recordDetails.put("UPDATE_BIRTH",recordDetails.get("UPDATE_BIRTH")+1);
                }else{
                    recordDetails.put("UPDATE_BIRTH",1);
                }
            }

            if (Constants.BLC_STATUS_TRUE.equalsIgnoreCase(status)) {
                birthModel.setBlcMessage(message);
                birthModel.setBlcTxId(txID);
                birthModel.setBlcStatus(status);
            }else {
                if (recordDetails.containsKey("ERROR_BIRTH")) {
                    recordDetails.put("ERROR_BIRTH", recordDetails.get("ERROR_BIRTH") + 1);
                } else {
                    recordDetails.put("ERROR_BIRTH", 1);
                }
            }

            birthModel = birthRepository.save(birthModel);
            BirthHistoryModel birthHistoryModel = new BirthHistoryModel();
            BeanUtils.copyProperties(birthModel, birthHistoryModel);
            birthHistoryModel = birthHistoryRepository.save(birthHistoryModel);
        }
        // logger.info("insertUpdateBlockchainBirth afterCallBLCTime:" + LocalDateTime.now());
        // logger.info("insertUpdateBlockchainBirth recordDetails:" + recordDetails);
        return recordDetails;
    }
}
