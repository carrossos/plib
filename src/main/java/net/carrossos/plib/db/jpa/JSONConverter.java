package net.carrossos.plib.db.jpa;

import java.util.List;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import net.carrossos.plib.utils.NonNull;

@Converter
public abstract class JSONConverter<T> implements AttributeConverter<T, String> {

	private static final List<com.fasterxml.jackson.databind.Module> MODULES;

	private final ObjectMapper mapper = new ObjectMapper();

	protected void configure(ObjectMapper mapper) {
		mapper.registerModules(MODULES);
		mapper.disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS);
	}

	protected abstract TypeReference<T> getType();

	@Override
	public String convertToDatabaseColumn(T value) {
		try {
			return mapper.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Failed to serialize to JSON: " + value, e);
		}
	}

	@Override
	public T convertToEntityAttribute(String value) {
		try {
			return NonNull.tryMapThrowing(value, v -> mapper.readValue(v, getType()));
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Failed to unserialize from JSON: " + value, e);
		}
	}

	public JSONConverter() {
		super();

		configure(mapper);
	}

	static {
		MODULES = ObjectMapper.findModules(JSONConverter.class.getClassLoader());
	}
}