package com.ndmc.ndmc_record.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentDto {
	private String applNo;
    private Long bndId;// Birth and Death Id
    private String transactionType; // NAME_INCLUSION/CORRECTION
    private String recordType; // BIRTH/DEATH/STILL-BIRTH
    private String documentName;
}
