/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.benchmark;

import com.geodesk.geom.Box;
import com.geodesk.feature.FeatureLibrary;
import com.geodesk.feature.Features;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FeatureBenchmarkRunner extends BenchmarkRunner
{
    protected FeatureLibrary world;
    protected BenchmarkPlan plan;
    protected final Map<String,QueryBenchmark.Action> actions = new HashMap<>();
    protected ExecutorService executor;

    public FeatureBenchmarkRunner(Path rootPath, int runs) throws IOException
    {
        super(rootPath, runs);
        actions.put("count", new QueryBenchmark.CountAction());
        actions.put("name", new QueryBenchmark.NameAction());
        actions.put("length", new QueryBenchmark.LengthAction());
        actions.put("tags", new QueryBenchmark.TagAction());
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
            // TODO: configure
    }


    @Override protected void setUp() throws IOException
    {
        world = new FeatureLibrary("d:\\geodesk\\tests\\de-v1.gol");
        try
        {
            plan = new BenchmarkPlan(rootPath, "benchmarks/new-benchmark.fab", world);
        }
        catch (ParseException ex)
        {
            throw new IOException("Parsing failed: " + ex.getMessage(), ex);
        }
    }

    @Override protected void tearDown()
    {
        world.close();
        executor.shutdown();
    }

    private Benchmark createBenchmark(String name)
    {
        String[] parts = name.split("-");
        String query = plan.queries.get(parts[0]);
        if(query == null) throw new RuntimeException("Unknown query: " + parts[0]);
        Features features = world.select(query);
        QueryBenchmark.Action action = actions.get(parts[1]);
        if(action == null) throw new RuntimeException("Unknown action: " + parts[1]);
        String spatial = parts[2];
        String shapes = (parts.length > 4) ? (parts[3] + "-" + parts[4]) : parts[3];
        switch(spatial)
        {
        case "bbox":
            return new BoxQueryBenchmark(name, features, getBoxes(shapes), action, executor);
        case "intersects":
            return new IntersectsQueryBenchmark(name, features, getPolygons(shapes), action, executor);
        case "within":
            return new WithinQueryBenchmark(name, features, getPolygons(shapes), action, executor);
        case "enclosing":
            return new EnclosingQueryBenchmark(name, features, getCircles(shapes), action, executor);
        default:
            throw new RuntimeException("Unknown spatial relation: " + spatial);
        }
    }

    private List<Box> getBoxes(String name)
    {
        List<Box> list = plan.boxes.get(name);
        if(list == null) throw new RuntimeException("Unknown boxes: " + name);
        return list;
    }

    private List<Circle> getCircles(String name)
    {
        List<Circle> list = plan.circles.get(name);
        if(list == null) throw new RuntimeException("Unknown circles: " + name);
        return list;
    }

    private List<Geometry> getPolygons(String name)
    {
        List<Geometry> list = plan.polygons.get(name);
        if(list == null) throw new RuntimeException("Unknown polygons: " + name);
        return list;
    }

    @Override protected List<Benchmark> createBenchmarks()
    {
        List<Benchmark> list = new ArrayList<>();
        for(String name: plan.benchmarks) list.add(createBenchmark(name));
        Collections.sort(list);
        return list;
    }

    public static void main(String[] args) throws Exception
    {
        BenchmarkRunner runner = new FeatureBenchmarkRunner(Path.of("d:\\geodesk\\benchmarks-new"), 5);
        runner.run();
    }
}
