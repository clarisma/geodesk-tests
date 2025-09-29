/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.tests;

import com.clarisma.common.util.Log;
import com.geodesk.feature.*;
import org.junit.Test;

public class FilterTest
{
    @Test public void testConnectedStreets() throws Exception
    {
        FeatureLibrary world = Features.open(TestSettings.golFile());
        Features streets = world.ways("w[highway]");
        for(Feature street: streets)
        {
            Log.debug("%s %s %s connects to:", street,
                street.stringValue("highway"),
                street.stringValue("name"));
            for(Feature connected: streets.connectedTo(street))
            {
                Log.debug("- %s %s %s", connected,
                    connected.stringValue("highway"),
                    connected.stringValue("name"));
            }
        }
        world.close();
    }
}
