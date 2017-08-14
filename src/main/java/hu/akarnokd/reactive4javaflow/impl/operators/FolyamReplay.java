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
import hu.akarnokd.reactive4javaflow.functionals.*;
import hu.akarnokd.reactive4javaflow.fused.ConditionalSubscriber;
import hu.akarnokd.reactive4javaflow.hot.*;
import hu.akarnokd.reactive4javaflow.impl.EmptySubscription;

import java.util.Objects;
import java.util.concurrent.Flow;

public final class FolyamReplay<T, R> extends Folyam<R> {

    final Folyam<T> source;

    final CheckedFunction<? super Folyam<T>, ? extends Flow.Publisher<? extends R>> handler;

    final int capacityHint;

    public FolyamReplay(Folyam<T> source, CheckedFunction<? super Folyam<T>, ? extends Flow.Publisher<? extends R>> handler, int capacityHint) {
        this.source = source;
        this.handler = handler;
        this.capacityHint = capacityHint;
    }

    @Override
    protected void subscribeActual(FolyamSubscriber<? super R> s) {
        CachingProcessor<T> mp = CachingProcessor.withCapacityHint(capacityHint);
        Flow.Publisher<? extends R> p;
        try {
            p = Objects.requireNonNull(handler.apply(mp), "The handler returned a null Flow.Publisher");
        } catch (Throwable ex) {
            EmptySubscription.error(s, ex);
            return;
        }

        if (s instanceof ConditionalSubscriber) {
            p.subscribe(new FolyamPublish.PublishConditionalSubscriber<>((ConditionalSubscriber<? super R>)s, mp));
        } else {
            p.subscribe(new FolyamPublish.PublishSubscriber<>(s, mp));
        }

        source.subscribe(mp);
    }
}
