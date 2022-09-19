package com.ndmc.ndmc_record.exception;

public class DateRangeException extends RuntimeException {

    private String message;

    public DateRangeException(String message) {
        super(message);
        this.message = message;
    }

    public DateRangeException() {
    }

}
