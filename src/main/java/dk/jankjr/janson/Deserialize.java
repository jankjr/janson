package dk.jankjr.janson;

import dk.jankjr.janson.annotations.*;
import dk.jankjr.janson.readers.InputStreamReader;
import dk.jankjr.janson.readers.Reader;
import dk.jankjr.janson.readers.StringReader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Created by jankjr on 23/12/2016.
 */
final public class Deserialize {
  public static <T> T fromJson(Class<T> cls, Reader stream) {
    try {
      return parseObject(cls, stream, null);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
  public static <T> T fromJson(Class<T> cls, String src) {
    return fromJson(cls, new StringReader(src));
  }
  public static <T> T fromJson(Class<T> cls, InputStream src) {
    return fromJson(cls, new InputStreamReader(src));
  }
  public static Map fromJson(Reader stream) {
    return fromJson(HashMap.class, stream);
  }
  public static Map fromJson(String src) {
    return fromJson(new StringReader(src));
  }
  public static Map fromJson(InputStream src) {
    return fromJson(new InputStreamReader(src));
  }

  private static <T> T parseObject(Class<T> cls, Reader stream, Field parentField) throws Exception {
    T inst = cls.newInstance();

    Reader.skipWhiteSpace(stream);
    Reader.failIfNot(stream, '{');
    Reader.skipWhiteSpace(stream);

    while(parseKeyValuePair(cls, stream, inst, parentField)){
      Reader.skipWhiteSpace(stream);
      if(!Reader.advanceIf(stream, ',')){
        break;
      }
      Reader.skipWhiteSpace(stream);
    }
    for(Field f : cls.getFields()){
      NotUndefined req = f.getAnnotation(NotUndefined.class);
      if(req != null) {
        if(f.get(inst) == null){
          throw req.value().getConstructor(String.class).newInstance(getMissingFieldMsg(f));
        }
      }
    }

    Reader.failIfNot(stream, '}');
    return inst;
  }

  private static String getMissingFieldMsg(Field f) {return "Missing field required field " + f.getName();}


  private static <T> boolean parseKeyValuePair(Class<T> cls, Reader stream, T inst, Field parentField) throws Exception {
    if(stream.peek() != '"') {
      return false;
    }
    String fieldName = parseString(stream);
    for(Field f : cls.getFields()){
      if(hasSerializedName(fieldName, f)){
        fieldName = f.getName();
        break;
      }
    }

    Reader.skipWhiteSpace(stream);
    Reader.failIfNot(stream, ':');
    Reader.skipWhiteSpace(stream);

    Class fieldType = Object.class;
    if(parentField != null && parentField.getAnnotation(CollectionType.class) != null){
      fieldType = parentField.getAnnotation(CollectionType.class).value();
    }
    Field thisField = null;

    boolean hidden = false;

    try {
      if(!doesImplement(cls, Map.class)){
        thisField = cls.getField(fieldName);

        Hidden hiddenAnnotation = thisField.getAnnotation(Hidden.class);
        if(hiddenAnnotation != null){
          hidden = hiddenAnnotation.deserization() == Visibility.HIDDEN;
        }

        fieldType = thisField.getType();
        if(fieldType.isInterface() && doesImplement(fieldType, Collection.class)){
          fieldType = ArrayList.class;
        }else if(fieldType.isInterface() && doesImplement(fieldType, Map.class)){
          fieldType = HashMap.class;
        }
      }
    } catch (NoSuchFieldException e) {}

    Object fieldValue = parseValue(fieldType, stream, thisField);

    if(!hidden){
      if(parentField != null && parentField.getAnnotation(KeyType.class) != null) {
        KeyType keyType = parentField.getAnnotation(KeyType.class);
        setValue(cls, inst, keyType.value().newInstance().fromJson(fieldName), fieldValue);
      } else {
        setValue(cls, inst, fieldName, fieldValue);
      }
    }

    Reader.skipWhiteSpace(stream);
    return true;
  }

  private static boolean hasSerializedName(String fieldName, Field f) {
    return f.getAnnotation(SerializedName.class) != null &&
       f.getAnnotation(SerializedName.class).value().equals(fieldName);
  }

  private static <T> void setValue(Class<T> cls, T inst, Object fieldName, Object fieldValue) {
    if(inst instanceof Map) {
      ((Map)inst).put(fieldName, fieldValue);
    } else {
      try {
        cls.getField((String)fieldName).set(inst, fieldValue);
      } catch (NoSuchFieldException | IllegalAccessException e) {}
    }
  }

  private static boolean doesImplement(Class cls, Class type){
    if(cls == type){
      return true;
    }
    for(Class in : cls.getInterfaces()){
      if(in == type){
        return true;
      }
    }
    return false;
  }


  private static Object parseValue(Class<?> aClass, Reader stream, Field parent) throws Exception {
    if(parseString(stream, "null")) {
      return null;
    }

    ValueType valTypeAnnotation = aClass.getAnnotation(ValueType.class);

    if(valTypeAnnotation != null){
      Object value = parseValue(valTypeAnnotation.from(), stream, null);
      return valTypeAnnotation.value().newInstance().fromJson(value);
    }
    if (aClass.isEnum()){
      EnumType enumType = aClass.getAnnotation(EnumType.class);
      if(enumType != null){
        Object value = parseValue(enumType.from(), stream, null);
        return enumType.value().newInstance().fromJson(aClass, value);
      } else {
        Object value = parseValue(Object.class, stream, null);
        return new DefaultEnumSerializer().fromJson(aClass, (String) value);
      }
    } else if(aClass == Object.class){
      return parseValueClassLess(stream);
    } else if (aClass == Boolean.class || aClass == boolean.class){
      return parseBoolean(stream);
    } else if (aClass == Byte.class || aClass == byte.class){
      return (byte)parseFloat(stream);
    } else if (aClass == Short.class || aClass == short.class){
      return (short)parseFloat(stream);
    } else if (aClass == Character.class || aClass == char.class){
      return (char)parseFloat(stream);
    } else if (aClass == Integer.class || aClass == int.class){
      return (int)parseFloat(stream);
    } else if (aClass == Long.class || aClass == long.class){
      return (long)parseFloat(stream);
    } else if (aClass == Float.class || aClass == float.class){
      return (float)parseFloat(stream);
    } else if (aClass == Double.class || aClass == double.class){
      return parseFloat(stream);
    } else if (aClass == BigInteger.class){
      return parseBigInt(stream);
    } else if (aClass == BigDecimal.class){
      return parseBigFloat(stream);
    } else if (aClass == String.class) {
      return parseString(stream);
    } else if (doesImplement(aClass, List.class)){
      return parseList(aClass, stream, parent);
    } else if (doesImplement(aClass, Map.class)){
      return parseObject(aClass, stream, parent);
    }
    return parseObject(aClass, stream, parent);
  }

  private static List parseList(Class<?> aClass, Reader stream, Field field) throws Exception {
    Class<?> colType = Object.class;

    if(field != null && field.getAnnotation(CollectionType.class) != null){
      colType = field.getAnnotation(CollectionType.class).value();
    }

    List inst = (List) aClass.newInstance();
    Reader.failIfNot(stream, '[');
    Reader.skipWhiteSpace(stream);
    if(Reader.advanceIf(stream, ']')) {
      return inst;
    }

    while(true){
      Object o = parseValue(colType, stream, null);
      inst.add(o);
      Reader.skipWhiteSpace(stream);
      if(!Reader.advanceIf(stream, ',')){
        break;
      }
      Reader.skipWhiteSpace(stream);
    }
    Reader.failIfNot(stream, ']');
    return inst;
  }


  private static Object parseValueClassLess(Reader stream) throws Exception {
    if(stream.peek() == '"'){
      return parseString(stream);
    }

    if (stream.peek() == '{'){
      return parseObject(HashMap.class, stream, null);
    }

    if (stream.peek() == '['){
      return parseList(ArrayList.class, stream, null);
    }

    if (stream.peek() == '-' || Reader.isDigit(stream)){
      return parseBigFloat(stream);
    }

    if ('t' == stream.peek() || 'f' == stream.peek()){
      return parseBoolean(stream);
    } else {
      throw new RuntimeException("Invalid json format");
    }
  }


  private static BigDecimal parseBigFloat(Reader stream) throws IOException {
    StringBuffer buff = new StringBuffer();
    readSignedInt(stream, buff);
    if('.' == stream.peek()) {
      buff.append(stream.next());
      readInteger(stream, buff);
    }

    readExp(stream, buff);

    return new BigDecimal(buff.toString());
  }

  private static void readExp(Reader stream, StringBuffer buff) throws IOException {
    if('e' == stream.peek() || 'E' == stream.peek()) {
      buff.append(stream.next());
      readSignedInt(stream, buff);
    }
  }

  private static void readSignedInt(Reader stream, StringBuffer buff) throws IOException {
    if(stream.peek() == '-' || stream.peek() == '+'){
      buff.append(stream.next());
    }
    readInteger(stream, buff);
  }

  private static void readInteger(Reader stream, StringBuffer buff) throws IOException {
    while(Reader.isDigit(stream)) buff.append(stream.next());
  }

  private static BigInteger parseBigInt(Reader stream) throws IOException {
    StringBuffer buff = new StringBuffer();
    readSignedInt(stream, buff);
    readExp(stream, buff);
    return new BigInteger(buff.toString());
  }

  private static int parseInteger(Reader stream) throws IOException {
    int value = 0;
    while(Reader.isDigit(stream)){
      value = value * 10 + (stream.next() - '0');
    }
    return value;
  }
  private static double parseSign(Reader stream) throws IOException {
    if(Reader.advanceIf(stream, '-')) return -1.0;
    return 1.0;
  }
  private static double parseFloat(Reader stream) throws IOException {
    double sign = parseSign(stream);
    double total = parseInteger(stream);
    if(Reader.advanceIf(stream, '.')){
      double fractional = parseInteger(stream);
      if(fractional != 0)
        total = total + fractional / Math.pow(10, Math.floor(Math.log10(fractional))) / 10.0;
    }
    total *= sign;
    if(Reader.advanceIf(stream, 'e') || Reader.advanceIf(stream, 'E')){
      double expSign = parseSign(stream);
      double exp = parseInteger(stream);
      total = total * Math.pow(10, expSign * exp);
    }
    return total;
  }

  private static boolean parseString(Reader stream, String str) throws IOException {
    if (!Reader.advanceIf(stream, str.charAt(0))){
      return false;
    }
    for(int i = 1 ; i < str.length() ; i ++){
      if(!Reader.advanceIf(stream, str.charAt(i))) {
        throw new RuntimeException("Invalid Json Input");
      }
    }
    return true;
  }

  private static Boolean parseBoolean(Reader stream) throws IOException {
    return parseString(stream, "true") || !parseString(stream, "false");
  }

  private static String parseString(Reader stream) throws IOException {
    Reader.failIfNot(stream, '"');
    StringBuffer buff = new StringBuffer();
    while(true){
      int c = stream.nextCodepoint();
      if(c == '"') { break; }
      if(c == '\\') {
        c = stream.next();
        if(c == '\\') buff.append("\\");
        else if(c == 'n') buff.append("\n");
        else if(c == 't') buff.append("\t");
        else if(c == 'r') buff.append("\r");
        else if(c == 'f') buff.append("\f");
        else if(c == 'b') buff.append("\b");
        else if(c == '/') buff.append("/");
        else if(c == '"') buff.append("\"");
        else if(c == 'u') {
          int c1 = Reader.readHex(stream);
          int c2 = Reader.readHex(stream);
          int c3 = Reader.readHex(stream);
          int c4 = Reader.readHex(stream);

          char codePoint = (char) ((c1 << 12) | (c2 << 8) | (c3 << 4) | c4);
          buff.append(codePoint);
        } else {
          throw new RuntimeException("Invalid json format");
        }
      } else {
        buff.appendCodePoint(c);
      }
    }
    return buff.toString();
  }

}
