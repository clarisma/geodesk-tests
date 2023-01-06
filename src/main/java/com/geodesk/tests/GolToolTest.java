/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.tests;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GolToolTest
{
    final Shell shell = new Shell();
    final String rootPath = "c:\\geodesk\\";
    final String mapDataPath = rootPath + "mapdata\\";


    public static class Shell
    {
        private boolean isWindows;

        public Shell()
        {
            isWindows = System.getProperty("os.name").regionMatches(true, 0,
                "windows",0,7);
        }

        Result run(String... args) throws IOException, InterruptedException, ExecutionException
        {
            Runtime rt = Runtime.getRuntime();
            String[] cmdArgs = new String[args.length + 2];
            System.arraycopy(args, 0, cmdArgs, 2, args.length);

            if(isWindows)
            {
                cmdArgs[0] = "cmd.exe";
                cmdArgs[1] = "/c";
            }
            else
            {
                cmdArgs[0] = "/bin/sh";
                cmdArgs[1] = "-c";
            }

            Process proc = rt.exec(cmdArgs);

            ResultReader reader = new ResultReader(proc.getInputStream());
            Future<?> future = Executors.newSingleThreadExecutor().submit(reader);
            Result result = reader.result;
            result.exitCode = proc.waitFor();
            future.get();   // wait for stream reading to complete
            return result;
        }

        private static class ResultReader extends BufferedReader implements Runnable
        {
            private final Result result = new Result();

            public ResultReader(InputStream in)
            {
                super(new InputStreamReader(in));
            }

            @Override public void run()
            {
                lines().forEach(line -> result.lines.add(line));
            }
        }

        public static class Result
        {
            public final List<String> lines = new ArrayList<>();
            public int exitCode;

            public int exitCode()
            {
                return exitCode;
            }
        }
    }

    long testQuery(String gol, String query) throws Exception
    {
        Shell.Result res = shell.run("gol", "query", gol, query, "-f=count");
        Assert.assertEquals(0, res.exitCode());
        return Long.parseLong(res.lines.get(0));
    }

    @Test public void testBuild() throws Exception
    {
        Assert.assertEquals(0, shell.run(
            "gol", "build", "monaco", mapDataPath + "monaco.osm.pbf")
            .exitCode());
        Path gol = Path.of("monaco.gol");
        Assert.assertTrue(Files.exists(gol));

        long featureCount = testQuery("monaco", "\"*\"");
        Assert.assertTrue(featureCount > 7000);
        Assert.assertTrue(featureCount < 100000);

        featureCount = testQuery("monaco", "\"na[leisure=marina][name='Port Hercule*']\"");
        Assert.assertEquals(1, featureCount);

        Files.delete(gol);
    }

    @Test public void testGolTool() throws Exception
    {

    }
}
