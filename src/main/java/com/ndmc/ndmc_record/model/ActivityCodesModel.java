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
@Table(name = "activity_codes")
public class ActivityCodesModel {	
	@Id
	//@GeneratedValue(strategy = GenerationType.AUTO)
	private Long activityCode;
	private String activityDesc;
	private String deleteFlag;
	private String createdBy;
	private Date createdDate;
}

