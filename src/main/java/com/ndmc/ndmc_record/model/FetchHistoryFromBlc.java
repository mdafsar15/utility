package com.ndmc.ndmc_record.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FetchHistoryFromBlc {

    private String description;
    private String transactionType;
    private String userName;
    private LocalDateTime date;
}
