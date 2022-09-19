package com.ndmc.ndmc_record.service;


import com.ndmc.ndmc_record.domain.ChildDetails;
import com.ndmc.ndmc_record.dto.GetDocumentResponse;
import com.ndmc.ndmc_record.dto.Response;
import org.hyperledger.fabric.gateway.ContractException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Component
public interface ChildDetailsService {

   public ResponseEntity<?> childRegistration(ChildDetails details) throws Exception;
   public List<Map<String,String>> getChildRecords() throws ContractException, IOException, NoSuchAlgorithmException, InterruptedException, TimeoutException;
  /*
    get child details from the mysql
   */
   //public GetDocumentResponse getChildRecordByChildRegNumber(String id) throws ContractException, IOException, NoSuchAlgorithmException, InterruptedException, TimeoutException;
   public ResponseEntity<Object> updateRecords(ChildDetails details, UUID id);
   public void deletedRecord(UUID id);
   public List<Map<String,String>> getChildDetailsByRegNumber(String childRegNumber);
   public Response pdfGenerationFlagUpdate(String childRegNumber,String flag);

   public Response updateChildRequestStatus(String childRegNumber,String reqStatus);
}
