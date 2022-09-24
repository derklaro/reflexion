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

import dev.derklaro.reflexion.Reflexion;
import dev.derklaro.reflexion.ReflexionException;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.NonNull;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.Unmodifiable;

/**
 * A collection of general purpose utility methods used within the library.
 *
 * @since 1.0
 */
public final class Util {

  private Util() {
    throw new UnsupportedOperationException();
  }

  /**
   * Constructs a new map from the given object array taking care of edge cases to ensure a small memory footprint if
   * possible.
   *
   * @param entries the base array to construct the map from.
   * @param <K>     the type of the keys in the new map.
   * @param <V>     the type of the values in the new map.
   * @return a new map with each pair of the given array in it.
   * @throws NullPointerException if the given object array is null.
   * @throws ReflexionException   if the given array doesn't contain a fitting amount of key-value pairs.
   */
  @SuppressWarnings("unchecked")
  public static <K, V> @Unmodifiable Map<K, V> newMapFromArray(@NonNull Object... entries) {
    // spare out some edge cases
    if (entries.length == 0) {
      return Collections.emptyMap();
    }

    // single key-value map
    if (entries.length == 2) {
      return Collections.singletonMap((K) entries[0], (V) entries[1]);
    }

    // check if the given array has a size which is dividable by 2 (key - value pairs)
    if ((entries.length % 2) != 0) {
      throw new ReflexionException("Given map based array has unexpected size of " + entries.length);
    }

    // fixed size map which prevents resizing
    Map<K, V> map = new HashMap<>(entries.length + 1, 1f);
    for (int i = 0; i < entries.length; i += 2) {
      map.put((K) entries[i], (V) entries[i + 1]);
    }

    return map;
  }

  /**
   * A fast version of a modulo operation.
   *
   * @param leftOperator  the left operand to execute the modulo on.
   * @param rightOperator the right operand.
   * @return the result of the modulo operation.
   * @deprecated use normal modulo instead, this method might return incorrect results.
   */
  @Deprecated
  @ScheduledForRemoval
  public static int fastModulo(int leftOperator, int rightOperator) {
    return leftOperator & (rightOperator - 1);
  }

  /**
   * Tries to find the first non-null value from the given array, returning null if no such value was found.
   *
   * @param choices the array to find the first non-null element in.
   * @param <T>     the type of elements in the given array.
   * @return the first non-null element in the given array, null if no such element was found.
   * @throws NullPointerException if the given array is null.
   */
  @SafeVarargs
  public static @UnknownNullability <T> T firstNonNull(T... choices) {
    // why would you do that
    if (choices.length == 0) {
      return null;
    }

    // well use the first one directly
    if (choices.length == 1) {
      return choices[0];
    }

    // choose between the first and second one
    if (choices.length == 2) {
      T left = choices[0];
      T right = choices[1];
      return left == null ? right : left;
    }

    // find the first one in the array
    for (T choice : choices) {
      if (choice != null) {
        return choice;
      }
    }

    // okay
    return null;
  }

  /**
   * Throws the given exception as an unchecked exception removing the need to catch non-runtime exceptions.
   *
   * @param throwable the throwable to rethrow.
   * @param <T>       the inferred type of the throwable.
   * @throws T                    the exception to rethrow.
   * @throws NullPointerException if the given throwable is null.
   */
  @SuppressWarnings("unchecked")
  public static <T extends Throwable> void throwUnchecked(@NonNull Throwable throwable) throws T {
    throw (T) throwable;
  }

  /**
   * Checks if all elements in both arrays match taking care of some edge cases to reduce computation time.
   *
   * @param leftArr  the left array to check.
   * @param rightArr the right array to check.
   * @param tester   the tester for the elements in the arrays.
   * @param <T>      the type of the elements in both arrays.
   * @return true if all elements in both arrays match the given matcher, false otherwise.
   * @throws NullPointerException if the given left/right array or element tester is null.
   */
  public static <T> boolean allMatch(@Nullable T[] leftArr, @Nullable T[] rightArr, @NonNull BiPredicate<T, T> tester) {
    // one array is null? they can't match
    if (leftArr == null || rightArr == null) {
      return leftArr == null && rightArr == null;
    }

    // must match if the arrays are the exact same or empty
    if (leftArr == rightArr || (leftArr.length == 0 && rightArr.length == 0)) {
      return true;
    }

    // if the sizes don't match we don't need to proceed
    if (leftArr.length != rightArr.length) {
      return false;
    }

    // only one item then compare only that one
    if (leftArr.length == 1) {
      return tester.test(leftArr[0], rightArr[0]);
    }

    // test all elements
    for (int i = 0; i < leftArr.length; i++) {
      if (!tester.test(leftArr[i], rightArr[i])) {
        return false;
      }
    }

    // all entries are matching
    return true;
  }

  /**
   * Filters all elements from the given collection and applies the matching ones to the given mapper and returns a
   * collection containing all filtered and mapped elements.
   *
   * @param in     the collection to filter and map the elements of.
   * @param tester the tester to filter the elements from the given input collection.
   * @param mapper the mapper to map the elements from the input collection which passed the given filter.
   * @param <T>    the type of elements before the mapping.
   * @param <O>    the type of elements after the mapping.
   * @return a collection containing all filtered and mapped elements from the given input collection.
   * @throws NullPointerException if the given input collection, tester or mapper is null.
   */
  public static @NonNull <T, O> Collection<O> filterAndMap(
    @NonNull Collection<T> in,
    @NonNull Predicate<T> tester,
    @NonNull Function<T, O> mapper
  ) {
    // skip empty collections
    if (in.isEmpty()) {
      return Collections.emptySet();
    }

    // singleton collection
    if (in.size() == 1) {
      T firstElement = in.iterator().next();
      return tester.test(firstElement) ? Collections.singleton(mapper.apply(firstElement)) : Collections.emptySet();
    }

    // map all elements
    Set<O> out = new HashSet<>();
    for (T t : in) {
      if (tester.test(t)) {
        out.add(mapper.apply(t));
      }
    }

    return out;
  }

  /**
   * Checks if a class with the given name is present in the current runtime. Calling this method will not initialize
   * the class.
   *
   * @param name the name of the class to check for presence.
   * @return true if a class with the given name is present, false otherwise.
   * @throws NullPointerException if the given name is null.
   */
  public static boolean isClassPresent(@NonNull String name) {
    try {
      // try to locate the class without initializing it - use the first available class loader for that
      ClassLoader cl = firstNonNull(
        Util.class.getClassLoader(),
        Thread.currentThread().getContextClassLoader(),
        ClassLoader.getSystemClassLoader());
      Class.forName(name, false, cl);

      return true;
    } catch (ClassNotFoundException exception) {
      return false;
    }
  }

  /**
   * Gets the binding instance to execute the requested action on a field or method. If the member is static this method
   * always returns {@code null} as no binding is required for the operation to succeed.
   * <p>
   * The given requested binding will be preferred over the binding of the reflexion instance.
   *
   * @param reflexionInstance the reflexion instance which created the accessor.
   * @param requestedBinding  the binding instance requested by the user, {@code null} if not given.
   * @param modifiers         the modifiers of the member the operation is made on.
   * @return the binding instance to use for executing the operation on the member.
   * @throws IllegalArgumentException if a binding is required but not given.
   */
  public static @Nullable Object getBinding(
    @NonNull Reflexion reflexionInstance,
    @Nullable Object requestedBinding,
    int modifiers
  ) {
    // static fields/methods don't need a binding to get the value from
    if (Modifier.isStatic(modifiers)) {
      return null;
    }

    // ensure that we got a binding
    Object binding = firstNonNull(requestedBinding, reflexionInstance.getBinding());
    if (binding == null) {
      throw new IllegalArgumentException("Missing binding to execute action on non-static accessor");
    }

    // binding is present
    return binding;
  }
}
