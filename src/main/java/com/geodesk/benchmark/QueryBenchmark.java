/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.benchmark;

import com.clarisma.common.util.Log;
import com.geodesk.feature.Features;
import com.geodesk.feature.Tags;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * A Benchmark that measures the performance of a batch of feature queries.
 * Each item in the batch is a *context*, which can represent a bounding box,
 * a polygon, etc. depending on the implementation details of the subclass.
 *
 * @param <T> the context type: Box, Polygon, etc.
 */
public abstract class QueryBenchmark<T> extends Benchmark
{
    /**
     * The features against which this benchmark will be applied.
     * This can be the library as a whole, or a selected set.
     */
    protected final Features features;
    /**
     * The list of contexts, one for each query that will be executed by this benchmark.
     */
    private final List<T> contexts;
    /**
     * The action that is performed on each query result: obtain its tags, measure it,
     * iterate its members, etc., or simply count it.
     */
    protected final Action action;
    /**
     * The total number of features all queries selected (per run)
     */
    private long count;
    /**
     * The result (per run) of the queries -- varies by action, e.g. average length, etc.
     * We store this data to ensure that the JVM does not eliminate parts of the
     * benchmark code as "dead code".
     */
    private double result;
    private final ExecutorService executor;
    private final List<Task> tasks;

    public QueryBenchmark(String name, Features features, List<T> contexts,
        Action action, ExecutorService executor)
    {
        super(name);
        this.features = features;
        this.contexts = contexts;
        this.action = action;
        this.executor = executor;
        if(executor != null)
        {
            tasks = createBatches(Runtime.getRuntime().availableProcessors() * 4);
        }
        else
        {
            tasks = null;
        }
    }

    abstract void performSingle(T shape, Result res);

    protected void performBatch(List<T> batch, Result res)
    {
        for(T s: batch) performSingle(s, res);
    }

    public static boolean withinTolerance(double v1, double v2)
    {
        double delta = Math.abs(v1 - v2);
        if(delta < 0.000001) return true;
        return (delta < Math.max(Math.abs(v1), Math.abs(v2)) * 0.00001);
    }

    @Override protected void perform()
    {
        if(executor == null)
        {
            performSequential();
        }
        else
        {
            performParallel();
        }
    }

    private void performSequential()
    {
        Result res = new Result();
        performBatch(contexts, res);
        setResult(res);
    }

    private void setResult(Result res)
    {
        if(runsPerformed() == 0)
        {
            count = res.count;
            result = res.result;
        }
        else if(res.count != count || !withinTolerance(res.result, result))
        {
            if(res.count != count)
            {
                throw new RuntimeException(String.format(
                    "%s: Count differs from previous runs " +
                        "(%d now vs. %d then)", name(),
                    res.count, count));
            }
            throw new RuntimeException(String.format(
                "%s: Result differs from previous runs " +
                    "(%f now vs. %f then)", name(),
                res.result, result));
        }
    }

    private List<Task> createBatches(int batchCount)
    {
        List<Task> tasks = new ArrayList<>();
        int totalContexts = contexts.size();
        int perBatch = totalContexts / batchCount;
        int leftover = totalContexts - perBatch * batchCount;
        int start = 0;
        int end = start + perBatch + leftover;
        while(start < totalContexts)
        {
            tasks.add(new Task(start, end-1));
            start = end;
            end += perBatch;
        }
        return tasks;
    }

    private void performParallel()
    {
        Result res = new Result();
        try
        {
            for (Future<Result> future : executor.invokeAll(tasks))
            {
                Result r = future.get();;
                res.count += r.count;
                res.result += r.result;
            }
        }
        catch(Exception ex)
        {
            throw new RuntimeException(ex);
        }
        res.result /= tasks.size();
        setResult(res);
    }

    @Override protected void reportDetails(Appendable out) throws IOException
    {
        out.append("    queries:     ");
        out.append(Integer.toString(contexts.size()));
        out.append("\n    features:    ");
        out.append(Long.toString(count));
        out.append("\n    avg-result:  ");
        out.append(Double.toString(result / count));
        out.append("\n");
    }

    public static class Result
    {
        long count;
        double result;
    }

    public abstract static class Action
    {
        abstract void perform(Features features, Result result);
    }

    public static class CountAction extends Action
    {
        @Override void perform(Features view, Result result)
        {
            view.forEach(f ->
            {
                result.count++;
            });
        }
    }

    public static class NameAction extends Action
    {
        @Override void perform(Features view, Result result)
        {
            view.forEach(f ->
            {
                result.result += f.tag("name").length();
                result.count++;
            });
        }
    }

    public static class LengthAction extends Action
    {
        @Override void perform(Features view, Result result)
        {
            view.forEach(f ->
            {
                result.result += f.length();
                result.count++;
            });
        }
    }

    public static class TagAction extends Action
    {
        @Override void perform(Features view, Result result)
        {
            view.forEach(f ->
            {
                Tags tags = f.tags();
                while(tags.next())
                {
                    result.result += tags.key().length();
                    result.result += tags.stringValue().length();
                }
                result.count++;
            });
        }
    }

    private class Task implements Callable<Result>
    {
        private final int first;
        private final int last;

        Task(int first, int last)
        {
            this.first = first;
            this.last = last;
        }

        @Override public Result call()
        {
            Result res = new Result();
            for(int i=first; i<=last; i++)
            {
                performSingle(contexts.get(i), res);
            }
            return res;
        }
    }
}
