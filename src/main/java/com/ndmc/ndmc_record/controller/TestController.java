package com.ndmc.ndmc_record.controller;

import com.ndmc.ndmc_record.dto.Response;
import com.ndmc.ndmc_record.dto.TestDto;
import com.ndmc.ndmc_record.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("testing")
//@CrossOrigin(origins = "*")
public class TestController {

    @Autowired
    TestService testService;

    @GetMapping("/get")
    public String getTestValue(){
        return "Testing";
    }

    @PostMapping("/post")
    public ResponseEntity<?> postTestValue(@RequestBody TestDto testDto){
        testService.saveRecords(testDto);
        return new ResponseEntity("Record Added successfully", HttpStatus.ACCEPTED);
    }
}
