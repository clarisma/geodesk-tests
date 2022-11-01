/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.tests;

import com.clarisma.common.util.Log;
import com.geodesk.feature.FeatureLibrary;
import com.geodesk.feature.Features;
import com.geodesk.feature.Filters;
import com.geodesk.feature.Way;
import org.junit.Test;

public class FilterTest
{
    @Test public void testConnectedStreets() throws Exception
    {
        FeatureLibrary world = new FeatureLibrary("c:\\geodesk\\tests\\de.gol");
        Features<Way> streets = world.ways("w[highway]");
        for(Way street: streets)
        {
            Log.debug("%s %s %s connects to:", street,
                street.stringValue("highway"),
                street.stringValue("name"));
            for(Way connected: streets.select(Filters.connectedTo(street)))
            {
                Log.debug("- %s %s %s", connected,
                    connected.stringValue("highway"),
                    connected.stringValue("name"));
            }
        }
        world.close();
    }
}
