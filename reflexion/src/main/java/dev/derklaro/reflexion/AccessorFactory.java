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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import lombok.NonNull;

/**
 * A conversion factory from java reflection types to reflexion wrappers of them. A factory should be designed in a way
 * that there is no exception thrown when initializing it. Exceptions should be ignored but {@link #isAvailable()}
 * should then return false to indicate that the factory is not available for the current environment.
 * <p>
 * When the default factory is chosen it will be done in two steps:
 * <ol>
 *   <li>Check if the factory is available.
 *   <li>Sort all factories based on their comparison overridden method and choosing the topmost one.
 * </ol>
 * <p>
 * External factories are accepted as well when being provided as a service to the current jvm. Note that the default
 * factories will always be under consideration as well.
 *
 * @since 1.0
 */
public interface AccessorFactory extends Comparable<AccessorFactory> {

  /**
   * Checks if this factory is available. This method should never throw an exception but can decide the return value
   * based on one.
   *
   * @return true if this factory is available, false otherwise.
   */
  boolean isAvailable();

  /**
   * Wraps the given java.lang.reflect field into a reflexion FieldAccessor wrapper.
   *
   * @param reflexion the base instance which tries to convert the field.
   * @param field     the field to wrap.
   * @return a wrapping accessor for the given field.
   * @throws NullPointerException if the given reflexion instance or field is null.
   * @throws ReflexionException   if the factory is unable to wrap the given field.
   */
  @NonNull FieldAccessor wrapField(@NonNull Reflexion reflexion, @NonNull Field field);

  /**
   * Wraps the given java.lang.reflect method into a reflexion MethodAccessor wrapper.
   *
   * @param reflexion the base instance which tries to convert the method.
   * @param method    the method to wrap.
   * @return a wrapping accessor for the given method.
   * @throws NullPointerException if the given reflexion instance or method is null.
   * @throws ReflexionException   if the factory is unable to wrap the given method.
   */
  @NonNull MethodAccessor<Method> wrapMethod(@NonNull Reflexion reflexion, @NonNull Method method);

  /**
   * Wraps the given java.lang.reflect constructor into a reflexion MethodAccessor wrapper.
   *
   * @param reflexion the base instance which tries to convert the constructor.
   * @param ct        the constructor to wrap.
   * @return a wrapping accessor for the given constructor.
   * @throws NullPointerException if the given reflexion instance or constructor is null.
   * @throws ReflexionException   if the factory is unable to wrap the given constructor.
   */
  @NonNull MethodAccessor<Constructor<?>> wrapConstructor(@NonNull Reflexion reflexion, @NonNull Constructor<?> ct);
}
