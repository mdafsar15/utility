package com.ndmc.ndmc_record.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
@Table(name = "marital_status")
public class MaritalStatusModel {
	@Id
	private Long maritalStatusCode;
	private String maritalStatusDesc;
	private String deleteFlag;
	private String createdBy;
	private Date createdDate;

}