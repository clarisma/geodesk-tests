/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.tests;

import com.clarisma.common.util.Log;
import com.geodesk.benchmark.BridgesBenchmark;
import com.geodesk.feature.filter.IntersectsFilter;
import com.geodesk.feature.filter.WithinFilter;
import com.geodesk.geom.Box;
import com.geodesk.geom.Tile;
import com.geodesk.feature.*;
import com.geodesk.feature.filter.AndFilter;
import com.geodesk.feature.store.TileIndexWalker;
import com.geodesk.feature.store.Tip;
import com.geodesk.util.MapMaker;
import com.geodesk.util.Marker;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;

import java.util.HashSet;
import java.util.Set;

public class AndFilterTest
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

    @Test public void testAndFilter() throws Exception
    {
        MapMaker map = new MapMaker();
        Feature bavaria = world
            .select("a[boundary=administrative][admin_level=4][name:en=Bavaria]")
            .in(Box.atLonLat(12.0231, 48.3310))
            .first();
        Feature danube = world
            .select("r[waterway=river][name:en=Danube]")
            .first();

        Features bridges = world.select("w[highway][bridge]");

        map.add(bavaria).color("red");
        map.add(danube).color("blue");

        for (Feature bridge : bridges
            .intersecting(bavaria)
            .intersecting(danube))
        {
            map.add(bridge).color("orange");
        }
        map.save("c:\\geodesk\\bridges-danube-bavaria.html");
    }

    @Test public void testAndFilterTiles() throws Exception
    {
        MapMaker map = new MapMaker();
        Feature bavaria = world
            .select("a[boundary=administrative][admin_level=4][name:en=Bavaria]")
            .in(Box.atLonLat(12.0231, 48.3310))
            .first();
        Feature danube = world
            .select("r[waterway=river][name:en=Danube]")
            .first();

        Features bridges = world.select("w[highway][bridge]");

        map.add(bavaria).color("red");
        map.add(danube).color("blue");

        TileIndexWalker walker = new TileIndexWalker(world.store());
        Filter filter = AndFilter.create(
            new WithinFilter(bavaria),
            new IntersectsFilter(danube));

        map.add(filter.bounds()).color("orange");
        walker.start(filter.bounds(), filter);
        while (walker.next())
        {
            Marker marker = map.add(Tile.polygon(walker.tile()))
                .tooltip(Tile.toString(walker.tile()) + "<br>" +
                    Tip.toString(walker.tip()));
            if (walker.filter() != filter) marker.color("green");
        }
        map.save("c:\\geodesk\\tiles-danube-bavaria.html");
    }

    @Test public void testAndFilterPerformance() throws Exception
    {
        BridgesBenchmark bm = new BridgesBenchmark(world);
        bm.run(10);
    }

    @Test public void testAirports() throws Exception
    {
        MapMaker map = new MapMaker();

        Features runways = world.select("w[aeroway=runway]");
        Features airports = world.select("a[aeroway=aerodrome]");
        final double minLength = 3000;

        Set<Feature> suitableRunways = new HashSet<>();
        Set<Feature> suitableAirports = new HashSet<>();

        for(Feature runway: runways)
        {
            double len = runway.doubleValue("length");
            if(len != 0) Log.debug("Got explicit length: %f", len);
            if(len == 0) len = runway.length();
            if (len >= minLength)
            {
                Feature airport = airports.intersecting(runway).first();
                if (airport == null)
                {
                    Log.debug("Runway %s is not within an airport", runway);
                }
                else
                {
                    suitableAirports.add(airport);
                    suitableRunways.add(runway);
                }
            }
        }
        for(Feature f: suitableAirports) map.add(f).color("orange");
        for(Feature f: suitableRunways) map.add(f).color("red").tooltip(f.tags().toString());
        map.save("c:\\geodesk\\airports.html");
    }

    @Test public void debugAirports() throws Exception
    {
        Feature runway = null;

        MapMaker map = new MapMaker();
        Features runways = world.select("w[aeroway=runway]");
        Features airports = world.select("a[aeroway=aerodrome]");

        for(Feature f: runways)
        {
            if(f.id() == 149993709)
            {
                runway = f;
                break;
            }
        }

        TileIndexWalker walker = new TileIndexWalker(world.store());
        Filter filter = new IntersectsFilter(runway);
        walker.start(filter.bounds(), filter);
        map.add(filter.bounds()).color("orange");
        while (walker.next())
        {
            Marker marker = map.add(Tile.polygon(walker.tile()))
                .tooltip(Tile.toString(walker.tile()) + "<br>" +
                    Tip.toString(walker.tip()));
            if (walker.filter() != filter) marker.color("green");
        }

        Feature airport = airports.in(filter.bounds()).first();
        Assert.assertNotNull(airport);
        Assert.assertTrue(runway.toGeometry().intersects(airport.toGeometry()));
        PreparedGeometry runwayPrepared = PreparedGeometryFactory.prepare(runway.toGeometry());
        Assert.assertTrue(runwayPrepared.intersects(airport.toGeometry()));

        airport = airports.select(filter).first();
        Assert.assertNotNull(airport);

        map.save("c:\\geodesk\\hamburg-airport.html");
    }
}