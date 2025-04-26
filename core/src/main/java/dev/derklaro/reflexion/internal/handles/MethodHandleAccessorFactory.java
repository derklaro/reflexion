/*
 * This file is part of reflexion, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2022-2023 Pasqual K., Aldin S. and contributors
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

package dev.derklaro.reflexion.internal.handles;

import dev.derklaro.reflexion.AccessorFactory;
import dev.derklaro.reflexion.FieldAccessor;
import dev.derklaro.reflexion.MethodAccessor;
import dev.derklaro.reflexion.Reflexion;
import dev.derklaro.reflexion.ReflexionException;
import dev.derklaro.reflexion.Result;
import dev.derklaro.reflexion.internal.util.Util;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A reflexion accessor factory which uses method handles to wrap methods and fields. <strong>DO NOT OVERRIDE THIS
 * CLASS! IT IS ONLY OPEN FOR INTERNAL USE!</strong>
 *
 * @since 1.0
 */
public class MethodHandleAccessorFactory implements AccessorFactory {

  private final Lookup trustedLookup;

  /**
   * Constructs a new method handle accessor factory instance.
   */
  public MethodHandleAccessorFactory() {
    this.trustedLookup = this.getTrustedLookup();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isAvailable() {
    // depend on the trusted lookup availability - if it is not present we can just
    // use the bare reflexion implementation, rather than needing to convert method
    // handles back and forth
    return this.trustedLookup != null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NotNull FieldAccessor wrapField(@NotNull Reflexion reflexion, @NotNull Field field) {
    try {
      boolean staticField = Modifier.isStatic(field.getModifiers());
      MethodHandle getter = this.convertFieldToGeneric(field, staticField, false);
      MethodHandle setter = this.convertFieldToGeneric(field, staticField, true);
      return new MethodHandleFieldAccessor(field, reflexion, getter, setter);
    } catch (Exception exception) {
      throw new ReflexionException(exception);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NotNull MethodAccessor<Method> wrapMethod(@NotNull Reflexion reflexion, @NotNull Method method) {
    try {
      MethodHandle unreflected = this.trustedLookup.unreflect(method);
      boolean staticMethod = Modifier.isStatic(method.getModifiers());
      return new MethodHandleMethodAccessor(method, reflexion, this.convertToGeneric(unreflected, staticMethod, false));
    } catch (Exception exception) {
      throw new ReflexionException(exception);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NotNull MethodAccessor<Constructor<?>> wrapConstructor(@NotNull Reflexion rfx, @NotNull Constructor<?> ctr) {
    try {
      MethodHandle unreflected = this.trustedLookup.unreflectConstructor(ctr);
      return new MethodHandleConstructorAccessor(ctr, rfx, this.convertToGeneric(unreflected, false, true));
    } catch (Exception exception) {
      throw new ReflexionException(exception);
    }
  }

  /**
   * Gets the IMPL_LOOKUP field instance if possible. This method does not throw an exception but returns null if the
   * field is not accessible for some reason.
   *
   * @return the IMPL_LOOKUP field instance, null if the lookup is not possible.
   */
  protected @Nullable Lookup getTrustedLookup() {
    return ImplLookupAccessor.findImplLookup();
  }

  /**
   * Converts the given method handle to a generic handle which can be invoked with an array of objects and returns an
   * object rather than requiring exact class instances.
   *
   * @param handle       the handle to generify.
   * @param staticMethod if the method wrapped by the given method handle is static.
   * @param ctor         if the method wrapped by the given method handle is a constructor.
   * @return a generified version of the given method handle.
   * @throws NullPointerException if the given method handle is null.
   */
  private @NotNull MethodHandle convertToGeneric(@NotNull MethodHandle handle, boolean staticMethod, boolean ctor) {
    MethodHandle target = handle.asFixedArity();

    // special case: we do not need the trailing array if we have 0 arguments anyway
    int paramCount = handle.type().parameterCount() - (ctor || staticMethod ? 0 : 1);
    MethodType methodType = MethodType.genericMethodType(ctor ? 0 : 1, paramCount > 0);
    if (paramCount > 0) {
      // spread the arguments we give into the handle only if we're not targeting a no-args method
      target = target.asSpreader(Object[].class, paramCount);
    }

    // drop leading 'this' argument on static methods
    if (staticMethod) {
      target = MethodHandles.dropArguments(target, 0, Object.class);
    }

    // convert target method handle to generic type
    return target.asType(methodType);
  }

  /**
   * Converts the given field to a generic handle which can be invoked using an object and returns an object rather than
   * requiring exact class instances.
   *
   * @param field       the field to wrap in a method handle.
   * @param staticField if the given field is static.
   * @param set         if the returned method handle should be a setter for the given field.
   * @return a method handle which can be used to either get or set the value of the given field.
   * @throws Exception            if any exception occurs during the field unreflection.
   * @throws NullPointerException if the given field is null.
   */
  private @NotNull MethodHandle convertFieldToGeneric(
    @NotNull Field field,
    boolean staticField,
    boolean set
  ) throws Exception {
    // we need to do this as unreflecting the field will cause java to throw exceptions when we access trusted final fields
    MethodHandle handle;
    if (staticField) {
      handle = set
        ? this.trustedLookup.findStaticSetter(field.getDeclaringClass(), field.getName(), field.getType())
        : this.trustedLookup.findStaticGetter(field.getDeclaringClass(), field.getName(), field.getType());
    } else {
      handle = set
        ? this.trustedLookup.findSetter(field.getDeclaringClass(), field.getName(), field.getType())
        : this.trustedLookup.findGetter(field.getDeclaringClass(), field.getName(), field.getType());
    }

    // generify the method type so that we don't need to worry about it when using the handles
    MethodType mt;
    if (staticField) {
      mt = set ? MethodType.methodType(void.class, Object.class) : MethodType.methodType(Object.class);
    } else {
      mt = set
        ? MethodType.methodType(void.class, Object.class, Object.class)
        : MethodType.methodType(Object.class, Object.class);
    }

    return handle.asType(mt);
  }

  /**
   * An accessor for field get/set access using method handles.
   *
   * @since 1.0
   */
  private static final class MethodHandleFieldAccessor implements FieldAccessor {

    private final Field field;
    private final Reflexion reflexion;

    private final MethodHandle getter;
    private final MethodHandle setter;

    /**
     * Constructs a new method handle field accessor instance.
     *
     * @param field     the field which is wrapped by the new accessor.
     * @param reflexion the reflexion instance which produced the reflection lookup.
     * @param getter    the getter method handle for the given field.
     * @param setter    the setter method handle for the given field.
     */
    public MethodHandleFieldAccessor(
      @NotNull Field field,
      @NotNull Reflexion reflexion,
      @NotNull MethodHandle getter,
      @NotNull MethodHandle setter
    ) {
      this.field = field;
      this.reflexion = reflexion;
      this.getter = getter;
      this.setter = setter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Field getMember() {
      return this.field;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Reflexion getReflexion() {
      return this.reflexion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull <T> Result<T> getValue() {
      return this.getValue(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <T> Result<T> getValue(@Nullable Object instance) {
      return Result.tryExecute(() -> {
        Object binding = Util.getBinding(this.reflexion, instance, this.field.getModifiers());
        return (T) (binding == null ? this.getter.invoke() : this.getter.invoke(binding));
      });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Result<Void> setValue(@Nullable Object value) {
      return this.setValue(null, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Result<Void> setValue(@Nullable Object instance, @Nullable Object value) {
      return Result.tryExecute(() -> {
        Object binding = Util.getBinding(this.reflexion, instance, this.field.getModifiers());
        if (binding == null) {
          this.setter.invoke(value);
        } else {
          this.setter.invoke(binding, value);
        }

        return null;
      });
    }
  }

  /**
   * A method accessor based on method handles.
   *
   * @since 1.0
   */
  private static final class MethodHandleMethodAccessor implements MethodAccessor<Method> {

    private final Method method;
    private final Reflexion reflexion;
    private final MethodHandle methodHandle;

    /**
     * Constructs a new method handle method accessor instance.
     *
     * @param method       the method which is wrapped by this accessor.
     * @param reflexion    the reflexion instance which produced the lookup call.
     * @param methodHandle the method handle to get the method value.
     */
    public MethodHandleMethodAccessor(
      @NotNull Method method,
      @NotNull Reflexion reflexion,
      @NotNull MethodHandle methodHandle
    ) {
      this.method = method;
      this.reflexion = reflexion;
      this.methodHandle = methodHandle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Method getMember() {
      return this.method;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Reflexion getReflexion() {
      return this.reflexion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull <V> Result<V> invoke() {
      return this.invoke(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <V> Result<V> invoke(@Nullable Object instance) {
      return Result.tryExecute(() -> {
        Object binding = Util.getBinding(this.reflexion, instance, this.method.getModifiers());
        return (V) this.methodHandle.invoke(binding);
      });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull <V> Result<V> invokeWithArgs(@NotNull Object... args) {
      return this.invoke(null, args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <V> Result<V> invoke(@Nullable Object instance, @NotNull Object... args) {
      if (args.length == 0) {
        // cannot provide an empty args array to the method handle due to the generic type,
        // we need to use invoke() instead as the method skips providing the args array
        return this.invoke(instance);
      } else {
        return Result.tryExecute(() -> {
          Object binding = Util.getBinding(this.reflexion, instance, this.method.getModifiers());
          return (V) this.methodHandle.invoke(binding, args);
        });
      }
    }
  }

  /**
   * A method accessor for constructors which is based on method handles.
   *
   * @since 1.0
   */
  private static final class MethodHandleConstructorAccessor implements MethodAccessor<Constructor<?>> {

    private final Reflexion reflexion;
    private final Constructor<?> method;
    private final MethodHandle methodHandle;

    /**
     * Constructs a new method handle constructor accessor instance.
     *
     * @param method       the constructor which is wrapped by the accessor.
     * @param reflexion    the reflexion instance which produced the reflection lookup call.
     * @param methodHandle the method handle to invoke the constructor.
     */
    public MethodHandleConstructorAccessor(
      @NotNull Constructor<?> method,
      @NotNull Reflexion reflexion,
      @NotNull MethodHandle methodHandle
    ) {
      this.method = method;
      this.reflexion = reflexion;
      this.methodHandle = methodHandle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Constructor<?> getMember() {
      return this.method;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Reflexion getReflexion() {
      return this.reflexion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull <V> Result<V> invoke() {
      return this.invoke(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <V> Result<V> invoke(@Nullable Object instance) {
      return Result.tryExecute(() -> (V) this.methodHandle.invoke());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull <V> Result<V> invokeWithArgs(@NotNull Object... args) {
      return this.invoke(null, args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <V> Result<V> invoke(@Nullable Object instance, @NotNull Object... args) {
      if (args.length == 0) {
        // cannot provide an empty args array to the method handle due to the generic type,
        // we need to use invoke() instead as the method skips providing the args array
        return this.invoke(null);
      } else {
        return Result.tryExecute(() -> (V) this.methodHandle.invoke(args));
      }
    }
  }
}
