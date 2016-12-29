package dk.jankjr.janson;

import dk.jankjr.janson.annotations.*;
import dk.jankjr.janson.readers.InputStreamReader;
import dk.jankjr.janson.readers.Reader;
import dk.jankjr.janson.readers.StringReader;

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
    return fromJson(HashMap.class, new StringReader(src));
  }
  public static Map fromJson(InputStream src) {
    return fromJson(HashMap.class, new InputStreamReader(src));
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

        if(thisField.getAnnotation(Hidden.class) != null){
          hidden = thisField.getAnnotation(Hidden.class).deserization() == Visibility.HIDDEN;
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
    if(advanceIfNull(stream)) return null;

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
    } else if (stream.peek() == '{'){
      return parseObject(HashMap.class, stream, null);
    } else if (stream.peek() == '['){
      return parseList(ArrayList.class, stream, null);
    } else if (stream.peek() == '-' || Reader.isDigit(stream)){
      return parseBigFloat(stream);
    } else if ('t' == stream.peek() || 'f' == stream.peek()){
      return parseBoolean(stream);
    } else if ('n' == stream.peek()){
      return parseNull(stream);
    }
    return fail("Invalid json format");
  }

  private static Object parseNull(Reader stream) {
    if(advanceIfNull(stream)) {
      return null;
    }
    return fail("Invalid json format");
  }

  private static boolean advanceIfNull(Reader stream){
    return Reader.advanceIf(stream, 'n') && Reader.advanceIf(stream, 'u') && Reader.advanceIf(stream, 'l') && Reader.advanceIf(stream, 'l');
  }


  private static BigDecimal parseBigFloat(Reader stream) {
    StringBuffer buff = new StringBuffer();
    if(Reader.advanceIf(stream, '-')){
      buff.append("-");
    }
    readInteger(stream, buff);
    if('.' == stream.peek()) {
      buff.append(stream.next());
      readInteger(stream, buff);
    }

    if('e' == stream.peek() && 'E' == stream.peek()) {
      buff.append(stream.next());
      readInteger(stream, buff);
    }
    if('.' == stream.peek()) {
      buff.append(stream.next());
      readInteger(stream, buff);
    }

    return new BigDecimal(buff.toString());
  }

  private static void readInteger(Reader stream, StringBuffer buff) {
    while(Reader.isDigit(stream)) buff.append(stream.next());
  }

  private static BigInteger parseBigInt(Reader stream) {
    StringBuffer buff = new StringBuffer();
    if(Reader.advanceIf(stream, '-')){
      buff.append("-");
    }
    readInteger(stream, buff);
    if('e' == stream.peek() && 'E' == stream.peek()) {
      buff.append(stream.next());
      readInteger(stream, buff);
    }
    return new BigInteger(buff.toString());
  }

  private static int parseInteger(Reader stream) {
    int value = 0;
    while(Reader.isDigit(stream)){
      value = value * 10 + (stream.next() - '0');
    }
    return value;
  }
  private static double parseSign(Reader stream) {
    if(Reader.advanceIf(stream, '-')) return -1.0;
    return 1.0;
  }
  private static double parseFloat(Reader stream) {
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

  private static Boolean parseBoolean(Reader stream) {
    if (Reader.advanceIf(stream, 't')) {
      if(Reader.advanceIf(stream, 'r') && Reader.advanceIf(stream, 'u') && Reader.advanceIf(stream, 'e')) {
        return Boolean.TRUE;
      }
      return fail("Invalid json input");
    }

    if(Reader.advanceIf(stream, 'f') && (Reader.advanceIf(stream, 'a') && Reader.advanceIf(stream, 'l') && Reader.advanceIf(stream, 's') && Reader.advanceIf(stream, 'e'))) {
      return Boolean.FALSE;
    }
    return fail("Invalid json input");
  }

  private static Boolean fail(String message) {
    throw new RuntimeException(message);
  }

  private static String parseString(Reader stream) {
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
          fail("Invalid json format");
        }
      } else {
        buff.appendCodePoint(c);
      }
    }
    return buff.toString();
  }

}
