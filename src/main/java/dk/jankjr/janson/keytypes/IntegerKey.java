package dk.jankjr.janson.keytypes;

import dk.jankjr.janson.annotations.JansonKeySerializer;

/**
 * Created by jankjr on 29/12/2016.
 */
public class IntegerKey extends JansonKeySerializer<Integer> {
  @Override
  public Integer fromJson(String src) {
    return Integer.parseInt(src);
  }
  @Override
  public String toJson(Integer inst) {
    return Integer.toString(inst);
  }
}
