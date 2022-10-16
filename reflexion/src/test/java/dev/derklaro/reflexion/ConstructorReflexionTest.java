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

import static dev.derklaro.reflexion.matcher.ConstructorMatcher.newMatcher;

import dev.derklaro.reflexion.helper.SeedClass;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConstructorReflexionTest {

  @Test
  void testConstructorLookup() {
    Assertions.assertEquals(7, Reflexion.on(SeedClass.class).findConstructors(newMatcher()).size());
  }

  @Test
  void testConstructorLookupMatcher() {
    Reflexion reflexion = Reflexion.on(SeedClass.class);

    Assertions.assertEquals(3, reflexion.findConstructors(newMatcher().hasModifier(Modifier.PRIVATE)).size());
    Assertions.assertEquals(0, reflexion.findConstructors(newMatcher()
      .exactTypeAt(Constructor::getParameterTypes, String.class, 0)
    ).size());
    Assertions.assertEquals(3, reflexion.findConstructors(newMatcher()
      .exactTypeAt(Constructor::getParameterTypes, int.class, 0)
    ).size());
    Assertions.assertEquals(1, reflexion.findConstructors(newMatcher()
      .hasModifier(Modifier.PRIVATE)
      .exactTypeAt(Constructor::getParameterTypes, int.class, 0)
    ).size());
  }

  @Test
  void testNoArgsConstructorLookup() {
    Reflexion reflexion = Reflexion.on(SeedClass.class);

    Optional<MethodAccessor<Constructor<?>>> acc = reflexion.findConstructor();
    Assertions.assertTrue(acc.isPresent());

    SeedClass seed = acc.get().<SeedClass>invokeWithArgs().getOrElse(null);
    Assertions.assertNotNull(seed);

    Assertions.assertEquals(234D, seed.getD());
    Assertions.assertEquals("", seed.getStr());
  }

  @Test
  void testArgsConstructorLookup() {
    Reflexion reflexion = Reflexion.on(SeedClass.class);

    Optional<MethodAccessor<Constructor<?>>> acc = reflexion.findConstructor(double.class, String.class);
    Assertions.assertTrue(acc.isPresent());

    SeedClass seed = acc.get().<SeedClass>invokeWithArgs(1234D, ":))").getOrElse(null);
    Assertions.assertNotNull(seed);

    Assertions.assertEquals(1234D, seed.getD());
    Assertions.assertEquals(":))", seed.getStr());
  }
}
