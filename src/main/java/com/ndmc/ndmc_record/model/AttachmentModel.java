package com.ndmc.ndmc_record.model;

import java.time.LocalDateTime;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonFormat;

import org.hibernate.annotations.GenericGenerator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "attachments")
public class AttachmentModel {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long fileId;
    private Long bndId;// Birth and Death Id
    private String fileType; // CORRECTIONLETTER
    private Long slaDetailsId;
    private String fileName; // fileName with extension
    private String savedFileName; // fileName to be saved in disk
    private String filePath;
    private String fileSize;
    private String fileHash;
    private String userId;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    private String status; //ACTIVE/INACTIVE
    private String blcMessage;
    private String blcStatus;
    private String blcTxId;

    @Transient
    private String doctype;
}
