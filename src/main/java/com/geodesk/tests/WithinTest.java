package com.geodesk.tests;

import com.clarisma.common.util.Log;
import com.geodesk.core.Box;
import com.geodesk.feature.Feature;
import com.geodesk.feature.FeatureLibrary;
import com.geodesk.feature.Features;
import com.geodesk.util.GeometryBuilder;
import org.junit.Test;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.prep.PreparedPolygon;

import static java.lang.System.out;

public class WithinTest
{
    @Test public void testWithinPoint()
    {
        long start = System.currentTimeMillis();
        FeatureLibrary world = new FeatureLibrary("c:\\geodesk\\tests\\de.gol");
        GeometryBuilder geomBuilder = new GeometryBuilder();

        double lon = 13.38686;
        double lat = 52.50806;

        Point pt = geomBuilder.createPointFromLonLat(lon, lat);

        Features<?> areas = world
            .select("a")
            .in(Box.atLonLat(lon, lat));

        for(int i=0; i<10; i++)
        {
            long startQuery = System.currentTimeMillis();

            int count = 0;
            for(Feature f: areas)
            {
                Geometry candidateGeom = f.toGeometry();
                try
                {
                    if (candidateGeom != null && candidateGeom.contains(pt))
                    {
                        Log.debug("- %s: %s", f, f.stringValue("name"));
                        count++;
                    }
                }
                catch(TopologyException ex)
                {
                    Log.debug("Exception while testing %s: %s", f, ex);
                    Log.debug("Valid geometry? %s", candidateGeom.isValid());
                }
            }

            long end = System.currentTimeMillis();
            out.format("Found %d features in %d ms (Total runtime %d ms)\n", count,
                end - startQuery, end - start);
        }
    }

    @Test public void testWithinPointPrepared()
    {
        long start = System.currentTimeMillis();
        FeatureLibrary world = new FeatureLibrary("c:\\geodesk\\tests\\de.gol");
        GeometryBuilder geomBuilder = new GeometryBuilder();

        double lon = 13.38686;
        double lat = 52.50806;

        Point pt = geomBuilder.createPointFromLonLat(lon, lat);

        Features<?> areas = world
            .select("a")
            .in(Box.atLonLat(lon, lat));

        for(int i=0; i<10; i++)
        {
            long startQuery = System.currentTimeMillis();

            int count = 0;
            for(Feature f: areas)
            {
                PreparedPolygon candidateGeom = new PreparedPolygon((Polygonal)f.toGeometry());
                try
                {
                    if (candidateGeom != null && candidateGeom.contains(pt))
                    {
                        Log.debug("- %s: %s", f, f.stringValue("name"));
                        count++;
                    }
                }
                catch(TopologyException ex)
                {
                    Log.debug("Exception while testing %s: %s", f, ex);
                    Log.debug("Valid geometry? %s", candidateGeom.getGeometry().isValid());
                }
            }

            long end = System.currentTimeMillis();
            out.format("Found %d features in %d ms (Total runtime %d ms)\n", count,
                end - startQuery, end - start);
        }
    }
}

