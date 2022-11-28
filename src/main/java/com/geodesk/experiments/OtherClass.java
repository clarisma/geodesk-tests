/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.experiments;

import com.clarisma.common.util.Log;

public class OtherClass
{
    public final static Junk JUNK = new Junk("other");

    public static class Junk
    {
        Junk(String name)
        {
            Log.debug("Created Junk: " + name);
        }
    }
}
