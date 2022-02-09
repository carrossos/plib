package net.carrossos.plib.persistency.reader;

import java.time.LocalDateTime;
import java.util.Optional;

import net.carrossos.plib.persistency.PersistencyException;
import net.carrossos.plib.persistency.Reference;
import net.carrossos.plib.persistency.TypeUtils;

public abstract class DefaultReader implements ObjectReader {

	@Override
	public int getLength() {
		throw new PersistencyException("Cannot read an array on " + getLocation());
	}

	@Override
	public Reference getReference() {
		return null;
	}

	@Override
	public ObjectReader readAttribute(String attribute) {
		throw new PersistencyException("Cannot read an object on " + getLocation());
	}

	@Override
	public boolean readBoolean() {
		throw new PersistencyException("Unsupported type on " + getLocation());
	}

	@Override
	public double readDouble() {
		String str = readString();

		try {
			return Double.parseDouble(str);
		} catch (NumberFormatException e) {
			throw new PersistencyException("Failed to parse '" + str + "' as number", e);
		}
	}

	@Override
	public int readInteger() {
		return TypeUtils.toInt(readLong());
	}

	@Override
	public ObjectReader readKey(int index) {
		throw new PersistencyException("Cannot read a key on " + getLocation());
	}

	@Override
	public LocalDateTime readLocalDate() {
		throw new PersistencyException("Unsupported type on " + getLocation());
	}

	@Override
	public long readLong() {
		return TypeUtils.toLong(readDouble());
	}

	@Override
	public Optional<?> readObject(Class<?> type) {
		throw new PersistencyException("Unsupported type '" + type.getName() + "' on " + getLocation());
	}

	@Override
	public String readString() {
		throw new PersistencyException("Unsupported type on " + getLocation());
	}

	@Override
	public ObjectReader readValue(int index) {
		throw new PersistencyException("Cannot read a value on " + getLocation());
	}

	public static ObjectReader emptyReader(String location) {
		return new EmptyReader(location);
	}
}
