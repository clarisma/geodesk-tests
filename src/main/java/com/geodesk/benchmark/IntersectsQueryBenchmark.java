/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.benchmark;

import com.geodesk.feature.Features;

import org.locationtech.jts.geom.Geometry;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class IntersectsQueryBenchmark extends QueryBenchmark<Geometry>
{
    public IntersectsQueryBenchmark(String name, Features features, List<Geometry> shapes,
        Action action, ExecutorService executor)
    {
        super(name, features, shapes, action, executor);
    }

    @Override void performSingle(Geometry geom, Result res)
    {
        action.perform(features.intersecting(geom), res);
    }
}
