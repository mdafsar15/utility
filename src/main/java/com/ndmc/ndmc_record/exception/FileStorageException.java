package com.ndmc.ndmc_record.exception;

public class FileStorageException extends RuntimeException {

	private String message;

	public FileStorageException(String message) {
		super(message);
		this.message = message;
	}

	public FileStorageException() {
	}

}
