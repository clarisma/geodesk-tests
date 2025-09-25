/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.tests;

import com.clarisma.common.util.Log;
import com.geodesk.feature.*;
import com.geodesk.feature.filter.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Relations are tricky for some of the JTS relate operations, because they
 * involve GeometryCollections. This test checks for exceptions thrown by the
 * JTS library.
 */
public class SpatialRelationTest
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

    @Test public void spatialPredicatesInvolvingRelations()
    {
        int mostPlaceholders = 0;
        int mostNodes = 0;
        int mostWays = 0;
        Feature trickiestRelation = null;

        for(var rel: world.relations("r[route=train]"))
        {
            int placeholders = 0;
            int nodes = 0;
            int ways = 0;
            for(Feature member : rel)
            {
                if(member instanceof Node) nodes++;
                if(member instanceof Way) ways++;
            }
            if(nodes > mostNodes && ways > mostWays && placeholders > mostPlaceholders)
            {
                mostNodes = nodes;
                mostWays = ways;
                mostPlaceholders = placeholders;
                trickiestRelation = rel;
            }
        }

        Log.debug("Testing against %s (%d nodes, %d ways, %d placeholders)", trickiestRelation,
            mostNodes, mostWays, mostPlaceholders);

        testSpatial("coveredBy", trickiestRelation);
        testSpatial("overlaps", trickiestRelation);
        testSpatial("within", trickiestRelation);
        testSpatial("intersects", trickiestRelation);
        testSpatial("crosses", trickiestRelation);
        testSpatial("touches", trickiestRelation);
    }

    private void testSpatial(String name, Feature test)
    {
        Filter filter = switch(name)
        {
            case "coveredBy" -> new CoveredByFilter(test);
            case "crosses" -> new CrossesFilter(test);
            case "intersects" -> new IntersectsFilter(test);
            case "overlaps" -> new OverlapsFilter(test);
            case "touches" -> new TouchesFilter(test);
            case "within" -> new WithinFilter(test);
            default -> null;
        };

        Log.debug("%s %s:", name, test);
        for(Feature f: world.select("r").select(filter))
        {
            Log.debug("- %s", f);
        }
    }
}
