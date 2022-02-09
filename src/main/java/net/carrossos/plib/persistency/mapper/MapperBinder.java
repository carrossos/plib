package net.carrossos.plib.persistency.mapper;

// package net.hunk.persistency.mapper;
//
// import java.lang.ref.WeakReference;
// import java.util.Optional;
// import java.util.OptionalInt;
//
// import net.hunk.persistency.ObjectReader;
// import net.hunk.persistency.Reference;
//
// public class MapperBinder implements Mapper {
//
//// private WeakReference<ObjectReader> reader;
//
// private WeakReference<ObjectReader> reader;
//
// public void bindTo(ObjectReader reader) {
// this.reader = new WeakReference<>(reader);
// }
//
// @Override
// public String getLocation() {
// return reader.get().getLocation();
// }
//
// @Override
// public Reference getReference() {
// return reader.get().getReference();
// }
//
// @Override
// public int getLength() {
// return reader.get().getLength();
// }
//
// @Override
// public ObjectReader readAttribute(String attribute) {
// return reader.get().readAttribute(attribute);
// }
//
// @Override
// public ObjectReader readArray(int index) {
// return reader.get().readArray(index);
// }
//
// @Override
// public ObjectReader readMap(int index) {
// return reader.get().readMap(index);
// }
//
// @Override
// public Optional<Boolean> readBoolean() {
// return reader.get().readBoolean();
// }
//
// @Override
// public Optional<String> readString() {
// return reader.get().readString();
// }
//
// @Override
// public OptionalInt readInteger() {
// return reader.get().readInteger();
// }
//
// }
