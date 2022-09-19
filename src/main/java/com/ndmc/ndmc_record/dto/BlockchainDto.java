package com.ndmc.ndmc_record.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class BlockchainDto {

    private String certPath;
    private String caUrl;
    private String walletPath;
    private String userName;
    private String userPassword;
    private String orgMsp;
}
