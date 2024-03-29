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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import lombok.NonNull;

/**
 * Internal utility class to read all java.lang.reflect members from a class and its super classes.
 *
 * @since 1.0
 */
final class ReflexionPopulator {

  private ReflexionPopulator() {
    throw new UnsupportedOperationException();
  }

  /**
   * Get all (declared) fields in the given class, and it's super classes.
   *
   * @param declaringClass the class to start the search from.
   * @return all fields in the given class, and it's super classes.
   * @throws NullPointerException if the given topmost class is null.
   */
  public static @NonNull Set<Field> getAllFields(@NonNull Class<?> declaringClass) {
    return mappingHierarchyTravel(declaringClass, Class::getDeclaredFields);
  }

  /**
   * Get all (declared) methods in the given class, and it's super classes.
   *
   * @param declaringClass the class to start the search from.
   * @return all methods in the given class, and it's super classes.
   * @throws NullPointerException if the given topmost class is null.
   */
  public static @NonNull Set<Method> getAllMethods(@NonNull Class<?> declaringClass) {
    return mappingHierarchyTravel(declaringClass, Class::getDeclaredMethods);
  }

  /**
   * Get all (declared) constructors in the given class, and it's super classes.
   *
   * @param declaringClass the class to start the search from.
   * @return all constructors in the given class and it's super classes.
   * @throws NullPointerException if the given topmost class is null.
   */
  public static @NonNull Set<Constructor<?>> getAllConstructors(@NonNull Class<?> declaringClass) {
    return mappingHierarchyTravel(declaringClass, Class::getDeclaredConstructors);
  }

  /**
   * Travels down the class tree beginning from the given topmost class, extracting all public and declared members from
   * each visited class (using the given extractor functions) and collects them into a set. This method is not cached.
   *
   * @param top       the topmost class to start the search from.
   * @param extractor the extractor function for declared members.
   * @param <T>       the type of member which gets extracted from the class tree.
   * @return all extracted members from the class tree.
   * @throws NullPointerException if the given topmost class or one of the extractor functions is null.
   */
  private static @NonNull <T extends Member> Set<T> mappingHierarchyTravel(
    @NonNull Class<?> top,
    @NonNull Function<Class<?>, T[]> extractor
  ) {
    // all public members + init
    Set<T> classMembers = new LinkedHashSet<>();

    // all private members
    Class<?> current = top;
    do {
      // skip no element arrays
      T[] members = extractor.apply(current);
      if (members.length == 0) {
        continue;
      }

      // single element optimization
      if (members.length == 1) {
        classMembers.add(members[0]);
        continue;
      }

      // add all elements of the set of known members
      Collections.addAll(classMembers, members);
    } while ((current = current.getSuperclass()) != null);

    return classMembers;
  }
}
