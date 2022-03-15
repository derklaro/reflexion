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

import dev.derklaro.reflexion.internal.util.Type;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TypeTest {

  @Test
  void testQualifiedName() {
    Assertions.assertEquals("java/lang/Object", Type.getQualifiedName(Object.class));
    Assertions.assertEquals("dev/derklaro/reflexion/SeedClass", Type.getQualifiedName(SeedClass.class));
  }

  @Test
  void testPrimitiveTypeSignature() {
    Assertions.assertEquals("Z", Type.getSignature(boolean.class));
    Assertions.assertEquals("B", Type.getSignature(byte.class));
    Assertions.assertEquals("C", Type.getSignature(char.class));
    Assertions.assertEquals("S", Type.getSignature(short.class));
    Assertions.assertEquals("I", Type.getSignature(int.class));
    Assertions.assertEquals("J", Type.getSignature(long.class));
    Assertions.assertEquals("F", Type.getSignature(float.class));
    Assertions.assertEquals("D", Type.getSignature(double.class));

    Assertions.assertEquals("[Z", Type.getSignature(boolean[].class));
    Assertions.assertEquals("[B", Type.getSignature(byte[].class));
    Assertions.assertEquals("[C", Type.getSignature(char[].class));
    Assertions.assertEquals("[S", Type.getSignature(short[].class));
    Assertions.assertEquals("[I", Type.getSignature(int[].class));
    Assertions.assertEquals("[J", Type.getSignature(long[].class));
    Assertions.assertEquals("[F", Type.getSignature(float[].class));
    Assertions.assertEquals("[D", Type.getSignature(double[].class));

    Assertions.assertEquals("[[Z", Type.getSignature(boolean[][].class));
    Assertions.assertEquals("[[B", Type.getSignature(byte[][].class));
    Assertions.assertEquals("[[C", Type.getSignature(char[][].class));
    Assertions.assertEquals("[[S", Type.getSignature(short[][].class));
    Assertions.assertEquals("[[I", Type.getSignature(int[][].class));
    Assertions.assertEquals("[[J", Type.getSignature(long[][].class));
    Assertions.assertEquals("[[F", Type.getSignature(float[][].class));
    Assertions.assertEquals("[[D", Type.getSignature(double[][].class));
  }

  @Test
  void testObjectTypeSignature() {
    Assertions.assertEquals("Ljava/lang/Object;", Type.getSignature(Object.class));
    Assertions.assertEquals("Ldev/derklaro/reflexion/SeedClass;", Type.getSignature(SeedClass.class));

    Assertions.assertEquals("[Ljava/lang/Object;", Type.getSignature(Object[].class));
    Assertions.assertEquals("[Ldev/derklaro/reflexion/SeedClass;", Type.getSignature(SeedClass[].class));

    Assertions.assertEquals("[[Ljava/lang/Object;", Type.getSignature(Object[][].class));
    Assertions.assertEquals("[[Ldev/derklaro/reflexion/SeedClass;", Type.getSignature(SeedClass[][].class));
  }
}
