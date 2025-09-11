/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.tests;

import java.nio.file.Path;

public class TestSettings
{
    public static String golFile()
    {
        // return "c:\\geodesk\\tests\\w2.gol";
        // return "c:\\geodesk\\tests\\de-good2.gol";
        return "c:\\geodesk\\tests\\de-libero.gol";
        // return "/home/md/geodesk/tests/w2.gol";
        // return "c:\\geodesk\\tests\\de-from-world.gol";
        // return "c:\\geodesk\\tests\\de6.gol";
        // return "c:\\geodesk\\tests\\germany.gol";
        // return "c:\\geodesk\\tests\\de.gol";
    }
    public static String tileURL() { return null; }

    public static Path outputPath()
    {
        return Path.of("c:\\geodesk\\debug\\");
    }
}
