package com.geodesk.tests;

import com.geodesk.feature.Feature;
import com.geodesk.feature.FeatureLibrary;
import com.geodesk.util.MapMaker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.geodesk.feature.Filters.*;

public class MapMakerTest
{
    FeatureLibrary features;

    @Before public void setUp()
    {
        features = new FeatureLibrary("d:\\geodesk\\tests\\w-good.gol");
    }

    @After public void tearDown()
    {
        features.close();
    }

    @Test public void testFrance() throws IOException
    {
        Feature france = features.select("a[boundary=administrative][admin_level=2][name=France]").first();
        MapMaker map = new MapMaker();

        map.add(features
            .select("a[boundary=administrative][admin_level=6]")
            .select(within(france)));
        map.save(TestSettings.outputPath().resolve("france.html").toString());
    }
}
