package com.ndmc.ndmc_record.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ReportSearchDto {
    private String registrationNumber;
    private String applicationNumber;
    private String mobileNumber;
    private String motherName;
    private String fatherName;
    private String eventPlace;
    private String name;
    private String status;

    @JsonFormat(pattern="yyyy-MM-dd", shape=JsonFormat.Shape.STRING)
    private LocalDate regStartDate;

    @JsonFormat(pattern="yyyy-MM-dd", shape=JsonFormat.Shape.STRING)
    private LocalDate regEndDate;
  
    @JsonFormat(pattern="yyyy-MM-dd", shape=JsonFormat.Shape.STRING)
    private LocalDate eventStartDate;

    @JsonFormat(pattern="yyyy-MM-dd", shape=JsonFormat.Shape.STRING)
    private LocalDate eventEndDate;
    private String recordType; // BIRTH, SBIRTH, DEATH

    private String stateName;

    private String divisionCode; // Added by Deepak

    private String causeOfDeath;

}
