package com.ndmc.ndmc_record.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "file.upload")
@Getter
@Setter
public class FileStorageProperties {
	private String uploadDir;

}