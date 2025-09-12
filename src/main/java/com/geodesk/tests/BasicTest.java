package com.geodesk.tests;

import com.geodesk.feature.Feature;
import com.geodesk.feature.FeatureLibrary;
import com.geodesk.feature.Features;
import com.geodesk.feature.Node;
import com.geodesk.util.MapMaker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class BasicTest
{
    FeatureLibrary features;

    @Before
    public void setUp()
    {
        features = new FeatureLibrary("c:\\geodesk\\tests\\liguria-libero4.gol");
    }

    @After
    public void tearDown()
    {
        features.close();
    }

    @Test
    public void testCounts()
    {
        System.out.format("%d total highways\n",
            features.select("w[highway]").count());
        System.out.format("%d total features\n", features.count());
        System.out.format("%d total nodes\n",
            features.select("n").count());
    }

    @Test
    public void testWayNodes()
    {
        long count = 0;
        for (var street : features.select("w[highway]"))
        {
            for(var node : street.nodes("n"))
            {
                System.out.format("%s: %s\n", street.toString(), node.toString());
                count++;
            }
        }
        System.out.format("%d waynodes", count);
    }
}
