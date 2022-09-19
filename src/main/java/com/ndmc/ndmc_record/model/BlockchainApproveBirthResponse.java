package com.ndmc.ndmc_record.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BlockchainApproveBirthResponse {

    private Long birthCertificateApproved;
    private String message;
    private String status;
    private String txID;

}
