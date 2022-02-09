package net.carrossos.plib.persistency;

public interface Context {

	public Object readReference(Class<?> type, Reference reference);

	public void saveReference(Class<?> type, Reference reference, Object instance);

	// public <T> void saveAlternateReference(Class<T> type, Reference reference, T
	// instance);
}
