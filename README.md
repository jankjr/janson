# janson
[![Coverage Status](https://coveralls.io/repos/github/jankjr/janson/badge.svg?branch=master)](https://coveralls.io/github/jankjr/janson?branch=master)

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
    <version>1.0.0</version>
</dependency>
```


## Examples and usage
janson has two main classes, `Serialize` and `Deserialize`, these provide a `fromJson` and `toJson` methods respectively.

A minimalist usage would be:

```java
Map<String, Object> obj = Deserialize.fromJson("{\"foo\": 42}");

String jsonRepresentation = Serialize.toJson(someInstance);
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
User jan = Deserialize.fromJson(User.class, "{\"id\": \"1234\", \"name\": \"Jan\", \"age\": 42}");
```

Janson does not require any special annotations when parsing data, and by default json is serialized into the class in a 'best fit' type of way. This means that missing fields are ignored, generic types are ignored, and nothing is hidden. This also means that without some guidance from the programmer janson does not do a good job at correctly fitting the data to classes.


### Constraining fields

janson supports some primitives for constraining the fields of the schema.

#### @NotUndefined

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


#### @Hidden

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

#### @AttributeName

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

This is very important, because without a schema janson will serialize all objects to HashMaps.

You can also use json objects themselves as dynamic maps instead of classes. And these also support the CollectionType to correction deserialize classes. 

```java
public class Request {  
  @CollectionType(Action.class)
  Map<String, Action> actions;
}
```

By default, the keys can be used directly as strings, it is also possible to use other keytypes, as long as you provide a `KeyType` annotation. `KeyType` requires class that extends the `JansonKeySerializer` interface as input.

Below is an example of how to specify a specific format for keys in a map.

```java
public class Request {  
  class public static StringToInteger implements JansonKeySerializer<Integer> {
    Integer fromJson(String src) { return Integer.parseString(src); }
    String toJson(Integer inst) { return Integer.toString(inst); }
  }

  @KeyType(StringToInteger.class)
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

Look in the `dk.jankjr.janson.keytypes` package to see the predefined types.

### ValueTypes

Sometimes strings are not simply strings nor numbers simply numbers, and custom serialization logic is often needed.

For this reason janson supports ValueTypes.

ValueTypes are classes marked with the `ValueType` annotation. It, like KeyType, takes a JansonSerializer class as an argument.

```java
class AWSCognitoIdSerializer implements JansonSerializer<AWSCognitoId, String> {
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

Keep in mind that janson will deserialize numbers to best fit, which means that numbers are deserialized to `BigDecimal` types. This might not always be the intended behavior, therefore it is possible to specify the type of the values that ValueTypes are serialized from by setting the `from` class.

```java
@ValueType(value = SomeFromFloatValueDeserializer.class, from = Float.class)
... 
```

### Enums

Enums are a useful feature in Java, and janson will happily map json strings directly to Enum instances.

The default behavior will map enums directly by name. This means that an enum definition:

```java
public enum AnimalType {
  CAT, DOG, BIRD
}
```

Would only accept the json strings "CAT", "DOG", "BIRD". If none of the 

Sometimes it is usefull to have a more domain specific mapping, by marking the Enum class with the @EnumType annotation.

```java
@EnumType(value = EvenOrOdd.Serialization.class, from = Integer.class)
public enum EvenOrOdd {
  EVEN, ODD;

  public static class Serialization implements EnumSerializer<Integer> {
    @Override
    public Enum fromJson(Class enumClass, Integer input) {
      return input % 2 == 0 ? EvenOrOdd.EVEN : EvenOrOdd.ODD;
    }
    @Override
    public Integer toJson(Enum input) {
      return input == EVEN ? 2 : 1;
    }
  }
}
```

Note: specifying a `from` attribute will make the `Deserialize` use the closest fits the source. Which for numbers would be a `BigDecimal`, this behavior mirrors `ValueType`.

