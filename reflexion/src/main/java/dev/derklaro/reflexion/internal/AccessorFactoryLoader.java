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
import dev.derklaro.reflexion.internal.natives.NativeAccessorFactory;
import java.util.Arrays;
import lombok.NonNull;

public final class AccessorFactoryLoader {

  private static final AccessorFactory[] FACTORIES = new AccessorFactory[]{
    new NativeAccessorFactory(), new MethodHandleAccessorFactory(), new BareAccessorFactory()
  };

  private AccessorFactoryLoader() {
    throw new UnsupportedOperationException();
  }

  public static @NonNull AccessorFactory doLoadFactory() {
    // filter out all non-available factories and use the best one
    return Arrays.stream(FACTORIES)
      .filter(AccessorFactory::isAvailable)
      .sorted()
      .findFirst()
      .orElseThrow(() -> new IllegalStateException("cannot happen"));
  }
}
