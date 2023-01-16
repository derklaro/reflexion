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

package dev.derklaro.reflexion.internal.jna;

import dev.derklaro.reflexion.internal.handles.MethodHandleAccessorFactory;
import dev.derklaro.reflexion.internal.util.Util;
import java.lang.invoke.MethodHandles;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;

/**
 * A reflexion accessor factory which uses method handles to wrap methods and fields, but looks up the IMPL_LOOKUP field
 * using a misc of native jvm internals via jna and pre-generated getter code rather than sun.misc.Unsafe or
 * reflections.
 *
 * @since 1.6.0
 */
public final class JnaAccessorFactory extends MethodHandleAccessorFactory {

  private static final boolean JNA_DEPENDENCY_AVAILABLE = Util.isClassPresent("com.sun.jna.Native");
  private static final boolean ON_J9 = System.getProperty("java.vm.name", "").toUpperCase(Locale.ROOT).contains("J9");

  private static final boolean JNA_LOOKUP_AVAILABLE = !ON_J9 && JNA_DEPENDENCY_AVAILABLE;

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isAvailable() {
    return JNA_LOOKUP_AVAILABLE && super.isAvailable();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected @Nullable MethodHandles.Lookup getTrustedLookup() {
    return JNA_LOOKUP_AVAILABLE ? JnaImplLookupAccessor.tryGetImplLookup() : null;
  }
}
