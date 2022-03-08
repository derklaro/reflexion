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
import org.jetbrains.annotations.Nullable;

public final class Reflexion {

  // the current used instance to wrap fields, methods and constructors
  public static final AccessorFactory ACCESSOR_FACTORY = AccessorFactoryLoader.doLoadFactory();

  // the class wrapped by this reflexion instance
  private final Class<?> wrappedClass;
  // the object to which this reflexion instance is bound
  // this is null when the reflexion is not bound to anything
  @Nullable
  private final Object binding;

  // class member caches, created lazily when needed
  private Set<Field> fields;
  private Set<Method> methods;
  private Set<Constructor<?>> constructors;

  private Reflexion(@NonNull Class<?> wrappedClass, @Nullable Object binding) {
    this.wrappedClass = wrappedClass;
    this.binding = binding;
  }

  // ------------------
  // factory methods
  // ------------------

  public static @NonNull Reflexion on(@NonNull Class<?> clazz) {
    return on(clazz, null);
  }

  public static @NonNull Reflexion on(@NonNull Object instance) {
    return on(instance.getClass(), null);
  }

  public static @NonNull Reflexion onBound(@NonNull Object instance) {
    return on(instance.getClass(), instance);
  }

  public static @NonNull Reflexion on(@NonNull Class<?> clazz, @Nullable Object binding) {
    return new Reflexion(clazz, binding);
  }

  public static @NonNull Optional<Reflexion> find(@NonNull String name) {
    return find(name, null);
  }

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

  public static @NonNull Optional<Reflexion> findAny(@Nullable ClassLoader loader, @NonNull String... names) {
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

  public static @NonNull Reflexion get(@NonNull String name, @Nullable ClassLoader loader) {
    return find(name, loader).orElseThrow(() -> new IllegalArgumentException("No class with name " + name + " found"));
  }

  public static @NonNull Reflexion getAny(@Nullable ClassLoader loader, @NonNull String... names) {
    return findAny(loader, names).orElseThrow(() -> {
      String fullNames = String.join(", ", names);
      return new IllegalArgumentException("No class with any name of " + fullNames + " found");
    });
  }

  // ------------------
  // instance methods
  // ------------------

  public @NonNull Reflexion bind(@Nullable Object binding) {
    return new Reflexion(this.wrappedClass, binding);
  }

  public @NonNull Class<?> getWrappedClass() {
    return this.wrappedClass;
  }

  public @Nullable Object getBinding() {
    return this.binding;
  }

  // ------------------
  // field access
  // ------------------

  public @NonNull Optional<FieldAccessor> findField(@NonNull String name) {
    return this.findField(FieldMatcher.newMatcher().hasName(name));
  }

  public @NonNull Optional<FieldAccessor> findField(@NonNull FieldMatcher matcher) {
    for (Field field : this.getFieldCache()) {
      if (matcher.test(field)) {
        return Optional.of(ACCESSOR_FACTORY.wrapField(this, field));
      }
    }
    return Optional.empty();
  }

  public @NonNull Collection<FieldAccessor> findFields(@NonNull FieldMatcher matcher) {
    return Util.filterAndMap(this.getFieldCache(), matcher, field -> ACCESSOR_FACTORY.wrapField(this, field));
  }

  // ------------------
  // method access
  // ------------------

  public @NonNull Optional<MethodAccessor<Method>> findMethod(@NonNull String name, @NonNull Class<?>... paramTypes) {
    return this.findMethod(MethodMatcher.newMatcher().hasName(name).exactTypes(Method::getParameterTypes, paramTypes));
  }

  public @NonNull Optional<MethodAccessor<Method>> findMethod(@NonNull MethodMatcher matcher) {
    for (Method method : this.getMethodCache()) {
      if (matcher.test(method)) {
        return Optional.of(ACCESSOR_FACTORY.wrapMethod(this, method));
      }
    }
    return Optional.empty();
  }

  public @NonNull Collection<MethodAccessor<Method>> findMethods(@NonNull MethodMatcher matcher) {
    return Util.filterAndMap(this.getMethodCache(), matcher, method -> ACCESSOR_FACTORY.wrapMethod(this, method));
  }

  // ------------------
  // constructor access
  // ------------------

  public @NonNull Optional<MethodAccessor<Constructor<?>>> findConstructor(@NonNull Class<?>... paramTypes) {
    return this.findConstructor(ConstructorMatcher.newMatcher().exactTypes(Constructor::getParameterTypes, paramTypes));
  }

  public @NonNull Optional<MethodAccessor<Constructor<?>>> findConstructor(@NonNull ConstructorMatcher matcher) {
    for (Constructor<?> constructor : this.getConstructorCache()) {
      if (matcher.test(constructor)) {
        return Optional.of(ACCESSOR_FACTORY.wrapConstructor(this, constructor));
      }
    }
    return Optional.empty();
  }

  public @NonNull Collection<MethodAccessor<Constructor<?>>> findConstructors(@NonNull ConstructorMatcher matcher) {
    return Util.filterAndMap(this.getConstructorCache(), matcher, ctr -> ACCESSOR_FACTORY.wrapConstructor(this, ctr));
  }

  // ------------------
  // private accessors
  // ------------------

  private @NonNull Set<Field> getFieldCache() {
    if (this.fields == null) {
      this.fields = ReflexionPopulator.getAllFields(this.wrappedClass);
    }
    return this.fields;
  }

  private @NonNull Set<Method> getMethodCache() {
    if (this.methods == null) {
      this.methods = ReflexionPopulator.getAllMethods(this.wrappedClass);
    }
    return this.methods;
  }

  private @NonNull Set<Constructor<?>> getConstructorCache() {
    if (this.constructors == null) {
      this.constructors = ReflexionPopulator.getAllConstructors(this.wrappedClass);
    }
    return this.constructors;
  }
}
