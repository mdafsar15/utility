package com.ndmc.ndmc_record.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.ndmc.ndmc_record.dto.*;
import com.ndmc.ndmc_record.model.SlaDetailsModel;

public interface SlaDetailsService {

	Long saveSlaDetails(AttachmentDto attachment, HttpServletRequest request) throws Exception;

    Map<Long, SlaDetailsModel> findAllPendingMap(String recordType, String nameInclusion);

	ApiResponse saveBirthInclusion(NameIncusionDto nameIncusionDto, HttpServletRequest request) throws Exception;

	ApiResponse saveBirthCorrection(BirthCorrectionDto birthCorrectionDto, HttpServletRequest request) throws Exception;

	ApiResponse saveStillBirthCorrection(StillBirthCorrectionDto stillBirthCorrectionDto, HttpServletRequest request) throws Exception;

	ApiResponse saveDeathCorrection(DeathCorrectionDto stillBirthCorrectionDto, HttpServletRequest request) throws Exception;


    ApiResponse approveRejectBirthIncusion(Long slaDetailsId, String status, String remarks, HttpServletRequest request) throws Exception;

	ApiResponse approveRejectBirthLegalData(Long slaDetailsId, String status, String remarks, HttpServletRequest request) throws Exception;

	ApiResponse approveRejectDeathLegalData(Long slaDetailsId, String status, String remarks, HttpServletRequest request) throws Exception;

	ApiResponse approveRejectStillBirthLegalData(Long slaDetailsId, String status, String remarks, HttpServletRequest request) throws Exception;

	List<SlaDetailsModel> findByStatus(String status) throws Exception;

    List<Long> recordIdsByStatus(String recordTypeDeath, String status) ;

	List<Long> recordIdsByStatus(String recordTypeDeath, String filterType, String status);

	ApiResponse getDetails(Long slaDetailsId, String transactionType, String recordType, HttpServletRequest request) throws Exception;

	boolean verifySlaIdToUploadDocument(Long slaId, HttpServletRequest request) throws Exception;

	ApiResponse updateBirthIncusion(Long slaId, NameIncusionDto nameIncusionDto, HttpServletRequest request) throws Exception;

    ApiResponse updateBirthCorrection(Long slaId, BirthCorrectionDto birthCorrectionDto, HttpServletRequest request) throws Exception;

    ApiResponse updateDeathCorrection(Long slaId, DeathCorrectionDto deathCorrectionDto,  HttpServletRequest request) throws Exception;

    ApiResponse updateStillBirthCorrection(Long slaId, StillBirthCorrectionDto stillBirthCorrectionDto, HttpServletRequest request) throws Exception;

    ApiResponse savePrintRequest(PrintRequestDto printRequestDto, HttpServletRequest request) throws Exception;

	ApiResponse updatePrintRequest(Long slaId, PrintRequestDto printRequestDto, HttpServletRequest request) throws Exception;

	ApiResponse updatePrintRequestBySlaId(Long slaId, HttpServletRequest request) throws Exception;

    ApiResponse getReportSearch(ReportSearchDto reportSearchDto, String type, HttpServletRequest request) throws Exception;

    ApiResponse saveOnlinePrintRequest(PrintRequestDto printRequestDto, HttpServletRequest request) throws Exception;

	List<Long> findSlaOrganizationId(Long organizationId);

	ApiResponse saveIndividualPrintRequest(PrintRequestDto printRequestDto, String useType, HttpServletRequest request) throws Exception;


	ApiResponse saveOnlineBirthCorrection(BirthCorrectionDto birthCorrectionDto, HttpServletRequest request) throws Exception;

	ApiResponse getOnlineSlaDetails(Long bndId, String onlineBirthCorrection, String recordTypeBirth, HttpServletRequest request);

	ApiResponse updateOnlineBirthCorrection(Long slaId, BirthCorrectionDto birthCorrectionDto, HttpServletRequest request) throws Exception;

	ApiResponse saveOnlineDeathCorrection(DeathCorrectionDto stillBirthCorrectionDto, HttpServletRequest request) throws Exception;

	ApiResponse saveOnlineStillBirthCorrection(StillBirthCorrectionDto stillBirthCorrectionDto, HttpServletRequest request) throws Exception;

	ApiResponse onlineAppointmentBySlaId(Long slaId, AppointmentDto appointmentDto, HttpServletRequest request);

	ApiResponse getOnlineCorrectionList(String recordType,HttpServletRequest request);

	ApiResponse updateOnlineAppointmentByAptId(Long aptId, AppointmentDto appointmentDto, HttpServletRequest request);

	ApiResponse getOnlineAppointmentByStatus(String status, HttpServletRequest request);

}
