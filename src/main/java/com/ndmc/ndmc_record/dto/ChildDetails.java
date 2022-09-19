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
public class ChildDetails {

    private Long sequenceNo;
    private String applicationNumber;
    private Long birthId;
    private String childName;
    private String genderCode;
    //@JsonFormat(pattern="yyyy-MM-dd", shape=JsonFormat.Shape.STRING)
    @JsonFormat(pattern= Constants.DATE_TIME_FORMAT)
    private LocalDateTime eventDate;
    private Float childWeight;
    private String registrationNumber;
    @JsonFormat(pattern= Constants.DATE_FORMAT)
    private LocalDate registrationDate;

}
