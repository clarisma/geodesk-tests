/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.tests;

import com.geodesk.geom.Box;
import com.geodesk.geom.Tile;
import com.geodesk.feature.*;
import com.geodesk.feature.store.TileIndexWalker;
import com.geodesk.feature.store.Tip;
import com.geodesk.util.MapMaker;
import com.geodesk.util.Marker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DisjointTest
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

    @Test public void testDisjoint() throws Exception
    {
        MapMaker map = new MapMaker();
        Feature bavaria = world
            .select("a[boundary=administrative][admin_level=4][name:en=Bavaria]")
            .in(Box.atLonLat(12.0231, 48.3310))
            .first();
        Features rivers = world.select("r[waterway=river]");

        map.add(bavaria).color("red");

        Features riversOutsideBavaria = rivers
            .select(Filters.disjoint(bavaria));

        TestUtils.checkNoDupes("rivers-outside-bavaria", TestUtils.getSet(riversOutsideBavaria));

        for (Feature river : riversOutsideBavaria)
        {
            map.add(river).color("blue");
        }
        map.save("c:\\geodesk\\disjoint-bavaria.html");
    }

    @Test public void debugMissingRiver() throws Exception
    {
        MapMaker map = new MapMaker();
        Feature bavaria = world
            .select("a[boundary=administrative][admin_level=4][name:en=Bavaria]")
            .in(Box.atLonLat(12.0231, 48.3310))
            .first();
        Features rivers = world.select("r[waterway=river][name=Theel]");

        map.add(bavaria).color("red");

        for (Feature river : rivers
            .select(Filters.disjoint(bavaria)))
        {
            map.add(river).color("blue");
        }
        map.save("c:\\geodesk\\river-theel.html");
    }

    @Test public void testDisjointFilterTiles() throws Exception
    {
        MapMaker map = new MapMaker();
        Feature bavaria = world
            .select("a[boundary=administrative][admin_level=4][name:en=Bavaria]")
            .in(Box.atLonLat(12.0231, 48.3310))
            .first();

        map.add(bavaria).color("red");

        TileIndexWalker walker = new TileIndexWalker(world.store());
        Filter filter = Filters.disjoint(bavaria);

        map.add(filter.bounds()).color("orange");
        walker.start(Box.ofWorld(), filter);
        while (walker.next())
        {
            Marker marker = map.add(Tile.polygon(walker.tile()))
                .tooltip(Tile.toString(walker.tile()) + "<br>" +
                    Tip.toString(walker.tip()));
            if (walker.filter() != filter) marker.color("green");
        }
        map.save("c:\\geodesk\\tiles-disjoint-bavaria.html");
    }

}
