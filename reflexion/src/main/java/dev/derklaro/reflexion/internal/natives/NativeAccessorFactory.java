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

package dev.derklaro.reflexion.internal.natives;

import dev.derklaro.reflexion.FieldAccessor;
import dev.derklaro.reflexion.Reflexion;
import dev.derklaro.reflexion.ReflexionException;
import dev.derklaro.reflexion.Result;
import dev.derklaro.reflexion.internal.handles.MethodHandleAccessorFactory;
import dev.derklaro.reflexion.internal.util.Type;
import dev.derklaro.reflexion.internal.util.Util;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * A factory which produces accessors for fields based on native code. Method and constructor accessors are based on
 * trusted method handles.
 *
 * @since 1.0
 */
public final class NativeAccessorFactory extends MethodHandleAccessorFactory {

  private static final boolean NATIVE_LOADED = NativeLibLoader.tryLoadNative();
  // native getters
  private static final Map<Class<?>, NativeFieldGetter<Object>> GETTERS = Util.newMapFromArray(
    boolean.class, (NativeFieldGetter<Object>) FNativeReflect::GetZFieldValue,
    byte.class, (NativeFieldGetter<Object>) FNativeReflect::GetBFieldValue,
    char.class, (NativeFieldGetter<Object>) FNativeReflect::GetCFieldValue,
    short.class, (NativeFieldGetter<Object>) FNativeReflect::GetSFieldValue,
    int.class, (NativeFieldGetter<Object>) FNativeReflect::GetIFieldValue,
    long.class, (NativeFieldGetter<Object>) FNativeReflect::GetJFieldValue,
    float.class, (NativeFieldGetter<Object>) FNativeReflect::GetFFieldValue,
    double.class, (NativeFieldGetter<Object>) FNativeReflect::GetDFieldValue);
  // native setters
  private static final Map<Class<?>, NativeFieldSetter> SETTERS = Util.newMapFromArray(
    boolean.class, (NativeFieldSetter) (o, n, i, val) -> FNativeReflect.SetZFieldValue(o, n, i, (boolean) val),
    byte.class, (NativeFieldSetter) (o, n, i, val) -> FNativeReflect.SetBFieldValue(o, n, i, (byte) val),
    char.class, (NativeFieldSetter) (o, n, i, val) -> FNativeReflect.SetCFieldValue(o, n, i, (char) val),
    short.class, (NativeFieldSetter) (o, n, i, val) -> FNativeReflect.SetSFieldValue(o, n, i, (short) val),
    int.class, (NativeFieldSetter) (o, n, i, val) -> FNativeReflect.SetIFieldValue(o, n, i, (int) val),
    long.class, (NativeFieldSetter) (o, n, i, val) -> FNativeReflect.SetJFieldValue(o, n, i, (long) val),
    float.class, (NativeFieldSetter) (o, n, i, val) -> FNativeReflect.SetFFieldValue(o, n, i, (float) val),
    double.class, (NativeFieldSetter) (o, n, i, val) -> FNativeReflect.SetDFieldValue(o, n, i, (double) val));

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isAvailable() {
    return NATIVE_LOADED;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull FieldAccessor wrapField(@NonNull Reflexion reflexion, @NonNull Field field) {
    return new NativeFieldAccessor(field, reflexion);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected @Nullable Lookup getTrustedLookup() {
    try {
      return (Lookup) FNativeReflect.GetObjectFieldValue(
        Type.getQualifiedName(Lookup.class),
        "IMPL_LOOKUP",
        Type.getSignature(Lookup.class),
        null);
    } catch (Exception exception) {
      return super.getTrustedLookup();
    }
  }

  /**
   * A functional interface which can be used to get the value of a field.
   *
   * @param <T> the type returned by the accessor.
   * @since 1.0
   */
  @FunctionalInterface
  private interface NativeFieldGetter<T> {

    /**
     * Get the value of a field.
     *
     * @param declaringClass the name of the class which is declaring the field.
     * @param name           the name of the field to get.
     * @param instance       the instance to get the field on.
     * @return the value of the field.
     */
    T getFieldValue(String declaringClass, String name, Object instance);
  }

  /**
   * A functional interface which can be used to set the value of a field.
   *
   * @since 1.0
   */
  @FunctionalInterface
  private interface NativeFieldSetter {

    /**
     * Sets the value of a field.
     *
     * @param declaringClass the name of the class which is declaring the field.
     * @param name           the name of the field to set.
     * @param instance       the instance to set the field in.
     * @param val            the new value of the field.
     */
    void setFieldValue(String declaringClass, String name, Object instance, Object val);
  }

  /**
   * Represents an accessor for a field which uses native code to get/set the value of a field.
   *
   * @since 1.0
   */
  private static final class NativeFieldAccessor implements FieldAccessor {

    private final Field field;
    private final Reflexion reflexion;

    /**
     * Constructs a new native field accessor instance.
     *
     * @param field     the field targeted by this accessor.
     * @param reflexion the reflexion instance which produced the lookup call.
     * @throws NullPointerException if the given field or reflexion instance is null.
     */
    public NativeFieldAccessor(@NonNull Field field, @NonNull Reflexion reflexion) {
      this.field = field;
      this.reflexion = reflexion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NonNull Field getMember() {
      return this.field;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NonNull Reflexion getReflexion() {
      return this.reflexion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NonNull <T> Result<T> getValue() {
      return this.getValue(Modifier.isStatic(this.field.getModifiers()) ? null : this.reflexion.getBinding());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NonNull Result<Void> setValue(@Nullable Object value) {
      return this.setValue(Modifier.isStatic(this.field.getModifiers()) ? null : this.reflexion.getBinding(), value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public @NonNull <T> Result<T> getValue(@Nullable Object instance) {
      return Result.tryExecute(() -> {
        // that's not a thing...
        if (instance == null && !Modifier.isStatic(this.field.getModifiers())) {
          throw new ReflexionException("Requires an instance to be given to call non-static field!");
        }

        // primitive types
        if (this.field.getType().isPrimitive()) {
          // can never be null
          NativeFieldGetter<Object> getter = GETTERS.get(this.field.getType());
          return (T) getter.getFieldValue(
            Type.getQualifiedName(this.field.getDeclaringClass()),
            this.field.getName(),
            instance);
        } else {
          // object field
          return (T) FNativeReflect.GetObjectFieldValue(
            Type.getQualifiedName(this.field.getDeclaringClass()),
            this.field.getName(),
            Type.getFieldSignature(this.field),
            instance);
        }
      });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NonNull Result<Void> setValue(@Nullable Object instance, @Nullable Object value) {
      return Result.tryExecute(() -> {
        // that's not a thing...
        if (instance == null && !Modifier.isStatic(this.field.getModifiers())) {
          throw new ReflexionException("Requires an instance to be given to call non-static field!");
        }

        // primitive types
        if (this.field.getType().isPrimitive()) {
          if (value == null) {
            // that is not a thing
            throw new ReflexionException("Cannot set primitive field to null");
          }

          // can never be null
          NativeFieldSetter setter = SETTERS.get(this.field.getType());
          setter.setFieldValue(
            Type.getQualifiedName(this.field.getDeclaringClass()),
            this.field.getName(),
            instance,
            value);
          // do not proceed
          return null;
        }

        // ensure that we're not going to assign invalid values
        if (value != null && !this.field.getType().isAssignableFrom(value.getClass())) {
          throw new ReflexionException("Unable to assign " + value + " to field of type " + this.field.getType());
        }

        // set the field value
        FNativeReflect.SetObjectFieldValue(
          Type.getQualifiedName(this.field.getDeclaringClass()),
          this.field.getName(),
          Type.getFieldSignature(this.field),
          instance,
          value);
        return null;
      });
    }
  }
}
