/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.experiments;

import com.clarisma.common.util.Log;
import org.eclipse.collections.api.map.primitive.MutableLongBooleanMap;
import org.eclipse.collections.api.map.primitive.MutableLongIntMap;
import org.eclipse.collections.api.map.primitive.MutableLongLongMap;
import org.eclipse.collections.api.map.primitive.MutableLongObjectMap;
import org.eclipse.collections.impl.map.mutable.primitive.LongBooleanHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.LongIntHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.LongLongHashMap;
import org.junit.Test;

import java.util.Random;

public class HashMapExperiment
{
    // What's the performance increase of pre-allocating a hashmap vs. growing
    // it (which requires pre-hashing)? Surprisingly, it's not that much,
    // at most 15%
    @Test public void testMapConstruction()
    {
        Random random = new Random();

        for(int run=0; run<10; run++)
        {
            long start = System.currentTimeMillis();
            MutableLongIntMap map = new LongIntHashMap(16);
            for(int i=0; i<20_000_000; i++)
            {
                map.put(random.nextLong(10_000_000_000L), i);
            }
            Log.debug("Created hashmap in %d ms", System.currentTimeMillis() - start);
        }
    }

    @Test public void testMapLookups()
    {
        MutableLongBooleanMap map = new LongBooleanHashMap();
        int count = 1_000_000;
        Random random = new Random();

        long[] lookups = new long[count];

        long id = 1_000_000;
        for(int i=0; i<count; i++)
        {
            map.put(id, (id % 2) == 0);
            lookups[i] = id;
            id += random.nextInt(1000);
        }

        for(int run=0; run<10; run++)
        {
            long start = System.currentTimeMillis();
            long sum = 0;
            for(int i=0; i<lookups.length; i++)
            {
                sum += map.get(lookups[i]) ? 1: 0;
            }
            Log.debug("Sum %d in %d ms", sum, System.currentTimeMillis() - start);
        }


    }
}
