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

package dev.derklaro.reflexion.internal;

import dev.derklaro.reflexion.AccessorFactory;
import dev.derklaro.reflexion.internal.bare.BareAccessorFactory;
import dev.derklaro.reflexion.internal.handles.MethodHandleAccessorFactory;
import dev.derklaro.reflexion.internal.jna.JnaAccessorFactory;
import dev.derklaro.reflexion.internal.natives.NativeAccessorFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import lombok.NonNull;

/**
 * Internal: loads the best accessor factory for the current environment of the jvm.
 *
 * @since 1.0
 */
public final class AccessorFactoryLoader {

  private static final AccessorFactory[] DEFAULT_FACTORIES = new AccessorFactory[]{
    new NativeAccessorFactory(), new JnaAccessorFactory(), new MethodHandleAccessorFactory(), new BareAccessorFactory()
  };

  private AccessorFactoryLoader() {
    throw new UnsupportedOperationException();
  }

  /**
   * Loads the best factory for the current environment, also making use of the service loader to allow other libraries
   * to opt in and offer their own implementation of an accessor factory. The factories are sorted based on their
   * implementation of comparable and the best matching one will be used in the environment.
   *
   * @return the best accessor factory for the current jvm environment.
   * @throws IllegalStateException if no accessor factories are available.
   */
  public static @NonNull AccessorFactory doLoadFactory() {
    // load all other factories, maybe brought in by external libs
    ClassLoader cl = AccessorFactoryLoader.class.getClassLoader();
    ServiceLoader<AccessorFactory> loader = ServiceLoader.load(AccessorFactory.class, cl);

    // find all factories which are available from the external ones
    List<AccessorFactory> availableFactories = new ArrayList<>();
    for (AccessorFactory factory : loader) {
      if (factory.isAvailable()) {
        availableFactories.add(factory);
      }
    }

    // add all factories which are available from the default ones
    for (AccessorFactory factory : DEFAULT_FACTORIES) {
      if (factory.isAvailable()) {
        availableFactories.add(factory);
      }
    }

    // if no factory is available this means we should hard crash (should by default never happen)
    if (availableFactories.isEmpty()) {
      throw new IllegalStateException("no accessor factory is available");
    }

    // sort the list to put the best factory to the top
    Collections.sort(availableFactories);
    return availableFactories.get(0);
  }
}
