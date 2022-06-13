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

import dev.derklaro.reflexion.internal.util.Util;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.jetbrains.annotations.Nullable;

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
    return Util.firstNonNull(tryResolveUsingDirectAccess(), tryResolveUsingUnsafe());
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
  private static @Nullable Lookup tryResolveUsingUnsafe() {
    try {
      // get the impl_lookup field (fail-fast)
      Field implLookupField = Lookup.class.getDeclaredField("IMPL_LOOKUP");

      // unsafe class lookup
      Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");

      // go over the 'theUnsafe' field
      Field field = unsafeClass.getDeclaredField("theUnsafe");
      field.setAccessible(true);

      // get the methods to resolve the field base / field offset
      Method staticFieldBase = unsafeClass.getMethod("staticFieldBase", Field.class);
      Method staticFieldOffset = unsafeClass.getMethod("staticFieldOffset", Field.class);

      // get the unsafe instance
      Object unsafe = field.get(null);

      // get the IMPL_LOOKUP field offset
      Object implBase = staticFieldBase.invoke(unsafe, implLookupField);
      long implOffset = (long) staticFieldOffset.invoke(unsafe, implLookupField);

      // get the impl_lookup using Unsafe.getObject
      Method getObject = unsafeClass.getMethod("getObject", Object.class, long.class);
      return (Lookup) getObject.invoke(unsafe, implBase, implOffset);
    } catch (Throwable exception) {
      // we catch a Throwable to prevent NoSuchMethodErrors from happening in the future when the 'staticFieldBase' and
      // 'staticFieldOffset' methods are removed from the unsafe class
      return null;
    }
  }
}
