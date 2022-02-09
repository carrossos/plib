package net.carrossos.plib.persistency.config;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Repeatable(PersistentObjects.class)
@Retention(RUNTIME)
@Target(TYPE)
public @interface PersistentObject {

	public int options() default 0;

	public String[] parameters() default {};

	public String source() default "";

	public String value() default "";
}
