package com.geodesk.tests;

import com.geodesk.core.Box;
import com.geodesk.core.Mercator;
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
