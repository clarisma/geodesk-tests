/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.benchmark;

import com.clarisma.common.fab.FabReader;
import com.clarisma.common.util.Log;
import com.geodesk.core.Box;
import com.geodesk.feature.Feature;
import com.geodesk.feature.Features;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class BenchmarkPlan
{
    protected final Path rootPath;
    protected final Path shapePath;
    protected final List<String> benchmarks = new ArrayList<>();
    protected final Map<String, BoxSpecs> boxSpecs = new HashMap<>();
    protected final Map<String, CircleSpecs> circleSpecs = new HashMap<>();
    protected final Map<String, String> polygonSpecs = new HashMap<>();
    protected final Map<String, List<Box>> boxes = new HashMap<>();
    protected final Map<String, List<Circle>> circles = new HashMap<>();
    protected final Map<String, List<Geometry>> polygons = new HashMap<>();
    protected final Map<String, String> queries = new HashMap<>();
    protected final ShapeMaker shapeMaker;

    public BenchmarkPlan(Path rootPath, String settings, Features<Feature> features) throws IOException, ParseException
    {
        this.rootPath = rootPath;
        shapePath = rootPath.resolve("shapes");
        if(!Files.exists(shapePath)) Files.createDirectories(shapePath);
        shapeMaker = new ShapeMaker(features);
        new Reader().read(getClass().getClassLoader().getResourceAsStream(settings));
        makeBoxes();
        makeCircles();
        makePolygons();
    }

    private static class BoxSpecs
    {
        String seedQuery;
        int count;
        int minMeters;
        int maxMeters;
    }

    private static class CircleSpecs
    {
        String seedQuery;
        int count;
        int minMeters;
        int maxMeters;
    }

    private class Reader extends FabReader
    {
        BiConsumer<String, String> kvConsumer;

        void boxSpecs(String k, String v)
        {
            String[] va = v.split("[,\\s]\\s*");
            BoxSpecs b = new BoxSpecs();
            b.seedQuery = va[0];
            b.count = Integer.parseInt(va[1]);
            b.minMeters = Integer.parseInt(va[2]);
            b.maxMeters = Integer.parseInt(va[3]);
            boxSpecs.put(k, b);
        }

        void circleSpecs(String k, String v)
        {
            String[] va = v.split("[,\\s]\\s*");
            CircleSpecs b = new CircleSpecs();
            b.seedQuery = va[0];
            b.count = Integer.parseInt(va[1]);
            b.minMeters = Integer.parseInt(va[2]);
            b.maxMeters = Integer.parseInt(va[3]);
            circleSpecs.put(k, b);
        }

        void polygonSpecs(String k, String v)
        {
            polygonSpecs.put(k,v);
        }

        void queries(String k, String v)
        {
            queries.put(k, v);
        }

        @Override protected void beginKey(String key)
        {
            switch (key)
            {
            case "boxes":
                kvConsumer = this::boxSpecs;
                break;
            case "circles":
                kvConsumer = this::circleSpecs;
                break;
            case "polygons":
                kvConsumer = this::polygonSpecs;
                break;
            case "queries":
                kvConsumer = this::queries;
                break;
            default:
                Log.warn("Skipping unknown section: %s", key);
            }
        }

        protected void keyValue(String key, String value)
        {
            if (key.equals("benchmarks"))
            {
                for (String s : value.split("[,\\s]\\s*"))
                {
                    benchmarks.add(s);
                }
                return;
            }
            if (kvConsumer != null) kvConsumer.accept(key, value);
        }

        protected void endKey()
        {
            kvConsumer = null;
        }
    }

    private void makeBoxes() throws IOException
    {
        for (Map.Entry<String, BoxSpecs> entry : boxSpecs.entrySet())
        {
            String name = entry.getKey();
            BoxSpecs specs = entry.getValue();
            String query = queries.get(specs.seedQuery);
            if(query == null) throw new RuntimeException("Unknown query: " + specs.seedQuery);
            Log.debug("Preparing boxes: %s", name);
            List<Box> boxList = shapeMaker.loadOrCreateBoxes(
                shapePath.resolve("boxes-" + name + ".bin"), specs.count,
                query, 500, specs.minMeters, specs.maxMeters);
            boxes.put(name, boxList);
        }
    }

    private void makeCircles() throws IOException
    {
        for (Map.Entry<String, CircleSpecs> entry : circleSpecs.entrySet())
        {
            String name = entry.getKey();
            CircleSpecs specs = entry.getValue();
            String query = queries.get(specs.seedQuery);
            if(query == null) throw new RuntimeException("Unknown query: " + specs.seedQuery);
            Log.debug("Preparing circles: %s", name);
            List<Circle> circleList = shapeMaker.loadOrCreateCircles(
                shapePath.resolve("circles-" + name + ".bin"), specs.count,
                query, 500, specs.minMeters, specs.maxMeters);
            circles.put(name, circleList);
        }
    }

    private void makePolygons() throws IOException, ParseException
    {
        for (Map.Entry<String, String> e : polygonSpecs.entrySet())
        {
            String name = e.getKey();
            Log.debug("Preparing polygons: %s", name);
            polygons.put(name, shapeMaker.loadOrCreatePolygons(
                shapePath.resolve("polygons-" + name + ".txt"), e.getValue()));
        }
    }
}