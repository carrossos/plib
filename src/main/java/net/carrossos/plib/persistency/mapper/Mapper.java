package net.carrossos.plib.persistency.mapper;

import java.util.function.Function;

import net.carrossos.plib.persistency.reader.ObjectReader;

public interface Mapper extends ObjectReader {
	public void bind(ObjectReader reader);

	public default <T> T bindAndRetrieve(ObjectReader reader, Function<ObjectReader, T> function) {
		bind(reader);

		try {
			return function.apply(this);
		} finally {
			unbind();
		}
	}

	public void unbind();

	// public default <T> T bindAndRetrieve(ObjectReader reader,
	// Function<ObjectReader, T> function, String attribute) {
	// bind(reader);
	//
	// try {
	// readAttribute(attribute);
	// } finally {
	// unbind();
	// }
	// }
}
