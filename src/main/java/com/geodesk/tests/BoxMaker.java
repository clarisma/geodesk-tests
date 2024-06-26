/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.tests;

import com.geodesk.geom.Box;
import com.geodesk.geom.Mercator;
import com.geodesk.geom.Bounds;

import java.util.Random;

public class BoxMaker
{
    private final Random random;
    private final Bounds bounds;
    private final double scale;

    public BoxMaker(Bounds bounds)
    {
        this.bounds = bounds;
        random = new Random();
        scale = Mercator.metersAtY(bounds.centerY());
    }

    public Box random(int minMeters, int maxMeters)
    {
        double widthMeters = random.nextInt(maxMeters-minMeters) + minMeters;
        double heightMeters = random.nextInt(maxMeters-minMeters) + minMeters;
        int w = (int)(widthMeters / scale);
        int h = (int)(widthMeters / scale);
        long xDelta = random.nextLong(bounds.width() - w);
        long yDelta = random.nextLong(bounds.height() - h);
        return Box.ofXYWidthHeight(
            (int)(bounds.minX() + xDelta), (int)(bounds.minY() + yDelta), w, h);
    }
}
