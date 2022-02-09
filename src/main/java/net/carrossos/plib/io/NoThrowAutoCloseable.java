package net.carrossos.plib.io;

public interface NoThrowAutoCloseable extends AutoCloseable {

	@Override
	void close();

}
