/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.tests;

import com.clarisma.common.util.Log;
import com.geodesk.core.Box;
import com.geodesk.feature.*;
import org.eclipse.collections.api.list.primitive.LongList;
import org.junit.Test;

public class NodeDiscrepancyTest
{
    @Test public void investigateDiscrepancy()
    {
        FeatureLibrary de4 = new FeatureLibrary("c:\\geodesk\\tests\\de4.gol");
        FeatureLibrary de5 = new FeatureLibrary("c:\\geodesk\\tests\\de5.gol");

        LongList nodes4 = TestUtils.getSet(de4.select("n"));
        LongList nodes5 = TestUtils.getSet(de5.select("n[!geodesk:duplicate][!geodesk:orphan]"));

        TestUtils.compareSets("de4", nodes4, "de5", nodes5);
    }

    @Test public void investigateRelation1958364() // 28415
    {
        FeatureLibrary de5 = new FeatureLibrary("c:\\geodesk\\tests\\de5.gol");
        for(Relation rel: de5.relations())
        {
            if(rel.id() == 1958364)
            {
                Log.debug(rel);
                for(Feature f: rel.members())
                {
                    Log.debug("- %s: %s", f, f.role());
                }
            }
        }
    }

    @Test public void investigateNode98677236() // 28415
    {
        FeatureLibrary de5 = new FeatureLibrary("c:\\geodesk\\tests\\de5.gol");
        for(Node n: de5.nodes())
        {
            if(n.id() == 98677236)
            {
                Log.debug("%s: %s", n, n.tags());
                Log.debug("All nodes at this location:");
                for(Node n2: de5.nodes().in(Box.atXY(n.x(), n.y())))
                {
                    Log.debug("- %s: %s", n2, n2.tags());
                }
            }
        }
    }
}

