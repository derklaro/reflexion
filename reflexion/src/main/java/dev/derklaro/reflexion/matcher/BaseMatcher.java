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

package dev.derklaro.reflexion.matcher;

import dev.derklaro.reflexion.internal.util.Util;
import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import lombok.NonNull;
import org.intellij.lang.annotations.Language;

/**
 * The base for all matcher which can match some kind of member in a class.
 *
 * @param <T> the type of the class member matched by this matcher.
 * @param <M> the type of the matcher itself.
 */
@SuppressWarnings("unchecked") // we all love generics, don't we?
public abstract class BaseMatcher<T extends Member, M extends BaseMatcher<T, M>> implements Predicate<T> {

  protected Predicate<T> currentMatcher = $ -> true;

  /**
   * Checks if the member has the exact name which was supplied.
   *
   * @param name the name the member must have.
   * @return the same instance as used to call the method, for chaining.
   * @throws NullPointerException if the given name is null.
   */
  public @NonNull M hasName(@NonNull String name) {
    return this.and(member -> member.getName().equals(name));
  }

  /**
   * Checks if the member has a name which matches the given regexp.
   *
   * @param name  the regexp for the member name.
   * @param flags the match flags to supply to the pattern.
   * @return the same instance as used to call the method, for chaining.
   * @throws NullPointerException     if the given name is null.
   * @throws IllegalArgumentException if illegal match flags were supplied.
   * @throws PatternSyntaxException   if the given regexp is invalid.
   */
  public @NonNull M hasMatchingName(@NonNull @Language("RegExp") String name, int... flags) {
    // compile the flags
    int compiledFlags = 0;
    if (flags.length > 0) {
      for (int flag : flags) {
        compiledFlags |= flag;
      }
    }

    Pattern pattern = Pattern.compile(name, compiledFlags);
    return this.and(member -> pattern.matcher(member.getName()).matches());
  }

  /**
   * Checks if the member has the given modifier.
   *
   * @param mod the modifier which the member should have.
   * @return the same instance as used to call the method, for chaining.
   */
  public @NonNull M hasModifier(int mod) {
    return this.and(member -> (member.getModifiers() & mod) == mod);
  }

  /**
   * Checks if the member has all the given modifiers.
   *
   * @param mods the modifiers which the member should have.
   * @return the same instance as used to call the method, for chaining.
   */
  public @NonNull M hasModifiers(int... mods) {
    // combine the modifiers
    int mod = 0;
    for (int i : mods) {
      mod |= i;
    }

    return this.hasModifier(mod);
  }

  /**
   * Checks if the given member doesn't have the given modifier.
   *
   * @param mod the modifier which the given member shouldn't have.
   * @return the same instance as used to call the method, for chaining.
   */
  public @NonNull M denyModifier(int mod) {
    return this.and(member -> (member.getModifiers() & mod) != mod);
  }

  /**
   * Checks if the given member doesn't have all the given modifiers.
   *
   * @param mods the modifiers which the member shouldn't have.
   * @return the same instance as used to call the method, for chaining.
   */
  public @NonNull M denyModifiers(int... mods) {
    // combine the modifiers
    int mod = 0;
    for (int i : mods) {
      mod |= i;
    }

    return this.denyModifier(mod);
  }

  /**
   * Checks if the supplied type from the reader function is exactly the given expected type.
   *
   * @param typeReader   the type extractor from the target member.
   * @param expectedType the type which is expected.
   * @return the same instance as used to call the method, for chaining.
   * @throws NullPointerException if the given reader function or expected type is null.
   */
  public @NonNull M exactType(@NonNull Function<T, Class<?>> typeReader, @NonNull Class<?> expectedType) {
    return this.and(member -> {
      Class<?> type = typeReader.apply(member);
      return type != null && type.equals(expectedType);
    });
  }

  /**
   * Checks if the supplied type from the reader function is an instance of the expected type (expected instanceof
   * input).
   *
   * @param typeReader   the type extractor from the target member.
   * @param expectedType the type which is being matched.
   * @return the same instance as used to call the method, for chaining.
   * @throws NullPointerException if the given reader function or expected type is null.
   */
  public @NonNull M superType(@NonNull Function<T, Class<?>> typeReader, @NonNull Class<?> expectedType) {
    return this.and(member -> {
      Class<?> type = typeReader.apply(member);
      return type != null && type.isAssignableFrom(expectedType);
    });
  }

  /**
   * Checks if the supplied type from the reader function is a derived instance of the given type (input instanceof
   * expected).
   *
   * @param typeReader   the type extractor from the target member.
   * @param expectedType the type which is being matched.
   * @return the same instance as used to call the method, for chaining.
   * @throws NullPointerException if the given type extractor or expected type is null.
   */
  public @NonNull M derivedType(@NonNull Function<T, Class<?>> typeReader, @NonNull Class<?> expectedType) {
    return this.and(member -> {
      Class<?> type = typeReader.apply(member);
      return type != null && expectedType.isAssignableFrom(type);
    });
  }

  /**
   * Checks if all types supplied by the given type reader match the given types.
   *
   * @param typesReader   the extractor of the types from the target member.
   * @param expectedTypes the types which are expected.
   * @return the same instance as used to call the method, for chaining.
   * @throws NullPointerException if the given extractor or expected type array is null.
   */
  public @NonNull M exactTypes(@NonNull Function<T, Class<?>[]> typesReader, @NonNull Class<?>... expectedTypes) {
    return this.and(member -> {
      Class<?>[] types = typesReader.apply(member);
      return types != null && Arrays.equals(types, expectedTypes);
    });
  }

  /**
   * Checks if all types supplied by the given type reader match the given types. (expected instanceof input).
   *
   * @param typesReader   the extractor of the types from the target member.
   * @param expectedTypes the types which are expected.
   * @return the same instance as used to call the method, for chaining.
   * @throws NullPointerException if the given extractor or expected type array is null.
   */
  public @NonNull M superTypes(@NonNull Function<T, Class<?>[]> typesReader, @NonNull Class<?>... expectedTypes) {
    return this.and(member -> {
      Class<?>[] types = typesReader.apply(member);
      return Util.allMatch(expectedTypes, types, (expected, actual) -> actual.isAssignableFrom(expected));
    });
  }

  /**
   * Checks if all types supplied by the given type reader match the given types. (input instanceof expected).
   *
   * @param reader        the extractor of the types from the target member.
   * @param expectedTypes the types which are expected.
   * @return the same instance as used to call the method, for chaining.
   * @throws NullPointerException if the given extractor or expected type array is null.
   */
  public @NonNull M derivedTypes(@NonNull Function<T, Class<?>[]> reader, @NonNull Class<?>... expectedTypes) {
    return this.and(member -> {
      Class<?>[] types = reader.apply(member);
      return Util.allMatch(expectedTypes, types, Class::isAssignableFrom);
    });
  }

  /**
   * Checks if the type array extracted from the target member has the given type at the given index. This matcher will
   * also not match if the given index either underflow the array length (&lt; 0) or overflows the array length.
   *
   * @param typesReader  the extractor of the types from the target member.
   * @param expectedType the type which is expected at the given position.
   * @param idx          the position to check the type of.
   * @return the same instance as used to call the method, for chaining.
   * @throws NullPointerException if the given type reader or expected type is null.
   */
  public @NonNull M exactTypeAt(@NonNull Function<T, Class<?>[]> typesReader, @NonNull Class<?> expectedType, int idx) {
    return this.and(member -> {
      Class<?>[] types = typesReader.apply(member);
      return types != null && idx >= 0 && types.length > idx && types[idx].equals(expectedType);
    });
  }

  /**
   * Checks if the type array extracted from the target member has the given type at the given index (expected
   * instanceof input). This matcher will also not match if the given index either underflow the array length (&lt; 0)
   * or overflows the array length.
   *
   * @param typesReader  the extractor of the types from the target member.
   * @param expectedType the type which is expected at the given position.
   * @param idx          the position to check the type of.
   * @return the same instance as used to call the method, for chaining.
   * @throws NullPointerException if the given type reader or expected type is null.
   */
  public @NonNull M superTypeAt(@NonNull Function<T, Class<?>[]> typesReader, @NonNull Class<?> expectedType, int idx) {
    return this.and(member -> {
      Class<?>[] types = typesReader.apply(member);
      return types != null && idx >= 0 && types.length > idx && types[idx].isAssignableFrom(expectedType);
    });
  }

  /**
   * Checks if the type array extracted from the target member has the given type at the given index (input instanceof
   * expected). This matcher will also not match if the given index either underflow the array length (&lt; 0) or
   * overflows the array length.
   *
   * @param reader       the extractor of the types from the target member.
   * @param expectedType the type which is expected at the given position.
   * @param idx          the position to check the type of.
   * @return the same instance as used to call the method, for chaining.
   * @throws NullPointerException if the given type reader or expected type is null.
   */
  public @NonNull M derivedTypeAt(@NonNull Function<T, Class<?>[]> reader, @NonNull Class<?> expectedType, int idx) {
    return this.and(member -> {
      Class<?>[] types = reader.apply(member);
      return types != null && idx >= 0 && types.length > idx && expectedType.isAssignableFrom(types[idx]);
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean test(T t) {
    return this.currentMatcher.test(t);
  }

  /**
   * Composes the current matching predicate with the supplied one using an AND operation. Note: other than the super
   * method this method mutates this object rather than returning a new matcher object.
   *
   * @param other the other predicate which must match.
   * @return the same instance as used to call the method, for chaining.
   * @throws NullPointerException if the other predicate is null.
   */
  @Override
  public @NonNull M and(@NonNull Predicate<? super T> other) {
    this.currentMatcher = this.currentMatcher.and(other);
    return (M) this;
  }

  /**
   * Composes the current matching predicate with the supplied one using an OR operation. Note: other than the super
   * method this method mutates this object rather than returning a new matcher object.
   *
   * @param other the other predicate which must match.
   * @return the same instance as used to call the method, for chaining.
   * @throws NullPointerException if the other predicate is null.
   */
  @Override
  public @NonNull M or(@NonNull Predicate<? super T> other) {
    this.currentMatcher = this.currentMatcher.or(other);
    return (M) this;
  }

  /**
   * Negates the current matching predicate. Note: other than the super method this method mutates this object rather
   * than returning a new matcher object.
   *
   * @return the same instance as used to call the method, for chaining.
   */
  @Override
  public @NonNull M negate() {
    this.currentMatcher = this.currentMatcher.negate();
    return (M) this;
  }
}
