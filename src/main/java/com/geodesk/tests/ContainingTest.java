/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.tests;

import com.geodesk.feature.FeatureLibrary;
import com.geodesk.feature.Features;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ContainingTest
{
    FeatureLibrary features;

    @Before public void setUp()
    {
        features = new FeatureLibrary(TestSettings.golFile());
    }

    @After public void tearDown() {
        features.close();
    }

    @Test public void testWithin()
    {
        Features counties = features.select("a[boundary=administrative][admin_level=6]");

        for (var county : counties)
        {
            for(var f: features.select("na").within(county))
            {
                assert(features.containing(f).contains(county));
            }
        }
    }

    @Test public void testContaining()
    {
        Features counties = features.select("a[boundary=administrative][admin_level=6]");

        for (var f : features.select("na"))
        {
            if(!f.toGeometry().isValid())
            {
                System.out.printf("Skipping %s: invalid geometry\n", f);
                continue;
            }
            for(var container: features.containing(f))
            {
                if(!features.within(container).contains(f))
                {
                    System.out.printf("%s contains %s, but %s is not within %s!?\n",
                        container, f, f, container);
                }
            }
        }
    }
}