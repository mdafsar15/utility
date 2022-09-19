package com.ndmc.ndmc_record.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BlockchainDeathHistoryResponse {

    @JsonProperty("TxId")
    private String TxId;
    @JsonProperty("Value")
    private DeathModel Value;
    @JsonProperty("Timestamp")
    private String Timestamp;
    @JsonProperty("IsDelete")
    private String IsDelete;


}
