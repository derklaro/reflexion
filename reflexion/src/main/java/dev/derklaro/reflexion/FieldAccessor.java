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

import java.lang.reflect.Field;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * An accessor which wraps a java.lang.reflect Field and makes it much easier to read/write from/to it.
 *
 * @since 1.0
 */
public interface FieldAccessor extends BaseAccessor<Field> {

  /**
   * Gets the value of the wrapped field using the instance the reflexion object which created this instance is bound to
   * (but only if the field is not static). This method never throws an exception, all exceptions are given back to the
   * caller in the returned result instance.
   *
   * @param <T> the type of the data returned from the field.
   * @return the result of the get operation, either holding the field value or a read exception.
   */
  @NonNull <T> Result<T> getValue();

  /**
   * Gets the value of the wrapped field using the given instance. This method never throws an exception, all exceptions
   * are given back to the caller in the returned result instance.
   * <p>
   * The given instance should be present in case the field is not static, and the associated reflexion instance has no
   * instance bound to it. If an instance is given in case the field is static, the instance will be ignored silently.
   *
   * @param instance the instance to use when getting the field value.
   * @param <T>      the type of the data returned from the field.
   * @return the result of the get operation, either holding the field value or a read exception.
   */
  @NonNull <T> Result<T> getValue(@Nullable Object instance);

  /**
   * Sets the value of the wrapped field using the instance the reflexion object which created this instance is bound to
   * (but only if the field is not static). This method never throws an exception, all exceptions are given back to the
   * caller in the returned result instance.
   *
   * @param value the new value of the field.
   * @return the result of the set operation, either successful or holding the set exception.
   */
  @NonNull Result<Void> setValue(@Nullable Object value);

  /**
   * Sets the value of the wrapped field in the given object instance. This method never throws an exception, all
   * exceptions are given back to the caller in the returned result instance.
   * <p>
   * The given instance should be present in case the field is not static, and the associated reflexion instance has no
   * instance bound to it. If an instance is given in case the field is static, the instance will be ignored silently.
   *
   * @param instance the instance of the object to set the new field value in.
   * @param value    the new value of the field to set.
   * @return the result of the set operation, either successful or holding the set exception.
   */
  @NonNull Result<Void> setValue(@Nullable Object instance, @Nullable Object value);
}
