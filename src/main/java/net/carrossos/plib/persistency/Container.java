package net.carrossos.plib.persistency;

import java.io.Closeable;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import net.carrossos.plib.persistency.binding.ObjectBinding;
import net.carrossos.plib.persistency.reader.ObjectReader;

public abstract class Container implements Closeable {

	private final Persistency persistency;

	private final Container parent;

	private final ParamsMap parameters;

	private final String name;

	@SuppressWarnings("unchecked")
	protected <T> Stream<T> mapToInstance(Stream<? extends ObjectReader> stream, Context context, Class<T> type,
			Consumer<Throwable> errorHandler) {
		ObjectBinding binding = persistency.getBinding(type);

		return stream.map(r -> {
			try {
				return (T) binding.read(context, r, null);
			} catch (PersistencyException e) {
				errorHandler.accept(e);
				return null;
			}
		}).filter(Objects::nonNull);
	}

	public String getName() {
		return name;
	}

	public ParamsMap getParameters() {
		return parameters;
	}

	// public abstract Object readReference(Reference reference);

	public Container getParent() {
		return parent;
	}

	public Persistency getPersistency() {
		return persistency;
	}

	public abstract <T> Stream<T> read(Context context, Class<T> type, Consumer<Throwable> errorHandler);

	public abstract <T> T readObject(Context context, Class<T> type, ObjectReader reference);

	public Container(Persistency persistency, Container parent, ParamsMap parameters, String name) {
		this.persistency = persistency;
		this.parent = parent;
		this.parameters = parameters;
		this.name = name;
	}

}
