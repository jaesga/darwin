package models.exception;

import models.api.errors.Error;

public class DarwinErrorException extends Exception {

	private Error error;

	public DarwinErrorException(Error error) {
		this.setError(error);
	}

	public DarwinErrorException(Error error, String message) {
		super(message);
		this.setError(error);
	}

	public Error getError() {
		return error;
	}

	public void setError(Error error) {
		this.error = error;
	}
}
