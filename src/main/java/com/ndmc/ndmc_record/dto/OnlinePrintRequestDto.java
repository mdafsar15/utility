package com.ndmc.ndmc_record.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ndmc.ndmc_record.config.Constants;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OnlinePrintRequestDto {
    private Long bndId;// Birth and Death Id
    private String transactionType; // PRINT_NAME_INCLUSION/PRINT_BIRTH_CORRECTION/PRINT_DEATH_CORRECTION/PRINT_STILL_BIRTH_CORRECTION
    private String recordType; //BIRTH/DEATH/STILL_BIRTH
    private String applNo;
    private String registrationNumber;
    private String applicantName;
    private String applicantAddress;
    @JsonFormat(pattern= Constants.DATE_FORMAT)
    private LocalDate dateOfEvent;
    @JsonFormat(pattern= Constants.DATE_FORMAT)
    private LocalDate dueDate;
    @JsonFormat(pattern= Constants.DATE_TIME_FORMAT)
    private LocalDateTime applicationDate;
    private String receiptNumber;
    private Integer noOfCopies;
    private Float fee;
    private String applicantEmailId;
    private String applicantContact;
    private Long uniqueId;
    private String divisionCode;
    private String organizationCode;
    private String genderCode;
    private String fatherName;
    private String motherName;
    private String childName;
    private String husbandWifeName;
    private String name;
}
