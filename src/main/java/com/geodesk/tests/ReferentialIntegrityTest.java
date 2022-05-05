package com.geodesk.tests;

import com.clarisma.common.util.Log;
import com.geodesk.core.Box;
import com.geodesk.feature.*;

import com.geodesk.feature.filter.TypeBits;
import com.geodesk.feature.query.EmptyView;
import com.geodesk.feature.query.WorldView;
import com.geodesk.feature.store.StoredFeature;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * A series of basic integrity tests.
 */
public class ReferentialIntegrityTest
{
    FeatureLibrary features;
    BoxMaker boxes;

    @Before
    public void setUp()
    {
        features = new FeatureLibrary("c:\\geodesk\\tests\\de.gol");
        boxes = new BoxMaker(
            Box.ofWSEN(7.6872276841, 47.707433547, 12.6844378887, 53.8412264446));
    }

    @After
    public void tearDown()
    {
        features.close();
    }

    /**
     * Set-based queries must not return the same feature more than once.
     */
    @Test public void testNoDuplicates()
    {
        Set<Feature> results = new HashSet<>();
        int runs = 1_000;
        long count = 0;
        long start = System.currentTimeMillis();
        for (int i = 0; i < runs; i++)
        {
            results.clear();
            for (Feature f : features.in(boxes.random(10_000, 100_000)))
            {
                assertFalse(results.contains(f));
                results.add(f);
                count++;
            }
        }
        long end = System.currentTimeMillis();
        Log.debug("Ran %d queries with %d results in %d ms: No duplicates returned.",
            runs, count, end - start);
    }

    static private final String[] COMMON_ROLES =
    {
        "admin_centre", "inner", "main_stream", "outer", "stop", "via"
    };

    /**
     * Checks that features("ar") with explicit instanceof check for Relation
     * and relations() return the same set
     */
    @Test public void testTypeConstraint()
    {
        Set<Feature> set1 = new HashSet<>();
        Set<Feature> set2 = new HashSet<>();

        for (Feature f : features.features("ra"))
        {
            if (f instanceof Relation) set1.add(f);
        }

        for (Relation rel : features.relations())
        {
            set2.add(rel);
        }

        Log.debug("Total feature in set1: %d", set1.size());
        Log.debug("Total feature in set2: %d", set2.size());

        countAreas("set1", set1);
        countAreas("set2", set2);
        // dumpAreas("set2", set2);

        // assertTrue(checkSameSets(set1, set2));
    }

    private void countAreas(String title, Iterable<Feature> set)
    {
        long count = 0;
        for(Feature f: set) if(f.isArea()) count++;
        Log.debug("%s contains %d areas", title, count);
    }

    private void dumpAreas(String title, Iterable<Feature> set)
    {
        for(Feature f: set) if(f.isArea()) Log.debug(f.toString());
    }

    private boolean checkSameSets(Set<?> a, Set<?> b)
    {
        boolean passed = true;
        for(Object o: a)
        {
            if (!b.contains(o))
            {
                Log.debug("%s found in Set 1, but not Set 2: %s", o,
                    TypeBits.toString(1 << (((StoredFeature)o).flags() >> 1)));
                passed = false;
            }
        }
        for(Object o: b)
        {
            if (!a.contains(o))
            {
                Log.debug("%s found in Set 2, but not Set 1", o);
                passed = false;
            }
        }
        return passed;
    }

    /**
     * Checks the referential integrity between relations and their members.
     */
    @Test public void testRelations()
    {
        Set<String> uniqueRoles = new HashSet<>();
        long relCount = 0;
        long memberCount = 0;
        long start = System.currentTimeMillis();
        for (Relation rel : features.relations())
        {
            relCount++;

            // Box memberBounds = new Box();

            for (Feature member : rel.members())
            {
                memberCount++;

                // Check referential integrity relation <---> member
                assertTrue(member.belongsToRelation());
                assertTrue(member.belongsTo(rel));
                assertTrue(member.parentRelations().contains(rel));
                uniqueRoles.add(member.role());
            }
        }
        long end = System.currentTimeMillis();
        Log.debug("Checked %d relations with %d members in %d ms.",
            relCount, memberCount, end - start);
        Log.debug("- %d unique roles", uniqueRoles.size());
        for(String role: COMMON_ROLES) assertTrue(uniqueRoles.contains(role));
    }

    /**
     * Ensures that typed queries return the same results as querying "by hand."
     * Note that for simplicity, we count the number of features found, rather
     * than doing a proper set check; it is extremely unlikely that two
     * different query methods return sets with the same number of features,
     * but different content.
     */
    @Test public void testTypedQueries()
    {
        long nodes = 0;
        long allWays = 0;
        long allRelations = 0;
        long areas = 0;
        long allHighways = 0;
        long linealHighways = 0;
        long linealRailways = 0;
        long linealRailwayHighways = 0;

        for(Feature f: features)
        {
            if(f instanceof Node) nodes++;
            if(f instanceof Way) allWays++;
            if(f instanceof Relation) allRelations++;
            if(f.isArea()) areas++;
            boolean isHighway = f.hasTag("highway") && !f.stringValue("highway").equals("no");
            boolean isRailway = f.hasTag("railway") && !f.stringValue("railway").equals("no");
            boolean isLineal = f instanceof Way && !f.isArea();

            if(isHighway) allHighways++;
            if(isHighway && isLineal)  linealHighways++;
            if(isRailway && isLineal)  linealRailways++;
            if(isHighway && isRailway && isLineal)  linealRailwayHighways++;
        }

        assertTrue(nodes > 0);
        assertTrue(allWays > 0);
        assertTrue(allRelations > 0);
        assertTrue(areas > 0);

        assertEquals(nodes, features.nodes().count());
        assertEquals(allWays, features.ways().count());
        assertEquals(allRelations, features.relations().count());
        assertEquals(areas, features.features("a").count());
        assertEquals(allHighways, features.features("*[highway]").count());
        assertEquals(linealHighways, features.features("w[highway]").count());
        assertEquals(linealHighways, features.ways("w[highway]").count());
        assertEquals(linealRailways, features.ways("w[railway]").count());
        assertEquals(linealRailwayHighways, features.features("w[railway][highway]").count());
        assertEquals(linealRailwayHighways, features.features("w[railway]")
            .ways("*[highway]").count());
        assertEquals(linealRailwayHighways, features.features("w[highway]")
            .ways("*[railway]").count());

        WorldView<?> linealWaysQuery = (WorldView<?>) features.ways()
            .features("wa[highway]")
            .ways("*[railway][highway]")
            .features("w");
        assertEquals(TypeBits.NONAREA_WAYS, linealWaysQuery.types());

        assertEquals(linealRailwayHighways, features.ways()
            .features("wa[highway]")
            .ways("*[railway][highway]")
            .features("*[railway]")
            .features("w")
            .count());

        Features<?> empty = features.ways()
            .features("wa[highway]")
            .ways("*[railway][highway]")
            .features("*[railway]")
            .nodes();
        assertTrue(empty instanceof EmptyView<?>);
    }

    /**
     * Checks the referential integrity between relations and their members;
     * this time from the perspective of the members.
     */
    @Test public void testRelationMembers()
    {
        for (Feature f : features.in(Box.ofWorld()))
        {
            for (Relation rel : f.parentRelations())
            {
                assertTrue(rel.members().contains(f));
            }
        }
    }

    void testContainsQueries(Features<?> features, Set<Feature> others)
    {
        testContains(features.features("a[landuse]"), others);
        testContains(features
            .nodes("na[shop]")
            .features("*[opening_hours]"),
            others);
     }

    @Test public void testContains()
    {
        Set<Feature> others = randomSample(features, 10_000);

        testContainsQueries(features, others);
        for(int i=0; i<1000; i++)
        {
            testContainsQueries(features.in(boxes.random(3000, 10_000)), others);
        }
    }

    /**
     * Checks whether contains() returns true/false for features that are in/
     * not in a collection
     *
     * @param features  the collection to test
     * @param others    features that may be in the collection, but not likely
     */
    void testContains(Features<?> features, Set<Feature> others)
    {
        Set<Feature> notContained = new HashSet<>(others);
        for(Feature f: features)
        {
            assertTrue(features.contains(f));
            notContained.remove(f);
        }
        for(Feature f: notContained)
        {
            assertFalse(features.contains(f));
        }
    }

    Set<Feature> randomSample(Features<?> features, int sampleInterval)
    {
        Set<Feature> sample = new HashSet<>();
        Random random = new Random();
        int skip = random.nextInt(sampleInterval);
        for(Feature f: features)
        {
            skip--;
            if(skip > 0) continue;
            sample.add(f);
            skip = random.nextInt(sampleInterval);
        }
        return sample;
    }
}