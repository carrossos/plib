package net.carrossos.plib.persistency.config;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Repeatable(PersistentFields.class)
@Retention(RUNTIME)
@Target(FIELD)
public @interface PersistentField {

	public MapperConfig[] mappers() default {};

	public int options() default 0;

	public String source() default "";

	public Class<?> type() default Void.class;

	public String value() default "";

	public MapperConfig[] valueMappers() default {};
}
