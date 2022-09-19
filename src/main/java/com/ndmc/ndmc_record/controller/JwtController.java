package com.ndmc.ndmc_record.controller;

import com.ndmc.ndmc_record.dto.JwtRequest;
import com.ndmc.ndmc_record.dto.JwtResponse;
import com.ndmc.ndmc_record.service.JwtService;
import com.ndmc.ndmc_record.utils.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
//@CrossOrigin(origins = "*", allowedHeaders = "*")
public class JwtController {

    @Autowired
    private JwtService jwtService;

    @PostMapping({"/authenticate"})
    public JwtResponse createJwtToken(@RequestBody JwtRequest jwtRequest, HttpServletRequest request) throws Exception {

        String ipAddress = CommonUtil.getIpAddressByRequest(request);
        jwtRequest.setUserIp(ipAddress);
        return jwtService.createJwtToken(jwtRequest);
    }
}
