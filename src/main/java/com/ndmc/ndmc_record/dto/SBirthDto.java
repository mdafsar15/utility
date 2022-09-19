package com.ndmc.ndmc_record.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ndmc.ndmc_record.config.Constants;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SBirthDto implements Serializable {

    private Long sbirthId;
    private Long sbirthIdTemp;
    private String divisionCode;
    private String registrationNumber;
    @JsonFormat(pattern= Constants.DATE_FORMAT)
    private LocalDate registrationDate;
    private String applicationNumber;
    private String eventPlace;

    @JsonFormat(pattern= Constants.DATE_TIME_FORMAT)
    private LocalDateTime eventDate;

    private String genderCode;
    private String name;
    private String permanentAddress;
    private String fatherName;
    private String fatherLiteracy;
    private String fatherOccupation;
    private String fatherReligion;
    private String motherName;
    private String motherLiteracy;
    private String motherOccupation;
    private String motherReligion;
    private String motherAge;
    private Float childWeight;
    private String numberWeekPregnancy;
    private String illegitimateBirth;
    private String birthOrder;
    private String deliveryAttentionCode;
    private String userId;
    @JsonFormat(pattern= Constants.DATE_TIME_FORMAT)
    private LocalDateTime createdAt;
    private String isVillageTown;
    private String nameVillageTown;
    private String districtName;
    private String stateName;
    private String eventPlaceFlag;
    private String isDelhiResident;
    private String isNdmcResident;
    private String addressAtBirth;
    private String motherAgeAtMarriage;
    private String methodOfDelivery;
    private String informantName;
    private String informantAddress;
    private String isOralInformant;
    private Integer activityCode;
    private String crNumber;
    private Float lateFee;
    @JsonFormat(pattern= Constants.DATE_TIME_FORMAT)
    private LocalDateTime modifiedAt;
    private String modifiedBy;
    private String fatherAdharNumber;
    private String motherAdharNumber;
    private String isActive;
    private String isDeleted;
    private String organizationCode;
    private String status;

    private String contactNumber;
    private String approvedBy;
    @JsonFormat(pattern= Constants.DATE_TIME_FORMAT)
    private LocalDateTime ApprovedAt;
    private String rejectedBy;
    @JsonFormat(pattern= Constants.DATE_TIME_FORMAT)
    private LocalDateTime rejectedAt;
    private String pinCode;
    private String motherNationality;
    private String fatherNationality;
    private String blcMessage;
    private String blcStatus;
    private String blcTxId;

    private String recordType;
    private String sdmLetterNo;
    private String sdmLetterImage;

    private String applicantName;
    private String applicantAddress;
    //private String sdmLetterImage;
   // private String sdmLetterImageHash;

    private String channelName;
}
