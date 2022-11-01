/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.experiments;

import com.geodesk.core.Mercator;
import com.geodesk.core.XY;
import com.geodesk.feature.FeatureLibrary;
import com.geodesk.feature.Features;
import com.geodesk.feature.Way;
import com.geodesk.feature.store.StoredWay;
import org.locationtech.jts.geom.LineSegment;

import static java.lang.System.out;

public class SegmentExperiment
{
    static FeatureLibrary world;

    public static void measureImmutable(Iterable<Way> ways)
    {
        long start = System.currentTimeMillis();
        double totalDistance = 0;

        for(Way way: ways)
        {
            StoredWay w = (StoredWay)way;
            StoredWay.XYIterator iter = w.iterXY();
            long prevXY = iter.nextXY();
            while(iter.hasNext())
            {
                long xy = iter.nextXY();
                LineSegment seg = new LineSegment(
                    XY.x(prevXY), XY.y(prevXY),
                    XY.x(xy), XY.y(xy));
                totalDistance += Mercator.distance(seg.p0, seg.p1);
                prevXY = xy;
            }
        }
        out.format("Immutable: Total distance %f calculated in %d ms\n",
            totalDistance, System.currentTimeMillis() - start);
    }

    public static void measureMutable(Iterable<Way> ways)
    {
        long start = System.currentTimeMillis();
        double totalDistance = 0;

        LineSegment seg = new LineSegment();
        for(Way way: ways)
        {
            StoredWay w = (StoredWay)way;
            StoredWay.XYIterator iter = w.iterXY();
            long prevXY = iter.nextXY();
            while(iter.hasNext())
            {
                long xy = iter.nextXY();
                seg.p0.x = XY.x(prevXY);
                seg.p0.y = XY.y(prevXY);
                seg.p1.x = XY.x(xy);
                seg.p1.y = XY.y(xy);
                totalDistance += Mercator.distance(seg.p0, seg.p1);
                prevXY = xy;
            }
        }
        out.format("  Mutable: Total distance %f calculated in %d ms\n",
            totalDistance, System.currentTimeMillis() - start);
    }

    public static void main(String[] args)
    {
        world = new FeatureLibrary("c:\\geodesk\\tests\\de.gol");
        Iterable<Way> ways = world.ways("a[building]").toList();
        measureImmutable(ways);
        measureMutable(ways);
        measureImmutable(ways);
        measureMutable(ways);
        measureImmutable(ways);
        measureMutable(ways);
        measureImmutable(ways);
        measureMutable(ways);
        measureImmutable(ways);
        measureMutable(ways);
        measureImmutable(ways);
        measureMutable(ways);
        world.close();
    }
}
