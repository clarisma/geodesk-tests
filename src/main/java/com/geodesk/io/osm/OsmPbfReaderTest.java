/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.io.osm;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;


public class OsmPbfReaderTest
{
    @Test
    public void testReader() throws IOException
    {
        OsmPbfReader reader = new OsmPbfReader();
        reader.read("c:\\geodesk\\mapdata\\de-2022-11-28.osm.pbf");
        // reader.read("/home/md/geodesk/mapdata/de-2021-01-29.osm.pbf");
        // reader.read("/home/md/geodesk/mapdata/planet.osm.pbf");
    }
}
