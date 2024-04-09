/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.benchmark;

import com.geodesk.geom.Box;
import com.geodesk.feature.Features;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class BoxQueryBenchmark extends QueryBenchmark<Box>
{
    public BoxQueryBenchmark(String name, Features features, List<Box> shapes, Action action,
        ExecutorService executor)
    {
        super(name, features, shapes, action, executor);
    }

    @Override void performSingle(Box box, Result res)
    {
        action.perform(features.in(box), res);
    }
}
