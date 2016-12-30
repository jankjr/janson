package dk.jankjr.janson;

import dk.jankjr.janson.annotations.*;
import dk.jankjr.janson.writers.Writer;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by jankjr on 23/12/2016.
 */
final class Serialize {
  static private void string(String original, Writer buff) throws IOException {
    buff.append("\"");
    for (int i = 0 ; i < original.length() ;){
      int codePoint = original.codePointAt(i);
      if (codePoint == '\n') {
        buff.append("\\n");
      } else if (codePoint == '\t') {
        buff.append("\\t");
      } else if (codePoint == '\f') {
        buff.append("\\f");
      } else if (codePoint == '\r') {
        buff.append("\\r");
      } else if (codePoint == '\\') {
        buff.append("\\\\");
      } else if(codePoint == '"') {
        buff.append("\\\"");
      } else {
        buff.appendCodePoint(codePoint);
      }
      i += Character.charCount(codePoint);
    }
    buff.append("\"");
  }

  static public Writer toJson(Object obj, Writer writer) throws Exception {
    objectToJson(obj, writer, null);
    return writer;
  }

  private static boolean fieldIsExposed(Field field) {return field.getAnnotation(Hidden.class) == null || field.getAnnotation(Hidden.class).deserization() == Visibility.EXPOSED;}

  private static Writer objectToJson(Object obj, Writer stringBuffer, Field parent) throws Exception {
    stringBuffer.append("{");

    if(obj instanceof Map){
      mapToJson((Map)obj, stringBuffer, parent);
    } else {
      List<Field> fields = new ArrayList<>();
      for(Field f : obj.getClass().getFields()){
        if(fieldIsExposed(f)) {
          fields.add(f);
        }
      }
      if(fields.size() != 0){
        writeKeyValue(fields.get(0).get(obj), stringBuffer, getFieldName(fields.get(0)), fields.get(0));
      }
      for (int i = 1 ; i < fields.size() ; i ++){
        stringBuffer.append(",");
        Field f = fields.get(i);
        writeKeyValue(f.get(obj), stringBuffer, getFieldName(f), fields.get(i));
      }
    }

    stringBuffer.append("}");
    return stringBuffer;
  }

  private static String getFieldName(Field f) {
    if(f.getAnnotation(SerializedName.class) != null){
      return f.getAnnotation(SerializedName.class).value();
    }
    return f.getName();
  }

  private static void mapToJson(Map obj, Writer stringBuffer, Field parent) throws Exception {
    Iterator it = obj.entrySet().iterator();
    while(it.hasNext()){
      Map.Entry ee = (Map.Entry) it.next();

      Object key;
      Object value = obj.get(ee.getKey());

      if(parent != null && parent.getAnnotation(KeyType.class) != null){
        key = parent.getAnnotation(KeyType.class).value().newInstance().toJson(ee.getKey());
      } else {
        key = ee.getKey();
      }

      writeKeyValue(value, stringBuffer, (String)key, null);
      if(!it.hasNext()) {
        break;
      }
      stringBuffer.append(",");
    }
  }



  private static void writeKeyValue(Object val, Writer stringBuffer, String field, Field parent) throws Exception {
    string(field, stringBuffer);
    stringBuffer.append(":");
    valueToJson(val, stringBuffer, parent);
  }

  private static void valueToJson(Object o, Writer stringBuffer, Field parent) throws Exception {
    if(o == null) { stringBuffer.append("null"); }
    else if(o.getClass().isEnum()){
      if(o.getClass().getAnnotation(EnumType.class) != null){
        Object value =
        o.getClass().getAnnotation(EnumType.class).value().newInstance().toJson((Enum) o);

        valueToJson(value, stringBuffer, parent);
      } else {
        string(new DefaultEnumSerializer().toJson((Enum) o), stringBuffer);
      }
    }
    else if(o.getClass().getAnnotation(ValueType.class) != null) {
      ValueType type = o.getClass().getAnnotation(ValueType.class);
      valueToJson(type.value().newInstance().toJson(o), stringBuffer, parent);
    }
    else if(o instanceof Collection) toJsonList((Collection) o, stringBuffer, parent);
    else if(o instanceof String) string((String) o, stringBuffer);
    else if(o instanceof Character) stringBuffer.append((int) (Character) o);
    else if(o instanceof Boolean || o instanceof Number) stringBuffer.append(o.toString());
    else objectToJson(o, stringBuffer, parent);
  }

  private static void toJsonList(Collection oo, Writer stringBuffer, Field parent) throws Exception {
    stringBuffer.append("[");

    Object [] o = oo.toArray();


    if(o.length != 0){
      valueToJson(o[0], stringBuffer, parent);
    }

    for(int i = 1 ; i < o.length ; i++) {
      stringBuffer.append(",");
      valueToJson(o[i], stringBuffer, parent);
    }

    stringBuffer.append("]");
  }
}
