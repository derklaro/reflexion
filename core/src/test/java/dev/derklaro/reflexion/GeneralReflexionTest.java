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
import dev.derklaro.reflexion.internal.bare.BareAccessorFactory;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GeneralReflexionTest {

  @Test
  void testClassWrap() {
    Reflexion reflexion = Reflexion.on(SeedClass.class);
    Assertions.assertEquals(SeedClass.class, reflexion.getWrappedClass());
  }

  @Test
  void testObjectWrap() {
    Reflexion reflexion = Reflexion.on(new SeedClass(1, 2, true, ""));
    Assertions.assertEquals(SeedClass.class, reflexion.getWrappedClass());
    Assertions.assertNull(reflexion.getBinding());
  }

  @Test
  void testObjectWrapBound() {
    SeedClass instance = new SeedClass(1, 2, true, "");
    Reflexion reflexion = Reflexion.onBound(instance);

    Assertions.assertEquals(SeedClass.class, reflexion.getWrappedClass());
    Assertions.assertSame(instance, reflexion.getBinding());
  }

  @Test
  void testObjectWrapBoundAccessor() {
    AccessorFactory factory = new BareAccessorFactory();
    Reflexion reflexion = Reflexion.on(SeedClass.class, null, factory);

    Assertions.assertEquals(SeedClass.class, reflexion.getWrappedClass());
    Assertions.assertNull(reflexion.getBinding());
    Assertions.assertSame(factory, reflexion.getAccessorFactory());

    SeedClass instance = new SeedClass(1, 2, true, "");
    Reflexion bound = reflexion.bind(instance);

    Assertions.assertNotSame(reflexion, bound);
    Assertions.assertEquals(SeedClass.class, bound.getWrappedClass());
    Assertions.assertSame(instance, bound.getBinding());
    Assertions.assertSame(factory, bound.getAccessorFactory());
  }

  @Test
  void testClassLookup() {
    Optional<Reflexion> reflexion = Reflexion.find("dev.derklaro.reflexion.helper.SeedClass");

    Assertions.assertTrue(reflexion.isPresent());
    Assertions.assertEquals(SeedClass.class, reflexion.get().getWrappedClass());
  }

  @Test
  void testClassLookupClassLoader() {
    ClassLoader loader = ClassLoader.getSystemClassLoader();
    Optional<Reflexion> reflexion = Reflexion.find("dev.derklaro.reflexion.helper.SeedClass", loader);

    Assertions.assertTrue(reflexion.isPresent());
    Assertions.assertEquals(SeedClass.class, reflexion.get().getWrappedClass());
  }

  @Test
  void testClassGet() {
    Reflexion reflexion = Reflexion.get("dev.derklaro.reflexion.helper.SeedClass");

    Assertions.assertEquals(SeedClass.class, reflexion.getWrappedClass());
    Assertions.assertThrows(ReflexionException.class, () -> Reflexion.get("a"));
  }

  @Test
  void testClassGetClassLoader() {
    ClassLoader loader = ClassLoader.getSystemClassLoader();
    Reflexion reflexion = Reflexion.get("dev.derklaro.reflexion.helper.SeedClass", loader);

    Assertions.assertEquals(SeedClass.class, reflexion.getWrappedClass());
    Assertions.assertThrows(ReflexionException.class, () -> Reflexion.get("a", loader));
  }

  @Test
  void testClassLookupAny() {
    Optional<Reflexion> reflexion = Reflexion.findAny("gone", "gone2", "dev.derklaro.reflexion.helper.SeedClass");

    Assertions.assertTrue(reflexion.isPresent());
    Assertions.assertEquals(SeedClass.class, reflexion.get().getWrappedClass());
  }

  @Test
  void testClassLookupAnyClassLoader() {
    ClassLoader loader = ClassLoader.getSystemClassLoader();
    Optional<Reflexion> reflexion = Reflexion.findAny(
      loader,
      "gone", "gone2", "dev.derklaro.reflexion.helper.SeedClass");

    Assertions.assertTrue(reflexion.isPresent());
    Assertions.assertEquals(SeedClass.class, reflexion.get().getWrappedClass());
  }

  @Test
  void testClassGetAny() {
    Reflexion reflexion = Reflexion.getAny("gone", "gone2", "dev.derklaro.reflexion.helper.SeedClass");

    Assertions.assertEquals(SeedClass.class, reflexion.getWrappedClass());
    Assertions.assertThrows(ReflexionException.class, () -> Reflexion.getAny("a", "b", "c"));
  }

  @Test
  void testClassGetAnyClassLoader() {
    ClassLoader loader = ClassLoader.getSystemClassLoader();
    Reflexion reflexion = Reflexion.getAny(loader, "gone", "gone2", "dev.derklaro.reflexion.helper.SeedClass");

    Assertions.assertEquals(SeedClass.class, reflexion.getWrappedClass());
    Assertions.assertThrows(ReflexionException.class, () -> Reflexion.getAny(loader, "a", "b", "c"));
  }
}
