package dk.jankjr.janson.annotations;

/**
 * Created by jankjr on 29/12/2016.
 */
public class DefaultEnumSerializer implements EnumSerializer<String> {
  @Override
  public Enum fromJson(Class aClass, String input) {
    for (Object e : aClass.getEnumConstants()) {
      if(((Enum)e).name().equals(input)){
        return (Enum) e;
      }
    }
    throw new RuntimeException(input + " is not in domain of " + aClass.getName());
  }

  @Override
  public String toJson(Enum input) {
    return input.name();
  }

}
