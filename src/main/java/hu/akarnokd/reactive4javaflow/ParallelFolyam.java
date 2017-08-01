/*
 * Copyright 2017 David Karnok
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hu.akarnokd.reactive4javaflow;

import hu.akarnokd.reactive4javaflow.functionals.*;
import hu.akarnokd.reactive4javaflow.impl.EmptySubscription;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

public abstract class ParallelFolyam<T> {

    public abstract int parallelism();

    public final void subscribe(FolyamSubscriber<? super T>[] subscribers) {
        if (validate(subscribers)) {
            applyPlugins(subscribers);
            subscribeActual(subscribers);
        }
    }

    protected abstract void subscribeActual(FolyamSubscriber<? super T>[] subscribers);

    final boolean validate(FolyamSubscriber<? super T>[] subscribers) {
        int p = parallelism();
        if (p != subscribers.length) {
            IllegalArgumentException ex = new IllegalArgumentException("Subscribers expected: " + p + ", actual: " + subscribers.length);
            for (FolyamSubscriber<? super T> s : subscribers) {
                EmptySubscription.error(s, ex);
            }
            return false;
        }
        return true;
    }

    final void applyPlugins(FolyamSubscriber<? super T>[] subscribers) {
        for (int i = 0; i < subscribers.length; i++) {
            subscribers[i] = Objects.requireNonNull(
                    FolyamPlugins.onSubscribe(this, subscribers[i]),
                    "The plugin onSubscribe handler returned a null value"
                    );
        }
    }

    // ------------------------------------------------------------------------
    // General source operators
    // ------------------------------------------------------------------------

    public static <T> ParallelFolyam<T> fromPublisher(Flow.Publisher<? extends T> source) {
        return fromPublisher(source, FolyamPlugins.defaultBufferSize());
    }

    public static <T> ParallelFolyam<T> fromPublisher(Flow.Publisher<? extends T> source, int prefetch) {
        Objects.requireNonNull(source, "source == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @SafeVarargs
    public static <T> ParallelFolyam<T> fromArray(Flow.Publisher<? extends T>... sources) {
        Objects.requireNonNull(sources, "sources == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public static <T> ParallelFolyam<T> defer(Callable<? extends ParallelFolyam<? extends T>> callable, int parallelism) {
        Objects.requireNonNull(callable, "callable == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    // ------------------------------------------------------------------------
    // Instance operators
    // ------------------------------------------------------------------------

    public final <R> ParallelFolyam<R> map(CheckedFunction<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper, "mapper == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public final <R> ParallelFolyam<R> map(CheckedFunction<? super T, ? extends R> mapper, ParallelFailureHandling failureHandling) {
        Objects.requireNonNull(mapper, "mapper == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public final <R> ParallelFolyam<R> map(CheckedFunction<? super T, ? extends R> mapper, CheckedBiFunction<? super Long, ? super Throwable, ? extends ParallelFailureHandling> failureHandling) {
        Objects.requireNonNull(mapper, "mapper == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public final <R> ParallelFolyam<R> mapOptional(CheckedFunction<? super T, ? extends Optional<R>> mapper) {
        Objects.requireNonNull(mapper, "mapper == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public final <R> ParallelFolyam<R> mapOptional(CheckedFunction<? super T, ? extends Optional<R>> mapper, ParallelFailureHandling failureHandling) {
        Objects.requireNonNull(mapper, "mapper == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public final <R> ParallelFolyam<R> mapWhen(CheckedFunction<? super T, ? extends Flow.Publisher<? extends R>> mapper) {
        return mapWhen(mapper, FolyamPlugins.defaultBufferSize());
    }

    public final <R> ParallelFolyam<R> mapWhen(CheckedFunction<? super T, ? extends Flow.Publisher<? extends R>> mapper, int prefetch) {
        Objects.requireNonNull(mapper, "mapper == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public final <U, R> ParallelFolyam<R> mapWhen(CheckedFunction<? super T, ? extends Flow.Publisher<? extends U>> mapper, CheckedBiFunction<? super T, ? super U, ? extends R> combiner) {
        return mapWhen(mapper, combiner, FolyamPlugins.defaultBufferSize());
    }

    public final <U, R> ParallelFolyam<R> mapWhen(CheckedFunction<? super T, ? extends Flow.Publisher<? extends U>> mapper, CheckedBiFunction<? super T, ? super U, ? extends R> combiner, int prefetch) {
        Objects.requireNonNull(mapper, "mapper == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public final ParallelFolyam<T> filter(CheckedPredicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public final ParallelFolyam<T> filter(CheckedPredicate<? super T> predicate, ParallelFailureHandling failureHandling) {
        Objects.requireNonNull(predicate, "predicate == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public final ParallelFolyam<T> filter(CheckedPredicate<? super T> predicate, CheckedBiFunction<? super Long, ? super Throwable, ? extends ParallelFailureHandling> failureHandling) {
        Objects.requireNonNull(predicate, "predicate == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public final ParallelFolyam<T> filterWhen(CheckedFunction<? super T, ? extends Flow.Publisher<Boolean>> predicate) {
        return filterWhen(predicate, FolyamPlugins.defaultBufferSize());
    }

    public final ParallelFolyam<T> filterWhen(CheckedFunction<? super T, ? extends Flow.Publisher<Boolean>> predicate, int prefetch) {
        Objects.requireNonNull(predicate, "predicate == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public final <R> R to(Function<? super ParallelFolyam<T>, ? extends R> converter) {
        return converter.apply(this);
    }

    public final <R> ParallelFolyam<R> compose(Function<? super ParallelFolyam<T>, ? extends ParallelFolyam<R>> composer) {
        return to(composer);
    }

    // state peeking operators

    public final ParallelFolyam<T> doOnNext(CheckedConsumer<? super T> handler) {
        Objects.requireNonNull(handler, "handler == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public final ParallelFolyam<T> doOnNext(CheckedConsumer<? super T> handler, ParallelFailureHandling failureHandling) {
        Objects.requireNonNull(handler, "handler == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public final ParallelFolyam<T> doOnNext(CheckedConsumer<? super T> handler, CheckedBiFunction<? super Long, ? super Throwable, ? extends ParallelFailureHandling> failureHandling) {
        Objects.requireNonNull(handler, "handler == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public final ParallelFolyam<T> doAfterNext(CheckedConsumer<? super T> handler) {
        Objects.requireNonNull(handler, "handler == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public final ParallelFolyam<T> doOnError(CheckedConsumer<? super Throwable> handler) {
        Objects.requireNonNull(handler, "handler == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public final ParallelFolyam<T> doOnComplete(CheckedRunnable handler) {
        Objects.requireNonNull(handler, "handler == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public final ParallelFolyam<T> doFinally(CheckedRunnable handler) {
        Objects.requireNonNull(handler, "handler == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    // // mappers of inner flows

    public final <R> ParallelFolyam<R> concatMap(CheckedFunction<? super T, ? extends Flow.Publisher<? extends R>> mapper) {
        return concatMap(mapper, FolyamPlugins.defaultBufferSize());
    }

    public final <R> ParallelFolyam<R> concatMap(CheckedFunction<? super T, ? extends Flow.Publisher<? extends R>> mapper, int prefetch) {
        Objects.requireNonNull(mapper, "mapper == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public final <R> ParallelFolyam<R> concatMapDelayError(CheckedFunction<? super T, ? extends Flow.Publisher<? extends R>> mapper) {
        return concatMapDelayError(mapper, FolyamPlugins.defaultBufferSize());
    }

    public final <R> ParallelFolyam<R> concatMapDelayError(CheckedFunction<? super T, ? extends Flow.Publisher<? extends R>> mapper, int prefetch) {
        Objects.requireNonNull(mapper, "mapper == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public final <R> ParallelFolyam<R> flatMap(CheckedFunction<? super T, ? extends Flow.Publisher<? extends R>> mapper) {
        return flatMap(mapper, FolyamPlugins.defaultBufferSize(), FolyamPlugins.defaultBufferSize());
    }

    public final <R> ParallelFolyam<R> flatMap(CheckedFunction<? super T, ? extends Flow.Publisher<? extends R>> mapper, int maxConcurrency) {
        return flatMap(mapper, maxConcurrency, FolyamPlugins.defaultBufferSize());
    }

    public final <R> ParallelFolyam<R> flatMap(CheckedFunction<? super T, ? extends Flow.Publisher<? extends R>> mapper, int maxConcurrency, int prefetch) {
        Objects.requireNonNull(mapper, "mapper == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public final <R> ParallelFolyam<R> flatMapDelayError(CheckedFunction<? super T, ? extends Flow.Publisher<? extends R>> mapper) {
        return flatMapDelayError(mapper, FolyamPlugins.defaultBufferSize(), FolyamPlugins.defaultBufferSize());
    }

    public final <R> ParallelFolyam<R> flatMapDelayError(CheckedFunction<? super T, ? extends Flow.Publisher<? extends R>> mapper, int maxConcurrency) {
        return flatMapDelayError(mapper, maxConcurrency, FolyamPlugins.defaultBufferSize());
    }

    public final <R> ParallelFolyam<R> flatMapDelayError(CheckedFunction<? super T, ? extends Flow.Publisher<? extends R>> mapper, int maxConcurrency, int prefetch) {
        Objects.requireNonNull(mapper, "mapper == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    // rail-wise reductions

    public final <R> ParallelFolyam<R> reduce(Callable<? extends R> initialSupplier, CheckedBiFunction<R, ? super T, R> reducer) {
        Objects.requireNonNull(initialSupplier, "initialSupplier == null");
        Objects.requireNonNull(reducer, "reducer == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public final <R> ParallelFolyam<R> collect(Callable<? extends R> initialSupplier, CheckedBiConsumer<R, ? super T> collector) {
        Objects.requireNonNull(initialSupplier, "initialSupplier == null");
        Objects.requireNonNull(collector, "collector == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    // ------------------------------------------------------------------------
    // Converters back to Folyam/Esetleg
    // ------------------------------------------------------------------------

    public final Folyam<T> sequential() {
        return sequential(FolyamPlugins.defaultBufferSize());
    }

    public final Folyam<T> sequential(int prefetch) {
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public final Folyam<T> sequential(SchedulerService executor) {
        return sequential(executor, FolyamPlugins.defaultBufferSize());
    }

    public final Folyam<T> sequential(SchedulerService executor, int prefetch) {
        Objects.requireNonNull(executor, "executor == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public final Esetleg<T> ignoreElements() {
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public final Esetleg<T> reduce(CheckedBiFunction<T, T, T> reducer) {
        Objects.requireNonNull(reducer, "reducer == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public final Folyam<T> sorted(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    // aggregators

    public final Esetleg<T> min(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public final <K> Esetleg<T> min(CheckedFunction<? super T, ? extends K> keySelector, Comparator<? super K> comparator) {
        Objects.requireNonNull(keySelector, "keySelector == null");
        Objects.requireNonNull(comparator, "comparator == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public final Esetleg<T> max(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public final <K> Esetleg<T> max(CheckedFunction<? super T, ? extends K> keySelector, Comparator<? super K> comparator) {
        Objects.requireNonNull(keySelector, "keySelector == null");
        Objects.requireNonNull(comparator, "comparator == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public final Esetleg<Integer> sumInt(CheckedFunction<? super T, ? extends Number> valueSelector) {
        Objects.requireNonNull(valueSelector, "valueSelector == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public final Esetleg<Long> sumLong(CheckedFunction<? super T, ? extends Number> valueSelector) {
        Objects.requireNonNull(valueSelector, "valueSelector == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public final Esetleg<Float> sumFloat(CheckedFunction<? super T, ? extends Number> valueSelector) {
        Objects.requireNonNull(valueSelector, "valueSelector == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public final Esetleg<Double> sumDouble(CheckedFunction<? super T, ? extends Number> valueSelector) {
        Objects.requireNonNull(valueSelector, "valueSelector == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }
}