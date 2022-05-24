package com.geodesk.tests;

import com.clarisma.common.util.Log;
import com.geodesk.core.Box;
import com.geodesk.feature.Feature;
import com.geodesk.feature.FeatureLibrary;
import com.geodesk.feature.Features;
import com.geodesk.feature.Filters;
import com.geodesk.util.MapMaker;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygonal;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedPolygon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.System.out;


public class SpatialFilterTest
{
    @Test public void testSpatial()
    {
        long start = System.currentTimeMillis();
        FeatureLibrary world = new FeatureLibrary("c:\\geodesk\\tests\\de.gol");
        Feature bavaria = world
            .features("a[boundary=administrative][admin_level=4][name:en=Bavaria]")
            .in(Box.atLonLat(12.0231, 48.3310))
            .first();
        Geometry bavariaPoly = bavaria.toGeometry();
        PreparedPolygon bavariaPrepared = new PreparedPolygon((Polygonal) bavariaPoly);

        long startQuery = System.currentTimeMillis();

        /*
        Features<?> places = world
            .features("n[place=city], n[place=town][population > 25000]");
         */
        Features<?> places = world
            .features("a[leisure=pitch][sport=soccer]");
        int count = 0;

        Set<Feature> found = new HashSet<>();

        for (Feature place : places.in(Box.of(bavariaPoly)))
        {
            // out.println(place.stringValue("name"));
            // count++;
            /*
            if(bavariaPoly.contains(place.toGeometry()))
            {
            //    out.println(place.stringValue("name"));
                count++;
            }
             */
            if (bavariaPrepared.contains(place.toGeometry()))
            {
                // out.println(place.stringValue("name"));
                found.add(place);
                count++;
            }
        }

        for (Feature place : places.in(Box.of(bavariaPoly)))
        {
            // out.println(place.stringValue("name"));
            // count++;
            /*
            if(bavariaPoly.contains(place.toGeometry()))
            {
            //    out.println(place.stringValue("name"));
                count++;
            }
             */
            if (bavariaPrepared.intersects(place.toGeometry()))
            {
                if (!found.contains(place))
                {
                    out.format("%s intersects, but not contained in\n", place);
                }
            }
        }
        long end = System.currentTimeMillis();
        out.format("Found %d features in %d ms (Total runtime %d ms)\n", count,
            end - startQuery, end - start);
    }

    @Test public void testSpatialBuildings()
    {
        long start = System.currentTimeMillis();
        FeatureLibrary world = new FeatureLibrary("c:\\geodesk\\tests\\de.gol");
        Feature bavaria = world
            .features("a[boundary=administrative][admin_level=4][name:en=Bavaria]")
            .in(Box.atLonLat(12.0231, 48.3310))
            .first();
        Geometry bavariaPoly = bavaria.toGeometry();
        PreparedPolygon bavariaPrepared = new PreparedPolygon((Polygonal) bavariaPoly);

        for (int i = 0; i < 10; i++)
        {
            long startQuery = System.currentTimeMillis();
            Features<?> places = world.features("a[building]");
            long count = 0; // places.in(Box.of(bavariaPoly)).count();

            for (Feature place : places.in(Box.of(bavariaPoly)))
            {
                Geometry candidateGeom = place.toGeometry();
                if (candidateGeom != null && bavariaPrepared.contains(candidateGeom))
                {
                    count++;
                }
            }

            long end = System.currentTimeMillis();
            out.format("Found %d features in %d ms (Total runtime %d ms)\n", count,
                end - startQuery, end - start);
        }
    }

    @Test public void testSpatialBuildingsFilter()
    {
        long start = System.currentTimeMillis();
        FeatureLibrary world = new FeatureLibrary("c:\\geodesk\\tests\\de.gol");
        Feature bavaria = world
            .features("a[boundary=administrative][admin_level=4][name:en=Bavaria]")
            .in(Box.atLonLat(12.0231, 48.3310))
            .first();
        Geometry bavariaPoly = bavaria.toGeometry();
        PreparedPolygon bavariaPrepared = new PreparedPolygon((Polygonal) bavariaPoly);

        for (int i = 0; i < 10; i++)
        {
            try
            {
                long startQuery = System.currentTimeMillis();
                Features<?> places = world.features("a[building]");
                long count = 0; // places.in(Box.of(bavariaPoly)).count();

                for (Feature place : places.select(Filters.within(bavariaPrepared)))
                {
                    count++;
                }

                long end = System.currentTimeMillis();
                out.format("Found %d features in %d ms (Total runtime %d ms)\n", count,
                    end - startQuery, end - start);
            }
            catch (Throwable ex)
            {
                Log.error("%s", ex);
                ex.printStackTrace();
            }
        }
    }

    @Test public void testSpatialBuildingsBbox()
    {
        long start = System.currentTimeMillis();
        FeatureLibrary world = new FeatureLibrary("c:\\geodesk\\tests\\de.gol");
        Feature bavaria = world
            .features("a[boundary=administrative][admin_level=4][name:en=Bavaria]")
            .in(Box.atLonLat(12.0231, 48.3310))
            .first();
        //Geometry bavariaPoly = bavaria.toGeometry();
        //PreparedPolygon bavariaPrepared = new PreparedPolygon((Polygonal)bavariaPoly);

        for (int i = 0; i < 10; i++)
        {
            long startQuery = System.currentTimeMillis();
            Features<?> places = world.features("a[building]");
            long count = places.in(bavaria.bounds()).count();

            long end = System.currentTimeMillis();
            out.format("Found %d features in %d ms (Total runtime %d ms)\n", count,
                end - startQuery, end - start);
        }
    }

    private class State
    {
        Feature feature;
        Geometry geom;
        PreparedGeometry prepared;
    }

    @Test public void testSpatialStates() throws IOException
    {
        long start = System.currentTimeMillis();
        FeatureLibrary world = new FeatureLibrary("c:\\geodesk\\tests\\de.gol");
        Feature germany = world
            .features("a[boundary=administrative][admin_level=2][name:en=Germany]")
            .in(Box.atLonLat(12.0231, 48.3310))
            .first();
        Log.debug("Fetching country geometry...");
        Geometry germanyPoly = germany.toGeometry();
        Log.debug("Preparing country geometry...");
        PreparedPolygon germanyPrepared = new PreparedPolygon((Polygonal) germanyPoly);
        Log.debug("Country geometries ready.");

        for (int i = 0; i < 1; i++)
        {
            long startQuery = System.currentTimeMillis();
            Features<?> states = world.features("a[boundary=administrative][admin_level=4][name]");

            List<State> stateList = new ArrayList<>();

            for (Feature state : states.in(Box.of(germanyPoly)))
            {
                Geometry stateGeom = state.toGeometry();
                if (stateGeom != null && germanyPrepared.contains(stateGeom))
                {
                    State s = new State();
                    s.feature = state;
                    s.geom = stateGeom;
                    stateList.add(s);
                }
            }

            long end = System.currentTimeMillis();
            out.format("Found %d German states in %d ms (Total runtime %d ms)\n",
                stateList.size(), end - startQuery, end - start);

            startQuery = System.currentTimeMillis();
            Features<?> counties = world.features("a[boundary=administrative][admin_level=6][name]");
            int countyCount = 0;
            Set<Feature> countySet = new HashSet<>();
            List<Geometry> countyGeometries = new ArrayList<>();

            for (Feature county : counties.in(Box.of(germanyPoly)))
            {
                Geometry countyGeom = county.toGeometry();
                if (countyGeom != null && germanyPrepared.contains(countyGeom))
                {
                    countyCount++;
                    countySet.add(county);
                    countyGeometries.add(countyGeom);
                }
            }

            end = System.currentTimeMillis();
            out.format("Found %d counties in Germany in %d ms\n",
                countyCount, end - startQuery);

            Log.debug("Creating GeometryCollection of counties...");
            Geometry totalCountyGeom = world.geometryFactory()
                .createGeometryCollection(countyGeometries.toArray(new Geometry[0]));
            Log.debug("Unioning the GeometryCollection...");
            totalCountyGeom = totalCountyGeom.buffer(0);
            Log.debug("Creating a map...");
            MapMaker map = new MapMaker();
            map.add(totalCountyGeom);
            map.save("c:\\geodesk\\germany-counties-total.html");
            Log.debug("Map created.");

            for (State s : stateList)
            {
                s.prepared = new PreparedPolygon((Polygonal) s.geom);
            }

            startQuery = System.currentTimeMillis();
            countyCount = 0;
            for (State s : stateList)
            {
                counties = world.features("a[boundary=administrative][admin_level=6][name]");
                for (Feature county : counties.in(Box.of(s.geom)))
                {
                    Geometry countyGeom = county.toGeometry();
                    if (countyGeom != null && s.prepared.contains(countyGeom))
                    {
                        countyCount++;
                        countySet.remove(county);
                    }
                }
            }
            end = System.currentTimeMillis();
            out.format("Found %d counties in German states in %d ms\n",
                countyCount, end - startQuery);

            for (Feature f : countySet)
            {
                Log.debug("%s was found by country query, but not be state query", f);
            }
        }
    }

    @Test public void testSpatialCityBuildings()
    {
        FeatureLibrary world = new FeatureLibrary("c:\\geodesk\\tests\\de.gol");
        for(int run=0; run<10; run++)
        {
            long start = System.currentTimeMillis();
            long cityCount = 0;
            long totalBuildingCount = 0;
            Feature cityWithMostBuildings = null;
            long mostBuildings = 0;
            for (Feature city : world
                .features("a[boundary=administrative][admin_level=8][name]"))
            {
                // Log.debug("- %s", city.stringValue("name"));
                cityCount++;
                long cityBuildingCount = world
                    .features("a[building]")
                    .select(Filters.within(city))
                    .count();
                if(cityBuildingCount > mostBuildings)
                {
                    mostBuildings = cityBuildingCount;
                    cityWithMostBuildings = city;
                    Log.debug("- %s: %s", city.stringValue("name"), cityBuildingCount);
                }
                totalBuildingCount += cityBuildingCount;
            }
            long end = System.currentTimeMillis();
            Log.debug("Found %d cities with %d buildings in %d ms",
                cityCount, totalBuildingCount, end - start);
            Log.debug("%s has most buildings (%d)",
                cityWithMostBuildings.stringValue("name"), mostBuildings);
        }
        world.close();
    }

    @Test public void testSpatialCityChurches()
    {
        FeatureLibrary world = new FeatureLibrary("c:\\geodesk\\tests\\de.gol");
        for(int run=0; run<10; run++)
        {
            long start = System.currentTimeMillis();
            long cityCount = 0;
            long totalChurchCount = 0;
            Feature cityWithMostChurches = null;
            long mostChurches = 0;
            for (Feature city : world
                .features("a[boundary=administrative][admin_level=8][name]"))
            {
                // Log.debug("- %s", city.stringValue("name"));
                cityCount++;
                long cityChurchCount = world
                    .features("na[amenity=place_of_worship][religion=christian]")
                    .select(Filters.within(city))
                    .count();
                if(cityChurchCount > mostChurches)
                {
                    mostChurches = cityChurchCount;
                    cityWithMostChurches = city;
                }
                totalChurchCount += cityChurchCount;
            }
            long end = System.currentTimeMillis();
            Log.debug("Found %d cities with %d churches in %d ms",
                cityCount, totalChurchCount, end - start);
            Log.debug("%s has most churches (%d)",
                cityWithMostChurches.stringValue("name"), mostChurches);
        }
        world.close();
    }

    @Test public void testSpatialCityChurchesBbox()
    {
        FeatureLibrary world = new FeatureLibrary("c:\\geodesk\\tests\\de.gol");
        for(int run=0; run<10; run++)
        {
            long start = System.currentTimeMillis();
            long cityCount = 0;
            long totalChurchCount = 0;
            Feature cityWithMostChurches = null;
            long mostChurches = 0;
            for (Feature city : world
                .features("a[boundary=administrative][admin_level=8][name]"))
            {
                // Log.debug("- %s", city.stringValue("name"));
                cityCount++;
                long cityChurchCount = world
                    .features("na[amenity=place_of_worship][religion=christian]")
                    .in(city.bounds())
                    .count();
                if(cityChurchCount > mostChurches)
                {
                    mostChurches = cityChurchCount;
                    cityWithMostChurches = city;
                }
                totalChurchCount += cityChurchCount;
            }
            long end = System.currentTimeMillis();
            Log.debug("Found %d cities with %d churches in %d ms",
                cityCount, totalChurchCount, end - start);
            Log.debug("%s has most churches (%d)",
                cityWithMostChurches.stringValue("name"), mostChurches);
        }
        world.close();
    }
}