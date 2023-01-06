/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.experiments;

import com.clarisma.common.util.Log;
import org.junit.Test;

public class UnicodeExperiment
{
    @Test public void testUnicode()
    {
        String s = "臺中市";
        Log.debug(s.length());
        for(int i=0; i<s.length(); i++)
        {
            char ch = s.charAt(i);
            Log.debug("%d: %c = %d", i, ch, (int)ch);
        }
    }
}
