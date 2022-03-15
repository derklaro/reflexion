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

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import org.jetbrains.annotations.Nullable;

/**
 * An accessor to get the IMPL_LOOKUP field using the unsafe field methods provided by the jvm.
 *
 * @since 1.0
 */
final class UnsafeFieldAccessor {

  private UnsafeFieldAccessor() {
    throw new UnsupportedOperationException();
  }

  /**
   * Tries to get the IMPL_LOOKUP using the field methods provided by sun.misc.Unsafe.
   *
   * @return the IMPL_LOOKUP field value or null if the lookup is not possible.
   */
  public static @Nullable Lookup findImplLookup() {
    try {
      // get the impl_lookup field (fail-fast)
      Field implLookupField = Lookup.class.getDeclaredField("IMPL_LOOKUP");

      // unsafe class lookup
      Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");

      // go over the 'theUnsafe' field
      Field field = unsafeClass.getDeclaredField("theUnsafe");
      field.setAccessible(true);

      // get the unsafe instance
      sun.misc.Unsafe unsafe = (sun.misc.Unsafe) field.get(null);

      // get the IMPL_LOOKUP field offset
      Object implBase = unsafe.staticFieldBase(implLookupField);
      long implOffset = unsafe.staticFieldOffset(implLookupField);

      // get the impl_lookup
      return (Lookup) unsafe.getObject(implBase, implOffset);
    } catch (Throwable exception) {
      // we catch a Throwable to prevent NoSuchMethodErrors from happening in the future when the 'staticFieldBase' and
      // 'staticFieldOffset' methods are removed from the unsafe class
      return null;
    }
  }
}
