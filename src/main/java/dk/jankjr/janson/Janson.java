package dk.jankjr.janson;

import dk.jankjr.janson.readers.InputStreamReader;
import dk.jankjr.janson.readers.Reader;
import dk.jankjr.janson.readers.StringReader;
import dk.jankjr.janson.writers.StringBufferWriter;
import dk.jankjr.janson.writers.Writer;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jankjr on 30/12/2016.
 */
public class Janson {
  public static <T> T fromJson(Class<T> cls, Reader stream) {
    return Deserialize.fromJson(cls, stream);
  }

  public static <T> T fromJson(Class<T> cls, String src) {
    return Deserialize.fromJson(cls, new StringReader(src));
  }

  public static <T> T fromJson(Class<T> cls, InputStream src) {
    return Deserialize.fromJson(cls, new InputStreamReader(src));
  }

  public static Map fromJson(Reader stream) {
    return Deserialize.fromJson(HashMap.class, stream);
  }

  public static Map fromJson(String src) {
    return fromJson(new StringReader(src));
  }

  public static Map fromJson(InputStream src) {
    return fromJson(new InputStreamReader(src));
  }

  static public String toJson(Object obj) throws Exception {
    StringBufferWriter writer = new StringBufferWriter();
    Serialize.toJson(obj, writer);
    return writer.getBuffer().toString();
  }

  static public Writer toJson(Object obj, Writer writer) throws Exception {
    return Serialize.toJson(obj, writer);
  }
}
