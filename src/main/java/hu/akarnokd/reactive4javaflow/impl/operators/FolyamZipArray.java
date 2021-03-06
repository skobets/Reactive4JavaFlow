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
import hu.akarnokd.reactive4javaflow.fused.*;
import hu.akarnokd.reactive4javaflow.impl.*;

import java.lang.invoke.*;
import java.util.*;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicInteger;

public final class FolyamZipArray<T, R> extends Folyam<R> {

    final Flow.Publisher<? extends T>[] sources;

    final CheckedFunction<? super Object[], ? extends R> zipper;

    final int prefetch;

    final boolean delayError;

    public FolyamZipArray(Flow.Publisher<? extends T>[] sources, CheckedFunction<? super Object[], ? extends R> zipper, int prefetch, boolean delayError) {
        this.sources = sources;
        this.zipper = zipper;
        this.prefetch = prefetch;
        this.delayError = delayError;
    }

    @Override
    protected void subscribeActual(FolyamSubscriber<? super R> s) {
        Flow.Publisher<? extends T>[] srcs = this.sources;
        int n = srcs.length;
        subscribe(srcs, n, s, zipper, prefetch, delayError);
    }

    public static <T, R> void subscribe(Flow.Publisher<? extends T>[] srcs, int n, FolyamSubscriber<? super R> actual, CheckedFunction<? super Object[], ? extends R> zipper, int prefetch, boolean delayError) {

        if (n == 0) {
            EmptySubscription.complete(actual);
            return;
        }
        if (actual instanceof ConditionalSubscriber) {
            if (n == 1) {
                srcs[0].subscribe(new FolyamMap.MapConditionalSubscriber<T, R>((ConditionalSubscriber<? super R>) actual, v -> zipper.apply(new Object[] { v })));
            } else {
                ZipArrayConditionalCoordinator<T, R> parent = new ZipArrayConditionalCoordinator<>((ConditionalSubscriber<? super R>) actual, zipper, n, prefetch, delayError);
                actual.onSubscribe(parent);
                parent.subscribe(srcs, n);
            }
        } else {
            if (n == 1) {
                srcs[0].subscribe(new FolyamMap.MapSubscriber<>(actual, v -> zipper.apply(new Object[]{ v })));
            } else {
                ZipArrayCoordinator<T, R> parent = new ZipArrayCoordinator<>(actual, zipper, n, prefetch, delayError);
                actual.onSubscribe(parent);
                parent.subscribe(srcs, n);
            }
        }
    }

    static abstract class AbstractZipCoordinator<T, R> extends AtomicInteger implements Flow.Subscription, QueuedFolyamSubscriberSupport<T> {

        final CheckedFunction<? super Object[], ? extends R> zipper;

        final QueuedInnerFolyamSubscriber<T>[] subscribers;

        final boolean delayError;

        volatile boolean cancelled;

        long requested;
        static final VarHandle REQUESTED = VH.find(MethodHandles.lookup(), AbstractZipCoordinator.class, "requested", Long.TYPE);

        Object[] values;

        Throwable error;
        static final VarHandle ERROR = VH.find(MethodHandles.lookup(), AbstractZipCoordinator.class, "error", Throwable.class);

        long emitted;

        AbstractZipCoordinator(CheckedFunction<? super Object[], ? extends R> zipper, int n, int prefetch, boolean delayError) {
            this.zipper = zipper;
            QueuedInnerFolyamSubscriber<T>[] subs = new QueuedInnerFolyamSubscriber[n];
            for (int i = 0; i < n; i++) {
                subs[i] = new QueuedInnerFolyamSubscriber<>(this, i, prefetch);
            }
            this.values = new Object[n];
            this.subscribers = subs;
            this.delayError = delayError;
        }

        void subscribe(Flow.Publisher<? extends T>[] sources, int n) {
            QueuedInnerFolyamSubscriber<T>[] subs = this.subscribers;

            for (int i = 0; i < n; i++) {
                if (cancelled || (!delayError && ERROR.getAcquire(this) != null)) {
                    return;
                }
                Flow.Publisher<? extends T> p = sources[i];
                if (p == null) {
                    EmptySubscription.error(subs[i], new NullPointerException("Flow.Publisher[" + i + "] == null"));
                    for (int j = i + 1; j < n; j++) {
                        EmptySubscription.complete(subs[i]);
                    }
                    break;
                } else {
                    p.subscribe(subs[i]);
                }
            }
        }

        @Override
        public final void request(long n) {
            SubscriptionHelper.addRequested(this, REQUESTED, n);
            drain();
        }

        @Override
        public final void cancel() {
            cancelled = true;
            QueuedInnerFolyamSubscriber<T>[] subs = this.subscribers;
            cancelSubscribers(subs);
            if (getAndIncrement() == 0) {
                values = null;
                clearSubscribers(subs);
            }
        }

        final void clearSubscribers(QueuedInnerFolyamSubscriber<T>[] subs) {
            for (QueuedInnerFolyamSubscriber<?> inner : subs) {
                inner.clear();
            }
        }

        final void cancelSubscribers(QueuedInnerFolyamSubscriber<T>[] subs) {
            for (QueuedInnerFolyamSubscriber<?> inner : subs) {
                inner.cancel();
            }
        }

        @Override
        public final void drain() {
            if (getAndIncrement() == 0) {
                drainLoop();
            }
        }

        abstract void drainLoop();

        @Override
        public final void innerError(QueuedInnerFolyamSubscriber<T> inner, int index, Throwable ex) {
            if (ExceptionHelper.addThrowable(this, ERROR, ex)) {
                inner.setDone();
                if (!delayError) {
                    QueuedInnerFolyamSubscriber<T>[] subs = this.subscribers;
                    for (int i = 0; i < index; i++) {
                        subs[i].cancel();
                    }
                    for (int i = index + 1; i < subs.length; i++) {
                        subs[i].cancel();
                    }
                }
                drain();
            } else {
                FolyamPlugins.onError(ex);
            }
        }
    }

    static final class ZipArrayCoordinator<T, R> extends AbstractZipCoordinator<T, R> {

        final FolyamSubscriber<? super R> actual;

        ZipArrayCoordinator(FolyamSubscriber<? super R> actual, CheckedFunction<? super Object[], ? extends R> zipper, int n, int prefetch, boolean delayError) {
            super(zipper, n, prefetch, delayError);
            this.actual = actual;
        }

        @Override
        void drainLoop() {
            int missed = 1;
            FolyamSubscriber<? super R> a = actual;
            Object[] vals = values;
            QueuedInnerFolyamSubscriber<T>[] subs = subscribers;
            int n = subs.length;
            long e = emitted;

            for (;;) {

                long r = (long)REQUESTED.getAcquire(this);

                for (;;) {
                    if (cancelled) {
                        values = null;
                        clearSubscribers(subs);
                        return;
                    }

                    if (!delayError) {
                        Throwable ex = (Throwable)ERROR.getAcquire(this);
                        if (ex != null) {
                            ex = ExceptionHelper.terminate(this, ERROR);
                            values = null;
                            clearSubscribers(subs);
                            a.onError(ex);
                            return;
                        }
                    }

                    int hasValue = 0;
                    int complete = 0;

                    for (int i = 0; i < n; i++) {
                        if (vals[i] != null) {
                            hasValue++;
                        } else {
                            QueuedInnerFolyamSubscriber<T> inner = subs[i];
                            boolean d = inner.isDone();
                            FusedQueue<T> q = inner.getQueue();
                            if (q != null) {
                                T v;

                                try {
                                    v = q.poll();
                                } catch (Throwable ex) {
                                    ExceptionHelper.addThrowable(this, ERROR, ex);
                                    if (!delayError) {
                                        ex = ExceptionHelper.terminate(this, ERROR);
                                        values = null;
                                        cancelSubscribers(subs);
                                        clearSubscribers(subs);
                                        a.onError(ex);
                                        return;
                                    }
                                    v = null;
                                    d = true;
                                    inner.setDone();
                                    inner.cancel();
                                    q.clear();
                                }

                                boolean empty = v == null;

                                if (d && empty) {
                                    inner.setPlain(SubscriptionHelper.CANCELLED);
                                    complete++;
                                } else
                                if (!empty) {
                                    vals[i] = v;
                                    hasValue++;
                                }
                            }
                        }
                    }
                    if (complete != 0) {
                        values = null;
                        cancelSubscribers(subs);
                        clearSubscribers(subs);
                        Throwable ex = ExceptionHelper.terminate(this, ERROR);
                        if (ex != null) {
                            a.onError(ex);
                        } else {
                            a.onComplete();
                        }
                        return;
                    }

                    if (e == r || hasValue != n) {
                        break;
                    }

                    R v;

                    try {
                        v = Objects.requireNonNull(zipper.apply(vals.clone()), "The zipper returned a null value");
                    } catch (Throwable ex) {
                        ExceptionHelper.addThrowable(this, ERROR, ex);
                        ex = ExceptionHelper.terminate(this, ERROR);
                        values = null;
                        cancelSubscribers(subs);
                        clearSubscribers(subs);
                        a.onError(ex);
                        return;
                    }

                    a.onNext(v);

                    e++;

                    Arrays.fill(vals, null);
                    for (QueuedInnerFolyamSubscriber<T> inner : subs) {
                        inner.request();
                    }
                }

                emitted = e;
                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }
    }


    static final class ZipArrayConditionalCoordinator<T, R> extends AbstractZipCoordinator<T, R> {

        final ConditionalSubscriber<? super R> actual;

        ZipArrayConditionalCoordinator(ConditionalSubscriber<? super R> actual, CheckedFunction<? super Object[], ? extends R> zipper, int n, int prefetch, boolean delayError) {
            super(zipper, n, prefetch, delayError);
            this.actual = actual;
        }

        @Override
        void drainLoop() {
            int missed = 1;
            ConditionalSubscriber<? super R> a = actual;
            Object[] vals = values;
            QueuedInnerFolyamSubscriber<T>[] subs = subscribers;
            int n = subs.length;
            long e = emitted;

            for (;;) {

                long r = (long)REQUESTED.getAcquire(this);

                for (;;) {
                    if (cancelled) {
                        values = null;
                        clearSubscribers(subs);
                        return;
                    }

                    if (!delayError) {
                        Throwable ex = (Throwable)ERROR.getAcquire(this);
                        if (ex != null) {
                            ex = ExceptionHelper.terminate(this, ERROR);
                            values = null;
                            clearSubscribers(subs);
                            a.onError(ex);
                            return;
                        }
                    }

                    int hasValue = 0;
                    int complete = 0;

                    for (int i = 0; i < n; i++) {
                        if (vals[i] != null) {
                            hasValue++;
                        } else {
                            QueuedInnerFolyamSubscriber<T> inner = subs[i];
                            boolean d = inner.isDone();
                            FusedQueue<T> q = inner.getQueue();
                            if (q != null) {
                                T v;

                                try {
                                    v = q.poll();
                                } catch (Throwable ex) {
                                    ExceptionHelper.addThrowable(this, ERROR, ex);
                                    if (!delayError) {
                                        ex = ExceptionHelper.terminate(this, ERROR);
                                        values = null;
                                        cancelSubscribers(subs);
                                        clearSubscribers(subs);
                                        a.onError(ex);
                                        return;
                                    }
                                    v = null;
                                    d = true;
                                    inner.setDone();
                                    inner.cancel();
                                    q.clear();
                                }

                                boolean empty = v == null;

                                if (d && empty) {
                                    inner.setPlain(SubscriptionHelper.CANCELLED);
                                    complete++;
                                } else
                                if (!empty) {
                                    vals[i] = v;
                                    hasValue++;
                                }
                            }
                        }
                    }
                    if (complete != 0) {
                        values = null;
                        cancelSubscribers(subs);
                        clearSubscribers(subs);
                        Throwable ex = ExceptionHelper.terminate(this, ERROR);
                        if (ex != null) {
                            a.onError(ex);
                        } else {
                            a.onComplete();
                        }
                        return;
                    }

                    if (e == r || hasValue != n) {
                        break;
                    }

                    R v;

                    try {
                        v = Objects.requireNonNull(zipper.apply(vals.clone()), "The zipper returned a null value");
                    } catch (Throwable ex) {
                        ExceptionHelper.addThrowable(this, ERROR, ex);
                        ex = ExceptionHelper.terminate(this, ERROR);
                        values = null;
                        cancelSubscribers(subs);
                        clearSubscribers(subs);
                        a.onError(ex);
                        return;
                    }

                    if (a.tryOnNext(v)) {
                        e++;
                    }

                    Arrays.fill(vals, null);
                    for (QueuedInnerFolyamSubscriber<T> inner : subs) {
                        inner.request();
                    }
                }

                emitted = e;
                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }
    }
}
