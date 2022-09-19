package com.ndmc.ndmc_record.exception;

public class NotFoundException extends RuntimeException {

	private String message;

	public NotFoundException(String message) {
		super(message);
		this.message = message;
	}

	public NotFoundException() {
	}
}

