/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.tests;

import com.geodesk.core.Box;
import com.geodesk.feature.FeatureLibrary;
import com.geodesk.util.MapMaker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.geodesk.feature.Filters.*;

public class MaxMetersFromTest
{
    FeatureLibrary features;

    @Before public void setUp()
    {
        features = new FeatureLibrary(TestSettings.golFile(), TestSettings.tileURL());
    }

    @After public void tearDown()
    {
        features.close();
    }

    @Test public void testFromPoint() throws IOException
    {
        MapMaker map = new MapMaker();
        map.add(features
            .select("a[building]")
            .select(maxMetersFromLonLat(500, 11.078, 49.454)));
        map.save(TestSettings.outputPath().resolve("max-meters.html").toString());
    }
}
