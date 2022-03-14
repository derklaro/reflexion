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
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
      boolean staticField = Modifier.isStatic(field.getModifiers());

      MethodHandle getter = this.convertFieldToGeneric(field, staticField, false);
      MethodHandle setter = this.convertFieldToGeneric(field, staticField, true);

      return new MethodHandleFieldAccessor(field, reflexion, getter, setter);
    } catch (Exception exception) {
      throw new ReflexionException(exception);
    }
  }

  @Override
  public @NonNull MethodAccessor<Method> wrapMethod(@NonNull Reflexion reflexion, @NonNull Method method) {
    try {
      MethodHandle unreflected = this.getLookup().unreflect(method);
      boolean staticMethod = Modifier.isStatic(method.getModifiers());
      return new MethodHandleMethodAccessor(method, reflexion, this.convertToGeneric(unreflected, staticMethod, false));
    } catch (Exception exception) {
      throw new ReflexionException(exception);
    }
  }

  @Override
  public @NonNull MethodAccessor<Constructor<?>> wrapConstructor(@NonNull Reflexion rfx, @NonNull Constructor<?> ctr) {
    try {
      MethodHandle unreflected = this.getLookup().unreflectConstructor(ctr);
      return new MethodHandleConstructorAccessor(ctr, rfx, this.convertToGeneric(unreflected, false, true));
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

  private @NonNull MethodHandle convertToGeneric(@NonNull MethodHandle handle, boolean staticMethod, boolean ctor) {
    MethodHandle target = handle.asFixedArity();
    // special thing - we do not need the trailing array if we have 0 arguments anyway
    int paramCount = handle.type().parameterCount() - (ctor || staticMethod ? 0 : 1);
    MethodType methodType = MethodType.genericMethodType(ctor ? 0 : 1, paramCount > 0);
    if (paramCount > 0) {
      // spread the arguments we give into the handle only if we're not targeting a no-args method
      target = target.asSpreader(Object[].class, paramCount);
    }
    // adds a leading 'this' argument which we can ignore
    if (staticMethod) {
      target = MethodHandles.dropArguments(target, 0, Object.class);
    }
    // convert the type to finish
    return target.asType(methodType);
  }

  private @NonNull MethodHandle convertFieldToGeneric(
    @NonNull Field field,
    boolean staticField,
    boolean set
  ) throws Exception {
    // we need to do this as unreflecting the field will cause java to throw exceptions when we access trusted final fields
    MethodHandle handle;
    if (staticField) {
      handle = set
        ? this.getLookup().findStaticSetter(field.getDeclaringClass(), field.getName(), field.getType())
        : this.getLookup().findStaticGetter(field.getDeclaringClass(), field.getName(), field.getType());
    } else {
      handle = set
        ? this.getLookup().findSetter(field.getDeclaringClass(), field.getName(), field.getType())
        : this.getLookup().findGetter(field.getDeclaringClass(), field.getName(), field.getType());
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

  @Override
  public int compareTo(@NonNull AccessorFactory o) {
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
    public @NonNull Field getMember() {
      return this.field;
    }

    @Override
    public @NonNull Reflexion getReflexion() {
      return this.reflexion;
    }

    @Override
    public @NonNull <T> Result<T> getValue() {
      return this.getValue(Modifier.isStatic(this.field.getModifiers()) ? null : this.reflexion.getBinding());
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull <T> Result<T> getValue(@Nullable Object instance) {
      return Result.tryExecute(() -> {
        if (Modifier.isStatic(this.field.getModifiers())) {
          // no need for the instance, ignore it
          return (T) this.getter.invoke();
        } else {
          // we need to give the instance
          return (T) this.getter.invoke(instance);
        }
      });
    }

    @Override
    public @NonNull Result<Void> setValue(@Nullable Object value) {
      return this.setValue(Modifier.isStatic(this.field.getModifiers()) ? null : this.reflexion.getBinding(), value);
    }

    @Override
    public @NonNull Result<Void> setValue(@Nullable Object instance, @Nullable Object value) {
      return Result.tryExecute(() -> {
        if (Modifier.isStatic(this.field.getModifiers())) {
          // no need for the instance, ignore it
          this.setter.invoke(value);
        } else {
          // we need to give the instance
          this.setter.invoke(instance, value);
        }
        return null;
      });
    }
  }

  private static final class MethodHandleMethodAccessor implements MethodAccessor<Method> {

    private final Method method;
    private final Reflexion reflexion;
    private final MethodHandle methodHandle;

    public MethodHandleMethodAccessor(Method method, Reflexion reflexion, MethodHandle methodHandle) {
      this.method = method;
      this.reflexion = reflexion;
      this.methodHandle = methodHandle;
    }

    @Override
    public @NonNull Method getMember() {
      return this.method;
    }

    @Override
    public @NonNull Reflexion getReflexion() {
      return this.reflexion;
    }

    @Override
    public @NonNull <V> Result<V> invoke() {
      return this.invoke(Modifier.isStatic(this.method.getModifiers()) ? null : this.reflexion.getBinding());
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull <V> Result<V> invoke(@Nullable Object instance) {
      return Result.tryExecute(() -> (V) this.methodHandle.invoke(instance));
    }

    @Override
    public @NonNull <V> Result<V> invokeWithArgs(@NonNull Object... args) {
      return this.invoke(Modifier.isStatic(this.method.getModifiers()) ? null : this.reflexion.getBinding(), args);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull <V> Result<V> invoke(@Nullable Object instance, @NonNull Object... args) {
      // convert no-args actually to a no-args call
      if (args.length == 0) {
        return this.invoke(instance);
      } else {
        return Result.tryExecute(() -> (V) this.methodHandle.invoke(instance, args));
      }
    }
  }

  private static final class MethodHandleConstructorAccessor implements MethodAccessor<Constructor<?>> {

    private final Reflexion reflexion;
    private final Constructor<?> method;
    private final MethodHandle methodHandle;

    public MethodHandleConstructorAccessor(Constructor<?> method, Reflexion reflexion, MethodHandle methodHandle) {
      this.method = method;
      this.reflexion = reflexion;
      this.methodHandle = methodHandle;
    }

    @Override
    public @NonNull Constructor<?> getMember() {
      return this.method;
    }

    @Override
    public @NonNull Reflexion getReflexion() {
      return this.reflexion;
    }

    @Override
    public @NonNull <V> Result<V> invoke() {
      return this.invoke(null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull <V> Result<V> invoke(@Nullable Object instance) {
      return Result.tryExecute(() -> (V) this.methodHandle.invoke());
    }

    @Override
    public @NonNull <V> Result<V> invokeWithArgs(@NotNull @NonNull Object... args) {
      return this.invoke(null, args);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull <V> Result<V> invoke(@Nullable Object instance, @NotNull @NonNull Object... args) {
      // convert no args calls back to an actual no-args invocation
      if (args.length == 0) {
        return this.invoke(null);
      } else {
        return Result.tryExecute(() -> (V) this.methodHandle.invoke(args));
      }
    }
  }
}
