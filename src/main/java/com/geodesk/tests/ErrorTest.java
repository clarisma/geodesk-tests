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
import com.geodesk.feature.Filter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class ErrorTest
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
     * Filter that throws exception after accepting 10 features
     */
    private static class BadFilter implements Filter
    {
        AtomicInteger count = new AtomicInteger();

        @Override public boolean accept(Feature feature)
        {
            int currentCount = count.incrementAndGet();
            if(currentCount > 10)
            {
                throw new RuntimeException("[Test] Something bad happened in the BadFilter!");
                // return false;
            }
            Log.debug("Accepted %d features (%s)", currentCount, feature);
            return true;
        }
    }

    /**
     *
     *
     * (Issue #22)
     */
    @Test public void testFilterError()
    {
        int count = 0;
        BadFilter badFilter = new BadFilter();

        for (Feature f : world.select("w").select(badFilter))
        {
            count++;
            Log.debug("%d: %s", count, f);
        }
    }

}