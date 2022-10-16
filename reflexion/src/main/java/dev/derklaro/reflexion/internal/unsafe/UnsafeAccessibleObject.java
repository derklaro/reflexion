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

package dev.derklaro.reflexion.internal.unsafe;

import dev.derklaro.reflexion.internal.util.Util;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import lombok.NonNull;

/**
 * A internal helper class which allows to set the {@code override} boolean in the AccessibleObject class. This can only
 * be used if the unsafe access is available and the offset of the boolean could get resolved. To verify that this util
 * is ready to be used, use the {@link #isAvailable()} method.
 *
 * @since 1.6
 */
public final class UnsafeAccessibleObject {

  private static final long ACCESSIBLE_OFFSET;
  private static final Object SOME_FIELD = new Object();

  static {
    long offset = -1;
    if (UnsafeAccess.isAvailable()) {
      try {
        // get the SOME_FIELD field in this class, leave it inaccessible
        Field inaccessibleField = UnsafeAccessibleObject.class.getDeclaredField("SOME_FIELD");

        // get the SOME_FIELD field in this class, make it accessible
        Field accessibleField = UnsafeAccessibleObject.class.getDeclaredField("SOME_FIELD");
        accessibleField.setAccessible(true);

        // find the offset of the accessible override boolean
        for (long off = 8; off < 128; off++) {
          Byte inaccessible = (Byte) UnsafeAccess.invokeMethod(
            "getByte",
            new Class[]{Object.class, long.class},
            inaccessibleField, off);
          Byte accessible = (Byte) UnsafeAccess.invokeMethod(
            "getByte",
            new Class[]{Object.class, long.class},
            accessibleField, off);

          // check if we got both bytes and if they differ
          if (inaccessible != null && accessible != null && inaccessible == 0 && accessible == 1) {
            offset = off;
            break;
          }
        }
      } catch (NoSuchFieldException ignored) {
        // should not happen
      }
    }

    ACCESSIBLE_OFFSET = offset;
  }

  private UnsafeAccessibleObject() {
    throw new UnsupportedOperationException();
  }

  /**
   * Checks if the offset of the override field in AccessibleObject could get resolved and can be used to override the
   * field when calling {@link #makeAccessible(AccessibleObject)}.
   *
   * @return true if this instance can force-set an object accessible, false otherwise.
   */
  public static boolean isAvailable() {
    return ACCESSIBLE_OFFSET != -1;
  }

  /**
   * Makes the given accessible object accessible.
   *
   * @param accessibleObject the object to make accessible.
   * @throws NullPointerException  if the given accessible object is null.
   * @throws IllegalStateException if this util is not available for usage.
   */
  public static void makeAccessible(@NonNull AccessibleObject accessibleObject) {
    Util.ensureTrue(isAvailable(), "UnsafeAccessibleObject couldn't be initialized an is unusable");
    UnsafeAccess.invokeMethod(
      "putByte",
      new Class[]{Object.class, long.class, byte.class},
      accessibleObject, ACCESSIBLE_OFFSET, (byte) 1);
  }
}
