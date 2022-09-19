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
@Table(name = "pincode_master")
public class StateMasterModel implements Serializable {

	private static final long serialVersionUID = 11L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	private String pincode;
	private String stateName;
	private String districtName;
	private String area;
	private String taluk;
	private String regionName;
	private String oldState;
	private String deleteFlag;
}
