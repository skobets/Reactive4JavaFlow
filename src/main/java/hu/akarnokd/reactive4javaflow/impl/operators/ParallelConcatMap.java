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

package hu.akarnokd.reactive4javaflow.impl.operators;

import hu.akarnokd.reactive4javaflow.*;
import hu.akarnokd.reactive4javaflow.functionals.CheckedFunction;

import java.util.concurrent.Flow;

/**
 * Concatenates the generated Publishers on each rail.
 *
 * @param <T> the input value type
 * @param <R> the output value type
 */
public final class ParallelConcatMap<T, R> extends ParallelFolyam<R> {

    final ParallelFolyam<T> source;

    final CheckedFunction<? super T, ? extends Flow.Publisher<? extends R>> mapper;

    final int prefetch;

    final boolean delayError;

    public ParallelConcatMap(
            ParallelFolyam<T> source,
            CheckedFunction<? super T, ? extends Flow.Publisher<? extends R>> mapper,
                    int prefetch, boolean delayError) {
        this.source = source;
        this.mapper = mapper;
        this.prefetch = prefetch;
        this.delayError = delayError;
    }

    @Override
    public int parallelism() {
        return source.parallelism();
    }

    @Override
    public void subscribeActual(FolyamSubscriber<? super R>[] subscribers) {
        int n = subscribers.length;

        @SuppressWarnings("unchecked")
        final FolyamSubscriber<T>[] parents = new FolyamSubscriber[n];

        for (int i = 0; i < n; i++) {
            parents[i] = FolyamConcatMap.createSubscribe(subscribers[i], mapper, prefetch, delayError);
        }

        source.subscribe(parents);
    }
}
