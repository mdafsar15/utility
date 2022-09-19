package com.ndmc.ndmc_record.service;

import com.ndmc.ndmc_record.model.ApplicationNumberCounter;

import java.time.LocalDateTime;

public interface ApplicatioNumberCounterService {
    public String getApplicationNumber(String orgCode, String registrationType, LocalDateTime registrationDate, String regNo) throws Exception;
    public String generateApplicationNumber(ApplicationNumberCounter counter) throws Exception;
    public ApplicationNumberCounter getRegistrationNumberCounter(Long organizationId, String organizationCode, String registrationType, LocalDateTime registrationDate) throws Exception;
    public String getRegistrationNumber(Long organizationId, String organizationCode, String registrationType, LocalDateTime registrationDate) throws Exception;
}

