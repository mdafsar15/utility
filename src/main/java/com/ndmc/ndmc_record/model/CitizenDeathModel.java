package com.ndmc.ndmc_record.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ndmc.ndmc_record.config.Constants;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "citizen_death_record")
@EntityListeners(AuditingEntityListener.class)
public class CitizenDeathModel {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "temp_death_id")
    private Long deathIdTemp;
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
    private String motherName;
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
    private Integer maritalStatusCode;
    @Transient
    private String maritalStatusCodeDesc;
    private String causeOfDeath;
    private String isPregnant;
    private Integer yearsSmoking;
    private Integer yearsDrinking;
    private Integer yearsTobacco;
    private String informantName;
    private String informantAddress;
    private String isOralInformant;
    private String isMedicalCertified;
    private String deceasedOccupation;
   // private Integer activityCode;
    private String deceasedAge;  // pattern=yyyy-MM-dd
    private String deceasedReligion;
    private Integer medicalAttentionCode;
    @Transient
    private String medicalAttentionCodeDesc;
    private String applStatus;
    private String crNumber;
    private String causeOfDeathDetails;
    private Float lateFee;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modifiedAt;
    private String modifiedBy;
    private String fatherAdharNumber;
    private String motherAdharNumber;
    private String deceasedAdharNumber;
    private String isActive;
    private String isDeleted;
    private String educationCode; // API call to get education details
    private String religionCode; // API
    private String nationality;
    private String occupationCode; // API
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
   private String applicantName;
   private String applicantAddress;
    private String area;

    private String chewArecanut;
    private String informantStreet;
    private String informantDoorNo;
    private String husbandWifeUID;
    private String husbandWifeName;
  //  private String rejectionRemark;

    private String recordType; // NEW, OLD
    private String type; // CITIZEN_DEATH
//    private String sdmLetterNo;
//    private String isPrinted;
    private String transactionType;

    //private String isPrinted;
    //private String transactionType;
//    private String printedBy;
//    @JsonFormat(pattern= Constants.DATE_TIME_FORMAT)
//    private LocalDateTime printedAt;
//
//    @Transient
//    private String approvalTimeLeft;
//    @Transient
//    private String doctype;
//    @Transient
//    private Boolean viewDocuments;
//    private Long correctionSlaId;

//    @Transient
//    private String printId;
    private Long organizationId;
  //  private String sdmLetterImage;
   // private String sdmLetterImageHash;

    private String addressAtDeath;
    /*
    @JsonDeserialize(using = CustomParameterDeserializer.class)
    @JsonBackReference(value = "correctionSlaId")
    @OneToOne
    @JoinColumn(name = "correction_sla_id")
    private SlaDetailsModel correctionSlaId;

     */

    private String isUnkownCase; // Y | N

}
