package com.ndmc.ndmc_record.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ndmc.ndmc_record.config.Constants;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor

public class DeathDto {
    private Long deathId;
    private Long deathIdTemp;
    private String divisionCode;
    private String registrationNumber;
    @JsonFormat(pattern= Constants.DATE_FORMAT)
    private LocalDate registrationDate;

    //@JsonFormat(pattern= Constants.DATE_TIME_FORMAT)
    //private LocalDateTime registrationDatetime;

    private String applicationNumber;
    private String eventPlace;
    @JsonFormat(pattern= Constants.DATE_TIME_FORMAT)
    private LocalDateTime eventDate;
    //@NotBlank(message = "Gender code is mandatory")
    private String genderCode;
    //@NotBlank(message = "Name is mandatory")
    private String name;
    //@NotBlank(message = "Permanent Address is mandatory")
    private String permanentAddress;
    //@NotBlank(message = "Father name is mandatory")
    private String fatherName;
    //@NotBlank(message = "Mother name is mandatory")
    private String motherName;
    private String userId;
    @JsonFormat(pattern= Constants.DATE_TIME_FORMAT)
    private LocalDateTime createdAt;
    //@NotBlank(message = "is Village Town is mandatory")
    private String isVillageTown;
    //@NotBlank(message = "Name of village is mandatory")
    private String nameVillageTown;
    //@NotBlank(message = "District name is mandatory")
    private String districtName;
    //@NotBlank(message = "State name is mandatory")
    private String stateName;
    //@NotBlank(message = "Event place flag is mandatory")
    private String eventPlaceFlag;
    //@NotBlank(message = "Is Delhi resident is mandatory")
    private String isDelhiResident;
    //@NotBlank(message = "Ndmc resident is mandatory")
    private String isNdmcResident;
    //@NotBlank(message = "Marital status code is mandatory")
    private Integer maritalStatusCode;
    //@NotBlank(message = "Cause of Death is mandatory")
    private String causeOfDeath;
    //@NotBlank(message = "Select is pregnant is mandatory")
    private String isPregnant;
    //@NotBlank(message = "Year of smoking is mandatory")

    //@NotBlank(message = "Informant Name is mandatory")
    private String informantName;
    //@NotBlank(message = "Informant Address is mandatory")
    private String informantAddress;
    private String isOralInformant;
    //@NotBlank(message = "Medical certified is mandatory")
    private String isMedicalCertified;
    //@NotBlank(message = "Deceased occupation is mandatory")
    private String deceasedOccupation;
    private Integer activityCode;
    //@NotBlank(message = "Deceased age is mandatory")
    private String deceasedAge;
    //@NotBlank(message = "Deceased religion is mandatory")
    private String deceasedReligion;
    //@NotBlank(message = "Medical Attention code is mandatory")
    private Integer medicalAttentionCode;
    private String applStatus;
    //@NotBlank(message = "CR Number is mandatory")
    private String crNumber;
    //@NotBlank(message = "COD is mandatory")
    private String causeOfDeathDetails;
    private Float lateFee;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modifiedAt;
    private String modifiedBy;
    //@NotBlank(message = "Father Aadhar number is mandatory")
    private String fatherAdharNumber;
    //@NotBlank(message = "Mother Aadhar number is mandatory")
    private String motherAdharNumber;
    //@NotBlank(message = "Deceased Aadhar number is mandatory")
    private String deceasedAdharNumber;
    private String isActive;
    private String isDeleted;
    //@NotBlank(message = "Education code number is mandatory")
    private String educationCode; // API call to get education details
    //@NotBlank(message = "Religion code is mandatory")
    private String religionCode; // API
    //@NotBlank(message = "Nationality is mandatory")
    private String nationality;
    //@NotBlank(message = "Occupation code is mandatory")
    private String occupationCode; // API
    private String organizationCode;
    private String status;

    //@NotBlank(message = "Contact number is mandatory")
    private String contactNumber;
    private String approvedBy;
    @JsonFormat(pattern= Constants.DATE_TIME_FORMAT)
    private LocalDateTime ApprovedAt;
    private String rejectedBy;
    @JsonFormat(pattern= Constants.DATE_TIME_FORMAT)
    private LocalDateTime rejectedAt;
    //@NotBlank(message = "Pin code is mandatory")
    private String pinCode;
    //@NotBlank(message = "Mother nationality is mandatory")
    private String motherNationality;
    //@NotBlank(message = "Father nationality is mandatory")
    private String fatherNationality;
    private String blcMessage;
    private String blcStatus;
    private String blcTxId;
    //@NotBlank(message = "Applicant name is mandatory")
    private String applicantName;
    //@NotBlank(message = "Applicant Address is mandatory")
    private String applicantAddress;
    //@NotBlank(message = "Area is mandatory")
    private String area;
    //@NotBlank(message = "Chew arecnut is mandatory")
    private String chewArecanut;
    //@NotBlank(message = "Informant street is mandatory")
    private String informantStreet;
    //@NotBlank(message = "Informant Door no is mandatory")
    private String informantDoorNo;
    //@NotBlank(message = "Husband wife UID is mandatory")
    private String husbandWifeUID;
    //@NotBlank(message = "Husband wife name is mandatory")
    private String husbandWifeName;


    private String recordType;
    private String sdmLetterNo;
    private String sdmLetterImage;
    private Integer yearsSmoking;
    private Integer yearsDrinking;
    private Integer yearsTobacco;
    private String addressAtDeath;

    private String isUnkownCase; // Y | N

    private String channelName;
}
