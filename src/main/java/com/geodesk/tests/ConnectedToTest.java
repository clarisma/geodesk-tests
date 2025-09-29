/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.tests;

import com.clarisma.common.util.Log;
import com.geodesk.feature.Feature;
import com.geodesk.feature.FeatureId;
import com.geodesk.feature.FeatureLibrary;
import com.geodesk.feature.Features;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.api.list.primitive.MutableLongList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;

import java.util.List;

import com.geodesk.tests.TestSettings;

public class ConnectedToTest
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

    @Test public void testConnectedTo()
    {
        Feature route = world
            .select("r[type=route_master][route_master=bicycle][ref=D10]")
            .first();

        for(Feature f: world
            .select("r[route=bicycle]")
            .connectedTo(route))
        {
            Log.debug("- %s %s", f, f.stringValue("name"));
        }
    }
}

