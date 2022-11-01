/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.tests;

import com.geodesk.core.Box;
import com.geodesk.feature.Feature;
import com.geodesk.feature.FeatureLibrary;
import com.geodesk.feature.Features;
import com.geodesk.feature.Way;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;

public class FeatureTest
{
    FeatureLibrary features;
    BoxMaker boxes;

    @Before public void setUp()
    {
        features = new FeatureLibrary(TestSettings.golFile());
    }

    @After public void tearDown()
    {
        features.close();
    }

    /**
     * Coordinates returned by `toXY` should match those of the feature's geometry.
     */
    @Test public void testCoordinates()
    {
        Features<Way> ways = features.ways();
        for (Way w : ways)
        {
            int[] coords = w.toXY();
            Geometry g = w.toGeometry();
            Assert.assertTrue(coords.length % 2 == 0);
            Assert.assertTrue(coords.length >= 4);
            Assert.assertEquals(coords.length / 2, g.getNumPoints());
        }
    }

    /**
     * Raise this limit as planet file grows.
     */
    static final long MAX_REALISTIC_ID = 16_000_000_000L;

    /**
     * Make sure IDs are not 0, negative, or unusually large (this limit can change!)
     */
    @Test public void testIds()
    {
        for (Feature f : features)
        {
            long id = f.id();
            Assert.assertTrue(id > 0);
            Assert.assertTrue(id <= MAX_REALISTIC_ID);
        }
    }
}