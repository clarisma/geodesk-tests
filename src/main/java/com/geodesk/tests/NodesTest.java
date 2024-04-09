/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.tests;

import com.clarisma.common.util.Log;
import com.geodesk.geom.Box;
import com.geodesk.feature.*;
import org.junit.Assert;
import org.junit.Test;

public class NodesTest extends AbstractFeatureTest
{
    /**
     * Checks each node tagged "geodesk:duplicate" to ensure:
     *
     * - The node has no tags other than "geodesk:*"
     * - There is at least one other node with the same x/y
     * - The other nodes have at least one tag
     */
    @Test public void testDuplicateNodes()
    {
        long duplicateNodeCount = 0;
        for (Feature node : world.nodes("n[geodesk:duplicate]"))
        {
            int tagCount = 0;
            Tags tags = node.tags();
            while (tags.next())
            {
                if (!tags.key().startsWith("geodesk:")) tagCount++;
            }
            Assert.assertEquals(0, tagCount);

            int nodeCount = 0;
            for (Feature otherNode : world.nodes().in(Box.atXY(node.x(), node.y())))
            {
                if (otherNode.equals(node)) continue;
                Assert.assertFalse(otherNode.tags().isEmpty());
                nodeCount++;
            }
            Assert.assertTrue(nodeCount > 0);
            duplicateNodeCount++;
        }
        Log.debug("Checked %,d duplicate nodes", duplicateNodeCount);
    }

    /**
     * Checks each node tagged "geodesk:orphan" to ensure:
     *
     * - The node has no tags other than "geodesk:*"
     * - The node does not belong to any way
     * - The node does not belong to any relation
     */
    @Test public void testOrphanNodes()
    {
        long orphanNodeCount = 0;
        for (Feature node : world.nodes("n[geodesk:orphan]"))
        {
            int tagCount = 0;
            Tags tags = node.tags();
            while (tags.next())
            {
                if (!tags.key().startsWith("geodesk:")) tagCount++;
            }
            Assert.assertEquals(0, tagCount);

            /*
            for (Way way : world.ways().in(Box.atXY(node.x(), node.y())))
            {
                Assert.assertFalse(way.nodes().contains(node));
            }
             */
            Assert.assertTrue(node.parents().ways().isEmpty());
            Assert.assertTrue(node.parents().relations().isEmpty());
            orphanNodeCount++;
        }
        Log.debug("Checked %,d orphan nodes", orphanNodeCount);
    }
}