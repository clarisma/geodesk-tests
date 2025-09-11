/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.tests;

import com.clarisma.common.util.Log;
import com.geodesk.feature.filter.SlowIntersectsFilter;
import com.geodesk.geom.Box;
import com.geodesk.feature.*;
import org.eclipse.collections.api.list.primitive.LongList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NorthwestTest
{
    FeatureLibrary world;

    @Before public void setUp()
    {
        world = new FeatureLibrary("c:\\geodesk\\tests\\germany.gol");
    }

    @After public void tearDown()
    {
        world.close();
    }

    @Test public void testSameIntersectsResults()
    {
        Feature country = world
            .select("a[boundary=administrative][admin_level=2][name:en=Germany]")
            .in(Box.atLonLat(12.0231, 48.3310))
            .first();
        Features buildings = world.select("wa[highway]");

        LongList slow = TestUtils.getSet(buildings.select(new SlowIntersectsFilter(country.toGeometry())));
        LongList fast = TestUtils.getSet(buildings.intersecting(country));

        TestUtils.checkNoDupes("intersects-fast", fast);
        TestUtils.compareSets("intersects-slow", slow, "intersects-fast", fast);
    }

    // Answer: broken geometry, member ways don't connect
    @Test public void whySlowIntersectionFailsToReturnRelation9675374()
    {
        for(Feature rel : world
            .relations("ra[name='Euskirchener Straße'][type=public_transport]"))
        {
            if(rel.id() == 9675374)
            {
                Log.debug("Area = %s", rel.isArea());
                for (Feature member : rel)
                {
                    Log.debug("- %s: %s", member, member.toGeometry());
                }
                Log.debug("Geometry = %s", rel.toGeometry());
            }
        }
    }
}
