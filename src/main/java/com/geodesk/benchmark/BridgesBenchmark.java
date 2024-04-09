/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.benchmark;

import com.geodesk.geom.Box;
import com.geodesk.feature.Feature;
import com.geodesk.feature.Features;
import com.geodesk.feature.Filters;

import java.io.IOException;

public class BridgesBenchmark extends Benchmark
{
    private final Feature bavaria;
    private final Feature danube;
    private final Features bridges;
    private long count;

    public BridgesBenchmark(Features world)
    {
        super("bridges-across-danube-in-bavaria");
        bavaria = world
            .select("a[boundary=administrative][admin_level=4][name:en=Bavaria]")
            .in(Box.atLonLat(12.0231, 48.3310))
            .first();
        danube = world
            .select("r[waterway=river][name:en=Danube]")
            .first();
        bridges = world.select("w[highway][bridge]");
    }

    @Override protected void perform()
    {
        count = bridges
            .select(Filters.intersects(bavaria))
            .select(Filters.intersects(danube))
            .count();
    }

    @Override protected void reportDetails(Appendable out) throws IOException
    {
        out.append("    features:    ");
        out.append(Long.toString(count));
        out.append("\n");
    }
}
