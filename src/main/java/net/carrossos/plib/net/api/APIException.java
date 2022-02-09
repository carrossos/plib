package net.carrossos.plib.net.api;

public class APIException extends RuntimeException {

	private static final long serialVersionUID = 5490772396940348332L;

	public APIException() {
		super();
	}

	public APIException(String message) {
		super(message);
	}

	public APIException(String message, Throwable cause) {
		super(message, cause);
	}

	public APIException(Throwable cause) {
		super(cause);
	}

}
