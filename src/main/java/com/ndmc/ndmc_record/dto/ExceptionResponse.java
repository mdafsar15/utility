package com.ndmc.ndmc_record.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExceptionResponse {
	private String message;
	private LocalDateTime dateTime;
	private int statusCode;

}