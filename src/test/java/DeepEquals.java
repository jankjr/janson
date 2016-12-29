import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Created by jankjr on 29/12/2016.
 */
public class DeepEquals {
  public static Set<Class<?>> primitives = new HashSet<>();
  static {
    DeepEquals.primitives.add(boolean.class);
    DeepEquals.primitives.add(Boolean.class);
    DeepEquals.primitives.add(byte.class);
    DeepEquals.primitives.add(Byte.class);
    DeepEquals.primitives.add(char.class);
    DeepEquals.primitives.add(Character.class);
    DeepEquals.primitives.add(short.class);
    DeepEquals.primitives.add(Short.class);
    DeepEquals.primitives.add(int.class);
    DeepEquals.primitives.add(Integer.class);
    DeepEquals.primitives.add(long.class);
    DeepEquals.primitives.add(Long.class);
    DeepEquals.primitives.add(float.class);
    DeepEquals.primitives.add(Float.class);
    DeepEquals.primitives.add(double.class);
    DeepEquals.primitives.add(Double.class);
    DeepEquals.primitives.add(BigInteger.class);
    DeepEquals.primitives.add(BigDecimal.class);
    DeepEquals.primitives.add(String.class);
    DeepEquals.primitives.add(Enum.class);
  }
  public static boolean deepEquals(Object a, Object b) throws IllegalAccessException, NoSuchFieldException {
    Class cls1 = a.getClass();
    Class cls2 = b.getClass();


    if(a instanceof List){
      return deepEqualsList((List) a, (List) b);
    }
    if(a instanceof Collection){
      return deepEqualsCollection((Collection) a, (Collection) b);
    }
    if(a instanceof Map){
      return deepEqualsMap((Map) a, (Map) b);
    }
    if(cls1.isEnum()) {
      return a == b;
    }

    if(DeepEquals.primitives.contains(cls1)){
      return a.equals(b);
    }

    if(!cls1.equals(cls2)) {
      return false;
    }

    for(Field f : cls1.getFields()){
      if(!deepEquals(f.get(a), f.get(b))) {
        return false;
      }
    }
    return true;
  }

  private static boolean deepEqualsList(List a, List b) throws NoSuchFieldException, IllegalAccessException {
    for(int i = 0 ; i < a.size() ; i ++){
      if(!deepEquals(a.get(i), b.get(i))) {
        return false;
      }
    }
    return true;
  }

  private static boolean deepEqualsMap(Map a, Map b) throws NoSuchFieldException, IllegalAccessException {
    if(a.size() != b.size()) {
      return false;
    }
    for(Object e : a.entrySet()){
      Map.Entry ee = (Map.Entry) e;
      if(!b.containsKey(ee.getKey())) {
        return false;
      }

      if(!deepEquals(b.get(ee.getKey()), ee.getValue())) {
        return false;
      }
    }
    return true;
  }

  private static boolean deepEqualsCollection(Collection a, Collection b) throws NoSuchFieldException, IllegalAccessException {
done:
    for(Object o1 : a){
      for(Object o2 : b){
        if(deepEquals(o1, o2)) {
          continue done;
        }
      }
      return false;
    }
    return true;
  }
}
