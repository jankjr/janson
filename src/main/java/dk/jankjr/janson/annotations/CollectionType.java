package dk.jankjr.janson.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by jankjr on 23/12/2016.
 *
 * Used to mark the type of a collection object.
 * This is used to model either lists or maps, of classes.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CollectionType {
  Class value();
}
