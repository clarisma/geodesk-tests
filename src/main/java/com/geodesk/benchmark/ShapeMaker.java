/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.benchmark;

import com.clarisma.common.util.Bytes;
import com.clarisma.common.util.Log;
import com.geodesk.benchmark_old.RandomBoxes;
import com.geodesk.core.Box;
import com.geodesk.core.Mercator;
import com.geodesk.feature.Feature;
import com.geodesk.feature.Features;
import com.geodesk.geom.Bounds;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

public class ShapeMaker
{
    protected final Features<Feature> features;
    protected final Random random = new Random();

    public ShapeMaker(Features<Feature> features)
    {
        this.features = features;
    }

    public List<Box> randomBoxes(int count, List<Feature> seeds,
        double maxOffset, double minMeters, double maxMeters)
    {
        Log.debug("  Creating %,d random boxes (%,dm - %,dm wide/tall)", count,
            (int)minMeters, (int)maxMeters);
        List<Box> boxes = new ArrayList<>(count);
        int i = 0;
        while (boxes.size() < count)
        {
            Feature seed = seeds.get(i);
            int x = seed.x();
            int y = seed.y();
            double delta = Mercator.deltaFromMeters(maxOffset, y);
            x += random.nextDouble(-delta, delta);
            y += random.nextDouble(-delta, delta);
            int minExtent = (int) Mercator.deltaFromMeters(minMeters, y);
            int maxExtent = (int) Mercator.deltaFromMeters(maxMeters, y);
            int w = random.nextInt(minExtent, maxExtent);
            int h = random.nextInt(minExtent, maxExtent);
            Box box = Box.ofXYWidthHeight(x - w / 2, y - h / 2, w, h);
            boxes.add(box);
            i++;
            if (i >= seeds.size()) i = 0;
        }
        Collections.shuffle(boxes, random);
        return boxes;
    }

    public List<Circle> randomCircles(int count, List<Feature> seeds,
        double maxOffset, int minRadiusMeters, int maxRadiusMeters)
    {
        Log.debug("  Creating %,d random circles (%,dm - %,dm radius)", count,
            (int)minRadiusMeters, (int)maxRadiusMeters);
        List<Circle> circles = new ArrayList<>(count);
        int i = 0;
        while (circles.size() < count)
        {
            Feature seed = seeds.get(i);
            int x = seed.x();
            int y = seed.y();
            double delta = Mercator.deltaFromMeters(maxOffset, y);
            x += random.nextDouble(-delta, delta);
            y += random.nextDouble(-delta, delta);
            int radius = (minRadiusMeters == maxRadiusMeters) ? minRadiusMeters :
                random.nextInt(minRadiusMeters, maxRadiusMeters);
            circles.add(new Circle(x,y,radius));
            i++;
            if (i >= seeds.size()) i = 0;
        }
        Collections.shuffle(circles, random);
        return circles;
    }

    public List<Geometry> randomPolygons(List<Feature> seeds)
    {
        List<Geometry> geoms = new ArrayList<>();
        for (Feature f : seeds)
        {
            if (!f.isArea()) continue;
            Geometry g = f.toGeometry();
            if (!g.isValid()) continue;
            geoms.add(g);
        }
        return geoms;
    }

    private static List<Box> readBoxes(Path path) throws IOException
    {
        Log.debug("  Reading: %s", path);
        byte[] ba = Files.readAllBytes(path);
        assert ba.length % 16 == 0;
        int count = ba.length / 16;
        List<Box> boxes = new ArrayList<>(count);
        for (int p = 0; p < ba.length; p+=16)
        {
            boxes.add(new Box(
                Bytes.getInt(ba, p),
                Bytes.getInt(ba, p+4),
                Bytes.getInt(ba, p+8),
                Bytes.getInt(ba, p+12)));
        }
        return boxes;
    }

    private static void writeBoxes(Path path, Collection<Box> boxes) throws IOException
    {
        byte[] ba = new byte[16 * boxes.size()];
        int p = 0;
        for (Box b : boxes)
        {
            Bytes.putInt(ba, p, b.minX());
            Bytes.putInt(ba, p+4, b.minY());
            Bytes.putInt(ba, p+8, b.maxX());
            Bytes.putInt(ba, p+12, b.maxY());
            p += 16;
        }
        Files.write(path, ba);
    }

    private static List<Circle> readCircles(Path path) throws IOException
    {
        Log.debug("  Reading: %s", path);
        FileInputStream fin = new FileInputStream(path.toFile());
        DataInputStream in = new DataInputStream(fin);
        int count = in.readInt();
        List<Circle> circles = new ArrayList<>(count);
        for (int i = 0; i < count; i++)
        {
            circles.add(new Circle(in.readInt(), in.readInt(), in.readInt()));
        }
        in.close();
        fin.close();
        return circles;
    }

    private static void writeCircles(Path path, Collection<Circle> circles) throws IOException
    {
        FileOutputStream fout = new FileOutputStream(path.toFile());
        DataOutputStream out = new DataOutputStream(fout);
        out.writeInt(circles.size());
        for (Circle c : circles)
        {
            out.writeInt(c.x);
            out.writeInt(c.y);
            out.writeInt(c.radius);
        }
        out.flush();
        fout.flush();
        out.close();
        fout.close();
    }

    private static List<Geometry> readPolygons(Path path) throws IOException, ParseException
    {
        Log.debug("  Reading: %s", path);
        WKTReader reader = new WKTReader();
        List<String> lines = Files.readAllLines(path);
        List<Geometry> geoms = new ArrayList<>(lines.size());
        for(String line: lines)
        {
            geoms.add(reader.read(line));
        }
        return geoms;
    }

    private static void writePolygons(Path path, Collection<Geometry> geoms) throws IOException
    {
        WKTWriter writer = new WKTWriter();
        Writer out = new PrintWriter(path.toFile());
        for(Geometry g: geoms)
        {
            writer.write(g, out);
            out.write("\n");
        }
        out.close();
    }

    public List<Box> loadOrCreateBoxes(Path path, int count, String seedQuery,
        double maxOffset, double minMeters, double maxMeters) throws IOException
    {
        if(Files.exists(path)) return readBoxes(path);
        List<Feature> seeds = (List<Feature>)features.select(seedQuery).toList();
        Collections.shuffle(seeds, random);
        List<Box> boxes = randomBoxes(count, seeds, maxOffset, minMeters, maxMeters);
        writeBoxes(path, boxes);
        return boxes;
    }

    public List<Circle> loadOrCreateCircles(Path path, int count, String seedQuery,
        double maxOffset, int minMeters, int maxMeters) throws IOException
    {
        if(Files.exists(path)) return readCircles(path);
        List<Feature> seeds = (List<Feature>)features.select(seedQuery).toList();
        Collections.shuffle(seeds, random);
        List<Circle> circles = randomCircles(count, seeds, maxOffset, minMeters, maxMeters);
        writeCircles(path, circles);
        return circles;
    }

    public List<Geometry> loadOrCreatePolygons(Path path, String seedQuery) throws IOException, ParseException
    {
        if(Files.exists(path)) return readPolygons(path);
        List<Feature> seeds = (List<Feature>)features.select(seedQuery).toList();
        Collections.shuffle(seeds, random);
        List<Geometry> polygons = new ArrayList<>(seeds.size());
        for(Feature f: seeds)
        {
            Geometry g = f.toGeometry();
            if(g.isValid()) polygons.add(g);
        }
        writePolygons(path, polygons);
        return polygons;
    }
}