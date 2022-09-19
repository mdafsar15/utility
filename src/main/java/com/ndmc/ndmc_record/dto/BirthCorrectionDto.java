package com.ndmc.ndmc_record.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ndmc.ndmc_record.config.Constants;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class 	BirthCorrectionDto {

	private Long bndId;// Birth and Death Id
	private String transactionType; // NAME_INCLUSION/CORRECTION
	private String recordType; // BIRTH/DEATH/STILL-BIRTH
	private String applNo;
	private String registrationNumber;
	private String applicantName;
	private String applicantAddress;
	@JsonFormat(pattern= Constants.DATE_TIME_FORMAT)
	private LocalDateTime dateOfEvent;
	private String applicantContact;
	private String applicantEmailId;
	private String childName;
	private Float childWeight;
	private String genderCode;
	private String fatherAdharNumber;
	private String motherAdharNumber;
	private String fatherName;
	private String motherName;
	private String permanentAddress;
	private String addressAtBirth;
	private String informantName;
	private String informantAddress;
	private String contactNumber;

	private String eventPlace;
	private String eventPlaceFlag;
	private String divisionCode;

	@JsonFormat(pattern= Constants.DATE_TIME_FORMAT)
	private LocalDateTime registrationDatetime;


	//Added by Deepak 09-05-22 for Online Services
	private String verifiedUIDNo;
	private String verifiedUIDName;
	private String verfiedUIDMobile;
	private String uploadedFile;

	//Added by Deepak 21-06-22 for Online Legal Correction Service
	//private Long appointmentId;
	private String correctionFields;
}
