package net.carrossos.plib.persistency;

public class PersistencyException extends RuntimeException {

	private static final long serialVersionUID = 3838638883348780813L;

	public PersistencyException() {
	}

	public PersistencyException(String m) {
		super(m);
	}

	public PersistencyException(String m, Throwable t) {
		super(m, t);
	}

	public PersistencyException(Throwable t) {
		super(t);
	}

}
