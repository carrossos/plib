package net.carrossos.plib.persistency.config;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import net.carrossos.plib.persistency.mapper.DefaultMapper;

@Retention(RUNTIME)
@Target(FIELD)
public @interface MapperConfig {
	public Class<? extends DefaultMapper> mapper() default DefaultMapper.class;

	public String[] value() default {};
}
