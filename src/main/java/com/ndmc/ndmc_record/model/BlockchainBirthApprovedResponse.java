package com.ndmc.ndmc_record.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BlockchainBirthApprovedResponse {

    private Long birthApproved;
    private BirthModel data;
    private String message;
    private String status;
    private String txID;

}
