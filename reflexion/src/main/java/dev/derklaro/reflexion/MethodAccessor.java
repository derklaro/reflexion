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

package dev.derklaro.reflexion;

import java.lang.reflect.Executable;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * An accessor which wraps a java.lang.reflect Method and makes it much easier to invoke it.
 *
 * @param <T> the type of the underlying executable, either a method or constructor.
 * @since 1.0
 */
public interface MethodAccessor<T extends Executable> extends BaseAccessor<T> {

  /**
   * Invokes the underlying method using the instance the reflexion object used to create this accessor is bound to (if
   * the method is not static). This method never throws an exception all exceptions are given back to the caller in the
   * returned result instance.
   * <p>
   * This method invokes the wrapped method without any arguments.
   *
   * @param <V> the type of values returned by the wrapped method.
   * @return the result instance of the invocation, either holding the result of the method or any exception thrown.
   */
  @NonNull <V> Result<V> invoke();

  /**
   * Invokes the underlying method using the given instance. This method never throws an exception all exceptions are
   * given back to the caller in the returned result instance.
   * <p>
   * This method invokes the wrapped method without any arguments. The given instance should be present in case the
   * method is not static, and the associated reflexion instance has no instance bound to it. If an instance is given in
   * case the method is static, the instance will be ignored silently.
   *
   * @param instance the instance to invoke the wrapped method on.
   * @param <V>      the type of values returned by the wrapped method.
   * @return the result instance of the invocation, either holding the result of the method or any exception thrown.
   */
  @NonNull <V> Result<V> invoke(@Nullable Object instance);

  /**
   * Invokes the underlying method using the instance the reflexion object used to create this accessor is bound to (if
   * the method is not static). This method never throws an exception all exceptions are given back to the caller in the
   * returned result instance.
   *
   * @param args the arguments to use on when invoking the wrapped method.
   * @param <V>  the type of values returned by the wrapped method.
   * @return the result instance of the invocation, either holding the result of the method or any exception thrown.
   */
  @NonNull <V> Result<V> invokeWithArgs(@NonNull Object... args);

  /**
   * Invokes the underlying method using the given instance. This method never throws an exception all exceptions are
   * given back to the caller in the returned result instance.
   * <p>
   * The given instance should be present in case the method is not static, and the associated reflexion instance has no
   * instance bound to it. If an instance is given in case the method is static, the instance will be ignored silently.
   *
   * @param instance the instance to invoke the wrapped method on.
   * @param args     the arguments to use on when invoking the wrapped method.
   * @param <V>      the type of values returned by the wrapped method.
   * @return the result instance of the invocation, either holding the result of the method or any exception thrown.
   */
  @NonNull <V> Result<V> invoke(@Nullable Object instance, @NonNull Object... args);
}
