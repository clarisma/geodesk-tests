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
import static com.geodesk.feature.Filters.*;
import static com.geodesk.tests.TestUtils.*;

import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.api.list.primitive.MutableLongList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;

import java.util.List;

public class WithinTest
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

    @Test public void testWithin()
    {
        Geometry bavaria = world
            .select("a[boundary=administrative][admin_level=4][name:en=Bavaria]")
            .first().toGeometry();
        Features highways = world.select("w[highway]");
        LongList slow = getSet(highways.select(slowWithin(bavaria)));
        LongList fast = getSet(highways.select(within(bavaria)));
        checkNoDupes("fast", fast);
        compareSets("slow", slow, "fast", fast);
    }
}
