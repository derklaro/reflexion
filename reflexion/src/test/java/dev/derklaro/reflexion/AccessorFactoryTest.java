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

import dev.derklaro.reflexion.helper.SeedClass;
import dev.derklaro.reflexion.internal.handles.MethodHandleAccessorFactory;
import dev.derklaro.reflexion.internal.jna.JnaAccessorFactory;
import dev.derklaro.reflexion.internal.natives.NativeAccessorFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class AccessorFactoryTest {

  static AccessorFactory[] factories() {
    return new AccessorFactory[]{
      new JnaAccessorFactory(),
      new NativeAccessorFactory(),
      new MethodHandleAccessorFactory()};
  }

  @ParameterizedTest
  @MethodSource("factories")
  void testStaticFieldWrap(AccessorFactory factory) {
    Reflexion reflexion = Reflexion.on(SeedClass.class, null, factory);
    Optional<FieldAccessor> accessor = reflexion.findField("LONG");

    Assertions.assertTrue(accessor.isPresent());
    Assertions.assertEquals(123456789L, accessor.get().getValue().getOrElse(0L));

    Result<Void> result = accessor.get().setValue(12345L);
    Assertions.assertTrue(result.wasSuccessful());
    Assertions.assertEquals(12345L, accessor.get().getValue().getOrElse(0L));

    Result<Void> invalidResult = accessor.get().setValue("This is not a thing");
    Assertions.assertTrue(invalidResult.wasExceptional());

    // reset the field
    Result<Void> resetResult = accessor.get().setValue(123456789L);
    Assertions.assertTrue(resetResult.wasSuccessful());
  }

  @ParameterizedTest
  @MethodSource("factories")
  void testInstanceFieldWrap(AccessorFactory factory) {
    SeedClass seedClass = new SeedClass(1, 2D, false, "World!!!");
    Reflexion reflexion = Reflexion.on(SeedClass.class, null, factory);
    Optional<FieldAccessor> accessor = reflexion.findField("str");

    Assertions.assertTrue(accessor.isPresent());
    Assertions.assertEquals("World!!!", accessor.get().getValue(seedClass).getOrElse(null));

    Result<Void> result = accessor.get().setValue(seedClass, "Hello, Moon, World, Mars...");
    Assertions.assertTrue(result.wasSuccessful());
    Assertions.assertEquals("Hello, Moon, World, Mars...", accessor.get().getValue(seedClass).getOrElse(null));

    Result<Void> invalidResult = accessor.get().setValue(seedClass, 12345L);
    Assertions.assertTrue(invalidResult.wasExceptional());
  }

  @ParameterizedTest
  @MethodSource("factories")
  void testStaticMethodWrap(AccessorFactory factory) {
    SeedClass seedClass = new SeedClass(1, 2, true, "WORLD :)");
    Reflexion reflexion = Reflexion.on(SeedClass.class, null, factory);
    Optional<MethodAccessor<Method>> accessor = reflexion.findMethod("abc", String.class, SeedClass.class);

    Assertions.assertTrue(accessor.isPresent());
    Assertions.assertEquals("HELLO // WORLD :)", accessor.get().invokeWithArgs("HELLO", seedClass).getOrElse(null));

    Result<Void> result = accessor.get().invokeWithArgs("Okay", "No that fails");
    Assertions.assertTrue(result.wasExceptional());
  }

  @ParameterizedTest
  @MethodSource("factories")
  void testInstanceMethodWrap(AccessorFactory factory) {
    SeedClass seedClass = new SeedClass(1, 2, true, "WORLD :)");
    Reflexion reflexion = Reflexion.on(SeedClass.class, null, factory);
    Optional<MethodAccessor<Method>> accessor = reflexion.findMethod("appendToStr", String.class);

    Assertions.assertTrue(accessor.isPresent());
    Assertions.assertEquals("WORLD :) HELLO", accessor.get().invoke(seedClass, "HELLO").getOrElse(null));

    Result<Void> result = accessor.get().invoke(seedClass, 12345L);
    Assertions.assertTrue(result.wasExceptional());
  }

  @ParameterizedTest
  @MethodSource("factories")
  void testConstructorWrap(AccessorFactory factory) {
    Reflexion reflexion = Reflexion.on(SeedClass.class, null, factory);
    Optional<MethodAccessor<Constructor<?>>> accessor = reflexion.findConstructor(double.class, String.class);

    Assertions.assertTrue(accessor.isPresent());

    Result<SeedClass> result = accessor.get().invokeWithArgs(1234D, "World :))");
    Assertions.assertTrue(result.wasSuccessful());
    Assertions.assertEquals(1234D, result.get().getD());
    Assertions.assertEquals("World :))", result.get().getStr());

    Result<SeedClass> invalidResult = accessor.get().invokeWithArgs("This will", "break :)");
    Assertions.assertTrue(invalidResult.wasExceptional());
  }
}
