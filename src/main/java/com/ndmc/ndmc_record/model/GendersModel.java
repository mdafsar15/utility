package com.ndmc.ndmc_record.model;

import java.io.Serializable;
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
@Table(name = "genders")
public class GendersModel implements Serializable {
	private static final long serialVersionUID = 11L;

	@Id
	private String genderId;
	private String genderCode;
	private String genderDesc;
	private String deleteFlag;
	private String createdBy;
	private Date createdDate;
}
