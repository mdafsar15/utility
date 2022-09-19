package com.ndmc.ndmc_record.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UploadFileResponse {

	private String fileName;

	private String fileDownloadUri;

	private String fileType;

	private long size;
	
	private int fileHash;

	private String documentName;
}
