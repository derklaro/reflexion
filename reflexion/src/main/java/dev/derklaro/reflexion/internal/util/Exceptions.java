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

package dev.derklaro.reflexion.internal.util;

import lombok.NonNull;

/**
 * A utility to improve working with exceptions thrown during the program runtime.
 *
 * @since 1.8.0
 */
public final class Exceptions {

  private Exceptions() {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws the given exception as an unchecked exception removing the need to catch non-runtime exceptions.
   *
   * @param throwable the throwable to rethrow.
   * @param <T>       the inferred type of the throwable.
   * @throws T                    the exception to rethrow.
   * @throws NullPointerException if the given throwable is null.
   */
  @SuppressWarnings("unchecked")
  public static <T extends Throwable> void throwUnchecked(@NonNull Throwable throwable) throws T {
    throw (T) throwable;
  }

  /**
   * Get if the given exception is an unrecoverable error which prevents the program to resume (on the current thread).
   *
   * @param throwable the throwable to check.
   * @return true if the exception is unrecoverable for the current thread, false otherwise.
   * @throws NullPointerException if the given throwable is null.
   */
  public static boolean isFatal(@NonNull Throwable throwable) {
    return throwable instanceof InterruptedException
      || throwable instanceof LinkageError
      || throwable instanceof ThreadDeath
      || throwable instanceof VirtualMachineError;
  }

  /**
   * Rethrows the given exception if it signals an unrecoverable error which prevents the program to resume (on the
   * current thread).
   *
   * @param throwable the throwable to rethrow if it is fatal.
   * @throws NullPointerException if the given throwable is null.
   */
  public static void rethrowIfFatal(@NonNull Throwable throwable) {
    if (isFatal(throwable)) {
      throwUnchecked(throwable);
    }
  }
}
