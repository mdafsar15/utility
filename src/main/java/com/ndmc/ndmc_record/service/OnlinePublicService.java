package com.ndmc.ndmc_record.service;

import com.ndmc.ndmc_record.dto.*;

import javax.servlet.http.HttpServletRequest;
import java.time.format.DateTimeParseException;

public interface OnlinePublicService {
    ApiResponse onlineSearchBirthEnquiry(OnlinePrintRequestDto printRequestDto, HttpServletRequest request) throws DateTimeParseException,Exception;

    ApiResponse onlineSearchInclusionEnquiry(OnlinePrintRequestDto printRequestDto, HttpServletRequest request) throws  DateTimeParseException,Exception;

    ApiResponse onlineSearchStillBirthEnquiry(OnlinePrintRequestDto printRequestDto, HttpServletRequest request) throws  DateTimeParseException,Exception;

    ApiResponse onlineSearchDeathEnquiry(OnlinePrintRequestDto printRequestDto, HttpServletRequest request) throws  DateTimeParseException,Exception;

    ApiResponse createLinkForSelfRegister(SelfRegistrationLinkDto selfRegistrationLinkDto, HttpServletRequest request);

    ApiResponse reviewRecordsByApplNo(String recordType, String applNo);


    ApiResponse citizenBirthRegisterEvent(CitizenBirthDto citizenBirthDto, String orgId);

    ApiResponse citizenDeathRegisterEvent(DeathDto deathDto, String orgId);

    ApiResponse citizenSBirthRegisterEvent(SBirthDto sBirthDto, String orgId);

    ApiResponse getCitizenRecordsByType(String type, HttpServletRequest request);

    ApiResponse viewCitizenRecordsByTypeAndId(String type, String tempId, HttpServletRequest request);

    ApiResponse getFilteredData(String type, CFCFilterDto cfcFilterDto, HttpServletRequest request);
}
