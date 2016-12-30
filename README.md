# janson
[![Coverage Status](https://coveralls.io/repos/github/jankjr/janson/badge.svg?branch=master)](https://coveralls.io/github/jankjr/janson?branch=master)
[![Build Status](https://travis-ci.org/jankjr/janson.svg?branch=master)](https://travis-ci.org/jankjr/janson)

A minimalistic json serializer/deserializer for java that tries its best to stay out of your way.

Adding as a maven dependency:

First add the repository

```xml
<repositories>
    <repository>
        <id>janson-mvn-repo</id>
        <url>https://raw.github.com/jankjr/janson/mvn-repo/</url>
    </repository>
    ...
```

Then add the package as a dependency

```xml
<dependency>
    <groupId>dk.jankjr</groupId>
    <artifactId>janson</artifactId>
</dependency>
```


## Examples and usage
janson has two main classes, `Serialize` and `Deserialize`, these provide a `fromJson` and `toJson` methods respectively.

A minimalist usage would be:

```java
Map<String, Object> obj = Janson.fromJson("{\"foo\": 42}");

String jsonRepresentation = Janson.toJson(someInstance);
```


This is of course quite boring, and dynamic features in a very static language like Java is always a pain. janson was built with Schema in mind, but that allows for parts of the model to be dynamic.

Lets try a more interesting example:

```java
public class User {
  public String id;
  public int age;
  public String name;
}
```

We can deserialize this from a json string very easily:

```java
User jan = Janson.fromJson(User.class, "{\"id\": \"1234\", \"name\": \"Jan\", \"age\": 42}");
```

Janson does not require any special annotations when parsing data, and by default json is serialized into the class in a 'best fit' type of way. This means that missing fields are ignored, generic types are ignored, and nothing is hidden. This also means that without some guidance from the programmer janson does not do a good job at correctly fitting the data to classes.


### Constraining fields

janson supports some primitives for constraining the fields of the schema.

#### `@NotUndefined`

By default janson will only deserialize the fields currently in the json, this means that missing fields are simply left to their default value.

However, sometimes missings fields should be treated as invalid input, and it is possible to do so using the `@NotUndefined` annotation.

For an example, in our example we could annotate the name field on our User to be required.

```java
public class User {
  ...
  @NotUndefined
  public String name;
}
```

By default a RuntimeException is thrown,however by providing a value to the annotation any class can be used as long as this Exception has a constructor with a String parameter.


#### `@Hidden`

Sometimes fields should never be serialized from outside sources, or ever serialized to json, in this case fields can be marked as `@Hidden`

```java
public class User {
  ...
  @Hidden
  public String secret;
}
```

Exposure can also be controlled more in a more finegrained manner by specifying from which part of the code this field is hidden. By default the field gets marked as ignored by both the serialization and deserialization code.


```java
public class User {
  ...
  @Hidden(serialization=Visibility.EXPOSED, deserialization=Visibility.HIDDEN)
  public String secret;
}
```

#### `@AttributeName`

Lastly sometimes the fields of the input json objects differ from the field names on the Java class:

```java
public class User {
  ...
  @AttributeName("favMusic")
  public String favoriteMusic;
}
```

In this case a json field would serialize to favoriteMusic if and only if it was called `"favMusic"`:

```json
{
  ...
  "favMusic": "...",
}
```

### Collections

janson also supports two types of collections:

  1. array-like collections (Anything that implements the `Collection` interface) 
  2. map-like collections (Anything that implements the `Map` interface)

Defining a field to be a collection is very simple. Just mark the field with a `CollectionType`.

```java
public class Request {  
  @CollectionType(Action.class)
  List<Action> actions;
}
```

Because of how java generics work, it is important to mark fields with the `@CollectionType` annotation to ensure that the json will deserialize to the correct model, without a Class janson will serialize all objects to HashMaps.

You can also use json objects as collections, this allows you to serialize json into typesafe maps. 

```java
public class Request {  
  @CollectionType(Action.class)
  Map<String, Action> actions;
}
```

By default keys are serialized to `String` types, as json fields names can only be strings. But it is possible serialize the to other types from a string, as long as you provide a `KeyType` annotation. `KeyType` takes a `JansonKeySerializer` class. (Which is actually just a specialization of the `Serializer` interface).

Below is an example of how to specify a specific format for keys in a map.

```java
public class Request {  
  class public static StringToInteger implements JansonKeySerializer<Integer> {
    Integer fromJson(String src) { return Integer.parseString(src); }
    String toJson(Integer inst) { return inst.toString(); }
  }

  @KeyType(Request.StringToInteger.class)
  @CollectionType(Action.class)
  Map<Integer, Action> actions;
}
```

janson comes with a few built in key serializers by default, the above could be shortened to:

```java
public class Request {
  @KeyType(IntegerKey.class)
  @CollectionType(Action.class)
  Map<Integer, Action> actions;
}
```

Look in the `dk.jankjr.janson.keytypes` package to see all predefined types.

### ValueTypes

Sometimes strings are not simply strings, nor are numbers simply numbers, but rather some a representation of types from some domain. One example could be a date, a date can be serialize to, and deserialized from a some textual representation into a Date object. 

For this reason janson supports `ValueType`s.

`ValueType` is an annotation that takes two parameters, a `value` parameter, a class that implements the `Serializer` interface.

```java
class AWSCognitoIdSerializer implements Serializer<AWSCognitoId, String> {
  AWSCognitoId fromJson(String src) { return new AWSCognitoId(src); }
  String toJson(AWSCognitoId inst) { return inst.toJson(); }
}

@ValueType(AWSCognitoIdSerializer.class)
public class AWSCognitoId {  
  

  public final Region region;
  public final String id;

  public AWSCognitoId(String src){
    region = Region.valueOf(src.split(":")[0]);
    id = src.split(":")[1];
  }

  public String toJson(){
    return region.toString() + ":" + id;
  }
}
```

As you can see, using this feature you can add an additional control over how objects are serialized.

Keep in mind that janson will deserialize numbers to best fit, which means that numbers are deserialized to `BigDecimal` types and maps are serialized to `HashMap`s. This might not always be the intended behavior, therefore it is possible to specify the type of the values that ValueTypes are serialized from by setting the `from` parameter of the `ValueType`.

```java
@ValueType(value = SomeFromFloatValueDeserializer.class, from = Float.class)
... 
```

As you can see, you can actually perform some very interesting mapping between json and java classes using this feature.

### Enums

Enums are a useful feature in Java, and janson will happily map json strings directly to Enum instances.

The default behavior will map enums directly by name using the built in `valueOf` method on Enums. This means that an enum definition:

```java
public enum AnimalType {
  CAT, DOG, BIRD
}
```

Would only accept the json strings "CAT", "DOG", "BIRD". If an enum could not be initialized, that is, the json contains the string `"PENGUIN"` the janson will throw an exception. This behavior can be overwritten or extended by creating a custom `EnumSerializer`, and marking the enum class with `EnumType`.

Sometimes it is usefull to have a more domain specific mapping, like mapping a integer to an enum instance, by marking the class with the `@EnumType(...)` annotation.

```java
@EnumType(value = OneTwoMany.Serialization.class, from = Integer.class)
public enum OneTwoMany {
  NONE, ONE, TWO, MANY;

  public static class Serialization implements EnumSerializer<Integer> {
    @Override
    public Enum fromJson(Class enumClass, Integer input) {
      return input <= 0 ? NONE : input == 1 ? ONE : input == 2 ? TWO : MANY;
    }
    @Override
    public Integer toJson(Enum input) {
      return input == NONE ? 0 : input == ONE ? 1 : INPUT == TWO : 2 ? 3;
    }
  }
}
```

Note: like `ValueType`, the `from` parameter will make the `Janson.fromJson` use the closest fits the source. Which for numbers would be a `BigDecimal`, and maps to `HashMap`, this behavior mirrors how the `ValueType`.