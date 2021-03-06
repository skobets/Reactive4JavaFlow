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

public class FolyamDoFinallyTest {

    @Test
    public void standard() {
        TestHelper.assertResult(
                Folyam.range(1, 5).doFinally(() -> { }),
                1, 2, 3, 4, 5);
    }

    @Test
    public void standard2() {
        TestHelper.assertResult(
                Folyam.range(1, 5).hide().doFinally(() -> { }),
                1, 2, 3, 4, 5);
    }

    @Test
    public void standardConditional() {
        TestHelper.assertResult(
                Folyam.range(1, 5).doFinally(() -> { })
                .filter(v -> true),
                1, 2, 3, 4, 5);
    }

    @Test
    public void standardConditional2() {
        TestHelper.assertResult(
                Folyam.range(1, 5).hide().doFinally(() -> { })
                .filter(v -> true),
                1, 2, 3, 4, 5);
    }

    @Test
    public void error() {
        int[] value = { 0 };
        Folyam.error(new IOException())
                .doFinally(() -> ++value[0])
                .test()
                .assertFailure(IOException.class);
    }

    @Test
    public void errorConditional() {
        int[] value = { 0 };
        Folyam.error(new IOException())
                .doFinally(() -> ++value[0])
                .filter(v -> true)
                .test()
                .assertFailure(IOException.class);
    }

    @Test
    public void finallyFails() {
        TestHelper.withErrorTracking(errors -> {
            Folyam.just(1)
                    .doFinally(() -> { throw new IOException(); })
                    .test()
                    .assertResult(1);

            TestHelper.assertError(errors, 0, IOException.class);
        });
    }

}
