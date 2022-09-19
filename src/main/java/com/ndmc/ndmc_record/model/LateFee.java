package com.ndmc.ndmc_record.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "late_fee")
public class LateFee implements Serializable {

    @Id
    private String certificateType; //B/D/S

    private Integer startDays;
    private Integer endDays;
    private Float fee;
    private String status;
    private LocalDateTime createdAt;
    private String createdBy;

}
