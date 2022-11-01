/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.tests;

import com.clarisma.common.util.Log;
import com.geodesk.core.Box;
import com.geodesk.feature.Feature;
import com.geodesk.feature.FeatureLibrary;
import org.junit.Test;

public class PolyformQueryTest
{
    @Test public void testPolyfomQueries()
    {
        FeatureLibrary world = new FeatureLibrary("c:\\geodesk\\tests\\de.gol");
        for(Feature f : world
            .select("na[amenity=fire_station],n[emergency=fire_hydrant]")
            .in(Box.ofWorld()))
        {
            Log.debug("%s: ", f);
            Log.debug("%s", f.tags().toString());
        }
    }
}
