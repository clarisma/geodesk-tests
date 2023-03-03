/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.experiments;

import com.clarisma.common.util.Log;
import org.junit.Test;

import java.nio.ByteBuffer;

public class FreeMemoryExperiment
{
    private static final int GB = 1024 * 1024 * 1024;
    private static final int MB = 1024 * 1024;

    private void reportFree()
    {
        /*
        for(int i=0; i<10; i++)
        {
            Runtime.getRuntime().gc();
        }
         */
        Log.debug("%,d bytes free", Runtime.getRuntime().freeMemory());
        Log.debug("%,d bytes total", Runtime.getRuntime().totalMemory());
        Log.debug("%,d bytes max", Runtime.getRuntime().maxMemory());
    }

    @Test public void testFreeMemory()
    {
        Log.debug("Start");
        reportFree();
        byte[] ba = new byte[GB];
        Log.debug("Allocated 1 GB");
        reportFree();
        byte[] ba2 = new byte[600 * MB];
        Log.debug("Allocated 600 MB");
        reportFree();
        byte[] ba3 = new byte[200 * MB];
        Log.debug("Allocated 200 MB");
        reportFree();
        byte[] ba4 = new byte[32 * MB];
        Log.debug("Allocated 32 MB");
        reportFree();
        ByteBuffer buf = ByteBuffer.allocateDirect(GB);
        Log.debug("Allocated 1 GB direct buffer");
        reportFree();
        ByteBuffer buf2 = ByteBuffer.allocateDirect(512 * MB);
        Log.debug("Allocated 512 MB GB direct buffer");
        reportFree();
        ByteBuffer buf3 = ByteBuffer.allocateDirect(GB);
        Log.debug("Allocated 1 GB direct buffer");
        reportFree();
    }
}
