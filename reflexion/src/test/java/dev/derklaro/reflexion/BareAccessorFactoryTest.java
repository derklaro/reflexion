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

import dev.derklaro.reflexion.helper.SeedClass;
import dev.derklaro.reflexion.internal.bare.BareAccessorFactory;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BareAccessorFactoryTest {

  private static final AccessorFactory BARE_FACTORY = new BareAccessorFactory();

  static Stream<Arguments> fieldGetSource() {
    return Stream.of(
      Arguments.of(new SeedClass(6473, 4634D, false, "Hello"), "i", 6473),
      Arguments.of(new SeedClass(1234, 1234D, true, "World"), "d", 1234D),
      Arguments.of(new SeedClass(12, 12D, false, "Very Hard Test!"), "str", "Very Hard Test!"));
  }

  @ParameterizedTest
  @MethodSource("fieldGetSource")
  void testFieldGet(Object on, String fieldName, Object expected) {
    Reflexion reflexion = Reflexion.on(on.getClass(), null, BARE_FACTORY);
    FieldAccessor accessor = reflexion.findField(fieldName).orElse(null);

    Assertions.assertNotNull(accessor);
    Assertions.assertEquals(expected, accessor.getValue(on).getOrElse(null));
  }

  @Test
  void testMethodInvoke() {
    Reflexion reflexion = Reflexion.on(SeedClass.class, null, BARE_FACTORY);

    MethodAccessor<?> accessor = reflexion.findMethod("abc").orElse(null);
    Assertions.assertNotNull(accessor);
    Assertions.assertEquals("World", accessor.invoke().getOrElse(null));

    accessor = reflexion.findMethod("abc", String.class, SeedClass.class).orElse(null);
    Assertions.assertNotNull(accessor);
    Assertions.assertEquals(
      "Hello // World",
      accessor.invokeWithArgs("Hello", new SeedClass(1, 1, false, "World")).getOrElse(null));

    accessor = reflexion.findMethod("getStr").orElse(null);
    Assertions.assertNotNull(accessor);
    Assertions.assertEquals("World", accessor.invoke(new SeedClass(1, 1, false, "World")).getOrElse(null));
  }

  @Test
  void testConstructorInvoke() {
    Reflexion reflexion = Reflexion.on(SeedClass.class, null, BARE_FACTORY);

    MethodAccessor<?> accessor = reflexion.findConstructor().orElse(null);
    Assertions.assertNotNull(accessor);
    Assertions.assertEquals(234D, accessor.<SeedClass>invoke().map(SeedClass::getD).getOrElse(null));

    accessor = reflexion.findConstructor(int.class, double.class, boolean.class, String.class).orElse(null);
    Assertions.assertNotNull(accessor);
    Assertions.assertEquals(
      "Hello World!",
      accessor.<SeedClass>invokeWithArgs(123, 123D, false, "Hello World!").map(SeedClass::getStr).getOrElse(null));
  }
}
