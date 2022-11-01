/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.tests;

import com.clarisma.common.util.Log;
import com.geodesk.feature.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LocalKeyTest
{
    @Test public void testLocalKeys() throws Exception
    {
        FeatureLibrary world = new FeatureLibrary("c:\\geodesk\\tests\\s7.gol");
        long count = 0;
        for (Feature n : world.nodes())
        {
            if (n.booleanValue("harbour"))
            {
                Log.debug("%s: %s", n, n.tags());
                count++;
            }
        }

        Log.debug("%d found manually, %d per query", count,
            world.nodes("[harbour]").count());

        world.close();
    }

    @Test public void test2() throws Exception
    {
        FeatureLibrary world = new FeatureLibrary("c:\\geodesk\\tests\\s7.gol");
        long count = 0;
        for (Feature n : world.nodes())
        {
            if (
                n.id() == 1485039266L ||
                    n.id() == 1910059730L ||
                    n.id() == 1281920924L ||
                    n.id() == 824086048)
            {
                String h = n.stringValue("harbour");
                Log.debug("%s: %s", n, h);
            }
        }
        world.close();
    }

    @Test public void test3() throws Exception
    {
        FeatureLibrary world = new FeatureLibrary("c:\\geodesk\\tests\\s7.gol");
        for (Feature n : world.nodes("[harbour]"))
        {
            Log.debug(n);
        }
        world.close();
    }

    @Test public void test4() throws Exception
    {
        FeatureLibrary world = new FeatureLibrary("c:\\geodesk\\tests\\s8.gol");

        Features<?> harbours = world.nodes("[harbour]");

        long nodeCount = world.nodes().count();
        long harbourCount = harbours.count();
        long nonHarbourCount = world.nodes("[!harbour]").count();

        Log.debug("Total nodes:        %d", nodeCount);
        Log.debug("[harbour] results:  %d", harbourCount);
        Log.debug("[!harbour] results: %d", nonHarbourCount);
        Log.debug("Total - [harbour]:  %d", nodeCount - harbourCount);
        Log.debug("Total - [!harbour]: %d", nodeCount - nonHarbourCount);

        Set<Feature> queryMatched = new HashSet<>();
        Log.debug("");
        Log.debug("Results returned by [harbour] query:");
        for (Feature f : harbours)
        {
            Log.debug("%20s", f);
            queryMatched.add(f);
        }

        Log.debug("");
        Log.debug("Check tag via Feature.stringValue(\"harbour\"), test harbours.contains():");

        List<Feature> manuallyMatched = new ArrayList<>();
        for (Feature f : world.nodes())
        {
            String h = f.stringValue("harbour");
            if (!h.isEmpty())
            {
                Log.debug("%20s: harbour=%s  harbours.contains(): %s", f, h,
                    harbours.contains(f));
                manuallyMatched.add(f);
            }
            else
            {
                Assert.assertFalse(harbours.contains(f));
            }
        }

        Log.debug("");
        Log.debug("Tags of manually-matched features:");
        for (Feature f : manuallyMatched)
            Log.debug("%s %s: %s",
                queryMatched.contains(f) ? "[harbour] -->" : "",
                f, f.tags());

        world.close();
    }

    @Test public void debug8a() throws Exception
    {
        FeatureLibrary world = new FeatureLibrary("c:\\geodesk\\tests\\s8.gol");

        Features<?> harbours = world.nodes("[harbour]");
        long count = harbours.count();
        Log.debug("%d results", count);

        world.close();
    }
}