package dk.jankjr.janson.annotations;

/**
 * Created by jankjr on 27/12/2016.
 *
 *
 */
public interface Serializer<T, S> {
  T fromJson(S src);
  S toJson(T inst);
}
