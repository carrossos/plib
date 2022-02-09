package net.carrossos.plib.persistency.reader;

import java.time.LocalDateTime;
import java.util.Objects;

import net.carrossos.plib.persistency.PersistencyException;
import net.carrossos.plib.persistency.Reference;

class EmptyReader implements ObjectReader {

	private final String location;

	@Override
	public int getLength() {
		return 0;
	}

	@Override
	public String getLocation() {
		return location;
	}

	@Override
	public Reference getReference() {
		return null;
	}

	@Override
	public boolean isPresent() {
		return false;
	}

	@Override
	public ObjectReader readAttribute(String attribute) {
		return null;
	}

	@Override
	public boolean readBoolean() {
		throw new PersistencyException("No value");
	}

	@Override
	public double readDouble() {
		throw new PersistencyException("No value");
	}

	@Override
	public int readInteger() {
		throw new PersistencyException("No value");
	}

	@Override
	public ObjectReader readKey(int index) {
		throw new PersistencyException("No value");
	}

	@Override
	public LocalDateTime readLocalDate() {
		throw new PersistencyException("No value");
	}

	@Override
	public long readLong() {
		throw new PersistencyException("No value");
	}

	@Override
	public Object readObject(Class<?> type) {
		throw new PersistencyException("No value");
	}

	@Override
	public String readString() {
		throw new PersistencyException("No value");
	}

	@Override
	public ObjectReader readValue(int index) {
		throw new PersistencyException("No value");
	}

	public EmptyReader(String location) {
		this.location = Objects.requireNonNull(location);
	}

}
