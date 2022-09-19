package com.ndmc.ndmc_record.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BlockchainStillBirthResponse {

    private Long birthRegistered;
    private SBirthModel data;
    private String message;
    private String status;
    private String txID;

}
