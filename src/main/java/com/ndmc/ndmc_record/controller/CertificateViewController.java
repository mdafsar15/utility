package com.ndmc.ndmc_record.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;


@Controller
public class CertificateViewController {
    @CrossOrigin(origins = "/**")
    @RequestMapping("/certificate/view")
    public String index() {
        return "index";
    }
}
