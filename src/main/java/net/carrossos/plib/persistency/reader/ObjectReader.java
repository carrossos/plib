package net.carrossos.plib.persistency.reader;

import java.time.LocalDateTime;

import net.carrossos.plib.persistency.Reference;

public interface ObjectReader {

	public int getLength();

	public String getLocation();

	public Reference getReference();

	public boolean isPresent();

	public ObjectReader readAttribute(String attribute);

	public boolean readBoolean();

	public double readDouble();

	public int readInteger();

	public ObjectReader readKey(int index);

	public LocalDateTime readLocalDate();

	public long readLong();

	public Object readObject(Class<?> type);

	public String readString();

	public ObjectReader readValue(int index);
}