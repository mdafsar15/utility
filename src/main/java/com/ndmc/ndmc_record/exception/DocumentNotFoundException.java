package com.ndmc.ndmc_record.exception;

public class DocumentNotFoundException extends RuntimeException {

	private String message;

	public DocumentNotFoundException(String message) {
		super(message);
		this.message = message;
	}

	public DocumentNotFoundException() {
	}
}