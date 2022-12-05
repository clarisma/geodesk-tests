/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.experiments;

import com.clarisma.common.util.Log;
import com.geodesk.feature.FeatureLibrary;
import com.geodesk.feature.Relation;
import com.geodesk.feature.Way;
import com.geodesk.tests.TestSettings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NodesExperiment
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

    @Test public void findEntrancesInRelationWays()
    {
        for(Relation rel: world.relations("a[building]"))
        {
            for(Way way: rel.memberWays())
            {
                if(!way.nodes("[entrance]").isEmpty())
                {
                    Log.debug(way);
                }
            }
        }
    }
}
