package com.ndmc.ndmc_record.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BlockchainAttachmentResponse {
	private Long attachment_ID;
	private Long attachmentModified;
	private AttachmentModel data;
	private String message;
	private String status;
	private String txID;

}
