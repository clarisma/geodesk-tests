package com.geodesk.feature.store;

import com.geodesk.benchmark.RandomBoxes;
import com.geodesk.feature.FeatureId;
import com.geodesk.core.MercatorToWSG84;
import com.geodesk.feature.Feature;
import com.geodesk.feature.FeatureLibrary;
import com.geodesk.feature.Features;
import com.geodesk.geom.Bounds;
import com.geodesk.map.SlippyMap;
import org.eclipse.collections.api.map.primitive.LongObjectMap;
import org.eclipse.collections.api.map.primitive.MutableLongObjectMap;
import org.eclipse.collections.api.set.primitive.MutableLongSet;
import org.eclipse.collections.impl.map.mutable.primitive.LongObjectHashMap;
import org.eclipse.collections.impl.set.mutable.primitive.LongHashSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

public class QueryDiscrepancyTest
{
    Path rootPath;
    FeatureLibrary gol1;
    FeatureLibrary gol2;
    Bounds[] boxes;
    String query;

    @Before
    public void setUp() throws Exception
    {
        rootPath = Path.of("/home/md/geodesk");
        gol1 = new FeatureLibrary("tests/de.gol");
        gol2 = new FeatureLibrary("tests/de3.gol");
        boxes = RandomBoxes.load(rootPath.resolve("benchmarks/germany-l.boxes")).boxes();
        query = "a[boundary=administrative]";
    }

    private void testNoDuplicates(Features<?> store, String query, Bounds bbox)
    {
        MutableLongSet ids = new LongHashSet();
        for(Feature f: store.features(query).in(bbox))
        {
            if(!ids.add(FeatureId.of(f.type(), f.id())))
            {
                Assert.fail(String.format("Duplicate result: %s", f));
            }
        }
    }

    private void testNoDuplicates(Features<?> store, String query, Bounds[] boxes)
    {
        for(Bounds b: boxes) testNoDuplicates(store, query, b);
    }


    @Test
    public void testNoDuplicates()
    {
        testNoDuplicates(gol1, query, boxes);
        testNoDuplicates(gol2, query, boxes);
    }

    private LongObjectMap<Feature> getFeatures(Features<?> store, String query, Bounds bbox)
    {
        MutableLongObjectMap<Feature> features = new LongObjectHashMap<>();
        for(Feature f: store.features(query).in(bbox))
        {
            features.put(FeatureId.of(f.type(), f.id()), f);
        }
        return features;
    }

    private boolean checkInBoth(
        LongObjectMap<Feature> set1,
        LongObjectMap<Feature> set2,
        String label1, String label2,
        Bounds bbox)
    {
        AtomicBoolean passed = new AtomicBoolean(true);
        set1.forEachKeyValue((fid, f) ->
        {
            if(!set2.containsKey(fid))
            {
                String msg = String.format("%s in %s but not in %s",
                    FeatureId.toString(fid), label1, label2);
                // Assert.fail(msg);
                System.err.println(msg);
                passed.set(false);

                SlippyMap map = new SlippyMap();
                map.setProjection(new MercatorToWSG84());
                map.addRectangle(bbox);
                map.addFeature(f);
                try
                {
                    map.writeHtml(rootPath.resolve(
                            String.format("tests/maps/discrepancy-%d-%d.html",
                            set1.hashCode(), fid))
                        .toFile());
                }
                catch(Exception ex)
                {
                    throw new RuntimeException(ex.getMessage(), ex);
                }
            }
        });
        return passed.get();
    }

    private void testResultsInBoth(String query, Bounds bbox)
    {
        LongObjectMap<Feature> f1 = getFeatures(gol1, query, bbox);
        LongObjectMap<Feature> f2 = getFeatures(gol2, query, bbox);
        checkInBoth(f1, f2, "set1", "set2", bbox);
        checkInBoth(f2, f1, "set2", "set1", bbox);
    }

    @Test
    public void testResultsInBoth()
    {
        for(Bounds b: boxes) testResultsInBoth(query, b);
    }


}
