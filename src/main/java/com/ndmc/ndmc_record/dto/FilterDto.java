package com.ndmc.ndmc_record.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FilterDto {

    private String registrationNumber;
    private String applicationNumber;

    //@NotBlank(message = "Start date is mandatory")
    @JsonFormat(pattern="yyyy-MM-dd", shape=JsonFormat.Shape.STRING)
    private LocalDate startDate;

    //@NotBlank(message = "End date is mandatory")
    @JsonFormat(pattern="yyyy-MM-dd", shape=JsonFormat.Shape.STRING)
    private LocalDate endDate;
    private String status;
    //private String recordType; // BIRTH, SBIRTH, DEATH

    private String motherName;
    private String fatherName;
    private String name;
    private String eventPlace;
    private String eventPlaceFlag;
    private String divisionCode;
    private String contactNumber;
    private String husbandWifeName;
    private String genderCode;
    @JsonFormat(pattern="yyyy-MM-dd", shape=JsonFormat.Shape.STRING)
    private LocalDate regStartDate;

    @JsonFormat(pattern="yyyy-MM-dd", shape=JsonFormat.Shape.STRING)
    private LocalDate regEndDate;

    @JsonFormat(pattern="yyyy-MM-dd", shape=JsonFormat.Shape.STRING)
    private LocalDate eventStartDate;

    @JsonFormat(pattern="yyyy-MM-dd", shape=JsonFormat.Shape.STRING)
    private LocalDate eventEndDate;

    /* Added for get Record fro particular user
    * Author:deepak
    * Date: 19-05-22
    * */
    private Long userId;
    private String causeOfDeath;

}
