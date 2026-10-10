package org.gallegos.exception;

import java.time.LocalDateTime;

import org.gallegos.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(StockInsufficienteException.class)
	public ResponseEntity<ErrorResponse> handleStockInsufficient(
	        StockInsufficienteException ex,
	        HttpServletRequest request) {

	    ErrorResponse error = new ErrorResponse(
	            LocalDateTime.now(),
	            HttpStatus.CONFLICT.value(),
	            "STOCK_INSUFFICIENT",
	            ex.getMessage(),
	            request.getHeader("X-Trace-Id")
	    );

	    return ResponseEntity
	            .status(HttpStatus.CONFLICT)
	            .body(error);
	}
	
	@ExceptionHandler(ProductNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleProductNotFound(
	        ProductNotFoundException ex,
	        HttpServletRequest request) {

	    ErrorResponse error = new ErrorResponse(
	            LocalDateTime.now(),
	            HttpStatus.NOT_FOUND.value(),
	            "PRODUCT_NOT_FOUND",
	            ex.getMessage(),
	            request.getHeader("X-Trace-Id")
	    );

	    return ResponseEntity
	            .status(HttpStatus.NOT_FOUND)
	            .body(error);
	}
	
	@ExceptionHandler(OrderNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleOrderNotFound(
	        OrderNotFoundException ex,
	        HttpServletRequest request) {

	    ErrorResponse error = new ErrorResponse(
	            LocalDateTime.now(),
	            HttpStatus.NOT_FOUND.value(),
	            "ORDER_NOT_FOUND",
	            ex.getMessage(),
	            request.getHeader("X-Trace-Id")
	    );

	    return ResponseEntity
	            .status(HttpStatus.NOT_FOUND)
	            .body(error);
	}


	@ExceptionHandler(InvalidOrderStatusException.class)
	public ResponseEntity<ErrorResponse> handleInvalidOrderStatus(
	        InvalidOrderStatusException ex,
	        HttpServletRequest request) {

	    ErrorResponse error = new ErrorResponse(
	            LocalDateTime.now(),
	            HttpStatus.CONFLICT.value(),
	            "INVALID_ORDER_STATUS",
	            ex.getMessage(),
	            request.getHeader("X-Trace-Id")
	    );

	    return ResponseEntity
	            .status(HttpStatus.CONFLICT)
	            .body(error);
	}
    
	@ExceptionHandler(InventoryUnavailableException.class)
	public ResponseEntity<ErrorResponse> handleInventoryUnavailable(
	        InventoryUnavailableException ex,
	        HttpServletRequest request) {

	    ErrorResponse error = new ErrorResponse(
	            LocalDateTime.now(),
	            HttpStatus.SERVICE_UNAVAILABLE.value(),
	            "INVENTORY_UNAVAILABLE",
	            ex.getMessage(),
	            request.getHeader("X-Trace-Id")
	    );

	    return ResponseEntity
	            .status(HttpStatus.SERVICE_UNAVAILABLE)
	            .body(error);
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationErrors(
	        MethodArgumentNotValidException ex,
	        HttpServletRequest request) {

	    ErrorResponse error = new ErrorResponse(
	            LocalDateTime.now(),
	            HttpStatus.BAD_REQUEST.value(),
	            "INVALID_DATA",
	            "Los datos enviados no son válidos",
	            request.getHeader("X-Trace-Id")
	    );

	    return ResponseEntity
	            .status(HttpStatus.BAD_REQUEST)
	            .body(error);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleInternalError(
	        Exception ex,
	        HttpServletRequest request) {

	    ErrorResponse error = new ErrorResponse(
	            LocalDateTime.now(),
	            HttpStatus.INTERNAL_SERVER_ERROR.value(),
	            "INTERNAL_ERROR",
	            "Ocurrió un error interno en el servidor",
	            request.getHeader("X-Trace-Id")
	    );

	    return ResponseEntity
	            .status(HttpStatus.INTERNAL_SERVER_ERROR)
	            .body(error);
	}
}

