package net.carrossos.plib.persistency;

import java.time.LocalDateTime;

public interface ObjectWriter {

	public void writeArray(String attribute, int size, int index, Object value);

	public void writeBoolean(String attribute, boolean value);

	public void writeBuffer(String attribute, byte[] value);

	public void writeDouble(String attribute, double value);

	public void writeInteger(String attribute, int value);

	public void writeLocaleDate(String attribute, LocalDateTime value);

	public void writeLong(String attribute, long value);

	public void writeObject(String attribute, Object value);

	public void writeString(String attribute, String value);
}
