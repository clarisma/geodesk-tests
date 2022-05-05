package com.geodesk.feature.store;

import com.geodesk.core.Tile;
import com.geodesk.core.Box;
import com.geodesk.geom.Bounds;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;

public class TileIndexWalkerTest
{
    public static final Logger log = LogManager.getLogger();

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
        log.debug("Bounds: {}", bounds);
        TileIndexWalker walker = new TileIndexWalker(
            features.baseMapping(), features.tileIndexPointer(),
            features.zoomLevels());
        walker.start(bounds);
        while(walker.next())
        {
            int tile = walker.tile();
            log.debug(Tile.toString(tile));
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