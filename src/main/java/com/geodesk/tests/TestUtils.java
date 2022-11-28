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
import com.geodesk.feature.Features;
import com.geodesk.feature.Tags;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.api.list.primitive.MutableLongList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;

public class TestUtils
{
    /**
     * Reports the difference between two collections of features (both must
     * be typed IDs sorted in ascending order).
     *
     * @param aName     name of first set
     * @param a         typed IDs in first set, sorted in ascending order
     * @param bName     name of second set
     * @param b         typed IDs in second set, sorted in ascending order
     *
     * @return true if both sets contain the same features
     */
    public static boolean compareSets(String aName, LongList a, String bName, LongList b)
    {
        int ia = 0;
        int ib = 0;
        boolean equal = true;

        for(;;)
        {
            long fa = a.get(ia);
            long fb = b.get(ib);
            if(fa != fb)
            {
                equal = false;
                if(fa < fb)
                {
                    Log.warn("%s is in %s, but not in %s",
                        FeatureId.toString(fa), aName, bName);
                    ia++;
                }
                else
                {
                    Log.warn("%s is in %s, but not in %s",
                        FeatureId.toString(fb), bName, aName);
                    ib++;
                }
            }
            else
            {
                ia++;
                ib++;
            }
            if(ia == a.size())
            {
                if(ib == b.size()) break;
                ia--;
            }
            else if(ib == b.size())
            {
                ib--;
            }
        }
        if(equal)
        {
            Log.debug("Great! %s and %s contain the same features.", aName, bName);
        }
        return equal;
    }

    /**
     * Reports any duplicates in a feature collection (must
     * be typed IDs sorted in ascending order).
     *
     */
    public static boolean checkNoDupes(String name, LongList list)
    {
        long prev = 0;
        long lastDupe = 0;
        for(int i=0; i<list.size(); i++)
        {
            long fid = list.get(i);
            if(fid == prev)
            {
                if(lastDupe == 0)
                {
                    Log.warn("Duplicates in %s:", name);
                }
                if(lastDupe != fid)
                {
                    Log.warn("- %s", FeatureId.toString(fid));
                    lastDupe = fid;
                }
            }
            prev = fid;
        }
        if(lastDupe == 0)
        {
            Log.debug("Great! There are no dupes in %s.", name);
            return true;
        }
        return false;
    }

    public static LongList getSet(Features<?> features)
    {
        MutableLongList list = new LongArrayList();
        for(Feature f: features)
        {
            list.add(FeatureId.of(f.type(), f.id()));
        }
        return list.sortThis();
    }

    static final String[] IGNORED_KEYS = { "name", "ref", "type", "source", "note",
        "admin_level", "wikidata", "description" };

    public static boolean isIgnoredKey(String k)
    {
        for(String ignored: IGNORED_KEYS)
        {
            if(k.equals(ignored)) return true;
        }
        return false;
    }

    public static String primaryTag(Feature f)
    {
        Tags tags = f.tags();
        while(tags.next())
        {
            String key = tags.key();
            if(!isIgnoredKey(key))
            {
                return key + "=" + tags.value();
            }
        }
        return "";
    }
}
