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

package dev.derklaro.reflexion;

import dev.derklaro.reflexion.internal.handles.MethodHandleAccessorFactory;
import dev.derklaro.reflexion.internal.natives.NativeAccessorFactory;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class HardReflectionTest {

  static AccessorFactory[] factories() {
    return new AccessorFactory[]{new NativeAccessorFactory(), new MethodHandleAccessorFactory()};
  }

  @ParameterizedTest
  @MethodSource("factories")
  void testHardFieldAccessIsPossible(AccessorFactory factory) {
    Reflexion reflexion = Reflexion.on(Lookup.class, null, factory);

    Optional<FieldAccessor> accessor = reflexion.findField("IMPL_LOOKUP");
    Assertions.assertTrue(accessor.isPresent());

    Lookup lookup = accessor.get().<Lookup>getValue().getOrElse(null);
    Assertions.assertNotNull(lookup);
    Assertions.assertEquals(Object.class, lookup.lookupClass());
  }

  @ParameterizedTest
  @MethodSource("factories")
  void testHardMethodAccessIsPossible(AccessorFactory factory) {
    Reflexion reflexion = Reflexion.on(Lookup.class, null, factory);

    Optional<MethodAccessor<Method>> accessor = reflexion.findMethod("fixmods", int.class);
    Assertions.assertTrue(accessor.isPresent());

    int adjustedMods = accessor.get().<Integer>invokeWithArgs(Modifier.PUBLIC).getOrElse(0);
    Assertions.assertEquals(1, adjustedMods);
  }

  @ParameterizedTest
  @MethodSource("factories")
  void testHardConstructorAccessIsPossible(AccessorFactory factory) {
    Reflexion reflexion = Reflexion.on(Lookup.class, null, factory);

    Optional<MethodAccessor<Constructor<?>>> accessor = reflexion.findConstructor(Class.class);
    Assertions.assertTrue(accessor.isPresent());

    Lookup lookup = accessor.get().<Lookup>invokeWithArgs(SeedClass.class).getOrElse(null);
    Assertions.assertNotNull(lookup);
  }
}
