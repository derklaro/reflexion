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

import java.lang.reflect.Member;
import lombok.NonNull;

/**
 * Represents the base for all accessors created by this library. Each accessor is bound to the reflexion object which
 * created it and holds a reference to the java.lang.reflect member which it wraps.
 *
 * @param <T> the member which is wrapped by the accessor.
 */
public interface BaseAccessor<T extends Member> {

  /**
   * Get the java.lang.reflect member which is wrapped by this accessor.
   *
   * @return the wrapped member.
   */
  @NonNull T getMember();

  /**
   * Get the reflexion instance which created this accessor instance.
   *
   * @return the reflexion instance which created this instance.
   */
  @NonNull Reflexion getReflexion();
}
