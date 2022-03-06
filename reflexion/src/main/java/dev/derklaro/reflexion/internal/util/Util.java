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

package dev.derklaro.reflexion.internal.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.Unmodifiable;

public final class Util {

  private Util() {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("unchecked")
  public static <K, V> @Unmodifiable Map<K, V> newMapFromArray(@NonNull Object... entries) {
    // spare out some edge cases
    if (entries.length == 0) {
      return Collections.emptyMap();
    }

    // single key-value map
    if (entries.length == 2) {
      return Collections.singletonMap((K) entries[0], (V) entries[1]);
    }

    // check if the given array has a size which is dividable by 2 (key - value pairs)
    if (fastModulo(entries.length, 2) != 0) {
      throw new IllegalArgumentException("Given map based array has unexpected size of " + entries.length);
    }

    // fixed size map which prevents resizing
    Map<K, V> map = new HashMap<>(entries.length + 1, 1f);
    for (int i = 0; i < entries.length; i += 2) {
      map.put((K) entries[i], (V) entries[i + 1]);
    }

    return map;
  }

  public static int fastModulo(int leftOperator, int rightOperator) {
    return leftOperator & (rightOperator - 1);
  }

  public static @NonNull <T> T getFirstNonNull(T... choices) {
    T firstNonNull = firstNonNull(choices);
    if (firstNonNull != null) {
      return firstNonNull;
    }

    // okay
    throw new IllegalArgumentException("none of the given choices is non-null");
  }

  public static @UnknownNullability <T> T firstNonNull(T... choices) {
    // why would you do that
    if (choices.length == 0) {
      return null;
    }

    // well use the first one directly
    if (choices.length == 1) {
      return choices[0];
    }

    // choose between the first and second one
    if (choices.length == 2) {
      T left = choices[0];
      T right = choices[1];
      return left == null ? right : left;
    }

    // find the first one in the array
    for (T choice : choices) {
      if (choice != null) {
        return choice;
      }
    }

    // okay
    return null;
  }

  @SuppressWarnings("unchecked")
  public static <T extends Throwable> void throwUnchecked(@NonNull Throwable throwable) throws T {
    throw (T) throwable;
  }
}
