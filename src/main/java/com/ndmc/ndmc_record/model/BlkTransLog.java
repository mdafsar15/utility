package com.ndmc.ndmc_record.model;

import lombok.*;
import org.apache.poi.ss.formula.functions.T;

import javax.json.Json;
import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "blc_transaction_log")
public class BlkTransLog {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long logId;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private String status;
    private String data;
    private String channel;
    private String chainCode;
    private String blcFunction;
    private String certType;
    private String certId;

}
