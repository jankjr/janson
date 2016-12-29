import dk.jankjr.janson.Deserialize;
import dk.jankjr.janson.Serialize;
import dk.jankjr.janson.annotations.*;
import dk.jankjr.janson.keytypes.IntegerKey;
import dk.jankjr.janson.writers.OutputStreamWriter;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by jankjr on 23/12/2016.
 */
public class Tests {

  public static class Test1 {
    public long a1 = 21L;
    public Long a2 = 42L;
    public char b1 = 'a';
    public Character b2 = 'b';
  }

  public static class Test2 {
    public Test1 a1 = new Test1();
    public Test1 a2 = new Test1();
  }

  public static class Test3 {
    @CollectionType(Test1.class)
    public List<Test1> entries = Arrays.asList(new Test1(), new Test1());
  }

  public static class Test4 {
    @CollectionType(Test1.class)
    public Map<String, Test1> entries = new HashMap<>();
    public Test4(){
      entries.put("foo", new Test1());
      entries.put("bar", new Test1());
    }
  }

  public static class Test5 {
    @KeyType(IntegerKey.class)
    @CollectionType(Test1.class)
    public Map<Integer, Test1> entries = new HashMap<>();

    public Test5(){
      entries.put(1, new Test1());
      entries.put(2, new Test1());
    }
  }

  public static class Test6 {
    public Visibility order1 = Visibility.EXPOSED;
    public Visibility order2 = Visibility.HIDDEN;
  }
  public static class Test7 {
    public Boolean trueish = true;
    public Boolean falseish = false;
    public boolean trueish1 = true;
    public boolean falseish1 = false;
  }

  public static class Test8 {
    @NotUndefined
    public String data = null;
  }


  @ValueType(ID.IDSerializer.class)
  public static class ID {
    public String region = "eu", id = "abc";
    public static class IDSerializer implements Serializer<ID, String> {
      @Override
      public ID fromJson(String src) {
        return ID.fromString(src);
      }

      @Override
      public String toJson(ID inst) {
        return inst.toString();
      }
    }

    private static ID fromString(String src) {
      String [] parts = src.split(":");
      ID id = new ID();
      id.region = parts[0];
      id.id = parts[1];
      return id;
    }
    public String toString(){
      return region + ":" + id;
    }
  }

  public static class Test9 {
    public ID id = new ID();
  }

  public static class Test10 {
    @CollectionType(String.class)
    public Map<String, String> m = new HashMap<>();

    public Test10(){
      m.put("\n\f\t\rå\\\"", "abc");
    }
  }

  @EnumType(value = Test11.Serialization.class, from = Integer.class)
  public static enum Test11 {
    EVEN, ODD;
    public static class Serialization implements EnumSerializer<Integer> {
      @Override
      public Enum fromJson(Class enumClass, Integer input) {
        return input % 2 == 0 ? Test11.EVEN : Test11.ODD;
      }
      @Override
      public Integer toJson(Enum input) {
        return input == EVEN ? 2 : 1;
      }
    }
  }
  public static class Test12 {
    public Test11 foo = Test11.EVEN;
    public Test11 bar = Test11.ODD;
  }
  public static class Test13 {
    public BigInteger ints = new BigInteger("10000000000000000000000");
  }
  public static class Test14 {
    @SerializedName("foo")
    public long bar = 42;
  }

  public static enum Test15Enum {
    FOO
  }
  public static class Test15 {
    public Test15Enum foo;
  }
  public static class Test16 {
    public Test15Enum foo = Test15Enum.FOO;
  }

  public static void textualTest(Class cls) {
    Object before = null;
    try {
      before = cls.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    String jsonBefore = Serialize.toJson(before);
    Object after = Deserialize.fromJson(cls, jsonBefore);
    try {
      assertThat("They are equals", DeepEquals.deepEquals(before, after));
    } catch (IllegalAccessException | NoSuchFieldException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Test
  public void canSerializePrimitives(){
    textualTest(Test1.class);
  }

  @Test
  public void canSerializeNestedStructures(){
    textualTest(Test2.class);
  }

  @Test
  public void handlesCollections(){
    textualTest(Test3.class);
  }

  @Test
  public void handlesMaps(){
    textualTest(Test4.class);
  }

  @Test
  public void handlesDifferentKeyTypes(){
    textualTest(Test5.class);
  }

  @Test
  public void handlesEnums(){
    textualTest(Test6.class);
  }

  @Test
  public void handlesBooleans(){
    textualTest(Test7.class);
  }

  @Test(expected = RuntimeException.class)
  public void handlesMissing(){
    textualTest(Test8.class);
  }


  @Test
  public void handlesutf8(){
    String src = "{\"p\": \"\\u0070\"}";
    Map<String, Object> o = Deserialize.fromJson(src);
    assertThat(o.get("p"), org.hamcrest.Matchers.equalTo("p"));
  }

  @Test
  public void handlesIds(){
    textualTest(Test9.class);
  }

  @Test
  public void handlesEscapeCharacters(){
    textualTest(Test10.class);
  }

  @Test
  public void handlesu32(){
    Map m = new HashMap<>();
    m.put("您好", "您好");

    String src = Serialize.toJson(m);
    Map m1 = Deserialize.fromJson(src);

    assertThat(m1.get("您好"), org.hamcrest.Matchers.equalTo("您好"));
  }

  @Test
  public void handleEnumTypes(){
    textualTest(Test12.class);
  }

  @Test
  public void handlesdigits(){
    Map m = new HashMap<>();
    m.put("a", 10.0f);

    String src = Serialize.toJson(m);
    Map m1 = Deserialize.fromJson(src);

    assertThat("", ((BigDecimal) m1.get("a")).floatValue() == 10.0f);
  }


  @Test
  public void handlesBigNumbers(){
    textualTest(Test13.class);
  }

  @Test
  public void handlesSerializedNames(){
    textualTest(Test14.class);
  }


  @Test
  public void inputStreamWorks() {
    String src = "{\"x\": 42}";
    InputStream is = new ByteArrayInputStream( src.getBytes() );
    Map obj = Deserialize.fromJson(is);
    assertThat("x is 42", obj.get("x").equals(new BigDecimal(42)));
  }

  @Test
  public void outputStreamWorks() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    OutputStreamWriter writer = new OutputStreamWriter(out);
    Serialize.toJson(new Test1(), writer);
    writer.flush();


    String json = new String(out.toByteArray());
    Test1 after = Deserialize.fromJson(Test1.class, json);


    assertThat("a1 is 21", after.a1 == 21);
  }


  @Test(expected = RuntimeException.class)
  public void badEnum() {
    String src = "{\"foo\": \"BAR\"}";
    Deserialize.fromJson(Test15.class, src);
  }

  @Test
  public void standardEnum() {
    Test16 test16 = new Test16();
    String src = Serialize.toJson(test16);

    Test16 test162 = Deserialize.fromJson(Test16.class, src);

    assertThat("equals", test16.foo == test162.foo);
  }
}
