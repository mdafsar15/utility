package com.ndmc.ndmc_record.serviceImpl;

import com.ndmc.ndmc_record.blockchainGatway.BlockchainGatway;
import com.ndmc.ndmc_record.dto.ApiResponse;
import com.ndmc.ndmc_record.dto.SBirthDto;
import com.ndmc.ndmc_record.model.SBirthModel;
import com.ndmc.ndmc_record.repository.SBirthRepository;
import com.ndmc.ndmc_record.service.SBirthHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Transactional

public class SBirthHistoryServiceImpl implements SBirthHistoryService {

    private final Logger logger = LoggerFactory.getLogger(SBirthHistoryServiceImpl.class);

    @Value("${initial.record.status}")
    private String recordStatus;

    @Value("${approved_status}")
    private String approvedStatus;

    @Autowired
    SBirthRepository birthRepository;

    @Autowired
    BlockchainGatway blockchainGatway;


    @Override
    @Transactional
    public ApiResponse saveSBirthRecords(SBirthDto birthDto) {
        ApiResponse res = new ApiResponse();
        SBirthModel birthModel = new SBirthModel();
        BeanUtils.copyProperties(birthDto, birthModel);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyMMddHHmmss");
        LocalDateTime now = LocalDateTime.now();
        birthModel.setRegistrationDatetime(
                birthDto.getRegistrationDate() == null ? now : birthDto.getRegistrationDate().atStartOfDay());
        //birthModel.setRegistrationDate(now);
        birthModel.setCreatedAt(now);

        birthModel.setDivisionCode(birthModel.getDivisionCode());
        int randomPIN = (int)(Math.random()*9000)+10000000;
        String val = ""+randomPIN;
        String orgCode = birthDto.getOrganizationCode();
        String applNo = orgCode+dtf.format(now);
        birthModel.setApplicationNumber(applNo+"/SBIRTH");
        // birthModel.setRegistrationNumber(val);
        birthModel.setRegistrationNumber(birthDto.getRegistrationNumber());


        birthModel.setModifiedAt(now);
        birthModel.setCreatedAt(now);
        birthModel.setStatus(recordStatus.toUpperCase(Locale.ROOT));

        // logger.info("Requested Body "+birthModel);
        //Calling Blockchain gateway
        birthModel = birthRepository.save(birthModel);
        // blockchainGatway.modifyRecord(birthModel);
        res.setMsg("Record added successfully!");
        res.setStatus(HttpStatus.OK);
        BeanUtils.copyProperties(birthModel, birthDto);
        return res;

    }

    @Override
    @Transactional
    public ApiResponse updateSBirthRecords(SBirthDto birthDto) {
        Optional<SBirthModel> existedData = birthRepository.findById(birthDto.getSbirthId());
        ApiResponse res = new ApiResponse();
        // BirthModel birthModel = new BirthModel();
        SBirthModel birthModel = existedData.get();
        // logger.info("Requested Body "+birthModel);

        if(existedData.equals(Optional.empty())){
            res.setMsg("No Data Found");
            res.setStatus(HttpStatus.BAD_REQUEST);
            // logger.info("Existed Data -------------------------------------------++++ "+existedData);
        }
        else{

            if(birthModel.getStatus().equals(approvedStatus) ){
                res.setMsg("Action not permitted!");
                res.setStatus(HttpStatus.BAD_REQUEST);

            }
            else {
                BeanUtils.copyProperties(birthDto, birthModel);
                //DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();
                birthModel.setModifiedAt(now);
                birthModel.setModifiedBy(birthDto.getModifiedBy());
                birthModel = birthRepository.save(birthModel);
                BeanUtils.copyProperties(birthModel, birthDto);
                res.setMsg("Birth record is updated successfully");
                res.setStatus(HttpStatus.OK);
            }
        }
        return res;

    }

    @Override
    public List<SBirthModel> getBirthRecords(String status, String orgCode) {
        return birthRepository.getSBirthDataByStatusAndOrganization(status, orgCode);
    }

    @Override
    public List<SBirthModel> getAllSBirthRecords() {
        return birthRepository.findAll();
    }

    @Override
    @Transactional
    public ApiResponse updateBirthRecordStatus(Long sbirthId, String status) {

        ApiResponse res = new ApiResponse();
        Optional<SBirthModel> existedData = birthRepository.findById(sbirthId);
        //boolean existedData = birthRepository.existsById(birthId);

        if(existedData.equals(Optional.empty())){
            res.setMsg("No Data Found");
            res.setStatus(HttpStatus.BAD_REQUEST);
        }
        else{
            SBirthModel birthModel = existedData.get();
            // logger.info("status ----- "+birthModel.getStatus());
            if(birthModel.getStatus().equals(approvedStatus) ){
                res.setMsg("Record Status is already Approved!");
                res.setStatus(HttpStatus.BAD_REQUEST);
            }
            else{
                birthRepository.updateSBirthStatusById(sbirthId,status.toUpperCase(Locale.ROOT));
                res.setMsg("Record status is updated successfully");
                res.setStatus(HttpStatus.OK);
            }
        }
        return res;
    }
}
