package com.ndmc.ndmc_record.service;

import com.ndmc.ndmc_record.dto.ApiResponse;
import com.ndmc.ndmc_record.model.CertificatePrintModel;
import org.apache.commons.codec.DecoderException;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface CertificatePrintService {
    public ResponseEntity<?> printBirthCertificate(Long recordId,Long slaId, HttpServletRequest request) throws Exception;

    public ResponseEntity<?> printDeathCertificate(Long deathId,Long slaId, HttpServletRequest request) throws Exception;

    public ResponseEntity<?> printSBirthCertificate(Long sbirthId,Long recordId, HttpServletRequest request) throws Exception;

    public ApiResponse verifyCertificate(String printId) throws Exception;
}
