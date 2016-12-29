package dk.jankjr.janson.annotations;

/**
 * Created by jankjr on 29/12/2016.
 */
public interface EnumSerializer<T> {
  Enum fromJson(Class enumClass, T input);
  T toJson(Enum input);
}
