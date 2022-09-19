package com.ndmc.ndmc_record.exception;

public class NullPointerException extends RuntimeException {

    private String message;

    public NullPointerException(String message) {
        super(message);
        this.message = message;
    }

    public NullPointerException() {
    }

}
