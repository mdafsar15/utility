package com.ndmc.ndmc_record.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BlockchainRejectDeathResponse {

    private Long deathCertificateRejected;
    private String message;
    private String status;
    private String txID;

}
