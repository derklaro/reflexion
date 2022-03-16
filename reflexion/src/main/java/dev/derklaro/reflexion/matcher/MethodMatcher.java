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

package dev.derklaro.reflexion.matcher;

import java.lang.reflect.Method;
import lombok.NonNull;

/**
 * A matcher for methods.
 *
 * @since 1.0
 */
public final class MethodMatcher extends BaseMatcher<Method, MethodMatcher> {

  private MethodMatcher() {
  }

  /**
   * Constructs a new method matcher instance.
   *
   * @return a new method matcher.
   */
  public static @NonNull MethodMatcher newMatcher() {
    return new MethodMatcher();
  }

  /**
   * Checks if the method has the given amount of parameters.
   *
   * @param count the expected amount of parameters.
   * @return the same instance as used to call the method, for chaining.
   */
  public @NonNull MethodMatcher parameterCount(int count) {
    return this.and(member -> member.getParameterCount() == count);
  }
}