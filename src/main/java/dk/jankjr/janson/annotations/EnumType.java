package dk.jankjr.janson.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by jankjr on 29/12/2016.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnumType {
  Class<? extends EnumSerializer> value() default DefaultEnumSerializer.class;
  Class from() default Object.class;
}
