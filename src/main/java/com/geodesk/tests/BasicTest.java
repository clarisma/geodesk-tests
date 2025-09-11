package com.geodesk.tests;

import com.geodesk.feature.Feature;
import com.geodesk.feature.FeatureLibrary;
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
        features = new FeatureLibrary("c:\\geodesk\\tests\\fr-libero6.gol");
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
}
