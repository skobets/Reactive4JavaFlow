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
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class FolyamRepeatWhenTest {

    @Test
    public void standard() {
        TestHelper.assertResult(
                Folyam.range(1, 5)
                .repeatWhen(h -> h.takeWhile(v -> false)),
                1, 2, 3, 4, 5
        );
    }


    @Test
    public void standardHidden() {
        TestHelper.assertResult(
                Folyam.range(1, 5).hide()
                        .repeatWhen(h -> h.takeWhile(v -> false)),
                1, 2, 3, 4, 5
        );
    }

    @Test
    public void standard2() {
        TestHelper.assertResult(
                Folyam.defer(() -> {
                    int[] c = { 0 };
                    return Folyam.range(1, 5)
                            .repeatWhen(h -> h.takeWhile(v -> c[0]++ < 2));
                }),
                1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 1, 2, 3, 4, 5
        );
    }

    @Test
    public void error() {
        TestHelper.assertFailureComposed(-1,
                f -> f.repeatWhen(h -> h), IOException.class);
    }

    @Test
    public void handlerCrash() {
        TestHelper.assertFailureComposed(0,
                f -> f.repeatWhen(v -> { throw new IOException(); })
                , IOException.class);
    }

    @Test
    public void immediateError() {
        TestHelper.assertFailureComposed(5,
                f -> f.repeatWhen(v -> Folyam.error(new IOException())), IOException.class);
    }

    @Test
    public void immediateComplete() {
        TestHelper.assertResult(
                Folyam.range(1, 5)
                        .repeatWhen(h -> Folyam.empty())
        );
    }

    @Test
    public void unrelated() {
        Folyam.range(1, 5)
                .repeatWhen(h -> Folyam.range(1, 2))
                .test()
                .assertResult(1, 2, 3, 4, 5, 1, 2, 3, 4, 5);
    }


    @Test
    public void unrelatedConditional() {
        Folyam.range(1, 5)
                .repeatWhen(h -> Folyam.range(1, 2))
                .filter(v -> true)
                .test()
                .assertResult(1, 2, 3, 4, 5, 1, 2, 3, 4, 5);
    }

    @Test
    public void repeatAsyncSource() {
        Folyam.range(1, 5)
                .subscribeOn(SchedulerServices.single())
                .repeatWhen(h -> {
                    int[] c = { 0 };
                    return h.takeWhile(v -> c[0]++ < 100);
                })
                .test()
                .awaitDone(5, TimeUnit.SECONDS)
                .assertValueCount(505)
                .assertNoErrors()
                .assertComplete();
    }
}
