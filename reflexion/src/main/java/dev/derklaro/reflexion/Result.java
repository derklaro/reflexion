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

import dev.derklaro.reflexion.internal.util.Exceptions;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

/**
 * A rust inspired result class which either holds the result of an executable or the exception thrown by it. That makes
 * it very easy to write clean code and give exceptions back to the actual producer of them rather than requiring a
 * wrapped rethrow of it.
 *
 * @param <S> the successful result type of the execution.
 * @since 1.0
 */
public final class Result<S> implements Supplier<S> {

  private final S result;
  private final Throwable exception;

  /**
   * Constructs a new result type. Either the result or the exception must be present. The state of the result is
   * determined by whether the given exception is present or absent.
   *
   * @param result    the result of the execution, can be null.
   * @param exception the exceptional result the execution, only present when the result failed.
   */
  private Result(S result, Throwable exception) {
    this.result = result;
    this.exception = exception;
  }

  // ------------------
  // factory methods
  // ------------------

  /**
   * Tries to execute the supplier and use the result of it as the value of this result. If the execution of the
   * supplier fails a result holding the thrown exception is returned instead.
   * <p>
   * This method instantly rethrows unrecoverable errors which prevent the current thread from resuming.
   *
   * @param supplier the which should get executed.
   * @param <T>      the type of the action result.
   * @return the result of the action execution, either holding the result value or the thrown exception.
   * @throws NullPointerException if the given supplier is null.
   */
  public static <T> Result<T> tryExecute(@NonNull ExceptionalSupplier<T> supplier) {
    try {
      T result = supplier.get();
      return success(result);
    } catch (Throwable exception) {
      Exceptions.rethrowIfFatal(exception);
      return exceptional(exception);
    }
  }

  /**
   * Constructs a new result instance holding the given object as its successful result.
   *
   * @param result the successful result of the action.
   * @param <T>    the type of the result.
   * @return a new result instance which succeeded and holds the given object as it's result value.
   */
  public static @NonNull <T> Result<T> success(@Nullable T result) {
    return new Result<>(result, null);
  }

  /**
   * Constructs a new result instance which failed and holds the given exception as the reason of the failure.
   *
   * @param exception the reason of the failure.
   * @param <T>       the expected successful return type, even tho the result is never successful.
   * @return a new result instance holding the given exception as the failure reason.
   * @throws NullPointerException if the given exception is null.
   */
  public static @NonNull <T> Result<T> exceptional(@NonNull Throwable exception) {
    return new Result<>(null, exception);
  }

  // ------------------
  // instance methods
  // ------------------

  /**
   * Get if this result was executed successfully or if it failed for some reason.
   *
   * @return true if this result was executed successfully, false otherwise.
   */
  public boolean wasSuccessful() {
    return this.exception == null;
  }

  /**
   * Get if this result execution failed for some reason.
   *
   * @return true if the result holds an exception, false otherwise.
   */
  public boolean wasExceptional() {
    return this.exception != null;
  }

  /**
   * Get the exception which was thrown during the execution.
   *
   * @return the thrown exception.
   * @throws NoSuchElementException if this result was executed successfully.
   */
  public @NonNull Throwable getException() {
    if (this.exception != null) {
      return this.exception;
    }

    throw new NoSuchElementException("Unable to retrieve error on successful result!");
  }

  /**
   * Get the successful result wrapped in this result.
   *
   * @return the successful result.
   * @throws NoSuchElementException if the result was executed exceptionally.
   */
  @Override
  public S get() {
    if (this.exception == null) {
      return this.result;
    }

    throw new NoSuchElementException("Unable to retrieve success result from exceptional result!");
  }

  /**
   * Get the successful result wrapped in this result or the given value if this result was executed exceptionally.
   *
   * @param or the value to get if the result was executed exceptionally.
   * @return the wrapped successful return value or the given value.
   */
  public @UnknownNullability S getOrElse(@Nullable S or) {
    return this.exception == null ? this.result : or;
  }

  /**
   * Get the successful result wrapped in this result or evaluates the given supplier value and returns that.
   *
   * @param supplier the supplier to evaluate if this result was executed exceptionally.
   * @return the wrapped success return value or the evaluated value from the given supplier.
   * @throws NullPointerException if the given supplier is null.
   */
  public @UnknownNullability S getOrEval(@NonNull Supplier<S> supplier) {
    return this.exception == null ? this.result : supplier.get();
  }

  /**
   * Get the successful result wrapped in this result or maps the wrapped exception using the supplied function.
   *
   * @param function the function to call with the exception thrown by the execution.
   * @return the wrapped success return value or the evaluated value from the given function.
   * @throws NullPointerException if the given function is null.
   */
  public @UnknownNullability S getOrMap(@NonNull Function<Throwable, S> function) {
    return this.exception == null ? this.result : function.apply(this.exception);
  }

  /**
   * Get the wrapped success value of this result or rethrows the wrapped exception unchecked.
   *
   * @return the successful return value of the result.
   */
  public @UnknownNullability S getOrThrow() {
    if (this.exception != null) {
      Exceptions.throwUnchecked(this.exception);
    }
    return this.result;
  }

  /**
   * Maps the successful result of this result into a new one or returns the same instance as before when this result
   * already contained an exception. This method will return an exceptional result if the mapping function throws an
   * exception.
   *
   * @param function the mapping function to map the success value of this result.
   * @param <M>      the new result type mapped from the given function.
   * @return a new result based on the mapping function if this result was executed successfully.
   * @throws NullPointerException if the given function is null.
   */
  @SuppressWarnings("unchecked")
  public @NonNull <M> Result<M> map(@NonNull Function<S, M> function) {
    if (this.exception != null) {
      return (Result<M>) this;
    } else {
      return Result.tryExecute(() -> function.apply(this.result));
    }
  }

  /**
   * Maps the successful result of this result into a new one or returns the same instance as before when this result
   * already contained an exception. This method will not catch any exceptions thrown by the mapping function.
   *
   * @param function the mapping function to map the success value of this result.
   * @param <M>      the new result type mapped from the given function.
   * @return a new result based on the mapping function if this result was executed successfully.
   * @throws NullPointerException if the given function is null.
   * @since 1.5.0
   */
  @SuppressWarnings("unchecked")
  public @NonNull <M> Result<M> flatMap(@NonNull Function<S, Result<M>> function) {
    if (this.exception != null) {
      return (Result<M>) this;
    } else {
      return function.apply(this.result);
    }
  }

  /**
   * Maps the wrapped exceptional result of this result into a new one or returns the same instance as before when this
   * result was executed successfully. This method will always return an exceptional result. This method will not catch
   * any exceptions thrown by the mapping function.
   *
   * @param function the mapping function for the wrapped exception.
   * @return a new result based on the mapping function if this result was executed exceptionally.
   * @throws NullPointerException if the given mapping function is null.
   */
  public @NonNull Result<S> mapExceptional(@NonNull Function<Throwable, Throwable> function) {
    return this.exception == null ? this : Result.exceptional(function.apply(this.exception));
  }

  /**
   * Maps the wrapped exceptional result of this result into a new one or returns the same instance as before when this
   * result was executed successfully. This method will not catch any exceptions thrown by the mapping function.
   *
   * @param function the mapping function for the wrapped exception.
   * @return a new result based on the mapping function if this result was executed exceptionally.
   * @throws NullPointerException if the given mapping function is null.
   * @since 1.5.0
   */
  @SuppressWarnings("unchecked")
  public @NonNull <M> Result<M> flatMapExceptional(@NonNull Function<Throwable, Result<M>> function) {
    if (this.exception == null) {
      return (Result<M>) this;
    } else {
      return function.apply(this.exception);
    }
  }

  /**
   * Executes the given consumer using the wrapped successful result value if present. This method does nothing if the
   * result completed exceptionally.
   *
   * @param consumer the consumer to call with the wrapped successful value.
   * @return the same instance as used to call the method, for chaining.
   * @throws NullPointerException if the given consumer is null.
   */
  public @NonNull Result<S> ifSuccess(@NonNull Consumer<S> consumer) {
    if (this.exception == null) {
      consumer.accept(this.result);
    }
    return this;
  }

  /**
   * Executes the given consumer using the wrapped exceptional result value if present. This method does nothing if the
   * result completed successfully.
   *
   * @param consumer the consumer to call with the wrapped exceptional result.
   * @return the same instance as used to call the method, for chaining.
   */
  public @NonNull Result<S> ifExceptional(@NonNull Consumer<Throwable> consumer) {
    if (this.exception != null) {
      consumer.accept(this.exception);
    }
    return this;
  }

  /**
   * Consumes both, the successful and exceptional result value wrapped in this result and passes them to the given
   * consumer. The given exception will be null if the result completed successfully.
   * <p>
   * Note: the successful result type might be null even when the result completed successfully, indicating that the
   * execution returned null.
   *
   * @param consumer the consumer to accept both, the successful and exceptional value wrapped in this result.
   * @return the same instance as used to call the method, for chaining.
   * @throws NullPointerException if the given consumer is null.
   */
  public @NonNull Result<S> consume(@NonNull BiConsumer<S, Throwable> consumer) {
    consumer.accept(this.result, this.exception);
    return this;
  }

  /**
   * Wraps the successful value of this result in an optional. Note: the optional value might be absent even when the
   * result completed successfully, indicating that the execution returned null.
   *
   * @return an optional of the wrapped return value in this result.
   */
  public @NonNull Optional<S> asOptional() {
    return Optional.ofNullable(this.result);
  }

  /**
   * Represents a supplier which can throw any exception when executing.
   *
   * @param <T> the result type supplied by this supplier.
   * @since 1.0
   */
  @FunctionalInterface
  public interface ExceptionalSupplier<T> {

    /**
     * Get the result of the supply.
     *
     * @return the result.
     * @throws Throwable if any exception occurs while evaluating the result of this supplier.
     */
    @Nullable T get() throws Throwable;
  }
}
