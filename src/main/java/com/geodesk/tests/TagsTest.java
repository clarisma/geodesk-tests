/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.tests;

import com.clarisma.common.util.Log;
import com.geodesk.benchmark.SimpleBenchmark;
import com.geodesk.feature.Feature;
import com.geodesk.feature.FeatureLibrary;
import com.geodesk.feature.Node;
import com.geodesk.feature.Way;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class TagsTest
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

    @Test public void testTagsPerformance()
    {
        List<Way> streets = world.ways("w[highway]").toList();

        AtomicLong count2 = new AtomicLong();
        SimpleBenchmark.run("taglookup", 10, () ->
        {
            for(Way street: streets)
            {
                int crossings = 0;
                for(Node node: street)
                {
                    if (node.hasTag("highway", "crossing") &&
                        (node.hasTag("crossing", "marked") ||
                         node.hasTag("crossing", "zebra")))
                    {
                        crossings++;
                    }
                }
                count2.addAndGet(crossings);
            }
        });

        AtomicLong count = new AtomicLong();
        SimpleBenchmark.run("select", 10, () ->
        {
            for(Way street: streets)
            {
                count.addAndGet(street.nodes("[highway=crossing][crossing=marked,zebra]").count());
            }
        });

        AtomicLong count3 = new AtomicLong();
        SimpleBenchmark.run("taglookup", 10, () ->
        {
            for(Way street: streets)
            {
                int crossings = 0;
                for(Node node: street)
                {
                    if (node.hasTag("highway", "crossing"))
                    {
                        String crossing = node.stringValue("crossing");
                        if (crossing.equals("marked") ||
                            crossing.equals("zebra"))
                        {
                            crossings++;
                        }
                    }
                }
                count3.addAndGet(crossings);
            }
        });


        Log.debug("%d = %d = %d crossings", count.get(), count2.get(), count3.get());
    }
}
