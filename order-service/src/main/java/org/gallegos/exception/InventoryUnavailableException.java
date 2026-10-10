package org.gallegos.exception;

public class InventoryUnavailableException extends RuntimeException{

	private static final long serialVersionUID = 1L;

	public InventoryUnavailableException(String message) {
		super(message);
	}
}
