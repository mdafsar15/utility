package com.ndmc.ndmc_record.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "certificate_print")
public class CertificatePrintModel {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "print_id")
    private Long printId;
    private Long recordId;
    private Long recordHistoryId;
    private String recordType;
    private String printApplicationNumber;
    private String applicationNumber;
    private Integer printSequenceNo;
    private String qrImage;

    private String modifiedBy;
    private String printedBy;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime printedAt;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modifiedAt;

    @Transient
    private Object data;
    private String blcTransactionId;
    private String dataType;
}
