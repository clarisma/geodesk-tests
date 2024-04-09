/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.benchmark;

import com.geodesk.feature.Features;

import java.util.List;
import java.util.concurrent.ExecutorService;

import static com.geodesk.feature.Filters.*;

public class EnclosingQueryBenchmark extends QueryBenchmark<Circle>
{
    public EnclosingQueryBenchmark(String name, Features features, List<Circle> shapes,
        Action action, ExecutorService executor)
    {
        super(name, features, shapes, action, executor);
    }

    @Override void performSingle(Circle circle, Result res)
    {
        action.perform(features.select(containsXY(circle.x, circle.y)), res);
    }
}
