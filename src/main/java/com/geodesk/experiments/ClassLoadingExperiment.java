/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.experiments;

import com.clarisma.common.util.Log;

public class ClassLoadingExperiment
{
    public final static OtherClass.Junk MY_JUNK  = new OtherClass.Junk("main");

    void callThis()
    {
        Log.debug("Called a method.");
    }

    void notThat()
    {
        Log.debug("I shouldn't be called, because I create more junk: " +
            OtherClass.JUNK);
    }

    class Inner
    {
        public final static OtherClass.Junk INNER_JUNK  = new OtherClass.Junk("inner");

        public static void innerStatic()
        {
            Log.debug("Called innerStatic()");
        }
    }

    public static void main(String[] args)
    {
        new ClassLoadingExperiment().callThis();
        Log.debug(Inner.class);
        // Inner.innerStatic();
        // new ClassLoadingExperiment().notThat();
    }
}
