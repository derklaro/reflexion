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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class UnreflectTest {

  private static final SeedClass SEED = new SeedClass(123, 123D, true, "Hello");

  @Test
  void testUnreflectStaticField() throws NoSuchFieldException {
    Field field = SeedClass.class.getDeclaredField("WORLD");
    FieldAccessor accessor = Assertions.assertDoesNotThrow(() -> Reflexion.unreflectField(field));
    Assertions.assertEquals("World", accessor.getValue().getOrThrow());
  }

  @Test
  void testUnreflectNonStaticField() throws NoSuchFieldException {
    Field field = SeedClass.class.getDeclaredField("i");
    FieldAccessor accessor = Assertions.assertDoesNotThrow(() -> Reflexion.unreflectField(field));
    Assertions.assertEquals(123, accessor.getValue(SEED).getOrThrow());
  }

  @Test
  void testUnreflectNonStaticFieldWithBinding() throws NoSuchFieldException {
    Field field = SeedClass.class.getDeclaredField("d");
    FieldAccessor accessor = Assertions.assertDoesNotThrow(() -> Reflexion.onBound(SEED).unreflect(field));
    Assertions.assertEquals(123D, accessor.getValue().getOrThrow());
  }

  @Test
  void testUnreflectStaticMethod() throws NoSuchMethodException {
    Method method = SeedClass.class.getDeclaredMethod("abc", String.class, SeedClass.class);
    MethodAccessor<?> accessor = Assertions.assertDoesNotThrow(() -> Reflexion.unreflectMethod(method));
    Assertions.assertEquals("Test // Hello", accessor.invokeWithArgs("Test", SEED).getOrThrow());
  }

  @Test
  void testUnreflectNonStaticMethod() throws NoSuchMethodException {
    Method method = SeedClass.class.getDeclaredMethod("appendToStr", String.class);
    MethodAccessor<?> accessor = Assertions.assertDoesNotThrow(() -> Reflexion.unreflectMethod(method));
    Assertions.assertEquals("Hello World", accessor.invoke(SEED, "World").getOrThrow());
  }

  @Test
  void testUnreflectNonStaticMethodWithBinding() throws NoSuchMethodException {
    Method method = SeedClass.class.getDeclaredMethod("appendToStr", String.class);
    MethodAccessor<?> accessor = Assertions.assertDoesNotThrow(() -> Reflexion.onBound(SEED).unreflect(method));
    Assertions.assertEquals("Hello World", accessor.invokeWithArgs("World").getOrThrow());
  }

  @Test
  void testUnreflectConstructor() throws NoSuchMethodException {
    Constructor<?> ct = SeedClass.class.getDeclaredConstructor(double.class, String.class);
    MethodAccessor<?> accessor = Assertions.assertDoesNotThrow(() -> Reflexion.unreflectConstructor(ct));

    SeedClass instance = accessor.<SeedClass>invokeWithArgs(12345D, "Testing").getOrThrow();
    Assertions.assertEquals(12345D, instance.getD());
    Assertions.assertEquals("Testing", instance.getStr());
  }
}
