/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.tests;

import com.geodesk.feature.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class IdLookupTest
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

    @Test public void testLookupNode() throws Exception
    {
        long presentId =  240109189;
        long absentId =  1;

        var f = world.node(presentId);
        Assert.assertNotNull(f);
        Assert.assertEquals(presentId, f.id());

        f = world.select("n[place]").node(presentId);
        Assert.assertNotNull(f);

        f = world.select("w").node(presentId);
        Assert.assertNull(f);

        f = world.node(absentId);
        Assert.assertNull(f);
    }

    @Test public void testLookupWay() throws Exception
    {
        long presentId =  27426031;
        long absentId =  1;

        var f = world.way(presentId);
        Assert.assertNotNull(f);
        Assert.assertEquals(presentId, f.id());

        f = world.select("a[building]").way(presentId);
        Assert.assertNotNull(f);

        f = world.select("n").way(presentId);
        Assert.assertNull(f);

        f = world.select("w").way(presentId);
        Assert.assertNull(f);

        f = world.select("r").way(presentId);
        Assert.assertNull(f);

        f = world.way(absentId);
        Assert.assertNull(f);
    }

    @Test public void testLookupRelation() throws Exception
    {
        long presentId =  2599004;
        long absentId =  1;

        var f = world.relation(presentId);
        Assert.assertNotNull(f);
        Assert.assertEquals(presentId, f.id());

        f = world.select("r[route_master]").relation(presentId);
        Assert.assertNotNull(f);

        f = world.select("r[restriction]").relation(presentId);
        Assert.assertNull(f);

        f = world.relation(absentId);
        Assert.assertNull(f);
    }
}