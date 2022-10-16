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

import dev.derklaro.reflexion.internal.unsafe.UnsafeAccess;
import dev.derklaro.reflexion.internal.unsafe.UnsafeAccessibleObject;
import dev.derklaro.reflexion.internal.util.Util;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

/**
 * An accessor to get the IMPL_LOOKUP field using the unsafe field methods provided by the jvm.
 *
 * @since 1.0
 */
final class ImplLookupAccessor {

  private ImplLookupAccessor() {
    throw new UnsupportedOperationException();
  }

  /**
   * Tries to get the IMPL_LOOKUP using the either direct field access or the methods provided by sun.misc.Unsafe.
   *
   * @return the IMPL_LOOKUP field value or null if the lookup is not possible.
   */
  public static @Nullable Lookup findImplLookup() {
    // we prefer direct access as the user is 100% sure what he is doing and we are not relying
    // on sun internal methods if not 100% needed
    return Util.firstNonNull(
      tryResolveUsingDirectAccess(),
      tryResolveUsingUnsafeGetObject(),
      tryResolveUsingUnsafeAccessibleObject());
  }

  /**
   * Tries to resolve the IMPL_LOOKUP field by direct access, only available on a modern jvm if unnamed modules were
   * granted access to the java.lang.invoke module.
   *
   * @return the IMPL_LOOKUP instance, resolved via direct field access or null if not possible.
   */
  private static @Nullable Lookup tryResolveUsingDirectAccess() {
    try {
      // get the impl_lookup field and make it accessible
      Field implLookupField = Lookup.class.getDeclaredField("IMPL_LOOKUP");
      implLookupField.setAccessible(true);

      // get the instance of the field
      return (Lookup) implLookupField.get(null);
    } catch (Throwable throwable) {
      // IllegalAccess, NoSuchField etc.
      return null;
    }
  }

  /**
   * Tries to resolve the IMPL_LOOKUP field using the methods provided by sun.misc.unsafe. This method only works on a
   * sun jvm and will most likely stop working in further releases of the jvm (20/21/22...) as all methods which are
   * returning field offsets were deprecated and will be removed.
   *
   * @return the IMPL_LOOKUP instance, resolved using sun.misc.unsafe or null if not possible.
   */
  @VisibleForTesting
  static @Nullable Lookup tryResolveUsingUnsafeGetObject() {
    if (UnsafeAccess.isAvailable()) {
      try {
        // get the impl_lookup field (fail-fast)
        Field lookupField = Lookup.class.getDeclaredField("IMPL_LOOKUP");

        // get the base & offset of the field
        Object implBase = UnsafeAccess.invokeMethod("staticFieldBase", new Class<?>[]{Field.class}, lookupField);
        Long offset = (Long) UnsafeAccess.invokeMethod("staticFieldOffset", new Class<?>[]{Field.class}, lookupField);

        // check if we got both results
        if (implBase != null && offset != null) {
          return (Lookup) UnsafeAccess.invokeMethod(
            "getObject",
            new Class<?>[]{Object.class, long.class},
            implBase, offset);
        }
      } catch (NoSuchFieldException ignored) {
        // should not happen
      }
    }

    // unable to get the field using that method
    return null;
  }

  /**
   * Tries to resolve the IMPL_LOOKUP field using the methods provided by sun.misc.unsafe. This method only works on a
   * sun jvm and tries to force-override the accessible state of the IMPL_LOOKUP field.
   *
   * @return the IMPL_LOOKUP instance or null if not possible.
   * @since 1.6
   */
  @VisibleForTesting
  static @Nullable Lookup tryResolveUsingUnsafeAccessibleObject() {
    if (UnsafeAccessibleObject.isAvailable()) {
      try {
        // get the impl_lookup field (fail-fast)
        Field lookupField = Lookup.class.getDeclaredField("IMPL_LOOKUP");

        // make it accessible and return it
        UnsafeAccessibleObject.makeAccessible(lookupField);
        return (Lookup) lookupField.get(null);
      } catch (Exception ignored) {
      }
    }

    // cannot get the field using that method
    return null;
  }
}
