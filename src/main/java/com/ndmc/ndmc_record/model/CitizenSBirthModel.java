package com.ndmc.ndmc_record.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ndmc.ndmc_record.config.Constants;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "citizen_still_birth_record")
public class CitizenSBirthModel implements Serializable {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "temp_sbirth_id")
    private Long sbirthIdTemp;
    private String trackingNo;
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
  //  private Integer activityCode;
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
//    private String approvedBy;
//    @JsonFormat(pattern= Constants.DATE_TIME_FORMAT)
//    private LocalDateTime approvedAt;
//    private String rejectedBy;
//    @JsonFormat(pattern= Constants.DATE_TIME_FORMAT)
//    private LocalDateTime rejectedAt;
    private String pinCode;
    private String motherNationality;
    private String fatherNationality;
//    @JsonIgnore
//    private String blcMessage;
//    @JsonIgnore
//    private String blcStatus;
//    @JsonIgnore
//    private String blcTxId;
//    private String rejectionRemark;

    private String recordType;
    private String type; // CITIZEN_SBIRTH

   // private String sdmLetterNo;
  //  private String isPrinted;
    private String transactionType;
//    private String printedBy;
//    @JsonFormat(pattern= Constants.DATE_TIME_FORMAT)
//    private LocalDateTime printedAt;

//    @Transient
//    private String approvalTimeLeft;
//    @Transient
//    private String doctype;
//    @Transient
//    private Boolean viewDocuments;
    private String applicantName;
    private String applicantAddress;
//    private Long correctionSlaId;
//
//    @Transient
//    private String printId;

    private Long organizationId;

   // private String sdmLetterImage;
   // private String sdmLetterImageHash;
   // @Transient
    private String area;

    /* @JsonDeserialize(using = CustomParameterDeserializer.class)
    @JsonBackReference(value = "correctionSlaId")
    @OneToOne
    @JoinColumn(name = "correction_sla_id")
    private SlaDetailsModel correctionSlaId;

     */
}
