/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.tests;


import com.clarisma.common.util.Log;
import com.geodesk.feature.Feature;
import com.geodesk.feature.Features;
import com.geodesk.feature.Relation;
import com.geodesk.feature.Way;
import org.junit.Assert;
import org.junit.Test;

public class MemberMatcherTest extends AbstractFeatureTest
{
    @Test public void testRoleQuery()
    {
        Features<Relation> rivers = world.relations("r[waterway=river]");
        for(Relation river: rivers)
        {
            for(Feature m: river.members(/* "[role=side_stream]" */))
            {
                Log.debug("- %s (name: %s, role: %s)", m, m.stringValue("name"), m.role());
            }
        }
    }

    @Test public void testMemberQuery()
    {
        Features<Relation> rivers = world.relations("r[waterway=river]");
        for(Relation river: rivers)
        {
            for(Feature m: river.members("n[!natural], w"))
            {
                Log.debug("- %s (name: %s, role: %s)", m, m.stringValue("name"), m.role());
            }
        }
    }

    @Test public void testTypedMemberQuery()
    {
        Features<Relation> rivers = world.relations();
        for(Relation river: rivers)
        {
            for(Feature m: river.members("w"))
            {
                Assert.assertTrue(m instanceof Way);
            }
        }
    }
}
