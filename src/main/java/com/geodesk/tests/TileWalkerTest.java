package com.geodesk.tests;

import com.clarisma.common.util.Log;
import com.geodesk.core.Box;
import com.geodesk.core.Tile;
import com.geodesk.feature.Feature;
import com.geodesk.feature.FeatureLibrary;
import com.geodesk.feature.store.TileIndexWalker;
import com.geodesk.util.MapMaker;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.geom.prep.PreparedPolygon;

public class TileWalkerTest
{
    @Test public void testTileWalker() throws Exception
    {
        FeatureLibrary world = new FeatureLibrary("c:\\geodesk\\tests\\de.gol");
        Feature bavaria = world
            .features("a[boundary=administrative][admin_level=4][name:en=Bavaria]")
            .in(Box.atLonLat(12.0231, 48.3310))
            .first();
        Geometry bavariaPoly = bavaria.toGeometry();
        PreparedGeometry bavariaPrepared = new PreparedPolygon((Polygon)bavariaPoly);

        MapMaker map = new MapMaker();
        TileIndexWalker walker = new TileIndexWalker(world.store());
        walker.start(bavaria.bounds());
        int tileCount = 0;
        int tilesOutside = 0;
        int tilesInside = 0;
        while (walker.next())
        {
            Box box = Tile.bounds(walker.tile());
            Geometry tileGeom = box.toGeometry(world.geometryFactory());

            tileCount++;

            boolean outside = bavariaPrepared.disjoint(tileGeom);
            boolean inside  = bavariaPrepared.containsProperly(tileGeom);
            if(outside) tilesOutside++;
            if(inside) tilesInside++;

            if(!inside && !outside) map.add(tileGeom);
        }
        map.add(bavariaPoly).color("red");
        map.save("c:\\geodesk\\tile-walker-test.html");

        Log.debug("%d tiles in bbox", tileCount);
        Log.debug("  %d tiles outside", tilesOutside);
        Log.debug("  %d tiles inside", tilesInside);
        Log.debug("  %d tiles must be queried", tileCount - tilesOutside);
        Log.debug("  %d tiles must be filtered", tileCount - tilesInside - tilesOutside);
    }
}
