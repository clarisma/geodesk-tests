/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.tests;

import com.clarisma.common.util.Log;
import com.clarisma.common.util.ProgressListener;
import com.clarisma.common.util.ProgressReporter;
import com.geodesk.geom.Box;
import com.geodesk.geom.Mercator;
import com.geodesk.feature.*;
import com.geodesk.io.osm.OsmPbfReader;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This test verifies that all nodes in an .osm.pbf file are contained in a GOL,
 * either as a feature node, or as coordinates on a way.
 * (The GOL must have been built from this exact .osm.pbf)
 *
 * Nodes may only be omitted if they are untagged and are not members of any
 * ways or relations.
 *
 * This test also verifies that all Mercator coordinates in the GOL are properly
 * converted back to the WGS-84 coordinates used by OSM.
 *
 */
public class NodeCoordinatesTest extends AbstractFeatureTest
{
    final static String ORIGINAL_OSM_PBF = "c:\\geodesk\\mapdata\\de-2022-11-28.osm.pbf";

    static class CoordinateChecker extends OsmPbfReader
    {
        private ProgressListener progress;
        private final FeatureLibrary world;

        CoordinateChecker(FeatureLibrary world)
        {
            this.world = world;
        }

        @Override protected WorkerThread createWorker()
        {
            return new CheckerThread();
        }

        private class CheckerThread extends WorkerThread
        {
            @Override protected void node(long id, int lon100nd, int lat100nd, Tags tags)
            {
                if(id == 26965283)
                {
                    Log.debug("node!");
                }
                int x = Mercator.xFromLon100nd(lon100nd);
                int y = Mercator.yFromLat100nd(lat100nd);

                double lon = Mercator.lonFromX(x);
                double lat = Mercator.latFromY(y);

                if(Math.round(lon * 10_000_000) != lon100nd ||
                    Math.round(lat * 10_000_000) != lat100nd)
                {
                    Log.error("Failed to convert from %d,%d back to WGS-84-100nd (%d,%d) --" +
                        "Got lon,lat %f,%f instead.", x,y, lon100nd, lat100nd, lon, lat);
                }

                boolean found = false;

                for(Feature f: world.select("nwa").in(Box.atXY(x,y)))
                {
                    if(f instanceof Node node)
                    {
                        if (node.x() == x && node.y() == y)
                        {
                            found = true;
                            break;
                        }
                    }
                    else if(f instanceof Way way)
                    {
                        for(Feature node: way.nodes())
                        {
                            if (node.x() == x && node.y() == y)
                            {
                                found = true;
                                break;
                            }
                        }
                    }
                }

                if(!found)
                {
                    Log.error("GOL does not contain node at %f,%f: node/%d (%d tags)",
                        lon, lat, id, tags.size());
                }
            }

            @Override protected void endBlock(Block block)
            {
                progress.progress(block.length);
            }
        }

        void check(String fileName) throws IOException
        {
            Log.debug("Checking %s against original %s", world.store().path(), ORIGINAL_OSM_PBF);
            Path pbfFile = Path.of(ORIGINAL_OSM_PBF);
            long fileSize = Files.size(pbfFile);
            progress = new ProgressReporter(fileSize, "bytes",
                "Checking", "Checked");
            read(fileName);
            progress.finished();
        }
    }

    @Test public void testAllCoordinatesPresent() throws Exception
    {
        CoordinateChecker checker = new CoordinateChecker(world);
        checker.check(ORIGINAL_OSM_PBF);
    }

    /**
     * Exhaustively test every longitude and every latitude (excluding polar)
     * to guarantee that WGS-84 to Mercator (and back) is lossless for
     * 100-nanodegree resolution.
     *
     * This test takes a long time to run:
     * - Roundtrip conversion of lat ~ 200ns
     * - Roundtrip conversion of lon ~  30ns
     * (Latitude requires trig; longitude is just a scaling op)
     */
    @Test public void testFullCoordinateSpectrum()
    {
        // takes 2 mins on dual-core
        for(int lon100nd=-1_800_000_000; lon100nd<=1_800_000_000; lon100nd++)
        {
            int x = Mercator.xFromLon100nd(lon100nd);
            double lon = Mercator.lonFromX(x);
            Assert.assertEquals(lon100nd, Math.round(lon * 10_000_000));
        }
        for(int lat100nd=-850_000_000; lat100nd<=850_000_000; lat100nd++)
        {
            if(lat100nd % 10_000_000 == 0) Log.debug("%d", lat100nd);
            int y = Mercator.yFromLat100nd(lat100nd);
            double lat = Mercator.latFromY(y);
            Assert.assertEquals(lat100nd, Math.round(lat * 10_000_000));
        }
    }
}
