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
