package com.ndmc.ndmc_record.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.ndmc.ndmc_record.config.Constants;
import org.hibernate.annotations.GenericGenerator;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sla_details_history")
public class SlaDetailsHistoryModel {
    
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "sla_details_history_id")
    private Long slaDetailsHistoryId;
    private Long slaDetailsId;
    private String applNo;
    private String certificateType; // Birth, Death, S Birth
    private String placeOfEvent;
    private String fatherName;
    private String childName;
    private String fatherAdharNumber;
    private String motherAdharNumber;
    private String deceaseAdharNumber;
    private String deceasedAge;
    private String applicantName;
    @JsonFormat(pattern= Constants.DATE_TIME_FORMAT)
    private LocalDateTime dateOfEvent;
    private String genderCode;
    private String motherName;
    private String applicantAddress;
    private String hospitalName;
    private String applicantContact;
    private String divisionCode;
    @JsonFormat(pattern= Constants.DATE_TIME_FORMAT)
    private LocalDateTime applDate;
    private Float amount;
    private Integer noOfCopies;
    private String status; //UPLOADING/PENDING/APPROVED/REJECTED
    @JsonFormat(pattern= Constants.DATE_TIME_FORMAT)
    private LocalDateTime createdAt;
    private String userId;
    private String remarks;
    private String deceasedName;
    @JsonFormat(pattern= Constants.DATE_TIME_FORMAT)
    private  LocalDateTime approvedAt;
    private String approvedBy;
    @JsonFormat(pattern= Constants.DATE_TIME_FORMAT)
    private  LocalDateTime rejectedAt;
    private String rejectedBy;
    private String transactionType;
    private String applicantEmailId;
    private Long bndId;// Birth and Death Id
    private String blcMessage;
    private String blcStatus;
    private String blcTxId;
    private String husbandWifeUID;
    private String husbandWifeName;
    private Integer maritalStatusCode;

    private String contactNumber;
    private Float childWeight;
    private String informantName;
    private String informantAddress;
    private String informantDoorNo;
    private String informantStreet;
    private String permanentAddress;

    private String recordType;
    private String registrationNumber;
    private String receiptNumber;
    private String addressAtBirth;
    private String deceaseAtAddress;

    @JsonFormat(pattern= Constants.DATE_TIME_FORMAT)
    private LocalDateTime updatedAt;
    private String updatedBy;

    private Float fee;

    @JsonFormat(pattern= Constants.DATE_TIME_FORMAT)
    private LocalDateTime dueDate;

    @Transient
    private String doctype;
    private Long slaOrganizationId;

    private String eventPlace;
    private String eventPlaceFlag;

    @JsonFormat(pattern= Constants.DATE_TIME_FORMAT)
    private LocalDateTime registrationDatetime;

    //Added by Deepak 09-05-22 for Online Services
    private String verifiedUIDNo;
    private String verifiedUIDName;
    private String verfiedUIDMobile;
    private String uploadedFile;

    //Added by Deepak 21-06-22 for Online Legal Correction Service
   // private Long appointmentId;
    private String correctionFields;

}
