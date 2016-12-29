package dk.jankjr.janson.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by jankjr on 27/12/2016.
 *
 * Keeps a field from being serialized.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Hidden {
  Visibility serialization() default Visibility.HIDDEN;
  Visibility deserization() default Visibility.HIDDEN;
}
