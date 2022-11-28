/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.tests;

import com.clarisma.common.util.Log;
import com.geodesk.benchmark.SimpleBenchmark;
import com.geodesk.core.Box;
import com.geodesk.core.Tile;
import com.geodesk.feature.Feature;
import com.geodesk.feature.FeatureLibrary;
import com.geodesk.feature.Filter;
import com.geodesk.feature.Filters;
import com.geodesk.feature.store.TileIndexWalker;
import com.geodesk.feature.store.Tip;
import com.geodesk.util.MapMaker;
import com.geodesk.util.Marker;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class FastFilterTest
{
    FeatureLibrary world;

    @Before public void setUp()
    {
        world = new FeatureLibrary(TestSettings.golFile());
    }

    @After public void tearDown()
    {
        world.close();
    }

    @Test public void testTileWalker() throws Exception
    {
        MapMaker map = new MapMaker();
        Feature bavaria = world
            // .select("a[boundary=administrative][admin_level=4][name:en=Bavaria]")
            .select("a[boundary=administrative][admin_level=2][name:en=Germany]")
            // .select("a[boundary=administrative][admin_level=6][name='Landkreis Erding']")
            .in(Box.atLonLat(12.0231, 48.3310))
            .first();
        Geometry bavariaPoly = bavaria.toGeometry();
        map.add(bavariaPoly).color("red");

        int tileCount = 0;
        TileIndexWalker walker = new TileIndexWalker(world.store());
        Filter filter = Filters.intersects(bavariaPoly);
        walker.start(bavaria.bounds(), filter);
        while (walker.next())
        {
            tileCount++;
            Marker marker = map.add(Tile.polygon(walker.tile()))
                .tooltip(Tile.toString(walker.tile()) + "<br>" +
                    Tip.toString(walker.tip()));
            if(walker.filter() != filter) marker.color("green");
        }

        Log.debug("%d tiles in query", tileCount);
        map.save("c:\\geodesk\\fast-intersects-germany-de-from-world.html");
    }

    @Test public void testIntersectsQueryPerformance() throws Exception
    {
        Feature bavaria = world
            .select("a[boundary=administrative][admin_level=4][name:en=Bavaria]")
            // .select("a[boundary=administrative][admin_level=2][name:en=Germany]")
            // .select("a[boundary=administrative][admin_level=6][name='Landkreis Erding']")
            .in(Box.atLonLat(12.0231, 48.3310))
            .first();
        Geometry bavariaPoly = bavaria.toGeometry();

        /*
        final int runs = 10;
        long count = 0;
        long start = System.currentTimeMillis();
        for(int i=0; i<runs; i++)
        {
            count = world
                // .select("w[highway]")
                // .select("n[place=city]")
                .select("a[building]")
                .select(Filters.intersects(bavariaPoly))
                .count();
        }
        long end = System.currentTimeMillis();

        Log.debug("Found %,d features each in %d runs, %d ms total", count, runs, end-start);
         */

        final AtomicLong count = new AtomicLong();
        SimpleBenchmark.run("fast-intersects", 10, () ->
        {
            count.set(world
                // .select("w[highway]")
                // .select("n[place=city]")
                .select("a[building]")
                .select(Filters.intersects(bavariaPoly))
                .count());
        });
        Log.debug("Found %,d features.", count.get());
    }

    @Test public void testIntersectsQuery() throws Exception
    {
        Feature bavaria = world
            .select("a[boundary=administrative][admin_level=4][name:en=Bavaria]")
            // .select("a[boundary=administrative][admin_level=2][name:en=Germany]")
            .in(Box.atLonLat(12.0231, 48.3310))
            .first();
        PreparedGeometry bavariaPrepared = PreparedGeometryFactory.prepare(bavaria.toGeometry());

        long count = 0;
        for(Feature f: world.select("w[highway]").select(
            Filters.intersects(bavariaPrepared)))
        {
            if(!bavariaPrepared.intersects(f.toGeometry()))
            {
                Assert.fail(String.format("%s does not intersect", f));
            }
            count++;
        }
        Log.debug("Found %,d features", count);
    }

    @Test public void testWithinQueryPerformance() throws Exception
    {
        Feature bavaria = world
            .select("a[boundary=administrative][admin_level=4][name:en=Bavaria]")
            // .select("a[boundary=administrative][admin_level=2][name:en=Germany]")
            // .select("a[boundary=administrative][admin_level=6][name='Landkreis Erding']")
            .in(Box.atLonLat(12.0231, 48.3310))
            .first();
        Geometry bavariaPoly = bavaria.toGeometry();

        /*
        final int runs = 10;
        long count = 0;
        long start = System.currentTimeMillis();
        for(int i=0; i<runs; i++)
        {
            count = world
                .select("a[building]")
                // .select("w[highway]")
                // .select("n[place=city]")
                .select(Filters.within(bavariaPoly))
                .count();
        }
        long end = System.currentTimeMillis();

        Log.debug("Found %,d features each in %d runs, %d ms total", count, runs, end-start);
         */

        final AtomicLong count = new AtomicLong();
        SimpleBenchmark.run("fast-within", 10, () ->
        {
            count.set(world
                // .select("w[highway]")
                // .select("n[place=city]")
                .select("a[building]")
                .select(Filters.within(bavariaPoly))
                .count());
        });
        Log.debug("Found %,d features.", count.get());
    }

    @Test public void testTileWalkerCrosses() throws Exception
    {
        MapMaker map = new MapMaker();
        Feature rhine = world
            .select("r[waterway=river][name:en=Rhine]")
            .first();
        Geometry rhineGeom = rhine.toGeometry();
        map.add(rhineGeom).color("red");

        int tileCount = 0;
        TileIndexWalker walker = new TileIndexWalker(world.store());
        Filter filter = Filters.crosses(rhineGeom);
        walker.start(rhine.bounds(), filter);
        while (walker.next())
        {
            tileCount++;
            Marker marker = map.add(Tile.polygon(walker.tile()))
                .tooltip(Tile.toString(walker.tile()));
            if(walker.filter() != filter) marker.color("green");
        }

        Log.debug("%d tiles in query", tileCount);
        map.save("c:\\geodesk\\fast-crosses-rhine.html");
    }

    @Test public void testCrossesQueryPerformance() throws Exception
    {
        Feature rhine = world
            .select("r[waterway=river][name:en=Rhine]")
            .first();

        final int runs = 3;
        long count = 0;
        long start = System.currentTimeMillis();
        for(int i=0; i<runs; i++)
        {
            count = world.select("w[highway][bridge]").select(
                Filters.crosses(rhine)).count();
        }
        long end = System.currentTimeMillis();

        Log.debug("Found %,d bridges across Rhine, each in %d runs, %d ms total", count, runs, end-start);
    }

    @Test public void testTouchesQuery() throws Exception
    {
        Feature bavaria = world
            .select("a[boundary=administrative][admin_level=4][name:en=Bavaria]")
            // .select("a[boundary=administrative][admin_level=2][name:en=Germany]")
            .in(Box.atLonLat(12.0231, 48.3310))
            .first();

        long count = 0;
        for(Feature f: world.select("a[boundary=administrative][admin_level=4][name]").select(
            Filters.touches(bavaria)))
        {
            Log.debug("- %s", f.stringValue("name"));
            count++;
        }
        Log.debug("Found %,d features", count);
    }
}
