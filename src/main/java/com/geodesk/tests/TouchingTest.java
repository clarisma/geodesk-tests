/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.tests;

import com.clarisma.common.util.Log;
import com.geodesk.feature.FeatureLibrary;
import com.geodesk.feature.Features;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TouchingTest
{
    FeatureLibrary features;

    @Before public void setUp()
    {
        features = new FeatureLibrary(TestSettings.golFile());
    }

    @After public void tearDown() {
        features.close();
    }

    @Test public void testTouching()
    {
        Features adminAreas = features.select("a[boundary=administrative]");
        Features counties = features.select("a[boundary=administrative][admin_level=6]");

        for (var county : counties)
        {
            System.out.printf("Admin areas that touch %s (%s):\n",
                county.stringValue("name"), county);
            for(var neighbor : adminAreas.touching(county))
            {
                System.out.printf("- %s: %s\n", neighbor, neighbor.stringValue("name"));
            }
        }
    }
}