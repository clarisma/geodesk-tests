/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.tests;

import com.clarisma.common.util.Log;
import org.junit.Test;

import java.util.UUID;

public class GuidTest
{
    @Test public void testGuid()
    {
        UUID guid = UUID.randomUUID();
        Log.debug("version = %d", guid.version());
        Log.debug("variant = %d", guid.variant());
        Log.debug("lo64 = %016x", guid.getLeastSignificantBits());
        Log.debug("hi64 = %016x", guid.getMostSignificantBits());
        Log.debug(guid);
        for(int i=0; i<20; i++) Log.debug(UUID.randomUUID());
    }
}
