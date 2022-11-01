/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.tests;

import com.clarisma.common.util.Log;
import com.geodesk.feature.Feature;
import com.geodesk.feature.FeatureLibrary;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MultiSelectorTest
{
    FeatureLibrary world;

    @Before public void setUp()
    {
        world = new FeatureLibrary(TestSettings.golFile(), TestSettings.tileURL());
    }

    @After public void tearDown()
    {
        world.close();
    }

    /**
     * Verifies that multi-selector queries return same results as combinations of equivalent single-selector queries
     * (Uses only counts for now).
     *
     * Test focus: same type, different keys
     *
     * (Issue #10)
     */
    @Test public void testMultiSelectorIndexing()
    {
        long onlyHighwayCount = 0;
        long onlyRailwayCount = 0;
        long bothHighwayAndRailwayCount = 0;
        long notHighwayOrRailwayCount = 0;

        for (Feature f : world.select("w"))
        {
            if (f.booleanValue("highway"))
            {
                if (f.booleanValue("railway"))
                {
                    bothHighwayAndRailwayCount++;
                }
                else
                {
                    onlyHighwayCount++;
                }
            }
            else if (f.booleanValue("railway"))
            {
                onlyRailwayCount++;
            }
            else
            {
                notHighwayOrRailwayCount++;
            }
        }

        Assert.assertEquals(onlyHighwayCount + bothHighwayAndRailwayCount,
            world.select("w[highway]").count());
        Assert.assertEquals(onlyHighwayCount,
            world.select("w[highway][!railway]").count());
        Assert.assertEquals(onlyRailwayCount + bothHighwayAndRailwayCount,
            world.select("w[railway]").count());
        Assert.assertEquals(onlyRailwayCount,
            world.select("w[!highway][railway]").count());
        Assert.assertEquals(bothHighwayAndRailwayCount,
            world.select("w[highway][railway]").count());
        Assert.assertEquals(notHighwayOrRailwayCount,
            world.select("w[!highway][!railway]").count());
        Assert.assertEquals(onlyHighwayCount + onlyRailwayCount + bothHighwayAndRailwayCount,
            world.select("w[highway], w[railway]").count());
    }

    /**
     * Verifies that multi-selector queries return same results as combinations of equivalent single-selector queries
     * (Uses only counts for now).
     *
     * Test focus: polyform queries (different types)
     *
     * (Issue #9)
     */
    @Test public void testPolyformQueries()
    {
        long nodesCount = world.select("n").count();
        long waysCount  = world.select("w").count();
        long areaCount  = world.select("a").count();
        long relationCount  = world.select("r").count();
        long stationCount = world.select("na[amenity=fire_station]").count();
        long hydrantCount  = world.select("n[emergency=fire_hydrant]").count();
        Assert.assertTrue(stationCount > 0);
        Assert.assertTrue(hydrantCount > 0);

        Log.debug("Nodes:     %d", nodesCount);
        Log.debug("Ways:      %d", waysCount);
        Log.debug("Areas:     %d", areaCount);
        Log.debug("Relations: %d", relationCount);

        Assert.assertEquals(nodesCount + waysCount,
            world.select("n, nw").count());
        Assert.assertEquals(nodesCount + waysCount,
            world.select("nw, n").count());
        Assert.assertEquals(nodesCount + waysCount + areaCount,
            world.select("na, wa, n").count());
        Assert.assertEquals(nodesCount + waysCount + areaCount,
            world.select("n, naw, an").count());
        Assert.assertEquals(nodesCount + relationCount + areaCount,
            world.select("ran, ar, n, na, nr").count());
        Assert.assertEquals(nodesCount + waysCount,
            world.select("n,w").count());
        Assert.assertEquals(nodesCount + areaCount,
            world.select("n,a").count());
        Assert.assertEquals(nodesCount + waysCount + areaCount,
            world.select("n,w,a").count());
        Assert.assertEquals(nodesCount + waysCount + areaCount,
            world.select("n,a,w").count());
        Assert.assertEquals(nodesCount + areaCount + relationCount,
            world.select("n,a,r").count());
        Assert.assertEquals(nodesCount + waysCount + areaCount + relationCount,
            world.select("n,w,a,r").count());
        Assert.assertEquals(nodesCount + waysCount + areaCount + relationCount,
            world.select("n,w,warn,a,r").count());
        Assert.assertEquals(nodesCount + waysCount + areaCount + relationCount,
            world.select("ran, ar, n, war, na, w, nr").count());

        Assert.assertEquals(stationCount + hydrantCount,
            world.select("na[amenity=fire_station], n[emergency=fire_hydrant]").count());
        Assert.assertEquals(stationCount + hydrantCount,
            world.select("n[emergency=fire_hydrant], na[amenity=fire_station]").count());
    }
}