package net.carrossos.plib.persistency;

import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

public interface ValueReader {

	public boolean readBoolean(Consumer<? super Boolean> consumer);

	public boolean readBuffer(Consumer<? super byte[]> consumer);

	public boolean readDouble(DoubleConsumer consumer);

	public boolean readInteger(IntConsumer consumer);

	public boolean readLocaleDate(Consumer<? super LocalDateTime> consumer);

	public boolean readLong(LongConsumer consumer);

	public boolean readObject(Class<?> type, Consumer<Object> consumer);

	public boolean readReference(Consumer<Reference> consumer);

	public boolean readString(Consumer<? super String> consumer);
}
