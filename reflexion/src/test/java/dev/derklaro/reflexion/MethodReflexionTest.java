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

import java.lang.reflect.Method;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MethodReflexionTest {

  @Test
  void testFindNoArgsStaticMethod() {
    Reflexion reflexion = Reflexion.on(SeedClass.class);

    Optional<MethodAccessor<Method>> acc = reflexion.findMethod("abc");
    Assertions.assertTrue(acc.isPresent());

    String result = acc.get().<String>invokeWithArgs().getOrElse(null);
    Assertions.assertNotNull(result);
    Assertions.assertEquals("World", result);
  }

  @Test
  void testFindArgsStaticMethod() {
    Reflexion reflexion = Reflexion.on(SeedClass.class);
    SeedClass seedClass = new SeedClass(1, 2, true, "test");

    Optional<MethodAccessor<Method>> acc = reflexion.findMethod("abc", String.class, SeedClass.class);
    Assertions.assertTrue(acc.isPresent());

    String result = acc.get().<String>invokeWithArgs("Hello", seedClass).getOrElse(null);
    Assertions.assertNotNull(result);
    Assertions.assertEquals("Hello // test", result);
  }

  @Test
  void testFindNoArgsMethod() {
    Reflexion reflexion = Reflexion.on(SeedClass.class);
    SeedClass seedClass = new SeedClass(1, 2, true, "test");

    Optional<MethodAccessor<Method>> acc = reflexion.findMethod("getStr");
    Assertions.assertTrue(acc.isPresent());

    String result = acc.get().<String>invoke(seedClass).getOrElse(null);
    Assertions.assertNotNull(result);
    Assertions.assertEquals("test", result);
  }

  @Test
  void testFindArgsMethod() {
    Reflexion reflexion = Reflexion.on(SeedClass.class);
    SeedClass seedClass = new SeedClass(1, 2, true, "test");

    Optional<MethodAccessor<Method>> acc = reflexion.findMethod("appendToStr", String.class);
    Assertions.assertTrue(acc.isPresent());

    String result = acc.get().<String>invoke(seedClass, "passed!").getOrElse(null);
    Assertions.assertNotNull(result);
    Assertions.assertEquals("test passed!", result);
  }
}
