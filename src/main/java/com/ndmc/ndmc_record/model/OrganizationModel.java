package com.ndmc.ndmc_record.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
@Table(name = "organization_master")
public class OrganizationModel {

	@Id
	private Long organizationId;
	private String organizationName;
	private String organizationType;
	private String organisationCode;
	private String divisionCode;
	private String organizationAddress;
	// private String hospitalCode;
	// private String institutionName;
	private LocalDateTime createdAt;
	private String createdBy;
	private LocalDateTime modifiedAt;
	private String modifiedBy;
	private String status;
	private String channelName;

}
