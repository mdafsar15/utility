package com.ndmc.ndmc_record.exception;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import com.ndmc.ndmc_record.config.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.ndmc.ndmc_record.dto.ExceptionResponse;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(DateRangeException.class)
	public ResponseEntity<Object> dateRangeException(DateRangeException exception, WebRequest webRequest) {
		logger.error("DateRangeException: " + exception.getMessage(), exception);
		String messageNotFound = exception.getMessage();
		ExceptionResponse response = new ExceptionResponse();
		response.setDateTime(LocalDateTime.now());
		response.setMessage(messageNotFound);
		response.setStatusCode(HttpStatus.NOT_ACCEPTABLE.value());
		ResponseEntity<Object> entity = new ResponseEntity<>(response, HttpStatus.NOT_ACCEPTABLE);
		System.gc();
		System.out.println("Cleanup completed...dateRangeException");
		return entity;
	}

	@ExceptionHandler(NullPointerException.class)
	public ResponseEntity<Object> nullPointerException(NullPointerException exception, WebRequest webRequest) {
		logger.error("NullPointerException: " + exception.getMessage(), exception);
		String messageNotFound = exception.getMessage();
		ExceptionResponse response = new ExceptionResponse();
		response.setDateTime(LocalDateTime.now());
		response.setMessage(messageNotFound);
		response.setStatusCode(HttpStatus.NOT_ACCEPTABLE.value());
		ResponseEntity<Object> entity = new ResponseEntity<>(response, HttpStatus.NOT_ACCEPTABLE);
		System.gc();
		System.out.println("Cleanup completed...nullPointerException");
		return entity;
	}

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<Object> notFoundExceptions(NotFoundException exception, WebRequest webRequest) {
		logger.error("NotFoundException: " + exception.getMessage(), exception);
		String messageNotFound ="No data was found. Please enter the correct input and try again later! ";
		ExceptionResponse response = new ExceptionResponse();
		response.setDateTime(LocalDateTime.now());
		response.setMessage(messageNotFound);
		response.setStatusCode(HttpStatus.NOT_FOUND.value());
		ResponseEntity<Object> entity = new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
		System.gc();
		System.out.println("Cleanup completed...notFoundExceptions");
		return entity;
	}

	@ExceptionHandler(DateTimeParseException.class)
	public ResponseEntity<Object> dateTimeParseException(DateTimeParseException exception, WebRequest webRequest) {
		logger.error("DateTimeParseException: " + exception.getMessage(), exception);
		ExceptionResponse response = new ExceptionResponse();
		response.setDateTime(LocalDateTime.now());
		response.setMessage(Constants.DATE_FORMAT_MESSAGE);
		response.setStatusCode(HttpStatus.NOT_FOUND.value());
		ResponseEntity<Object> entity = new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
		System.gc();
		System.out.println("Cleanup completed...dateTimeParseException");
		return entity;
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Object> illegalArgumentException(IllegalArgumentException exception, WebRequest webRequest) {
		logger.error("IllegalArgumentException : " + exception.getMessage(), exception);
		ExceptionResponse response = new ExceptionResponse();
		response.setDateTime(LocalDateTime.now());
		response.setMessage(exception.getMessage());
		response.setStatusCode(HttpStatus.BAD_REQUEST.value());
		ResponseEntity<Object> entity = new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		System.gc();
		System.out.println("Cleanup completed...illegalArgumentException");
		return entity;
	}

	@ExceptionHandler(value = Exception.class)
	public ResponseEntity<?> databaseConnectionFailsException(Exception exception, WebRequest webRequest) {
		logger.error("Exception: " + exception.getMessage(), exception);
		String messageInternalServerError = "Your request cannot be processed now due to some technical issue. Please try again later!" ;

		ExceptionResponse response = new ExceptionResponse();
		response.setDateTime(LocalDateTime.now());
		response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		response.setMessage(messageInternalServerError + " Exception: " + exception.getMessage());
		ResponseEntity<Object> entity = new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		System.gc();
		System.out.println("Cleanup completed...Exception");
		return entity;

	}
	
	@Override
	protected ResponseEntity<Object>  handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
	    String name = ex.getParameterName();
	    logger.error(name + " parameter is missing", ex);
		ExceptionResponse response = new ExceptionResponse();
		response.setDateTime(LocalDateTime.now());
		response.setMessage(name + " parameter is missing");
		response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		ResponseEntity<Object> entity = new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		System.gc();
		System.out.println("Cleanup completed...handleMissingServletRequestParameter");
		return entity;

	}

}
