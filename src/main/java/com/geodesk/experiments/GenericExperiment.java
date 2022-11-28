/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.experiments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class GenericExperiment
{
    class Fruit implements Iterable<Fruit>
    {
        List<Fruit> fruits = new ArrayList<>();

        Fruit peel()
        {
            return this;
        }

        @Override public Iterator<Fruit> iterator()
        {
            return fruits.iterator();
        }
    }

    class Banana extends Fruit
    {
        @Override Banana peel()
        {
            return this;
        }

    }
}
