package org.gallegos.exception;

public class StockInsufficienteException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public StockInsufficienteException(String message) {
		super(message);
	}

}
