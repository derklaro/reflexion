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

import dev.derklaro.reflexion.internal.util.Util;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UtilTest {

  @Test
  void testMapFromArray() {
    Assertions.assertSame(Collections.emptyMap(), Util.newMapFromArray());

    Map<String, String> singleton = Util.newMapFromArray("Hello", "World");
    Assertions.assertEquals(1, singleton.size());
    Assertions.assertEquals("World", singleton.get("Hello"));

    Map<Integer, String> multiple = Util.newMapFromArray(0, "Hello", 1, "World", 2, "!");
    Assertions.assertEquals(3, multiple.size());
    Assertions.assertEquals("Hello", multiple.get(0));
    Assertions.assertEquals("World", multiple.get(1));
    Assertions.assertEquals("!", multiple.get(2));
  }

  @Test
  void testFastModulo() {
    Assertions.assertEquals(4 % 2, Util.fastModulo(4, 2));
    Assertions.assertEquals(20 % 10, Util.fastModulo(20, 10));
    Assertions.assertEquals(500 % 10, Util.fastModulo(500, 10));
  }

  @Test
  void testFirstNonNull() {
    Assertions.assertNull(Util.firstNonNull());
    Assertions.assertNull(Util.firstNonNull(null, null));
    Assertions.assertEquals("Hello", Util.firstNonNull(null, "Hello", "World"));
    Assertions.assertEquals("Hello", Util.firstNonNull(null, "Hello", null, null, "World", null));
  }

  @Test
  void testAllMatch() {
    Assertions.assertFalse(Util.allMatch(new Object[0], null, ($, $1) -> true));
    Assertions.assertTrue(Util.allMatch(new Object[0], new Object[0], ($, $1) -> true));

    Assertions.assertTrue(Util.allMatch(
      new String[]{"Hello", "World", ":)"},
      new String[]{"Hello", "World", ":)"},
      String::equals));
    Assertions.assertFalse(Util.allMatch(
      new String[]{"Hello", "World", ":("},
      new String[]{"Hello", "World", ":)"},
      String::equals));
    Assertions.assertFalse(Util.allMatch(
      new String[]{"Hello"},
      new String[]{"Hello", "World", ":)"},
      String::equals));
  }

  @Test
  void testFilterAndMap() {
    Collection<String> input = Arrays.asList("Hello", "World", ":)");

    Collection<Character> out = Util.filterAndMap(input, in -> in.length() > 2, in -> in.charAt(0));
    Assertions.assertEquals(2, out.size());

    Iterator<Character> iterator = out.iterator();
    Assertions.assertEquals('W', iterator.next());
    Assertions.assertEquals('H', iterator.next());
  }

  @Test
  void testEnsureTrue() {
    Assertions.assertDoesNotThrow(() -> Util.ensureTrue(true, "Hello World!"));

    Throwable thrown = Assertions.assertThrows(
      IllegalStateException.class,
      () -> Util.ensureTrue(false, "Hello World!"));
    Assertions.assertEquals("Hello World!", thrown.getMessage());
  }
}
