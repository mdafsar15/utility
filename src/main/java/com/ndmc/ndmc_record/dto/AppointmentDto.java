package com.ndmc.ndmc_record.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ndmc.ndmc_record.config.Constants;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDto {
    @JsonFormat(pattern = Constants.DATE_TIME_FORMAT)
    private LocalDateTime appointmentDateTime;
    private String status; // OPEN | CLOSED
    private Long slaId;

    @JsonFormat(pattern = Constants.DATE_TIME_FORMAT)
    private LocalDateTime modifiedAt;

    private String modifiedBy;
}
