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
package hu.akarnokd.reactive4javaflow.impl;

import hu.akarnokd.reactive4javaflow.FolyamSubscriber;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.Flow;

public final class StrictSubscriber<T> implements FolyamSubscriber<T>, Flow.Subscription {

    final Flow.Subscriber<? super T> actual;

    Flow.Subscription upstream;
    static final VarHandle UPSTREAM;

    long requested;
    static final VarHandle REQUESTED;

    Throwable error;
    static final VarHandle ERROR;

    int wip;
    static final VarHandle WIP;

    static {
        try {
            UPSTREAM = MethodHandles.lookup().findVarHandle(StrictSubscriber.class, "upstream", Flow.Subscription.class);
            REQUESTED = MethodHandles.lookup().findVarHandle(StrictSubscriber.class, "requested", Long.TYPE);
            ERROR = MethodHandles.lookup().findVarHandle(StrictSubscriber.class, "error", Throwable.class);
            WIP = MethodHandles.lookup().findVarHandle(StrictSubscriber.class, "wip", Integer.TYPE);
        } catch (Throwable ex) {
            throw new InternalError(ex);
        }
    }

    public StrictSubscriber(Flow.Subscriber<? super T> actual) {
        this.actual = actual;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        actual.onSubscribe(this);
        SubscriptionHelper.deferredReplace(this, UPSTREAM, REQUESTED, subscription);
    }

    @Override
    public void onNext(T item) {
        HalfSerializer.onNext(actual, this, WIP, ERROR, item);
    }

    @Override
    public void onError(Throwable throwable) {
        HalfSerializer.onError(actual, this, WIP, ERROR, throwable);
    }

    @Override
    public void onComplete() {
        HalfSerializer.onComplete(actual, this, WIP, ERROR);
    }

    @Override
    public void request(long n) {
        if (n <= 0L) {
            cancel();
            onError(new IllegalArgumentException("§3.9 violated: positive request amount required"));
        } else {
            SubscriptionHelper.deferredRequest(this, UPSTREAM, REQUESTED, n);
        }
    }

    @Override
    public void cancel() {
        SubscriptionHelper.cancel(this, UPSTREAM);
    }
}
