/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.tests;

import com.clarisma.common.util.Log;
import com.geodesk.feature.Feature;
import com.geodesk.feature.FeatureId;
import com.geodesk.feature.FeatureLibrary;
import com.geodesk.feature.Features;
import static com.geodesk.feature.Filters.*;
import static com.geodesk.tests.TestUtils.*;

import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.api.list.primitive.MutableLongList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;

import java.util.List;

public class IntersectsTest
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

    LongList getFeatures(Features<?> features)
    {
        MutableLongList list = new LongArrayList();
        for(Feature f: features)
        {
            list.add(FeatureId.of(f.type(), f.id()));
        }
        return list.sortThis();
    }

    @Test public void testIntersects()
    {
        Features<?> restaurants = world.select("na[amenity=restaurant]");
        Geometry country = world
            .select("a[boundary=administrative][admin_level=2][name:en=Germany]")
            .first().toGeometry();
        List<Feature> states = (List<Feature>)world
            .select("a[boundary=administrative][admin_level=4][name]")
            .select(slowWithin(country)).toList();

        LongList inCountry = getFeatures(restaurants.select(slowWithin(country)));
        MutableLongList inStates = new LongArrayList();
        for(Feature state: states)
        {
            Log.debug("- %s", state.stringValue("name"));
            LongList inState = getFeatures(restaurants.select(slowIntersects(state)));
            inStates.addAll(inState);
        }

        inStates.sortThis();
        compareSets("country", inCountry, "all_states", inStates);
    }
}
