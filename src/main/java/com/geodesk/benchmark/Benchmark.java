/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.benchmark;

import com.clarisma.common.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An individual Benchmark. Benchmarks are intended to be run multiple times.
 * Outputs reflect the best, worst, average and median execution times (in
 * milliseconds).
 *
 */
public abstract class Benchmark implements Runnable, Comparable<Benchmark>
{
    protected final String name;
    private final List<Long> timings = new ArrayList<>();

    protected Benchmark(String name)
    {
        this.name = name;
    }

    protected abstract void perform();

    /**
     * Subclasses can override this method to report additional information
     * about the benchmark, such as number of tasks performed.
     * (This information must be the same for each individual run).
     * Output must be in key-value form.
     *
     * @param out
     * @throws IOException
     */
    protected void reportDetails(Appendable out) throws IOException
    {
        // do nothing
    }

    /**
     * Runs the benchmark and measures the time to perform its task(s).
     */
    @Override public void run()
    {
        long start = System.nanoTime();
        perform();
        long elapsed = System.nanoTime() - start;
        timings.add((elapsed + 500_000) / 1_000_000);
    }

    public long mostRecentTiming()
    {
        return timings.get(timings.size()-1);
    }

    public String name()
    {
        return name;
    }

    public int runsPerformed()
    {
        return timings.size();
    }

    public void report(Appendable out) throws IOException
    {
        out.append(name);
        out.append(":\n");

        Collections.sort(timings);

        int runs = timings.size();
        long best = timings.get(0);
        long worst = timings.get(runs-1);
        long total = 0;
        for(long t: timings) total += t;
        long average = total / runs;
        long median = timings.get(runs / 2);
        if(runs % 1 == 0) median = (median + timings.get(runs / 2 - 1)) / 2;

        reportDetails(out);
        out.append("    best-time:   ");
        out.append(Long.toString(best));
        out.append("\n    worst-time:  ");
        out.append(Long.toString(worst));
        out.append("\n    avg-time:    ");
        out.append(Long.toString(average));
        out.append("\n    median-time: ");
        out.append(Long.toString(median));
        out.append("\n    total-time: ");
        out.append(Long.toString(total));
        out.append("\n");
    }

    @Override public int compareTo(Benchmark other)
    {
        return name.compareTo(other.name);
    }

    public void run(int runs)
    {
        for(int run=0; run<runs; run++)
        {
            run();
            Log.debug("%s - run #%d - %,d ms", name, run + 1, mostRecentTiming());
        }
        try
        {
            report(System.out);
        }
        catch(IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
