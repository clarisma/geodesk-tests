/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.benchmark;

import com.clarisma.common.util.Log;

import java.io.IOException;

public class SimpleBenchmark extends Benchmark
{
    private final Runnable target;

    public SimpleBenchmark(String name, Runnable target)
    {
        super(name);
        this.target = target;
    }

    @Override protected void perform()
    {
        target.run();
    }

    public static void run(String name, int runs, Runnable target)
    {
        Benchmark bm = new SimpleBenchmark(name, target);
        bm.run(runs);
    }
}
