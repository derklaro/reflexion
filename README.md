# Reflexion

![Maven Central Release](https://img.shields.io/maven-central/v/dev.derklaro.reflexion/reflexion)
![License](https://img.shields.io/badge/License-MIT-brightgreen)
![Workflow Status](https://github.com/derklaro/reflexion/actions/workflows/ci.yml/badge.svg)

A small library which makes the day-to-day life of anyone who is using Java reflections much easier by providing an easy
to access api while also opening the back door to allow reflection calls which were previously not possible.

### Example usage

When using traditional reflection code, a field lookup looks like this:

```java
public final class ReflectionHelper {

  public static String getHelloWorldFieldValue() {
    try {
      Class<?> clazz = Class.forName("test.HelloWorld");
      Field field = clazz.getDeclaredField("helloWorld");
      field.setAccessible(true);
      return field.get(null);
    } catch (ClassNotFoundException | IllegalAccessException ex) {
      ex.printStackTrace();
      return null;
    }
  }
}
```

with reflexion the same code can simply get replaced using:

```java
public final class ReflectionHelper {

  public static String getHelloWorldFieldValue() {
    return Reflexion.find("test.HelloWorld")
      .flatMap(reflexion -> reflexion.findField("helloWorld"))
      .map(accessor -> accessor.getValue().getOrElse(null))
      .orElse(null);
  }
}
```

### How does it work?

Reflexion uses some tricks under the hood to get access to trusted reflection instances which allows 
you to override basically anything you want. These doors are getting more and more closed, see for
example [JDK-8277863](https://bugs.openjdk.java.net/browse/JDK-8277863) which deprecated the last way
of doing things which are normally forbidden by the jvm.
Therefore, Reflexion uses (when possible) native code to obtain and change values of fields which allows
you to do everything without any jre checks. The native libraries are pre-compiled available in the library
for the most common platforms:

| Operating System | Available Processor Architectures |
|------------------|-----------------------------------|
| Linux            | x86_64, aarch64, arm              |
| Apple            | x86_64, aarch64                   |
| Windows          | x86_64, aarch64                   |

**Note:** native code usage is not a requirement of the library! If you remove the native libraries from the
compile-classpath or set the system property `dev.derklaro.reflexion.native-disabled` to `true` no native code
will be used at all (note that the system property name depends on the package naming, if you relocate reflexion
into your application the system property might vary). 

### Why is this necessary?

Reflection are a great tool when it comes to point when hooking into a platform is necessary which you
can't control. There are a lot of libraries out there which are relying on reflections for years now and
are getting more and more cornered to change something which is out of their control. Some good examples
are [guice](https://github.com/google/guice) or [spring](https://github.com/spring-projects/spring-framework).

On the other hand, working with reflection is just a pain. Even after introducing MethodHandles to Java it
is still not possible to easily and cleanly access a field, method or constructor without the need to catch
exceptions and making your code look horrible.
But the **worst** part about method handles is the missing generification of them which makes it hard to write
"normal" reflection like code with method handles (sure that is possible and used in this library as well, but
it's another code smell).

### Enough talking, where do I get it?
Reflexion is released to maven central (replace `${VERSION}` with the latest version of the library, visible
in the first badge of this readme):
```xml
<dependency>
  <groupId>dev.derklaro.reflexion</groupId>
  <artifactId>reflexion</artifactId>
  <version>${VERSION}</version>
</dependency>
```

```kotlin
  implementation("dev.derklaro.reflexion", "reflexion", "${VERSION}")
```

#### Snapshots

Snapshots are released to the Sonatype snapshot repository: `https://s01.oss.sonatype.org/content/repositories/snapshots/`

### Issue reporting and contributing

Issues can be reported through the [GitHub issue tracker](https://github.com/derklaro/reflexion/issues/new/).
Contributions are always welcome and a :star: always appreciated.

### License

Reflexion is released under the terms of the MIT license. See [license.txt](license.txt)
or https://opensource.org/licenses/MIT.

### Alternatives

There a no real alternatives to the library out in the wild (at least I found none) which can do the same things
as reflexion can. But there are some other libraries which provide access to fluent reflection:

 - [jOOR](https://github.com/jOOQ/jOOR)
 - [OkReflect](https://github.com/zeshaoaaa/OkReflect)
 - [FEST-Reflect](https://github.com/alexruiz/fest-reflect)
