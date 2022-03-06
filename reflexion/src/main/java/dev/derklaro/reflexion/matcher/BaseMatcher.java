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

import java.lang.reflect.Member;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.NonNull;

@SuppressWarnings("unchecked") // we all love generics, don't we?
public abstract class BaseMatcher<T extends Member, M extends BaseMatcher<T, M>> implements Predicate<T> {

  protected Predicate<T> currentMatcher = $ -> true;

  public @NonNull M hasName(@NonNull String name) {
    return this.and(member -> member.getName().equals(name));
  }

  public @NonNull M hasMatchingName(@NonNull String name) {
    return this.and(member -> member.getName().matches(name));
  }

  public @NonNull M hasModifier(int mod) {
    return this.and(member -> (member.getModifiers() & mod) == mod);
  }

  public @NonNull M hasModifiers(int... mods) {
    // combine the modifiers
    int mod = 0;
    for (int i : mods) {
      mod |= i;
    }

    return this.hasModifier(mod);
  }

  public @NonNull M denyModifier(int mod) {
    return this.and(member -> (member.getModifiers() & mod) != mod);
  }

  public @NonNull M denyModifiers(int... mods) {
    // combine the modifiers
    int mod = 0;
    for (int i : mods) {
      mod |= i;
    }

    return this.denyModifier(mod);
  }

  public @NonNull M exactType(@NonNull Function<T, Class<?>> typeReader, @NonNull Class<?> expectedType) {
    return this.and(member -> {
      Class<?> type = typeReader.apply(member);
      return type != null && type.equals(expectedType);
    });
  }

  public @NonNull M superType(@NonNull Function<T, Class<?>> typeReader, @NonNull Class<?> expectedType) {
    return this.and(member -> {
      Class<?> type = typeReader.apply(member);
      return type != null && type.isAssignableFrom(expectedType);
    });
  }

  public @NonNull M derivedType(@NonNull Function<T, Class<?>> typeReader, @NonNull Class<?> expectedType) {
    return this.and(member -> {
      Class<?> type = typeReader.apply(member);
      return type != null && expectedType.isAssignableFrom(type);
    });
  }

  public @NonNull M exactTypeAt(@NonNull Function<T, Class<?>[]> typesReader, @NonNull Class<?> expectedType, int idx) {
    return this.and(member -> {
      Class<?>[] types = typesReader.apply(member);
      return types != null && idx >= 0 && types.length > idx && types[idx].equals(expectedType);
    });
  }

  public @NonNull M superTypeAt(@NonNull Function<T, Class<?>[]> typesReader, @NonNull Class<?> expectedType, int idx) {
    return this.and(member -> {
      Class<?>[] types = typesReader.apply(member);
      return types != null && idx >= 0 && types.length > idx && types[idx].isAssignableFrom(expectedType);
    });
  }

  public @NonNull M derivedTypeAt(@NonNull Function<T, Class<?>[]> reader, @NonNull Class<?> expectedType, int idx) {
    return this.and(member -> {
      Class<?>[] types = reader.apply(member);
      return types != null && idx >= 0 && types.length > idx && expectedType.isAssignableFrom(types[idx]);
    });
  }

  @Override
  public boolean test(T t) {
    return this.currentMatcher.test(t);
  }

  @Override
  public @NonNull M and(@NonNull Predicate<? super T> other) {
    this.currentMatcher = this.currentMatcher.and(other);
    return (M) this;
  }

  @Override
  public @NonNull M or(@NonNull Predicate<? super T> other) {
    this.currentMatcher = this.currentMatcher.or(other);
    return (M) this;
  }

  @Override
  public @NonNull M negate() {
    this.currentMatcher = this.currentMatcher.negate();
    return (M) this;
  }
}
