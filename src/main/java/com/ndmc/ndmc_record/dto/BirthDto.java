package com.ndmc.ndmc_record.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ndmc.ndmc_record.config.Constants;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor

public class BirthDto {

    @NotNull
    private List<ChildDetails> childDetails;
    private String divisionCode;
    private Long birthId;
    private Long birthIdTemp;
    private String registrationNumber;
    @JsonFormat(pattern= Constants.DATE_FORMAT)
    //@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDate registrationDate;
    private String applicationNumber;
    //@NotBlank(message = "Event Place is mandatory")
    private String eventPlace;
    //@NotBlank(message = "Permanent address is mandatory")
    private String permanentAddress;
    private String name;
    //@NotBlank(message = "Father name is mandatory")
    private String fatherName;
    //@NotBlank(message = "Father literacy is mandatory")
    private String fatherLiteracy;
    //@NotBlank(message = "Father occupation is mandatory")
    private String fatherOccupation;
    //@NotBlank(message = "Father religion is mandatory")
    private String fatherReligion;
    //@NotBlank(message = "Mother name is mandatory")
    private String motherName;
    //@NotBlank(message = "Mother literacy is mandatory")
    private String motherLiteracy;
    //@NotBlank(message = "Mother occupation is mandatory")
    private String motherOccupation;
    //@NotBlank(message = "Mother religion is mandatory")
    private String motherReligion;
    //@NotBlank(message = "Mother Age is mandatory")
    private String motherAge;
    //@NotBlank(message = "Child weight is mandatory")

    //@NotBlank(message = "Pregnancy week is mandatory")
    private String numberWeekPregnancy;
    private String illegitimateBirth;
    //@NotBlank(message = "Birth order is mandatory")
    private String birthOrder;
    //@NotBlank(message = "Delivery Attention code is mandatory")
    private String deliveryAttentionCode;
    private String userId;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    //@NotBlank(message = "Is village Town is mandatory")
    private String isVillageTown;
    //@NotBlank(message = "Name of Village Town is mandatory")
    private String nameVillageTown;
    //@NotBlank(message = "District name is mandatory")
    private String districtName;
    //@NotBlank(message = "State name is mandatory")
    private String stateName;
    //@NotBlank(message = "Event Place flag is mandatory")
    private String eventPlaceFlag;
    //@NotBlank(message = "Is Delhi resident is mandatory")
    private String isDelhiResident;
    //@NotBlank(message = "Is Ndmc resident is mandatory")
    private String isNdmcResident;
    //@NotBlank(message = "address at birth is mandatory")
    private String addressAtBirth;
    //@NotBlank(message = "Mother age at marriage is mandatory")
    private String motherAgeAtMarriage;
    //@NotBlank(message = "Method of delivery is mandatory")
    private String methodOfDelivery;
    //@NotBlank(message = "Informant name is mandatory")
    private String informantName;
    //@NotBlank(message = "Informant address is mandatory")
    private String informantAddress;
    private String isOralInformant;
    private Integer activityCode;
    //@NotBlank(message = "CR Number is mandatory")
    private String crNumber;
    private Float lateFee;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modifiedAt;
    private String modifiedBy;
    //@NotBlank(message = "Father Aadhar is mandatory")
    private String fatherAdharNumber;
    //@NotBlank(message = "Mother Aadhar is mandatory")
    private String motherAdharNumber;
    private String isActive;
    private String isDeleted;
    private String organizationCode;
    private String status;

    private String contactNumber;
    private String approvedBy;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime ApprovedAt;
    private String rejectedBy;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rejectedAt;
    private String pinCode;
    private String motherNationality;
    private String fatherNationality;
    private String blcMessage;
    private String blcStatus;
    private String blcTxId;
    private String applicantName;
    private String applicantAddress;
    private String recordType;
    private String sdmLetterNo;
   // private String sdmLetterImage;
    private Long inclusionSlaId;
    private String correctionSlaId;
    private String area;
    private String sdmLetterImage;
    //private String sdmLetterImageHash;

    private String channelName;
}
