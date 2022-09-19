package com.ndmc.ndmc_record.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class DailyReportCount {


   // private String transactionType; //
    private Long noOfApplication;
    private Long noOfCopy;
    private float amount;

}
