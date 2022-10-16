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

package dev.derklaro.reflexion.internal.bare;

import dev.derklaro.reflexion.AccessorFactory;
import dev.derklaro.reflexion.FieldAccessor;
import dev.derklaro.reflexion.MethodAccessor;
import dev.derklaro.reflexion.Reflexion;
import dev.derklaro.reflexion.ReflexionException;
import dev.derklaro.reflexion.Result;
import dev.derklaro.reflexion.internal.unsafe.UnsafeAccessibleObject;
import dev.derklaro.reflexion.internal.util.Util;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * An accessor factory which creates accessors based on the plain java.lang.reflect members.
 *
 * @since 1.0
 */
public final class BareAccessorFactory implements AccessorFactory {

  /**
   * Makes the given accessible object accessible, either using force through sun.misc.Unsafe or by using direct access
   * to AccessibleObject.setAccessible. Exceptions caused by this method are not caught.
   * <p>
   * On Java 9 or later this method might throw an InaccessibleObjectException.
   *
   * @param accessibleObject the object ot make accessible.
   * @throws NullPointerException if the given object is null.
   * @since 1.6
   */
  private static void makeAccessible(@NonNull AccessibleObject accessibleObject) {
    if (UnsafeAccessibleObject.isAvailable()) {
      // yes, we can use force
      UnsafeAccessibleObject.makeAccessible(accessibleObject);
    } else {
      // no, just try to do it the normal way
      accessibleObject.setAccessible(true);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isAvailable() {
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull FieldAccessor wrapField(@NonNull Reflexion reflexion, @NonNull Field field) {
    try {
      makeAccessible(field);
      return new BareFieldAccessor(field, reflexion);
    } catch (Exception exception) {
      throw new ReflexionException(exception);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull MethodAccessor<Method> wrapMethod(@NonNull Reflexion reflexion, @NonNull Method method) {
    try {
      makeAccessible(method);
      return new BareMethodAccessor(method, reflexion);
    } catch (Exception exception) {
      throw new ReflexionException(exception);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull MethodAccessor<Constructor<?>> wrapConstructor(@NonNull Reflexion rfx, @NonNull Constructor<?> ctr) {
    try {
      makeAccessible(ctr);
      return new BareConstructorAccessor(rfx, ctr);
    } catch (Exception exception) {
      throw new ReflexionException(exception);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int compareTo(@NonNull AccessorFactory o) {
    // always prefer other factories over this one
    return o instanceof BareAccessorFactory ? 0 : 1;
  }

  /**
   * A field accessor which invokes the bare java.lang.reflect field.
   *
   * @since 1.0
   */
  private static final class BareFieldAccessor implements FieldAccessor {

    private final Field field;
    private final Reflexion reflexion;

    /**
     * Constructs a new bare field accessor instance.
     *
     * @param field     the field to wrap.
     * @param reflexion the reflexion instance which produced the instance.
     */
    public BareFieldAccessor(Field field, Reflexion reflexion) {
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
      return this.getValue(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public @NonNull <T> Result<T> getValue(@Nullable Object instance) {
      return Result.tryExecute(() -> {
        Object binding = Util.getBinding(this.reflexion, instance, this.field.getModifiers());
        return (T) this.field.get(binding);
      });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NonNull Result<Void> setValue(@Nullable Object value) {
      return this.setValue(null, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NonNull Result<Void> setValue(@Nullable Object instance, @Nullable Object value) {
      return Result.tryExecute(() -> {
        Object binding = Util.getBinding(this.reflexion, instance, this.field.getModifiers());
        this.field.set(binding, value);

        return null;
      });
    }
  }

  /**
   * A method accessor which invokes the given bare method.
   *
   * @since 1.0
   */
  private static final class BareMethodAccessor implements MethodAccessor<Method> {

    private final Method method;
    private final Reflexion reflexion;

    /**
     * Constructs a new bare method accessor instance.
     *
     * @param method    the method to wrap.
     * @param reflexion the reflexion instance which produced this instance.
     */
    public BareMethodAccessor(Method method, Reflexion reflexion) {
      this.method = method;
      this.reflexion = reflexion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NonNull Method getMember() {
      return this.method;
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
    public @NonNull <V> Result<V> invoke() {
      return this.invoke(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public @NonNull <V> Result<V> invoke(@Nullable Object instance) {
      return Result.tryExecute(() -> {
        Object binding = Util.getBinding(this.reflexion, instance, this.method.getModifiers());
        return (V) this.method.invoke(binding);
      });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NonNull <V> Result<V> invokeWithArgs(@NonNull Object... args) {
      return this.invoke(null, args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public @NonNull <V> Result<V> invoke(@Nullable Object instance, @NonNull Object... args) {
      return Result.tryExecute(() -> {
        Object binding = Util.getBinding(this.reflexion, instance, this.method.getModifiers());
        return (V) this.method.invoke(binding, args);
      });
    }
  }

  /**
   * A constructor accessor which invokes constructors based on the given bare constructor.
   *
   * @since 1.0
   */
  private static final class BareConstructorAccessor implements MethodAccessor<Constructor<?>> {

    private final Reflexion reflexion;
    private final Constructor<?> constructor;

    /**
     * Constructs a new bare constructor accessor instance.
     *
     * @param reflexion   the reflexion which produced this instance.
     * @param constructor the constructor to wrap.
     */
    public BareConstructorAccessor(Reflexion reflexion, Constructor<?> constructor) {
      this.reflexion = reflexion;
      this.constructor = constructor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NonNull Constructor<?> getMember() {
      return this.constructor;
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
    @SuppressWarnings("unchecked")
    public @NonNull <V> Result<V> invoke() {
      return Result.tryExecute(() -> (V) this.constructor.newInstance());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NonNull <V> Result<V> invoke(@Nullable Object instance) {
      return this.invoke();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public @NonNull <V> Result<V> invokeWithArgs(@NonNull Object... args) {
      return Result.tryExecute(() -> (V) this.constructor.newInstance(args));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NonNull <V> Result<V> invoke(@Nullable Object instance, @NonNull Object... args) {
      return this.invokeWithArgs(args);
    }
  }
}
