package com.ndmc.ndmc_record.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.ndmc.ndmc_record.config.Constants;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "certificate_history_ndmc")
public class CertificateHistoryModel {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    private String applNo;
    private String regno;
    private String name;
    private String gender;
    @JsonFormat(pattern= Constants.DATE_TIME_FORMAT)
    private LocalDateTime doe;
    private String poe;
    private String motherName;
    private String motherUid;
    private String fatherName;
    private String fatherUid;
    @Column(name = "even_add_of_b_n_d")
    private String evenAddOfBnD;
    @Column(name = "permanentaddressof_b_n_d")
    private String permanentaddressofBnD;
    @JsonFormat(pattern= Constants.DATE_TIME_FORMAT)
    private LocalDateTime dtRegn;
    @JsonFormat(pattern= Constants.DATE_TIME_FORMAT)
    private LocalDateTime issuedate;
    private String uniquenum;
    private String organisationName;
    private String agencyName;
    private String dept;
    private String husbWifeName;
    private String husbWifeUidNo;
    private String deceasedUidNo;
    private String transTime;
    private String activityType;
    private String useridName;
    private String ipAddress;
    private String location;
    private String referenceno;
    private Long recordId;
    private String recordType;
    private String userId;
}
