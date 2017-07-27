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
import hu.akarnokd.reactive4javaflow.impl.FunctionalHelper;
import hu.akarnokd.reactive4javaflow.impl.StrictSubscriber;
import hu.akarnokd.reactive4javaflow.impl.consumers.LambdaSubscriber;
import hu.akarnokd.reactive4javaflow.impl.operators.FolyamJust;

import java.util.Objects;
import java.util.concurrent.Flow;
import java.util.function.Function;

public abstract class Esetleg<T> implements Flow.Publisher<T> {

    @Override
    public final void subscribe(Flow.Subscriber<? super T> s) {
        Objects.requireNonNull(s, "s == null");
        if (s instanceof FolyamSubscriber) {
            subscribe((FolyamSubscriber<? super T>)s);
        } else {
            subscribe(new StrictSubscriber<T>(s));
        }
    }

    public final void subscribe(FolyamSubscriber<? super T> s) {
        s = Objects.requireNonNull(FolyamPlugins.onSubscribe(this, s), "The plugin returned a null value");
        try {
            subscribeActual(s);
        } catch (Throwable ex) {
            FolyamPlugins.onError(ex);
        }
    }

    protected abstract void subscribeActual(FolyamSubscriber<? super T> s);

    public final <R> R to(Function<? super Esetleg<T>, R> converter) {
        return converter.apply(this);
    }

    public final <R> Esetleg<R> compose(Function<? super Esetleg<T>, ? extends Esetleg<R>> composer) {
        return to(composer);
    }

    public final AutoDisposable subscribe(CheckedConsumer<? super T> onNext, CheckedConsumer<? super Throwable> onError, CheckedRunnable onComplete) {
        LambdaSubscriber<T> consumer = new LambdaSubscriber<>(onNext, onError, onComplete, FunctionalHelper.REQUEST_UNBOUNDED);
        subscribe(consumer);
        return consumer;
    }

    public final TestConsumer<T> test() {
        TestConsumer<T> tc = new TestConsumer<>();
        subscribe(tc);
        return tc;
    }

    public final TestConsumer<T> test(long initialRequest) {
        TestConsumer<T> tc = new TestConsumer<>(initialRequest);
        subscribe(tc);
        return tc;
    }

    public final TestConsumer<T> test(long initialRequest, boolean cancelled, int fusionMode) {
        TestConsumer<T> tc = new TestConsumer<>(initialRequest);
        if (cancelled) {
            tc.close();
        }
        tc.requestFusionMode(fusionMode);
        subscribe(tc);
        return tc;
    }

    public final <E extends Flow.Subscriber<? super T>> E subscribeWith(E s) {
        subscribe(s);
        return s;
    }

    public final Folyam<T> just(T item) {
        Objects.requireNonNull(item, "item == null");
        return FolyamPlugins.onAssembly(new FolyamJust<>(item));
    }

    public static <T> Esetleg<Boolean> sequenceEqual(Flow.Publisher<? extends T> first, Flow.Publisher<? extends T> second) {
        return sequenceEqual(first, second, Objects::equals);
    }

    public static <T> Esetleg<Boolean> sequenceEqual(Flow.Publisher<? extends T> first, Flow.Publisher<? extends T> second, CheckedBiPredicate<? super T, ? super T> isEqual) {
        Objects.requireNonNull(first, "first == null");
        Objects.requireNonNull(second, "second == null");
        Objects.requireNonNull(isEqual, "isEqual == null");
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet!");
    }
}
