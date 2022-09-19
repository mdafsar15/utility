package com.ndmc.ndmc_record.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BlockchainSlaDetailsResponse {
    //private Long slaRegistered;
    private Long slaDetails_ID;
    private Long slaModified;
    private SlaDetailsModel data;
    private String message;
    private String status;
    private String txID;

}
