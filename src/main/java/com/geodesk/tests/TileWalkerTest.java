/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.tests;

import com.clarisma.common.util.Log;
import com.geodesk.geom.Box;
import com.geodesk.geom.Tile;
import com.geodesk.feature.Feature;
import com.geodesk.feature.FeatureLibrary;
import com.geodesk.feature.store.BoxCoordinateSequence;
import com.geodesk.feature.store.TileIndexWalker;
import com.geodesk.geom.Bounds;
import com.geodesk.util.MapMaker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygonal;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedPolygon;

public class TileWalkerTest
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
        Feature bavaria = world
            .select("a[boundary=administrative][admin_level=4][name:en=Bavaria]")
            // .select("a[boundary=administrative][admin_level=2][name:en=Germany]")
            .in(Box.atLonLat(12.0231, 48.3310))
            .first();
        Geometry bavariaPoly = bavaria.toGeometry();
        PreparedGeometry bavariaPrepared = new PreparedPolygon((Polygonal)bavariaPoly);

        MapMaker map = new MapMaker();
        int tileCount = 0;
        int tilesAlwaysFiltered = 0;
        int tilesOutside = 0;
        int tilesInside = 0;
        long start = System.currentTimeMillis();
        final int runs = 100;
        for(int run=0; run<runs; run++)
        {
            TileIndexWalker walker = new TileIndexWalker(world.store());
            Bounds targetBounds = bavaria.bounds();
            walker.start(targetBounds);
            while (walker.next())
            {
                boolean inside = false;
                boolean outside = false;
                tileCount++;
                Box box = Tile.bounds(walker.tile());
                if(box.minX() <= targetBounds.minX() &&
                    box.minY() <= targetBounds.minY() &&
                    box.maxX() >= targetBounds.maxX() &&
                    box.maxY() >= targetBounds.maxY())
                {
                    // If tile bbox contains the target bbox, no need
                    // to check tile, we always have to apply filter
                    tilesAlwaysFiltered++;
                }
                else
                {
                    // Geometry tileGeom = box.toGeometry(world.geometryFactory());
                    Geometry tileGeom = world.geometryFactory().createPolygon(
                        new BoxCoordinateSequence(box));

                    outside = bavariaPrepared.disjoint(tileGeom);
                    if (outside)
                    {
                        inside = false;
                    }
                    else
                    {
                        inside = bavariaPrepared.containsProperly(tileGeom);
                    }
                    if (run==0 && !inside && !outside) map.add(tileGeom);
                    if (run==0 && inside) map.add(tileGeom).color("green");
                }
                if (outside) tilesOutside++;
                if (inside) tilesInside++;
            }
        }
        long end = System.currentTimeMillis();
        map.add(bavariaPoly).color("red");
        map.save("c:\\geodesk\\tile-walker-test-germany.html");

        Log.debug("%d tiles in bbox", tileCount);
        Log.debug("  %d tiles outside", tilesOutside);
        Log.debug("  %d tiles inside", tilesInside);
        Log.debug("  %d tiles must be queried", tileCount - tilesOutside);
        Log.debug("  %d tiles must be filtered", tileCount - tilesInside - tilesOutside);
        Log.debug("    Of these, %d are always filtered, no tile geometry check needed", tilesAlwaysFiltered);
        Log.debug("Walking performed %d times in %d ms", runs, end - start);
    }
}
