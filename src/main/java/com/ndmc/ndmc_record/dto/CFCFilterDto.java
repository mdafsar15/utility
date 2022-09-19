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
public class CFCFilterDto {
    private String registrationNumber;
    private String applicationNumber;
    private String mobileNumber;
    private String motherName;
    private String fatherName;
    private String divisionCode;
    private String eventPlace;
    private String eventPlaceFlag; // H for Hospital & N for Home
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

    private String genderCode;
    private String husbandWifeName;
    private String trackingNo;

}
