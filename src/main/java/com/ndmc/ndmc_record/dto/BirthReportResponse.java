package com.ndmc.ndmc_record.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ndmc.ndmc_record.config.Constants;
import lombok.*;

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BirthReportResponse {
    private Long birthId;
    private String divisionCode;
    private String registrationNumber;
    @JsonFormat(pattern= Constants.DATE_TIME_FORMAT)
    private LocalDateTime registrationDatetime;
    private String applicationNumber;
    private String originalApplicationNumber;
    private String eventPlace;
    @JsonFormat(pattern= Constants.DATE_TIME_FORMAT)
    private LocalDateTime eventDate;
    private String genderCode;
    private String name;
    private String permanentAddress;
    private String fatherName;
    private String fatherLiteracy;
    private String fatherLiteracyDesc;
    private String fatherOccupation;
    private String fatherOccupationDesc;
    private String fatherReligion;
    private String fatherReligionDesc;
    private String motherName;
    private String motherLiteracy;
    private String motherLiteracyDesc;
    private String motherOccupation;
    private String motherOccupationDesc;
    private String motherReligion;
    private String motherReligionDesc;
    private String motherAge;
    private Float childWeight;
    private String numberWeekPregnancy;
    private String illegitimateBirth;
    private String birthOrder;
    private String deliveryAttentionCode;
    private String deliveryAttentionDesc;
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
    private String methodOfDeliveryDesc;
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
    private LocalDateTime approvedAt;
    private String rejectedBy;
    @JsonFormat(pattern= Constants.DATE_TIME_FORMAT)
    private LocalDateTime rejectedAt;
    private String pinCode;
    private String motherNationality;
    private String fatherNationality;
    @JsonIgnore
    private String blcMessage;
    @JsonIgnore
    private String blcStatus;
    @JsonIgnore
    private String blcTxId;
    private String rejectionRemark;
    private String recordType;
    private String sdmLetterNo;
    private String isPrinted;
    private String transactionType;

    private String printedBy;
    @JsonFormat(pattern= Constants.DATE_TIME_FORMAT)
    private LocalDateTime printedAt;

    @Transient
    private String approvalTimeLeft;
    @Transient
    private String doctype;
    @Transient
    private Boolean viewDocuments;

    private String applicantName;
    private String applicantAddress;

    private Long inclusionSlaId;
    private Long correctionSlaId;
    @Transient
    private String printId;

    private String area;
    private String sdmLetterImage;
    private String sdmLetterImageHash;
    private Long organizationId;

}
