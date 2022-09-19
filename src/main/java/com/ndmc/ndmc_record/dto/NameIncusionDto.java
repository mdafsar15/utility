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
public class NameIncusionDto {
	private Long bndId;
	private String childName;
	private String applicantEmailId;
	private String applicantContact;
	private String applicantName;
	private String applicantAddress;
	private String applNo;
	private String transactionType;
	private String recordType;



}
