package com.ndmc.ndmc_record.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ndmc.ndmc_record.config.Constants;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PrintCountDto {
    @JsonFormat(pattern= Constants.DATE_FORMAT)
    private LocalDate printDateStart;

    @JsonFormat(pattern= Constants.DATE_FORMAT)
    private LocalDate printDateEnd;
}
