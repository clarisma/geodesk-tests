/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.tests;

import com.clarisma.common.util.Log;
import com.geodesk.feature.Feature;
import com.geodesk.feature.FeatureLibrary;
import com.geodesk.feature.Features;
import com.geodesk.feature.Filters;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ContainsTest
{
    FeatureLibrary features;

    @Before public void setUp()
    {
        features = new FeatureLibrary(TestSettings.golFile());
    }

    @After public void tearDown()
    {
        features.close();
    }

    private void testShouldContain(double lon, double lat, String... names)
    {
        Map<String,Boolean> present = new HashMap<>(names.length);
        for(String name: names) present.put(name, false);

        for (Feature f : features.select("a").select(Filters.containsLonLat(lon, lat)))
        {
            // Log.debug("- %s %s", f, f.stringValue("name"));
            String name = f.stringValue("name");
            if(!name.isEmpty()) present.put(name, true);
        }
        for(Map.Entry<String,Boolean> entry: present.entrySet())
        {
            if(!entry.getValue())
            {
                Assert.fail(String.format(
                    "Expected \"%s\" to contain lon=%f, lat=%f",
                    entry.getKey(), lon, lat));
            }
        }
    }

    @Test public void testContainsKnown()
    {
        testShouldContain(13.39662, 52.52099,
            "Pergamonmuseum", "Museumsinsel", /* "Spreeinsel", */ "Mitte", "Berlin",
            "Berliner Urstromtal", "Deutschland", /* "Deutschland (Landmasse)", */
            "Umweltzone Berlin");
        testShouldContain(6.95825, 50.94131,
            "Kölner Dom", "Altstadt-Nord", "Innenstadt", "Köln",
            "Nordrhein-Westfalen", "Deutschland", /* "Deutschland (Landmasse)", */
            /* "Umweltzone Köln", */ "Verkehrsverbund Rhein-Sieg");
        testShouldContain(8.5601, 50.0376,
            "Flughafen Frankfurt am Main", "Flughafen", "Frankfurt am Main",
            "Hessen", "Deutschland", /* "Deutschland (Landmasse)", */
            "Regierungsbezirk Darmstadt",
            /* "Umweltzone Köln", */ "Rhein-Main-Verkehrsverbund");
        testShouldContain(9.4739, 47.6144,
            "Obersee", "Bodensee",
            "Baden-Württemberg", "Deutschland" /* "Deutschland (Landmasse)", */
            );
    }

    @Test public void testContainsFeature()
    {
        Features parks = features.select("a[leisure=park][name]");
        for(Feature park: parks)
        {
            Log.debug("%s %s is located in:", park, park.stringValue("name"));
            for(Feature f: features.select(Filters.contains(park)).select("nw"))
            {
                Log.debug("- %s: %s %s", f, TestUtils.primaryTag(f), f.stringValue("name"));
            }
        }
    }

    @Test public void testContainsPerformance()
    {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) testContainsKnown();
        Log.debug("Executed in %d ms", System.currentTimeMillis() - start);
    }
}
