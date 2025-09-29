/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

/*
package com.geodesk.feature.store;

import com.clarisma.common.util.Log;
import com.geodesk.geom.Tile;
import com.geodesk.geom.Box;
import com.geodesk.geom.Bounds;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;

public class TileIndexWalkerTest
{
    TestFeatureStore features;

    @Before
    public void setUp() throws Exception
    {
        features = new TestFeatureStore(Path.of("c:\\geodesk\\tests\\de.gol"));
    }

    @Test
    public void testFullTraversal()
    {
        walk(Box.ofWorld());
    }

    private void walk(Bounds bounds)
    {
        Log.debug("Bounds: %s", bounds);
        TileIndexWalker walker = new TileIndexWalker(
            features.baseMapping(), features.tileIndexPointer(),
            features.zoomLevels());
        walker.start(bounds);
        while(walker.next())
        {
            int tile = walker.tile();
            Log.debug(Tile.toString(tile));
        }
    }

    @Test
    public void testBoundsTraversal()
    {
        int tileMunich = Tile.fromString("12/2179/1421");
        Box munich = Tile.bounds(tileMunich);
        walk(munich);
        munich.translate(1,-1);
        walk(munich);
        munich.translate(-1,1);
        munich.buffer(1);
        walk(munich);
    }
}
 */