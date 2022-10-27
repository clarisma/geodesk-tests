package com.geodesk.experiments;

import com.geodesk.feature.FeatureLibrary;
import org.junit.Test;

import static java.lang.System.out;

public class BBoxCheckExperiment
{
    @Test public void testSpatialBuildingsBbox()
    {
        long start = System.currentTimeMillis();
        FeatureLibrary world = new FeatureLibrary("c:\\geodesk\\tests\\de.gol");

        for(int i=0; i<10; i++)
        {
            long startQuery = System.currentTimeMillis();
            long count = world.select("*[name='*weg']").count();
            long end = System.currentTimeMillis();
            out.format("Found %d features in %d ms (Total runtime %d ms)\n", count,
                end - startQuery, end - start);
        }
        world.close();
    }
}
