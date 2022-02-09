package net.carrossos.plib.persistency.config;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Repeatable(PersistentCollections.class)
@Retention(RUNTIME)
@Target(FIELD)
public @interface PersistentCollection {

	public MapperConfig[] keyMappers() default {};

	public Class<?> keyType() default Void.class;

	public MapperConfig[] mappers() default {};

	public int options() default 0;

	public String source() default "";

	public MapperConfig[] valueMappers() default {};

	public Class<?> valueType() default Void.class;
}
