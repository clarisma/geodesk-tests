/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.benchmark;

import com.clarisma.common.util.Log;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * A class that runs a series of benchmarks.
 * Benchmark execution follows a *script*, which indicates the order in
 * which benchmarks are executed. The BenchmarkRunner can load an exisiting
 * script or create a new one with a random execution order.
 *
 *
 *
 * count-restaurants-bbox-s
 * count-buildings-intersect-state
 */
public abstract class BenchmarkRunner
{
    protected final Random random = new Random();
    protected final Path rootPath;
    protected final int runs;
    private List<Benchmark> benchmarks;
    private final Map<String,Benchmark> benchmarksByName = new HashMap<>();
    private List<Benchmark> order;

    public BenchmarkRunner(Path rootPath, int runs) throws IOException
    {
        this.rootPath = rootPath;
        if(!Files.exists(rootPath)) Files.createDirectories(rootPath);
        this.runs = runs;
    }

    protected void setUp() throws IOException
    {
        // do nothing
    }

    protected void tearDown()
    {
        // do nothing
    }

    protected abstract List<Benchmark> createBenchmarks();

    public void run() throws IOException
    {
        setUp();
        try
        {
            benchmarks = createBenchmarks();
            for (Benchmark bm : benchmarks) benchmarksByName.put(bm.name(), bm);
            order = loadOrCreateScript(runs);
            int count = 0;
            for (Benchmark bm : order)
            {
                count++;
                Log.debug("%d/%d: %s", count, order.size(), bm.name());
                bm.run();
            }
            saveResults();
        }
        finally
        {
            tearDown();
        }
    }

    private void saveResults() throws IOException
    {
        Path path = rootPath.resolve("results.txt");
        PrintStream out = new PrintStream(new FileOutputStream(path.toFile()));
        long total = 0;
        long best = 0;
        long worst = 0;
        long average = 0;
        long median = 0;
        for(Benchmark bm: benchmarks)
        {
            bm.report(out);
            out.println();
            total += bm.total;
            best += bm.best;
            worst += bm.worst;
            average += bm.average;
            median += bm.median;
        }

        out.append("all:\n");
        out.append("    best-time:   ");
        out.append(Long.toString(best));
        out.append("\n    worst-time:  ");
        out.append(Long.toString(worst));
        out.append("\n    avg-time:    ");
        out.append(Long.toString(average));
        out.append("\n    median-time: ");
        out.append(Long.toString(median));
        out.append("\n    total-time:  ");
        out.append(Long.toString(total));
        out.append("\n");

        out.close();
    }

    private List<Benchmark> readScript(Path path) throws IOException
    {
        List<String> script = Files.readAllLines(path);
        List<Benchmark> list = new ArrayList<>(script.size());
        for(String name : script)
        {
            Benchmark bm = benchmarksByName.get(name);
            if(bm == null)
            {
                throw new RuntimeException("Unknown benchmark: "+ name);
            }
            list.add(bm);
        }
        return list;
    }

    private void writeScript(Path path, List<Benchmark> list) throws IOException
    {
        List<String> script = new ArrayList<>(list.size());
        for (Benchmark bm : list) script.add(bm.name());
        Files.write(path, script);
    }

    private List<Benchmark> createScript(int runs)
    {
        List<Benchmark> script = new ArrayList<>();
        for(Benchmark bm: benchmarks)
        {
            for(int i=0; i<runs; i++) script.add(bm);
        }
        Collections.shuffle(script, random);
        return script;
    }

    private List<Benchmark> loadOrCreateScript(int runs) throws IOException
    {
        Path scriptPath = rootPath.resolve("script.txt");
        if(Files.exists(scriptPath)) return readScript(scriptPath);
        List<Benchmark> script = createScript(runs);
        writeScript(scriptPath, script);
        return script;
    }

}
