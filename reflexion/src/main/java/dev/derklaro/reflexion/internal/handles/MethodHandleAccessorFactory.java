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

package dev.derklaro.reflexion.internal.handles;

import dev.derklaro.reflexion.AccessorFactory;
import dev.derklaro.reflexion.FieldAccessor;
import dev.derklaro.reflexion.MethodAccessor;
import dev.derklaro.reflexion.Reflexion;
import dev.derklaro.reflexion.ReflexionException;
import dev.derklaro.reflexion.Result;
import dev.derklaro.reflexion.internal.bare.BareAccessorFactory;
import dev.derklaro.reflexion.internal.natives.NativeAccessorFactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MethodHandleAccessorFactory implements AccessorFactory {

  private final Lookup trustedLookup;

  public MethodHandleAccessorFactory() {
    this.trustedLookup = this.getTrustedLookup();
  }

  @Override
  public boolean isAvailable() {
    return true;
  }

  @Override
  public @NonNull FieldAccessor wrapField(@NonNull Reflexion reflexion, @NonNull Field field) {
    try {
      MethodHandle getter = this.getLookup().unreflectGetter(field);
      MethodHandle setter = this.getLookup().unreflectSetter(field);

      return new MethodHandleFieldAccessor(field, reflexion, getter, setter);
    } catch (Exception exception) {
      throw new ReflexionException(exception);
    }
  }

  @Override
  public @NonNull MethodAccessor<Method> wrapMethod(@NonNull Reflexion reflexion, @NonNull Method method) {
    try {
      MethodHandle handle = this.getLookup().unreflect(method);
      return new MethodHandleMethodAccessor<>(method, reflexion, handle);
    } catch (Exception exception) {
      throw new ReflexionException(exception);
    }
  }

  @Override
  public @NonNull MethodAccessor<Constructor<?>> wrapConstructor(@NonNull Reflexion rfx, @NonNull Constructor<?> ctr) {
    try {
      MethodHandle handle = this.getLookup().unreflectConstructor(ctr);
      return new MethodHandleMethodAccessor<>(ctr, rfx, handle);
    } catch (Exception exception) {
      throw new ReflexionException(exception);
    }
  }

  protected @Nullable Lookup getTrustedLookup() {
    return UnsafeFieldAccessor.findImplLookup();
  }

  private @NonNull Lookup getLookup() {
    return this.trustedLookup == null ? MethodHandles.lookup() : this.trustedLookup;
  }

  @Override
  public int compareTo(@NotNull AccessorFactory o) {
    // always prefer native over this one
    if (o instanceof NativeAccessorFactory) {
      return 1;
    }

    // prefer the bare one over this one if we don't have the trusted lookup
    if (o instanceof BareAccessorFactory) {
      return this.trustedLookup != null ? -1 : 1;
    }

    // prefer the handle based accessor with the trusted lookup
    if (o instanceof MethodHandleAccessorFactory) {
      return this.trustedLookup != null ? -1 : ((MethodHandleAccessorFactory) o).trustedLookup != null ? 1 : 0;
    }

    // no opinion
    return 0;
  }

  private static final class MethodHandleFieldAccessor implements FieldAccessor {

    private final Field field;
    private final Reflexion reflexion;

    private final MethodHandle getter;
    private final MethodHandle setter;

    public MethodHandleFieldAccessor(Field field, Reflexion reflexion, MethodHandle getter, MethodHandle setter) {
      this.field = field;
      this.reflexion = reflexion;
      this.getter = getter;
      this.setter = setter;
    }

    @Override
    public @NonNull Field getField() {
      return this.field;
    }

    @Override
    public @NonNull <T> Result<T> getValue() {
      return this.getValue(this.reflexion.getBinding());
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull <T> Result<T> getValue(@Nullable Object instance) {
      return Result.tryExecute(() -> (T) this.getter.invoke(instance));
    }

    @Override
    public @NonNull Result<Void> setValue(@Nullable Object value) {
      return this.setValue(this.reflexion.getBinding(), value);
    }

    @Override
    public @NonNull Result<Void> setValue(@Nullable Object instance, @Nullable Object value) {
      return Result.tryExecute(() -> {
        this.setter.invoke(instance, value);
        return null;
      });
    }
  }

  private static final class MethodHandleMethodAccessor<T extends Executable> implements MethodAccessor<T> {

    private final T method;
    private final Reflexion reflexion;
    private final MethodHandle methodHandle;

    public MethodHandleMethodAccessor(T method, Reflexion reflexion, MethodHandle methodHandle) {
      this.method = method;
      this.reflexion = reflexion;
      this.methodHandle = methodHandle;
    }

    @Override
    public @NonNull T getMethod() {
      return this.method;
    }

    @Override
    public @NonNull <V> Result<V> invoke() {
      return this.invoke(this.reflexion.getBinding());
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull <V> Result<V> invoke(@Nullable Object instance) {
      return Result.tryExecute(() -> (V) this.methodHandle.invoke(instance));
    }

    @Override
    public @NonNull <V> Result<V> invoke(@NotNull @NonNull Object... args) {
      return this.invoke(this.reflexion.getBinding(), args);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull <V> Result<V> invoke(@Nullable Object instance, @NotNull @NonNull Object... args) {
      return Result.tryExecute(() -> (V) this.methodHandle.invoke(instance, args));
    }
  }
}
