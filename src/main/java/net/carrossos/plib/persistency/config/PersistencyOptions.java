package net.carrossos.plib.persistency.config;

public interface PersistencyOptions {

	public static final int OPTIONAL = 1;

	public static final int PRIMARY = 2;

	public static final int ALTERNATE = 4;

	public static final int REFERENCE = 8;

	public static final int OBJECT = 16;
}
