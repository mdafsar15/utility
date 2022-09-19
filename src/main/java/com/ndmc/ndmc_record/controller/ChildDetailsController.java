package com.ndmc.ndmc_record.controller;
import com.lowagie.text.DocumentException;
import com.ndmc.ndmc_record.blockchainGatway.BlockchainGatway;
import com.ndmc.ndmc_record.domain.ChildDetails;
import com.ndmc.ndmc_record.domain.RecordDetails;
import com.ndmc.ndmc_record.dto.GetDocumentResponse;
import com.ndmc.ndmc_record.dto.Response;
import com.ndmc.ndmc_record.enums.ApprovalEnum;
import com.ndmc.ndmc_record.repository.ChildDetailsRepository;
import com.ndmc.ndmc_record.serviceImpl.ChildDetailsServiceImpl;
import com.ndmc.ndmc_record.serviceImpl.PdfGenaratorUtil;
import org.apache.tomcat.util.json.JSONParser;
import org.hyperledger.fabric.gateway.ContractException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeoutException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("transaction")
//@CrossOrigin(origins = "*")
public class ChildDetailsController {
    @Autowired
    private ChildDetailsServiceImpl childService;
    @Autowired
    private PdfGenaratorUtil pdfGenaratorUtil;
    @Autowired
    private BlockchainGatway blockchain;
    @Autowired
    private ChildDetailsRepository childRepo;
//using
    @PostMapping("/registration")
    public ResponseEntity<Response> childRegistration(@RequestBody RecordDetails details) throws Exception {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        System.out.println(formatter.format(date));
//        details.setReq_status(ApprovalEnum.PENDING);
        details.setCreatedAt(date);
        details.setModifiedAt(date);
//        childService.childRegistration(details);
        blockchain.insertRecord(details);
        Response res=new Response();
        res.setMsg("child details save successfully");
        res.setChildRegNumber(details.getApplicationNumber());
        //JSONObject resp=new JSONObject();resp.put("msg","child details save successfully");
        return new ResponseEntity<Response>(res, HttpStatus.ACCEPTED);
    }
//using
    @GetMapping("/get/details")
    public ResponseEntity<?> childGetDetails() throws Exception {
        List<Map<String,String>> list=childService.getChildRecords();
        //List<Map<String,String>> list=null;
       // blockchain.queryAllRecords();
        return new ResponseEntity<>(list,HttpStatus.OK);
    }
    //using
    @GetMapping("/get/details/blockchain")
    public ResponseEntity<?> childGetBlockchainDetails() throws Exception { ;
        return new ResponseEntity<>(blockchain.queryAllRecords(),HttpStatus.OK);
    }

    //using search filter
    @GetMapping("/get/details/by/{childRegNumber}")
    public ResponseEntity<List<Map<String,String>>> getChildRecordById(@RequestParam String childRegNumber) throws ContractException, IOException, NoSuchAlgorithmException, InterruptedException, TimeoutException {
        List<Map<String,String>> list = childService.getChildDetailsByRegNumber(childRegNumber);
        return new ResponseEntity<>(list,HttpStatus.OK);
    }

    @GetMapping("/get/details/blockchain/by/{childRegNumber}")
    public ResponseEntity<?> getBlockChildRecordById(@RequestParam String childRegNumber) throws Exception {
       // List<Map<String,String>> list = childService.getChildDetailsByRegNumber(childRegNumber);

        return new ResponseEntity<>(blockchain.queryRecordByRegNumber(childRegNumber),HttpStatus.OK);
    }

    @GetMapping("/get/details/history/blockchain/by/{childRegNumber}")
    public ResponseEntity<?> getBlockchainHistoryChildRecordById(@RequestParam String childRegNumber) throws Exception {
        // List<Map<String,String>> list = childService.getChildDetailsByRegNumber(childRegNumber);
        return new ResponseEntity<>(blockchain.getRecordHistory(childRegNumber),HttpStatus.OK);
    }
    @PutMapping("/edit/records/{id}")
    public ResponseEntity<Response> editChildDetails(@RequestBody ChildDetails details,@PathVariable UUID id) throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        System.out.println(formatter.format(date));
        details.setModifiedDate(date);
        details.setReq_status(details.getReq_status());
//        details.setApprovalStatus(ApprovalEnum.PENDING); //commented by Gunjan

        if(details.getReq_status().equals("INITIALISED")) {
            blockchain.modifyRecord(details);
        }
        else {
            childService.updateRecords(details, id);
        }
        Response res=new Response();
        res.setMsg("child details updated successfully");
        res.setChildRegNumber(details.getRegistrationNumber());
        return ResponseEntity.ok().body(res);
    }
    @DeleteMapping("/record/{id}")
    public ResponseEntity<?> deleteUppclDetail(@RequestParam UUID id) {
     childService.deletedRecord(id);
     return new ResponseEntity<>("data deleted ",HttpStatus.OK);
    }
    @GetMapping("/getby/{regNumber}")
    public ResponseEntity<?> getChildRecordByChildRegNumber(@RequestParam String regNumber) throws Exception {
        // GetDocumentResponse list = childService.getChildRecordByChildRegNumber(regNumber);
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/child/download/")
                .path(regNumber+".pdf")
                .toUriString();
        GetDocumentResponse list=new GetDocumentResponse();
        list.setDownloadApiPath(fileDownloadUri);
        System.out.println(fileDownloadUri);
        return new ResponseEntity<>(list,HttpStatus.OK);
    }
    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity downloadFileFromLocal(@PathVariable String fileName, HttpServletRequest request) {

        Path path = Paths.get( "/home/welcome/Desktop/ndmc_record/pdfFiles/"+ fileName);
        System.out.println("path ============ "+path);
        Resource resource = new ClassPathResource("/tmp/"+fileName);
        String contentType = "application/octet-stream";
        try {
            resource = new UrlResource(path.toUri());
            System.out.println(resource.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/pdf/flag/update")
    public ResponseEntity<Response> updatePdfFlag(@RequestParam String childRegNumber,@RequestParam String flag) throws Exception {
        System.out.println("child reg ------- "+childRegNumber+" flag ---- "+flag);
      Response res=childService.pdfGenerationFlagUpdate(childRegNumber,flag);
        ChildDetails details = childRepo.findByRegistrationNumber(childRegNumber);
        System.out.println("gender ------- "+details.getGender());
            try {
                Map<String, String> data = new HashMap<String, String>();
                data.put("childName", details.getChildName());
                data.put("dateOfBirth", details.getDateOfBirth());
                data.put("birthOfTime", details.getDateOfTime());
                data.put("birthCity", details.getBirthCityName());
                data.put("birthCountry", details.getBirthCountryName());
                data.put("address", details.getAddress());
                data.put("country", details.getCountry());
                data.put("city", details.getCity());
                data.put("fatherName", details.getFatherName());
                data.put("motherName", details.getMotherName());
                data.put("religion", details.getReligion());
                data.put("regNumber", details.getRegistrationNumber());
                data.put("gender", String.valueOf(details.getGender()));
                data.put("pincode", details.getPostalCode());
                data.put("state", details.getState());
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date();
                details.setCreatedDate(date);
                pdfGenaratorUtil.createPdf("students", data, details.getRegistrationNumber(), details.getCreatedBy(), details.getCreatedDate().toString());
                System.out.println("after block chain method call -------------- ");
                //            System.out.println("absolute file path -------  "+file);
            /*if (Files.exists(file)) {
                response.setContentType("application/pdf");
                response.addHeader("Content-Disposition",
                        "attachment; filename=" + file.getFileName());
                Files.copy(file, response.getOutputStream());
                response.getOutputStream().flush();
                // pdfGenaratorUtil.uploadFile(response, "childDetails");
            }*/
            } catch (DocumentException | IOException ex) {
                ex.printStackTrace();
            }

        return  new ResponseEntity<>(res,HttpStatus.OK);
    }


    @GetMapping("/update/childRequest/status")
    public ResponseEntity<Response> updateChildRecordStatus(@RequestParam String childRegNumber,
                                                            @RequestParam String requestStatus) throws Exception {
       Response res= childService.updateChildRequestStatus(childRegNumber,requestStatus);
        ChildDetails detail = childRepo.findByRegistrationNumber(childRegNumber);
       if(requestStatus.equals("SEND")) {
           String response =blockchain.insertRecord(detail);

       }
       else if(requestStatus.equals("APPROVED")) {
//           String response =blockchain.insertRecord(details);
           String str=blockchain.queryRecordByRegNumber(childRegNumber);
           str = str.replaceAll("\\[", "").replaceAll("\\]","");
           JSONObject  details= new JSONObject(str);
           System.out.println(details.get("ChildName"));
           try {
               Map<String, String> data = new HashMap<String, String>();
               data.put("childName",(String) details.get("ChildName"));
               data.put("dateOfBirth", (String)details.get("DateOfBirth"));
               data.put("birthOfTime",(String)details.get("TimeOfBirth"));
               data.put("birthCity", (String)details.get("CityOfBirth"));
               data.put("birthCountry", (String)details.get("CountryOfBirth"));
               data.put("address", (String)details.get("Address"));
               data.put("country", (String)details.get("Country"));
               data.put("city", (String)details.get("City"));
               data.put("fatherName", (String)details.get("FatherName"));
               data.put("motherName", (String)details.get("MotherName"));
               data.put("religion", (String)details.get("Religion"));
               data.put("regNumber", (String)details.get("RegistrationNumber"));
               data.put("gender", (String)details.get("Gender"));
               data.put("pincode", (String)details.get("PostalCode"));
               data.put("state", (String)details.get("State"));
               data.put("fatherAadharNo",(String)details.get("FatherAadharNumber"));
               data.put("motherAadharNo",(String)details.get("MotherAadharNumber"));
               data.put("childAadharNo",(String)details.get("ChildAadharNumber"));
               data.put("guardianAadharNo",(String)details.get("GurdianAadharNumber"));

               pdfGenaratorUtil.createPdf("students", data, detail.getRegistrationNumber(),
                       detail.getCreatedBy(), detail.getCreatedDate().toString());
               System.out.println("after block chain method call -------------- ");

           } catch (DocumentException | IOException ex) {
               ex.printStackTrace();
           }
       }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/data/modify/blockchain")
    public ResponseEntity<Response> childBlockchainModify(@RequestBody RecordDetails details) throws Exception {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        System.out.println(formatter.format(date));
//        details.setReq_status(ApprovalEnum.SEND);
        //details.setCreatedDate(date);
        details.setModifiedAt(date);
        blockchain.modifyRecord(details);
        Response res=new Response();
        res.setMsg("child details modified successfully");
        res.setChildRegNumber(details.getApplicationNumber());
        //JSONObject resp=new JSONObject();resp.put("msg","child details save successfully");
        return new ResponseEntity<Response>(res, HttpStatus.ACCEPTED);
    }

}
