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

import dev.derklaro.reflexion.internal.util.Util;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

public final class Result<S> implements Supplier<S> {

  private final S result;
  private final Throwable exception;

  private Result(S result, Throwable exception) {
    this.result = result;
    this.exception = exception;
  }

  // ------------------
  // factory methods
  // ------------------

  public static <T> Result<T> tryExecute(@NonNull ExceptionalSupplier<T> supplier) {
    try {
      T result = supplier.get();
      return success(result);
    } catch (Throwable exception) {
      return exceptional(exception);
    }
  }

  public static @NonNull <T> Result<T> success(@Nullable T result) {
    return new Result<>(result, null);
  }

  public static @NonNull <T> Result<T> exceptional(@NonNull Throwable exception) {
    return new Result<>(null, exception);
  }

  // ------------------
  // instance methods
  // ------------------

  public boolean wasSuccessful() {
    return this.exception == null;
  }

  public boolean wasExceptional() {
    return this.exception != null;
  }

  public @NonNull Throwable getException() {
    if (this.exception != null) {
      return this.exception;
    }

    throw new NoSuchElementException("Unable to retrieve error on successful result!");
  }

  @Override
  public S get() {
    if (this.exception == null) {
      return this.result;
    }

    throw new NoSuchElementException("Unable to retrieve success result from exceptional result!");
  }

  public @UnknownNullability S getOrElse(@Nullable S or) {
    return this.exception == null ? this.result : or;
  }

  public @UnknownNullability S getOrElse(@NonNull Function<Throwable, S> function) {
    return this.exception == null ? this.result : function.apply(this.exception);
  }

  public @UnknownNullability S getOrThrow() {
    if (this.exception != null) {
      Util.throwUnchecked(this.exception);
    }
    return this.result;
  }

  @SuppressWarnings("unchecked")
  public @NonNull <M> Result<M> map(@NonNull Function<S, M> function) {
    if (this.exception != null) {
      return (Result<M>) this;
    } else {
      return Result.tryExecute(() -> function.apply(this.result));
    }
  }

  public @NonNull Result<S> mapExceptional(@NonNull Function<Throwable, Throwable> function) {
    return this.exception == null ? this : Result.exceptional(function.apply(this.exception));
  }

  public void ifSuccess(@NonNull Consumer<S> consumer) {
    if (this.exception == null) {
      consumer.accept(this.result);
    }
  }

  public void ifExceptional(@NonNull Consumer<Throwable> consumer) {
    if (this.exception != null) {
      consumer.accept(this.exception);
    }
  }

  public void consume(@NonNull BiConsumer<S, Throwable> consumer) {
    consumer.accept(this.result, this.exception);
  }

  public @NonNull Optional<S> asOptional() {
    return Optional.ofNullable(this.result);
  }

  @FunctionalInterface
  public interface ExceptionalSupplier<T> {

    @Nullable T get() throws Throwable;
  }
}
