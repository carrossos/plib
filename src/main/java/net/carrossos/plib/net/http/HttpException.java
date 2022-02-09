package net.carrossos.plib.net.http;

import java.net.URI;

public class HttpException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final int code;

	private final URI uri;

	private final String method;

	public int getCode() {
		return code;
	}

	public String getMethod() {
		return method;
	}

	public URI getUri() {
		return uri;
	}

	public HttpException(String method, URI uri, int code) {
		super(method + " request to '" + uri + "' failed with code " + code);

		this.uri = uri;
		this.method = method;
		this.code = code;
	}

}
