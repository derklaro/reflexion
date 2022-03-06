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

package dev.derklaro.reflexion.internal.util;

import java.lang.reflect.Field;
import java.util.Map;
import lombok.NonNull;

public final class Type {

  private static final Map<Class<?>, String> PRIMITIVE_TYPES = Util.newMapFromArray(
    boolean.class, "Z",
    byte.class, "B",
    char.class, "C",
    short.class, "S",
    int.class, "I",
    long.class, "J",
    float.class, "F",
    double.class, "D");

  private Type() {
    throw new UnsupportedOperationException();
  }

  public static @NonNull String getQualifiedName(@NonNull Class<?> clazz) {
    return clazz.getName().replace('.', '/');
  }

  public static @NonNull String getFieldSignature(@NonNull Field field) {
    return getSignature(field.getType());
  }

  public static @NonNull String getSignature(@NonNull Class<?> clazz) {
    if (clazz.isPrimitive()) {
      return PRIMITIVE_TYPES.get(clazz);
    } else if (clazz.isArray()) {
      return String.format("[%s", getSignature(clazz.getComponentType()));
    } else {
      return String.format("L%s;", getQualifiedName(clazz));
    }
  }
}
