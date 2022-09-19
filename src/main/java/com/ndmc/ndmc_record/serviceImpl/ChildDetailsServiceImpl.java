package com.ndmc.ndmc_record.serviceImpl;


import com.ndmc.ndmc_record.domain.ChildDetails;
import com.ndmc.ndmc_record.dto.Response;
import com.ndmc.ndmc_record.enums.DataFlagEnum;
import com.ndmc.ndmc_record.repository.ChildDetailsRepository;
import com.ndmc.ndmc_record.service.ChildDetailsService;
//import jdk.internal.cmm.SystemResourcePressureImpl;
//import netscape.javascript.JSObject;
import org.hyperledger.fabric.gateway.ContractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import javax.transaction.Transactional;

@Service
@Transactional
public class ChildDetailsServiceImpl implements ChildDetailsService {
    private final Logger logger = LoggerFactory.getLogger(ChildDetailsServiceImpl.class);
     @Autowired
    private ChildDetailsRepository childRepo;


    @Override
    @Transactional
    public ResponseEntity<?> childRegistration(ChildDetails details) throws Exception {
        // logger.info("data ----- "+details);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println(dtf.format(now));
        details.setRegistrationNumber("ChILDREG" + dtf.format(now));
       // details.setFlag(DataFlagEnum.PRESENT);
        childRepo.save(details);
        System.out.println("save data in database ---------- ");
//        String certificate = blockchain.blockChainCertificatePersist(details);
        return new ResponseEntity<>("Data save successfully", HttpStatus.OK);
    }

    @Override
    public List<Map<String,String>> getChildRecords() throws ContractException, IOException, NoSuchAlgorithmException, InterruptedException, TimeoutException {
        List<Map<String,String>> list = childRepo.getAllData();
        return  list;
    }

    @Override
    @Transactional
    public ResponseEntity<Object> updateRecords(ChildDetails details, UUID id){
        // logger.info("id ----- "+id);
        Optional<ChildDetails> recordOptional = childRepo.findByChildId(id);
        // logger.info("recordOptional --------  "+recordOptional.isPresent());
        if (!recordOptional.isPresent()) {
         //   throw new IllegalArgumentException("id must be persent in the update call");
            return ResponseEntity.notFound().build();
        }
        childRepo.save(details);
        return ResponseEntity.noContent().build();
    }

    @Override
    @Transactional
    public void deletedRecord(UUID id) {
        // logger.info("id ----- " + id);
        Optional<ChildDetails> details = childRepo.findByChildId(id);
        details.get().setFlag(DataFlagEnum.NO);
        // logger.info("This uuid data is deleted  " + id);
        childRepo.save(details.get());
    }

    @Override
    public List<Map<String,String>> getChildDetailsByRegNumber(String childRegNumber) {
        List<Map<String,String>> list = childRepo.getFindByRegistrationNumber(childRegNumber);
        return list;
    }

    @Override
    @Transactional
    public Response pdfGenerationFlagUpdate(String childRegNumber,String flag) {
        int a= childRepo.updateFlag(childRegNumber,flag);
        Response res=new Response();
        res.setMsg("pdf generated flag updated "+a);
        res.setChildRegNumber(childRegNumber);
        return res;
    }

    @Override
    @Transactional
    public Response updateChildRequestStatus(String childRegNumber, String reqStatus) {
        childRepo.updateChildDetailsRequestStatus(childRegNumber,reqStatus);
        Response res=new Response();
        res.setChildRegNumber(childRegNumber);
        res.setMsg("data request status is updated successfully");
        return res;
    }



}
