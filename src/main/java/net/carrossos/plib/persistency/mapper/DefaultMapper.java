package net.carrossos.plib.persistency.mapper;

import java.time.LocalDateTime;
import java.util.Objects;

import net.carrossos.plib.persistency.ParamsMap;
import net.carrossos.plib.persistency.PersistencyException;
import net.carrossos.plib.persistency.Reference;
import net.carrossos.plib.persistency.reader.ObjectReader;

public class DefaultMapper implements Mapper {

	protected final ParamsMap parameters;

	private final Mapper root;

	private ObjectReader next;

	@Override
	public void bind(ObjectReader reader) {
		if (reader == this) {
			throw new IllegalArgumentException(this + " cannot bind to itself!");
		}

		if (root == null) {
			next = Objects.requireNonNull(reader);
		} else {
			root.bind(reader);
		}
	}

	@Override
	public int getLength() {
		return next.getLength();
	}

	@Override
	public String getLocation() {
		return next.getLocation();
	}

	@Override
	public Reference getReference() {
		return next.getReference();
	}

	@Override
	public boolean isPresent() {
		return next.isPresent();
	}

	@Override
	public ObjectReader readAttribute(String attribute) {
		return next.readAttribute(attribute);
	}

	@Override
	public boolean readBoolean() {
		return next.readBoolean();
	}

	@Override
	public double readDouble() {
		return next.readDouble();
	}

	@Override
	public int readInteger() {
		return next.readInteger();
	}

	@Override
	public ObjectReader readKey(int index) {
		return next.readKey(index);
	}

	@Override
	public LocalDateTime readLocalDate() {
		return next.readLocalDate();
	}

	@Override
	public long readLong() {
		return next.readLong();
	}

	@Override
	public Object readObject(Class<?> type) {
		return next.readObject(type);
	}

	@Override
	public String readString() {
		return next.readString();
	}

	@Override
	public ObjectReader readValue(int index) {
		return next.readValue(index);
	}

	@Override
	public void unbind() {
		if (root == null) {
			next = null;
		} else {
			root.unbind();
		}
	}

	public DefaultMapper() {
		this.root = null;
		this.next = null;
		this.parameters = new ParamsMap(null);
	}

	public DefaultMapper(Mapper root, ObjectReader next, String... params) {
		this.root = Objects.requireNonNull(root);
		this.next = Objects.requireNonNull(next);
		this.parameters = new ParamsMap(params);
	}

	protected static void checkType(Class<?> ret, Class<?> expect) {
		if (!expect.isAssignableFrom(ret)) {
			throw new PersistencyException("This mapper will return an instance of " + ret.getCanonicalName() + " but "
					+ expect.getCanonicalName() + " is expected!");
		}
	}

}
