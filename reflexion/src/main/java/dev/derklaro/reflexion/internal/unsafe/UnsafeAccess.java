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

package dev.derklaro.reflexion.internal.unsafe;

import dev.derklaro.reflexion.internal.util.Util;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import lombok.NonNull;
import org.jetbrains.annotations.UnknownNullability;

/**
 * A utility class which allows access to sun.misc.Unsafe without needing to hassle with it. To verify that this util is
 * ready to be used, use the {@link #isAvailable()} method.
 *
 * @since 1.6
 */
public final class UnsafeAccess {

  private static final Object THE_UNSAFE;

  static {
    Object unsafeInstance = null;
    try {
      // resolve the unsafe class
      Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
      // get the unsafe instance
      Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
      theUnsafe.setAccessible(true);
      unsafeInstance = theUnsafe.get(null);
    } catch (Exception ignored) {
    }

    // assign the instance
    THE_UNSAFE = unsafeInstance;
  }

  private UnsafeAccess() {
    throw new UnsupportedOperationException();
  }

  /**
   * Checks if the sun.misc.Unsafe instance could be resolved during the class init, and is now ready to call method
   * with it.
   *
   * @return true if this instance can be used to access sun.misc.Unsafe, false otherwise.
   */
  public static boolean isAvailable() {
    return THE_UNSAFE != null;
  }

  /**
   * Calls the given method which is provided in sun.misc.Unsafe. This method returns null if either the method could
   * not be resolved or the invocation throws an exception.
   *
   * @param methodName the name of the method to invoke.
   * @param argTypes   the argument types of the method.
   * @param args       the arguments to pass to the method.
   * @return the result of the method invocation.
   * @throws NullPointerException  if the given method name, argument types or arguments array is null.
   * @throws IllegalStateException if this util is not available for usage.
   */
  public static @UnknownNullability Object invokeMethod(
    @NonNull String methodName,
    @NonNull Class<?>[] argTypes,
    @NonNull Object... args
  ) {
    Util.ensureTrue(isAvailable(), "UnsafeAccess couldn't be initialized an is unusable");
    try {
      // resolve the target method
      Method method = THE_UNSAFE.getClass().getMethod(methodName, argTypes);
      return method.invoke(THE_UNSAFE, args);
    } catch (Exception exception) {
      return null;
    }
  }
}
