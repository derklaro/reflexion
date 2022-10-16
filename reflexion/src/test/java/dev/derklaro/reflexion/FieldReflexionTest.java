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

import static dev.derklaro.reflexion.matcher.FieldMatcher.newMatcher;

import dev.derklaro.reflexion.helper.SeedClass;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FieldReflexionTest {

  static Stream<Arguments> fieldNameAccessProvider() {
    return Stream.of(
      Arguments.of(new SeedClass(1234, 1, true, "World!"), "i", 1234),
      Arguments.of(new SeedClass(1, 1234D, true, "World!"), "d", 1234D),
      Arguments.of(new SeedClass(1, 1, true, "World"), "b", true),
      Arguments.of(new SeedClass(1, 1, true, "Testing :)"), "str", "Testing :)"));
  }

  static Stream<Arguments> fieldNameAccessSetProvider() {
    return Stream.of(
      Arguments.of(new SeedClass(1234, 1, true, "World!"), "i", 1234, 5678),
      Arguments.of(new SeedClass(1, 1234D, true, "World!"), "d", 1234D, 5678D),
      Arguments.of(new SeedClass(1, 1, true, "World"), "b", true, false),
      Arguments.of(new SeedClass(1, 1, true, "Testing :)"), "str", "Testing :)", "TEEEEST"));
  }

  static Stream<Arguments> fieldStaticNameAccessProvider() {
    return Stream.of(
      Arguments.of(SeedClass.class, "WORLD", "World"),
      Arguments.of(SeedClass.class, "LONG", 123456789L));
  }

  static Stream<Arguments> fieldStaticNameAccessSetProvider() {
    return Stream.of(
      Arguments.of(SeedClass.class, "WORLD", "World", "Hello, World!"),
      Arguments.of(SeedClass.class, "LONG", 123456789L, 987654321L));
  }

  @Test
  void testFieldsLookup() {
    Assertions.assertEquals(12, Reflexion.on(SeedClass.class).findFields(newMatcher()).size());
  }

  @Test
  void testMatcherLookup() {
    Reflexion reflexion = Reflexion.on(SeedClass.class);

    Assertions.assertTrue(reflexion.findField(newMatcher().exactType(Field::getType, int.class)).isPresent());
    Assertions.assertTrue(reflexion.findField(newMatcher().exactType(Field::getType, String.class)).isPresent());
    Assertions.assertTrue(reflexion.findField(newMatcher().derivedType(Field::getType, Object.class)).isPresent());
    Assertions.assertTrue(reflexion.findField(newMatcher().derivedType(Field::getType, Comparable.class)).isPresent());

    Assertions.assertFalse(reflexion.findField(newMatcher().exactType(Field::getType, float.class)).isPresent());
    Assertions.assertFalse(reflexion.findField(newMatcher().exactType(Field::getType, Object.class)).isPresent());

    Assertions.assertEquals(0, reflexion.findFields(newMatcher().exactType(Field::getType, float.class)).size());
    Assertions.assertEquals(0, reflexion.findFields(newMatcher().exactType(Field::getType, Object.class)).size());

    Assertions.assertEquals(2, reflexion.findFields(newMatcher().exactType(Field::getType, int.class)).size());
    Assertions.assertEquals(4, reflexion.findFields(newMatcher().exactType(Field::getType, String.class)).size());

    Assertions.assertEquals(4, reflexion.findFields(newMatcher().exactType(Field::getType, int.class)
      .or(newMatcher().exactType(Field::getType, double.class))).size());

    Assertions.assertEquals(4, reflexion.findFields(newMatcher().hasModifier(Modifier.STATIC)).size());
    Assertions.assertEquals(2, reflexion.findFields(newMatcher()
      .hasModifier(Modifier.STATIC)
      .denyModifier(Modifier.FINAL)
    ).size());
    Assertions.assertEquals(1, reflexion.findFields(newMatcher()
      .exactType(Field::getType, String.class)
      .hasModifiers(Modifier.STATIC, Modifier.FINAL)
      .denyModifiers(Modifier.NATIVE, Modifier.PROTECTED)
    ).size());
  }

  @ParameterizedTest
  @MethodSource("fieldNameAccessProvider")
  void testFieldNameAccessInstance(Object on, String name, Object expected) {
    Reflexion reflexion = Reflexion.onBound(on);
    Object value = reflexion.findField(name).map(acc -> acc.getValue().getOrElse(null)).orElse(null);
    Assertions.assertEquals(expected, value);
  }

  @ParameterizedTest
  @MethodSource("fieldNameAccessSetProvider")
  void testFieldNameSetAccessInstance(Object on, String name, Object initial, Object newVal) {
    Reflexion reflexion = Reflexion.onBound(on);
    FieldAccessor accessor = reflexion.findField(name).orElse(null);

    Assertions.assertNotNull(accessor);
    Assertions.assertEquals(initial, accessor.getValue().getOrElse(null));

    accessor.setValue(newVal);
    Assertions.assertEquals(newVal, accessor.getValue().getOrElse(null));
  }

  @ParameterizedTest
  @MethodSource("fieldStaticNameAccessProvider")
  void testStaticFieldNameAccess(Class<?> clazz, String name, Object expected) {
    Reflexion reflexion = Reflexion.on(clazz);
    Object value = reflexion.findField(name).map(acc -> acc.getValue().getOrElse(null)).orElse(null);
    Assertions.assertEquals(expected, value);
  }

  @ParameterizedTest
  @MethodSource("fieldStaticNameAccessSetProvider")
  void testStaticFieldNameSetAccessInstance(Class<?> clazz, String name, Object initial, Object newVal) {
    Reflexion reflexion = Reflexion.on(clazz);
    FieldAccessor accessor = reflexion.findField(name).orElse(null);

    Assertions.assertNotNull(accessor);
    Assertions.assertEquals(initial, accessor.getValue().getOrElse(null));

    accessor.setValue(newVal);
    Assertions.assertEquals(newVal, accessor.getValue().getOrElse(null));

    // reset the field
    accessor.setValue(initial);
  }
}
