/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.tests;

import com.clarisma.common.util.Log;
import com.geodesk.feature.*;
import com.geodesk.feature.match.Matcher;
import com.geodesk.feature.match.RoleMatcher;
import com.geodesk.feature.match.TypeBits;
import com.geodesk.feature.query.EmptyView;
import com.geodesk.feature.query.WorldView;
import com.geodesk.feature.store.StoredFeature;
import com.geodesk.feature.store.StoredRelation;
import com.geodesk.geom.Box;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Lineal;
import org.locationtech.jts.geom.Polygonal;

import java.util.*;

import static org.junit.Assert.*;

public class WayNodesTest
{
    FeatureLibrary features;

    @Before public void setUp()
    {
        features = new FeatureLibrary(TestSettings.golFile(), TestSettings.tileURL());
    }

    @After public void tearDown() {
        features.close();
    }

    @Test public void testNodeParentCounts()
    {
        Features streets = features.select("w[highway]");
        for(var street : streets)
        {
            long nNodes = street.nodes().count();
            assert(nNodes >= 2);
            long nodeCount = 0;
            for(var node: street.nodes())
            {
                assert(node.belongsTo(street));
                long nParents = node.parents().count();
                if(nParents == 0)
                {
                    Log.debug("No parents found for %s in %s", node, street);
                }
                assert(nParents > 0);
                long nParentWays = node.parents().ways().count();
                long nParentRelations = node.parents().relations().count();
                assert(nParentWays + nParentRelations == nParents);
                nodeCount++;
            }
            assert(nNodes == nodeCount);
        }
    }

    @Test public void testNodesInRelations()
    {
        for(var rel: features.relations())
        {
            for(var node: rel.members().nodes())
            {
                assert(node.parents().relations().count() > 0);
                assert(node.parents().relations().contains(rel));
                assert(node.belongsToRelation());
                assert(node.belongsTo(rel));
            }
        }
    }
}