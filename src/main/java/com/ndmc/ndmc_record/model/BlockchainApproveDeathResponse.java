package com.ndmc.ndmc_record.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BlockchainApproveDeathResponse {

    private Long deathCertificateApproved;
    private String message;
    private String status;
    private String txID;

}
