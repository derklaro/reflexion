/*
 * This file is part of reflexion, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2022 Pasqual K., Aldin S. and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package dev.derklaro.reflexion;

import dev.derklaro.reflexion.internal.AccessorFactoryLoader;
import dev.derklaro.reflexion.internal.util.Util;
import dev.derklaro.reflexion.matcher.ConstructorMatcher;
import dev.derklaro.reflexion.matcher.FieldMatcher;
import dev.derklaro.reflexion.matcher.MethodMatcher;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import lombok.NonNull;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * The main entry point for working with the reflexion library. This class is designed to allow read and write access to
 * classes and their members without the need to think about exceptions and writing non-fluent code which makes your
 * code look ugly.
 * <p>
 * Consider the following example: you need a class which can only access via it's name and need to get the value of a
 * field in it. This can look like the following code when doing it using the following plain java.lang reflection:
 * <pre>
 * {@code
 *  public static String getHelloWorldFieldValue() {
 *    try {
 *      Class&lt;?&gt; class = Class.forName("test.HelloWorld");
 *      Field field = class.getDeclaredField("helloWorld");
 *      field.setAccessible(true);
 *      return field.get(null);
 *    } catch (ClassNotFoundException | IllegalAccessException ex) {
 *      ex.printStackTrace();
 *      return null;
 *    }
 *  }
 * }
 * </pre>
 * With reflexion the same code looks like this:
 * <pre>
 * {@code
 *  public static String getHelloWorldFieldValue() {
 *    return Reflexion.find("test.HelloWorld")
 *      .flatMap(reflexion -> reflexion.findField("helloWorld"))
 *      .map(accessor -> accessor.&lt;String&gt;getValue().getOrElse(null))
 *      .orElse(null);
 *  }
 * }
 * </pre>
 * <p>
 * Or consider this example: you need to find a method in a class which has 2 parameters and the name of the method
 * starts with "hello" and returns a string. With normal java.lang reflection this could look like this:
 * <pre>
 * {@code
 *  public static String getHelloWorldMethodValue() {
 *    try {
 *      for (Method method : HelloWorld.class.getDeclaredMethods()) {
 *        if (method.getParameterCount() == 2
 *          && method.getReturnType().equals(String.class)
 *          && method.getName().matches("hello.*")) {
 *          return method.invoke(null);
 *        }
 *      }
 *    } catch (IllegalAccessException | InvocationTargetException ex) {
 *      ex.printStackTrace();
 *      return null;
 *    }
 *  }
 * }
 * </pre>
 * With reflexion the same code looks like this:
 * <pre>
 * {@code
 *  public static String getHelloWorldMethodValue() {
 *    String valueMethod = Reflexion.on(HelloWorld.class).findMethod(MethodMatcher.newMatcher()
 *        .parameterCount(2)
 *        .hasName("helloWorld)
 *        .exactType(Method::getReturnType, String.class))
 *      .map(accessor -> accessor.&lt;String&gt;invoke().getOrElse(null))
 *      .orElse(null);
 *  }
 * }
 * </pre>
 * <p>
 * As probably seen by the methods shown above using reflexion over normal java.lang reflection is much more convenient.
 * Furthermore, reflexion does everything for you as safe as possible while also providing the best performance to
 * access class members. To archive that you should cache your obtained reflexion instances, as each of them will
 * maintain a non-shared cache over all members in a class. A good practice is something like:
 * <pre>
 * {@code
 *  public static final Reflexion HELLO_WORLD_REFLEXION = Reflexion.on(HelloWorld.class);
 * }
 * </pre>
 * Note that the member caches are only populated when they are actually needed, meaning that if you only query fields
 * from a reflexion instance no methods or constructors from the class will get fetched and vise-versa.
 *
 * @since 1.0
 */
public final class Reflexion {

  /**
   * The current used accessor factory to wrap fields, methods and constructors. Initialized once when the class is
   * first used.
   */
  public static final AccessorFactory ACCESSOR_FACTORY = AccessorFactoryLoader.doLoadFactory();

  // the class wrapped by this reflexion instance
  private final Class<?> wrappedClass;
  // the accessor factory we should use
  private final AccessorFactory accFactory;
  // the object to which this reflexion instance is bound
  // this is null when the reflexion is not bound to anything
  @Nullable
  private final Object binding;

  // class member caches, created lazily when needed
  private Set<Field> fields;
  private Set<Method> methods;
  private Set<Constructor<?>> constructors;

  /**
   * Constructs a new reflexion instance. Do not use this constructor directly refer to the static factory methods in
   * this class (see the top-level documentation comment for more details).
   *
   * @param wrappedClass the class wrapped by the constructed reflexion instance.
   * @param binding      the instance to which the reflection instance is bound, null for no binding.
   * @param factory      the accessor factory to use when wrapping reflection objects.
   * @throws NullPointerException if the given wrapped class is null.
   */
  private Reflexion(@NonNull Class<?> wrappedClass, @Nullable Object binding, @NonNull AccessorFactory factory) {
    this.wrappedClass = wrappedClass;
    this.binding = binding;
    this.accFactory = factory;
  }

  // ------------------
  // factory methods
  // ------------------

  /**
   * Constructs a new reflexion instance wrapping and targeting the given class. The method call produces the same
   * result as calling {@code Reflexion.on(clazz, null)}.
   *
   * @param clazz the class to wrap in the reflexion object.
   * @return a new reflexion object wrapping the given class.
   * @throws NullPointerException if the given class to wrap is null.
   */
  public static @NonNull Reflexion on(@NonNull Class<?> clazz) {
    return on(clazz, null);
  }

  /**
   * Constructs a new reflexion instance wrapping the class of the given object instance. The method call will not bind
   * the reflexion object to the given instance as is equivalent to {@code Reflexion.on(instance.getClass(), null)}.
   * <p>
   * If you want a reflexion object which targets the class of the given instance as binds the reflexion instance
   * directly to it use {@code Reflexion.onBound(instance)} instead.
   *
   * @param instance the instance of the object to wrap the class of.
   * @return a new reflexion object wrapping the class of the given object instance.
   * @throws NullPointerException if the given instance is null.
   */
  public static @NonNull Reflexion on(@NonNull Object instance) {
    return on(instance.getClass(), null);
  }

  /**
   * Constructs a new reflexion instance wrapping the class of the given object instance and binds it to the given
   * instance. This method call is equivalent to {@code Reflexion.on(instance.getClass(), instance)}.
   *
   * @param instance the instance of the object to wrap the class of and bind the reflexion instance to it.
   * @return a new reflexion object bound to the given instance and wrapping the class of it.
   * @throws NullPointerException if the given instance is null.
   */
  public static @NonNull Reflexion onBound(@NonNull Object instance) {
    return on(instance.getClass(), instance);
  }

  /**
   * Constructs a new reflexion instance wrapping the given class and binds it to the given object instance, if any is
   * provided.
   *
   * @param clazz   the class to wrap in the new reflexion object.
   * @param binding the instance of the object to bind the reflexion instance to, can be null for no binding.
   * @return a new reflexion instance wrapping the given class and optionally bound to the given instance.
   * @throws NullPointerException if the given class to wrap is null.
   */
  public static @NonNull Reflexion on(@NonNull Class<?> clazz, @Nullable Object binding) {
    return on(clazz, binding, ACCESSOR_FACTORY);
  }

  /**
   * Constructs a new reflexion instance wrapping the given class and binds it to the given object instance, if any is
   * provided.
   *
   * @param clazz   the class to wrap in the new reflexion object.
   * @param binding the instance of the object to bind the reflexion instance to, can be null for no binding.
   * @param factory the accessor factory to use when constructing accessor in the reflexion object.
   * @return a new reflexion instance wrapping the given class and optionally bound to the given instance.
   * @throws NullPointerException if the given class to wrap is null.
   */
  public static @NonNull Reflexion on(
    @NonNull Class<?> clazz,
    @Nullable Object binding,
    @NonNull AccessorFactory factory
  ) {
    return new Reflexion(clazz, binding, factory);
  }

  /**
   * Tries to find and wrap a class with the given name in a reflexion instance. This method will make use of the first
   * available class loader in the context, identical to {@code Reflexion.find(name, null)} in the following order:
   * <ol>
   *   <li>the thread context class loader.
   *   <li>the reflexion class loader.
   *   <li>the system class loader.
   * </ol>
   * <p>
   * This method will not initialize the class when wrapping it.
   *
   * @param name the name of the class to find.
   * @return an optional reflexion class object wrapping a class with the given name.
   * @throws NullPointerException if the given class name is null.
   */
  public static @NonNull Optional<Reflexion> find(@NonNull String name) {
    return find(name, null);
  }

  /**
   * Tries to find and wrap a class with the given name in a reflexion instance. This method will make use of the first
   * available class loader in the context if no explicit loader is given in the following order:
   * <ol>
   *   <li>the thread context class loader.
   *   <li>the reflexion class loader.
   *   <li>the system class loader.
   * </ol>
   * <p>
   * This method will not initialize the class when wrapping it.
   *
   * @param name   the name of the class to find.
   * @param loader the loader to search the class in, null to use the first available contextual loader.
   * @return an optional reflexion class object wrapping a class with the given name.
   * @throws NullPointerException if the given class name is null.
   */
  public static @NonNull Optional<Reflexion> find(@NonNull String name, @Nullable ClassLoader loader) {
    try {
      // no loader, try the context loader
      // if the context loader is null we try this class loader in order someone tried something weird
      // if this class has no loader either we fall back to the system class loader (should never happen)
      ClassLoader classLoader = Util.firstNonNull(
        loader,
        Thread.currentThread().getContextClassLoader(),
        Reflexion.class.getClassLoader(),
        ClassLoader.getSystemClassLoader());
      Class<?> wrappedClass = Class.forName(name, false, classLoader);
      // found the class, wrap it
      return Optional.of(on(wrappedClass));
    } catch (ClassNotFoundException exception) {
      // class not found, nothing to wrap
      return Optional.empty();
    }
  }

  /**
   * Tries to find and wrap a class with one of the given names. The order of the given name array will be the same as
   * the search order. This method will make use of the first available class loader in the context in the following
   * order:
   * <ol>
   *   <li>the thread context class loader.
   *   <li>the reflexion class loader.
   *   <li>the system class loader.
   * </ol>
   * <p>
   * This method will not initialize the class when wrapping it.
   *
   * @param names the names of the class to search for.
   * @return an optional reflexion instance wrapping the first class which can be resolved from the given names.
   * @throws NullPointerException if the given name array or an element of it is null.
   */
  public static @NonNull Optional<Reflexion> findAny(@NonNull String @NonNull ... names) {
    // search until one returns a value that is present
    for (String name : names) {
      Optional<Reflexion> reflexion = find(name);
      if (reflexion.isPresent()) {
        // found one
        return reflexion;
      }
    }
    // found none
    return Optional.empty();
  }

  /**
   * Tries to find and wrap a class with one of the given names. The order of the given name array will be the same as
   * the search order. This method will make use of the first available class loader in the context if no explicit
   * loader is given in the following order:
   * <ol>
   *   <li>the thread context class loader.
   *   <li>the reflexion class loader.
   *   <li>the system class loader.
   * </ol>
   * <p>
   * This method will not initialize the class when wrapping it.
   *
   * @param loader the loader to search the class in, null to use the first available contextual loader.
   * @param names  the names of the class to search for.
   * @return an optional reflexion instance wrapping the first class which can be resolved from the given names.
   * @throws NullPointerException if the given name array or an element of it is null.
   */
  public static @NonNull Optional<Reflexion> findAny(@Nullable ClassLoader loader, @NonNull String @NonNull ... names) {
    // search until one returns a value that is present
    for (String name : names) {
      Optional<Reflexion> reflexion = find(name, loader);
      if (reflexion.isPresent()) {
        // found one
        return reflexion;
      }
    }
    // found none
    return Optional.empty();
  }

  /**
   * Tries to find and wrap a class with the given name in a reflexion instance. If no class with the given name is
   * found an illegal argument exception is raised. If you want a method to find a class which does not raise an
   * exception, consider using {@link #find(String, ClassLoader)} instead. This method will make use of the first
   * available class loader in the context if no explicit loader is given in the following order:
   * <ol>
   *   <li>the thread context class loader.
   *   <li>the reflexion class loader.
   *   <li>the system class loader.
   * </ol>
   * <p>
   * This method will not initialize the class when wrapping it.
   *
   * @param name   the name of the class to find.
   * @param loader the loader to search the class in, null to use the first available contextual loader.
   * @return a reflexion class object wrapping a class with the given name.
   * @throws NullPointerException if the given class name is null.
   * @throws ReflexionException   if no class with the given name was found.
   */
  public static @NonNull Reflexion get(@NonNull String name, @Nullable ClassLoader loader) {
    return find(name, loader).orElseThrow(() -> new ReflexionException("No class with name " + name + " found"));
  }

  /**
   * Tries to find and wrap a class with one of the given names. If no class with the given name is found an illegal
   * argument exception is raised. If you want a method to find a class which does not raise an exception, consider
   * using {@link #findAny(ClassLoader, String...)} instead. The order of the given name array will be the same as the
   * search order. This method will make use of the first available class loader in the context if no explicit loader is
   * given in the following order:
   * <ol>
   *   <li>the thread context class loader.
   *   <li>the reflexion class loader.
   *   <li>the system class loader.
   * </ol>
   * <p>
   * This method will not initialize the class when wrapping it.
   *
   * @param loader the loader to search the class in, null to use the first available contextual loader.
   * @param names  the names of the class to search for.
   * @return a reflexion instance wrapping the first class which can be resolved from the given names.
   * @throws NullPointerException if the given name array or an element of it is null.
   * @throws ReflexionException   if no class with one of the given names could be found.
   */
  public static @NonNull Reflexion getAny(@Nullable ClassLoader loader, @NonNull String @NonNull ... names) {
    return findAny(loader, names).orElseThrow(() -> {
      String fullNames = String.join(", ", names);
      return new ReflexionException("No class with any name of " + fullNames + " found");
    });
  }

  // ------------------
  // java reflect helpers
  // ------------------

  /**
   * Unreflects the given field and wraps it in a field accessor, while preserving the functionality of bindings and
   * better abstraction. This method is specifically useful when an iteration for internal reasons is made, or a java
   * field instance is already known.
   * <p>
   * This method creates a new reflexion instance which is bound to the declaring class of the field and then unreflects
   * it. If a specific binding is required use {@code Reflexion.onBound(instance).unreflect(field)} instead.
   *
   * @param field the field to wrap in a field accessor.
   * @return a field accessor for the given field, bound to this reflexion instance.
   * @throws NullPointerException if the given field is null.
   * @since 1.7.0
   */
  public static @NonNull FieldAccessor unreflectField(@NonNull Field field) {
    return Reflexion.on(field.getDeclaringClass()).unreflect(field);
  }

  /**
   * Unreflects the given method and wraps it in a method accessor, while preserving the functionality of bindings and
   * better abstraction. This method is specifically useful when an iteration for internal reasons is made, or a java
   * method instance is already known.
   * <p>
   * This method creates a new reflexion instance which is bound to the declaring class of the method and then
   * unreflects it. If a specific binding is required use {@code Reflexion.onBound(instance).unreflect(method)}
   * instead.
   *
   * @param method the method to wrap in a method accessor.
   * @return a method accessor for the given method, bound to this reflexion instance.
   * @throws NullPointerException if the given method is null.
   * @since 1.7.0
   */
  public static @NonNull MethodAccessor<Method> unreflectMethod(@NonNull Method method) {
    return Reflexion.on(method.getDeclaringClass()).unreflect(method);
  }

  /**
   * Unreflects the given constructor and wraps it in a method accessor, while preserving the functionality of bindings
   * and better abstraction. This method is specifically useful when an iteration for internal reasons is made, or a
   * java constructor instance is already known.
   * <p>
   * This method creates a new reflexion instance which is bound to the declaring class of the constructor and then
   * unreflects it. If a specific binding is required use {@code Reflexion.onBound(instance).unreflect(constructor)}
   * instead.
   *
   * @param constructor the constructor to wrap in a method accessor.
   * @return a method accessor for the given constructor, bound to this reflexion instance.
   * @throws NullPointerException if the given constructor is null.
   * @since 1.7.0
   */
  public static @NonNull MethodAccessor<Constructor<?>> unreflectConstructor(@NonNull Constructor<?> constructor) {
    return Reflexion.on(constructor.getDeclaringClass()).unreflect(constructor);
  }

  // ------------------
  // instance methods
  // ------------------

  /**
   * Binds the current reflexion instance to the given binding even when this instance is already bound to an instance.
   * Binding a reflexion instance has no direct effect on how the library performs, however all accessor methods which
   * would normally require an instance to be passed in can then be executed without passing in an instance, using the
   * instance to which the reflexion object was bound. For example:
   * <pre>
   * {@code
   *  public static String getHelloWorldFieldValue() {
   *    HelloWorld obj = new HelloWorld("Hello, World!");
   *
   *    // without binding
   *    var unboundAcc = Reflexion.on(HelloWorld.class).findField("message").get();
   *    System.out.println("Value: " + unboundAcc.getValue(obj)); // prints "Hello, World!"
   *
   *    // with binding
   *    var boundAcc = Reflexion.on(HelloWorld.class).bind(obj).findField("message").get();
   *    System.out.println("Value: " + boundAcc.getValue()); // prints "Hello, World!"
   *
   *    // but we can still change the object to get the field value of
   *    HelloWorld world = new HelloWorld("Hello, Moon!");
   *    System.out.println("Value: " + boundAcc.getValue(world)); // prints "Hello, Moon!"
   *  }
   * }
   * </pre>
   * <p>
   * Note: this method always returns a new reflexion instance and not the instance the method was called on. Re-using
   * the instance from before the method call is still possible without any changes to the bindings. Changes to the
   * returned reflexion instance will not reflect into this instance as vice-versa.
   *
   * @param binding the object instance to bind the reflexion instance to, null to release the binding.
   * @return a new reflexion instance targeting the same class as the current one but with the given binding set.
   */
  @Contract(value = "_ -> new", pure = true)
  public @NonNull Reflexion bind(@Nullable Object binding) {
    return new Reflexion(this.wrappedClass, binding, this.accFactory);
  }

  /**
   * Gets the class which is wrapped by this reflexion instance.
   *
   * @return the wrapped class.
   */
  public @NonNull Class<?> getWrappedClass() {
    return this.wrappedClass;
  }

  /**
   * Gets the object instance to which this reflexion instance is bound.
   *
   * @return the bound object instance, null if not bound to any instance.
   */
  public @Nullable Object getBinding() {
    return this.binding;
  }

  /**
   * Get the accessor factory which is associated with this reflexion instance.
   *
   * @return the associated accessor factory with this reflexion instance.
   */
  public @NonNull AccessorFactory getAccessorFactory() {
    return this.accFactory;
  }

  // ------------------
  // java reflect helpers
  // ------------------

  /**
   * Unreflects the given field and wraps it in a field accessor, while preserving the functionality of bindings and
   * better abstraction. This method is specifically useful when an iteration for internal reasons is made, or a java
   * field instance is already known.
   *
   * @param field the field to wrap in a field accessor.
   * @return a field accessor for the given field, bound to this reflexion instance.
   * @throws NullPointerException if the given field is null.
   * @since 1.7.0
   */
  public @NonNull FieldAccessor unreflect(@NonNull Field field) {
    return this.accFactory.wrapField(this, field);
  }

  /**
   * Unreflects the given method and wraps it in a method accessor, while preserving the functionality of bindings and
   * better abstraction. This method is specifically useful when an iteration for internal reasons is made, or a java
   * method instance is already known.
   *
   * @param method the method to wrap in a method accessor.
   * @return a method accessor for the given method, bound to this reflexion instance.
   * @throws NullPointerException if the given method is null.
   * @since 1.7.0
   */
  public @NonNull MethodAccessor<Method> unreflect(@NonNull Method method) {
    return this.accFactory.wrapMethod(this, method);
  }

  /**
   * Unreflects the given constructor and wraps it in a method accessor, while preserving the functionality of bindings
   * and better abstraction. This method is specifically useful when an iteration for internal reasons is made, or a
   * java constructor instance is already known.
   *
   * @param constructor the constructor to wrap in a method accessor.
   * @return a method accessor for the given constructor, bound to this reflexion instance.
   * @throws NullPointerException if the given constructor is null.
   * @since 1.7.0
   */
  public @NonNull MethodAccessor<Constructor<?>> unreflect(@NonNull Constructor<?> constructor) {
    return this.accFactory.wrapConstructor(this, constructor);
  }

  // ------------------
  // field access
  // ------------------

  /**
   * Finds a field with the given name in the wrapped class and creates a field accessor wrapper for it. Internally this
   * method uses a cache for all fields in the class, meaning that each call to the method will not result in duplicate
   * lookups in the wrapped class.
   * <p>
   * Example usage:
   * <pre>
   * {@code
   *  public static String getHelloWorldFieldValue(HelloWorld obj) {
   *    return Reflexion.on(HelloWorld.class)
   *      .findField("helloWorld")
   *      .flatMap(acc -> acc.getValue(obj).asOptional())
   *      .orElse("Unable to resolve field value :/");
   *  }
   * }
   * </pre>
   *
   * @param name the name of the field to get.
   * @return an optional accessor for the field with the given name.
   * @throws NullPointerException if the given field name is null.
   */
  public @NonNull Optional<FieldAccessor> findField(@NonNull String name) {
    return this.findField(FieldMatcher.newMatcher().hasName(name));
  }

  /**
   * Finds the first field which matches the given matcher. Internally this method uses a cache for all fields in the
   * class, meaning that each call to the method will not result in duplicate lookups in the wrapped class.
   * <p>
   * Example usage:
   * <pre>
   * {@code
   *  public static String getHelloWorldFieldValue(HelloWorld obj) {
   *    return Reflexion.on(HelloWorld.class)
   *      .findField(FieldMatcher.newMatcher().hasName("world"))
   *      .flatMap(acc -> acc.getValue(obj).asOptional())
   *      .orElse("Unable to resolve field value :/");
   *  }
   * }
   * </pre>
   *
   * @param matcher the matcher for the field to find.
   * @return an optional accessor for the first field matching the given matcher.
   * @throws NullPointerException if the given matcher is null.
   */
  public @NonNull Optional<FieldAccessor> findField(@NonNull FieldMatcher matcher) {
    for (Field field : this.getFieldCache()) {
      if (matcher.test(field)) {
        return Optional.of(this.accFactory.wrapField(this, field));
      }
    }
    return Optional.empty();
  }

  /**
   * Finds all fields which are matching the given matcher. Internally this method uses a cache for all fields in the
   * class, meaning that each call to the method will not result in duplicate lookups in the wrapped class.
   * <p>
   * Example usage:
   * <pre>
   * {@code
   *  public static Collection<FieldAccessor> getHelloWorldFields() {
   *    return Reflexion.on(HelloWorld.class)
   *      .findFields(FieldMatcher.newMatcher().hasName("world"));
   *  }
   * }
   * </pre>
   *
   * @param matcher the matcher to match the fields.
   * @return a collection of field accessors for all fields which are matching the given matcher.
   * @throws NullPointerException if the given matcher is null.
   */
  public @NonNull Collection<FieldAccessor> findFields(@NonNull FieldMatcher matcher) {
    return Util.filterAndMap(this.getFieldCache(), matcher, field -> this.accFactory.wrapField(this, field));
  }

  // ------------------
  // method access
  // ------------------

  /**
   * Finds a method with the given name and parameter types in the wrapped class and creates a method accessor wrapper
   * for it. The given parameter types must match exactly and aren't derived types. Internally this method uses a cache
   * for all methods in the class, meaning that each call to the method will not result in duplicate lookups in the
   * wrapped class.
   * <p>
   * Example usage:
   * <pre>
   * {@code
   *  public static String getHelloWorldMethodValue(HelloWorld obj) {
   *    return Reflexion.on(HelloWorld.class)
   *      .findMethod("helloWorld")
   *      .flatMap(acc -> acc.invoke(obj).asOptional())
   *      .orElse("Unable to resolve method value :/");
   *  }
   * }
   * </pre>
   *
   * @param name       the name of the method to get.
   * @param paramTypes the type of all parameters of the method.
   * @return an optional accessor for the method with the given name and parameters.
   * @throws NullPointerException if the given method name or parameter array is null.
   */
  public @NonNull Optional<MethodAccessor<Method>> findMethod(@NonNull String name, @NonNull Class<?>... paramTypes) {
    return this.findMethod(MethodMatcher.newMatcher().hasName(name).exactTypes(Method::getParameterTypes, paramTypes));
  }

  /**
   * Finds the first method which matches the given matcher in the wrapped class and creates a method accessor wrapper
   * for it. Internally this method uses a cache for all methods in the class, meaning that each call to the method will
   * not result in duplicate lookups in the wrapped class.
   * <p>
   * Example usage:
   * <pre>
   * {@code
   *  public static String getHelloWorldMethodValue(HelloWorld obj) {
   *    return Reflexion.on(HelloWorld.class)
   *      .findMethod(MethodMatcher.newMatcher().hasName("helloWorld"))
   *      .flatMap(acc -> acc.invoke(obj).asOptional())
   *      .orElse("Unable to resolve method value :/");
   *  }
   * }
   * </pre>
   *
   * @param matcher the matcher for the method to find.
   * @return an optional accessor for the first method matching the given matcher.
   * @throws NullPointerException if the given matcher is null.
   */
  public @NonNull Optional<MethodAccessor<Method>> findMethod(@NonNull MethodMatcher matcher) {
    for (Method method : this.getMethodCache()) {
      if (matcher.test(method)) {
        return Optional.of(this.accFactory.wrapMethod(this, method));
      }
    }
    return Optional.empty();
  }

  /**
   * Finds all methods which are matching the given matcher in the wrapped class and creates a method accessor wrapper
   * for each of them. Internally this method uses a cache for all methods in the class, meaning that each call to the
   * method will not result in duplicate lookups in the wrapped class.
   * <p>
   * Example usage:
   * <pre>
   * {@code
   *  public static Collection<MethodAccessor<Method>> getHelloWorldMethods() {
   *    return Reflexion.on(HelloWorld.class)
   *      .findMethods(MethodMatcher.newMatcher().hasName("world"));
   *  }
   * }
   * </pre>
   *
   * @param matcher the matcher for the method to find.
   * @return a collection of method accessors for all methods which are matching the given matcher.
   * @throws NullPointerException if the given matcher is null.
   */
  public @NonNull Collection<MethodAccessor<Method>> findMethods(@NonNull MethodMatcher matcher) {
    return Util.filterAndMap(this.getMethodCache(), matcher, method -> this.accFactory.wrapMethod(this, method));
  }

  // ------------------
  // constructor access
  // ------------------

  /**
   * Finds a constructor with the given parameter types in the wrapped class and creates a method accessor wrapper for
   * it. The given parameter types must match exactly and aren't derived types. Internally this method uses a cache for
   * all constructors in the class, meaning that each call to the method will not result in duplicate lookups in the
   * wrapped class.
   * <p>
   * Example usage:
   * <pre>
   * {@code
   *  public static HelloWorld newHelloWorldInstance(String msg) {
   *    return Reflexion.on(HelloWorld.class)
   *      .findConstructor(String.class)
   *      .flatMap(acc -> acc.invoke(msg).asOptional())
   *      .orElse(null);
   *  }
   * }
   * </pre>
   *
   * @param paramTypes the type of all parameters of the constructor.
   * @return an optional accessor for the constructor with the given parameters.
   * @throws NullPointerException if the given parameter array is null.
   */
  public @NonNull Optional<MethodAccessor<Constructor<?>>> findConstructor(@NonNull Class<?>... paramTypes) {
    return this.findConstructor(ConstructorMatcher.newMatcher().exactTypes(Constructor::getParameterTypes, paramTypes));
  }

  /**
   * Finds the first constructor matching the given matcher in the wrapped class and creates a method accessor wrapper
   * for it. Internally this method uses a cache for all constructors in the class, meaning that each call to the method
   * will not result in duplicate lookups in the wrapped class.
   * <p>
   * Example usage:
   * <pre>
   * {@code
   *  public static HelloWorld newHelloWorldInstance(String msg) {
   *    return Reflexion.on(HelloWorld.class)
   *      .findConstructor(ConstructorMatcher.newMatcher()
   *        .exactTypeAt(Constructor::getParameterTypes, String.class, 0))
   *      .flatMap(acc -> acc.invoke(msg).asOptional())
   *      .orElse(null);
   *  }
   * }
   * </pre>
   *
   * @param matcher the matcher for the constructor to find.
   * @return an optional accessor for the constructor with the given parameters.
   * @throws NullPointerException if the given matcher is null.
   */
  public @NonNull Optional<MethodAccessor<Constructor<?>>> findConstructor(@NonNull ConstructorMatcher matcher) {
    for (Constructor<?> constructor : this.getConstructorCache()) {
      if (matcher.test(constructor)) {
        return Optional.of(this.accFactory.wrapConstructor(this, constructor));
      }
    }
    return Optional.empty();
  }

  /**
   * Finds all constructors matching the given matcher in the wrapped class and creates a method accessor wrapper for
   * it. Internally this method uses a cache for all constructors in the class, meaning that each call to the method
   * will not result in duplicate lookups in the wrapped class.
   * <p>
   * Example usage:
   * <pre>
   * {@code
   *  public static Collection<MethodAccessor<Constructor<?>>> findHelloWorldConstructors() {
   *    return Reflexion.on(HelloWorld.class)
   *      .findConstructors(ConstructorMatcher.newMatcher()
   *        .exactTypeAt(Constructor::getParameterTypes, String.class, 0));
   *  }
   * }
   * </pre>
   *
   * @param matcher the matcher for the constructor to find.
   * @return a collection of method accessors for all constructors which are matching the given matcher.
   * @throws NullPointerException if the given matcher is null.
   */
  public @NonNull Collection<MethodAccessor<Constructor<?>>> findConstructors(@NonNull ConstructorMatcher matcher) {
    return Util.filterAndMap(this.getConstructorCache(), matcher, ctr -> this.accFactory.wrapConstructor(this, ctr));
  }

  // ------------------
  // private accessors
  // ------------------

  /**
   * Internal: gets the field cache for the wrapped class. This method populates the cache before returning it if the
   * cache isn't initialized yet.
   *
   * @return the field cache for the wrapped class.
   */
  private @NonNull Set<Field> getFieldCache() {
    if (this.fields == null) {
      this.fields = ReflexionPopulator.getAllFields(this.wrappedClass);
    }
    return this.fields;
  }

  /**
   * Internal: gets the method cache for the wrapped class. This method populates the cache before returning it if the
   * cache isn't initialized yet.
   *
   * @return the method cache for the wrapped class.
   */
  private @NonNull Set<Method> getMethodCache() {
    if (this.methods == null) {
      this.methods = ReflexionPopulator.getAllMethods(this.wrappedClass);
    }
    return this.methods;
  }

  /**
   * Internal: gets the constructor cache for the wrapped class. This method populates the cache before returning it if
   * the cache isn't initialized yet.
   *
   * @return the constructor cache for the wrapped class.
   */
  private @NonNull Set<Constructor<?>> getConstructorCache() {
    if (this.constructors == null) {
      this.constructors = ReflexionPopulator.getAllConstructors(this.wrappedClass);
    }
    return this.constructors;
  }
}
