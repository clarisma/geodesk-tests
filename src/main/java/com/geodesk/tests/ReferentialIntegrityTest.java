package com.geodesk.tests;

import com.clarisma.common.util.Log;
import com.geodesk.core.Box;
import com.geodesk.feature.*;

import com.geodesk.feature.match.TypeBits;
import com.geodesk.feature.query.EmptyView;
import com.geodesk.feature.query.WorldView;
import com.geodesk.feature.store.StoredFeature;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Lineal;
import org.locationtech.jts.geom.Polygonal;

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
     * Checks that features("ar") with explicit instanceof check for Relation and relations() return the same set
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
        for (Feature f : set) if (f.isArea()) count++;
        Log.debug("%s contains %d areas", title, count);
    }

    private void dumpAreas(String title, Iterable<Feature> set)
    {
        for (Feature f : set) if (f.isArea()) Log.debug(f.toString());
    }

    private boolean checkSameSets(Set<?> a, Set<?> b)
    {
        boolean passed = true;
        for (Object o : a)
        {
            if (!b.contains(o))
            {
                Log.debug("%s found in Set 1, but not Set 2: %s", o,
                    TypeBits.toString(1 << (((StoredFeature) o).flags() >> 1)));
                passed = false;
            }
        }
        for (Object o : b)
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
        for (String role : COMMON_ROLES) assertTrue(uniqueRoles.contains(role));
    }

    /**
     * Ensures that typed queries return the same results as querying "by hand." Note that for simplicity, we count the
     * number of features found, rather than doing a proper set check; it is extremely unlikely that two different query
     * methods return sets with the same number of features, but different content.
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

        for (Feature f : features)
        {
            if (f instanceof Node) nodes++;
            if (f instanceof Way) allWays++;
            if (f instanceof Relation) allRelations++;
            if (f.isArea()) areas++;
            boolean isHighway = f.hasTag("highway") && !f.stringValue("highway").equals("no");
            boolean isRailway = f.hasTag("railway") && !f.stringValue("railway").equals("no");
            boolean isLineal = f instanceof Way && !f.isArea();

            if (isHighway) allHighways++;
            if (isHighway && isLineal) linealHighways++;
            if (isRailway && isLineal) linealRailways++;
            if (isHighway && isRailway && isLineal) linealRailwayHighways++;
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
     * Checks the referential integrity between relations and their members; this time from the perspective of the
     * members.
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
        for (int i = 0; i < 1000; i++)
        {
            testContainsQueries(features.in(boxes.random(3000, 10_000)), others);
        }
    }

    /**
     * Checks whether contains() returns true/false for features that are in/ not in a collection
     *
     * @param features the collection to test
     * @param others   features that may be in the collection, but not likely
     */
    void testContains(Features<?> features, Set<Feature> others)
    {
        Set<Feature> notContained = new HashSet<>(others);
        for (Feature f : features)
        {
            assertTrue(features.contains(f));
            notContained.remove(f);
        }
        for (Feature f : notContained)
        {
            assertFalse(features.contains(f));
        }
    }

    /**
     * Creates a set of features from the given input set. Features are picked at random
     *
     * @param features
     * @param sampleInterval
     * @return
     */
    <T extends Feature> Set<T> randomSample(Features<T> features, int sampleInterval)
    {
        Set<T> sample = new HashSet<>();
        Random random = new Random();
        int skip = random.nextInt(sampleInterval);
        for (T f : features)
        {
            skip--;
            if (skip > 0) continue;
            sample.add(f);
            skip = random.nextInt(sampleInterval);
        }
        return sample;
    }

    /**
     * This Test checks the following:
     *
     * - A way's bounding box must be the tightest bbox that includes
     *   all of its nodes
     * - If a way is an area, its first and last node must be the same
     * - An area must have at least 4 nodes; all others at least 2
     * - Coordinates of the way must match the nodes
     * - Geometry of an area must be polygonal
     * - Geometry of non-area must be lineal
     * - Nodes obtained via filters must match the results
     *   of checking nodes "manually"
     */
    @Test public void testWays()
    {
        long wayCount = 0;
        long totalNodeCount = 0;
        long totalFeatureNodeCount = 0;
        long totalHighwayNodeCount = 0;
        long totalEntranceNodeCount = 0;
        for (Way way : features.ways())
        {
            Box wayBox = way.bounds();
            Box calculatedBox = new Box();

            Node firstNode = null;
            Node lastNode = null;
            int nodeCount = 0;
            int highwayNodeCount = 0;
            int entranceNodeCount = 0;

            int[] xy = way.toXY();

            for (Node node : way.nodes())
            {
                if (firstNode == null) firstNode = node;
                lastNode = node;
                calculatedBox.expandToInclude(node.x(), node.y());
                assertEquals(xy[nodeCount * 2], node.x());
                assertEquals(xy[nodeCount * 2 + 1], node.y());
                nodeCount++;
                String v = node.stringValue("highway");
                if(!v.isEmpty() && !v.equals("no")) highwayNodeCount++;
                v = node.stringValue("entrance");
                if(!v.isEmpty() && !v.equals("no")) entranceNodeCount++;
            }

            assertEquals(nodeCount, way.nodes().count());
            assertEquals(nodeCount, xy.length / 2);
            assertEquals(calculatedBox, wayBox);

            Geometry geom = way.toGeometry();
            if (way.isArea())
            {
                assertEquals(firstNode, lastNode);
                assertTrue(nodeCount >= 4);
                assertTrue(geom instanceof Polygonal);
            }
            else
            {
                assertTrue(nodeCount >= 2);
                assertTrue(geom instanceof Lineal);
            }

            for(Node node: way.nodes("*"))
            {
                assertTrue(node.id() > 0);
                totalFeatureNodeCount++;
            }

            assertEquals(highwayNodeCount, way.nodes("*[highway]").count());
            assertEquals(entranceNodeCount, way.nodes("n[entrance]").count());

            wayCount++;
            totalNodeCount += nodeCount;
            totalHighwayNodeCount += highwayNodeCount;
            totalEntranceNodeCount += entranceNodeCount;
        }
        Log.debug("Tested %d ways:", wayCount);
        Log.debug("- %d total nodes", totalNodeCount);
        Log.debug("- %d feature nodes", totalFeatureNodeCount);
        Log.debug("- %d nodes tagged 'highway'", totalHighwayNodeCount);
        Log.debug("- %d nodes tagged 'entrance'", totalEntranceNodeCount);
    }

    /*
    @Test public void testFeatureNodes()
    {
        for (Way way : features.ways())
        {
            for (Node node : way.nodes("*"))
            {
                Log.debug("- %s: %s", node, node.stringValue("highway"));
            }
        }
    }
     */

    @Test public void testParentWays()
    {
        long nodeCount = 0;
        long parentWayCount = 0;
        long waysAtNodes = 0;
        Set<Way> sample = randomSample(features.ways(), 1_000);
        long start = System.currentTimeMillis();
        for (Way way : sample)
        {
            for (Node node : way.nodes("n"))
            {
                Features<Way> parentWays = node.parentWays();
                Assert.assertTrue(parentWays.contains(way));
                for(Way parentWay: parentWays)
                {
                    Assert.assertTrue(parentWay.nodes().contains(node));
                    parentWayCount++;
                }
                waysAtNodes += features.ways().in(node.bounds()).count();
//                long count = parentWays.count();
//                if(count > 1)
//                {
//                    Log.debug("%s appears in %d ways", node, count);
//                }
                nodeCount++;
            }
        }
        long end = System.currentTimeMillis();
        Log.debug("Checked %d ways with %d feature nodes (%d parent ways) in %d ms",
            sample.size(), nodeCount, parentWayCount, end-start);
        Log.debug("ParentWay queries consulted %d ways at node location.", waysAtNodes);
    }

    @Test public void testNodes()
    {
        long nodeCount = 0;
        long wayNodeCount = 0;
        long parentWayCount = 0;
        Node nodeWithMost = null;
        long mostParentCount = 0;
        for(Node node: features.nodes())
        {
            nodeCount++;
            long count = node.parentWays().count();
            if(count > 0)
            {
                wayNodeCount++;
                if (count > mostParentCount)
                {
                    mostParentCount = count;
                    nodeWithMost = node;
                }
                parentWayCount += count;
            }
        }
        Log.debug("Checked %d nodes: %d are way-nodes, with %d parent ways",
            nodeCount, wayNodeCount, parentWayCount);
        Log.debug("%s has %d parent ways", nodeWithMost, mostParentCount);
    }


    private void assertNotTagged(Feature f, String k)
    {
        String v = f.stringValue(k);
        assertTrue(v.isEmpty() || v.equals("no"));
        Tags tags = f.tags();
        while(tags.next())
        {
            if(tags.key().equals(k))
            {
                v = tags.stringValue();
                assertTrue(v.isEmpty() || v.equals("no"));
            }
        }
    }

    /**
     * This test gets the tags of all features and queries them in various ways:
     *
     * - Directly
     * - Through the iterator
     * - Converting the tags to a HashMap and looking up each one
     *
     * All methods of lookup should return the same results.
     */
    @Test public void testTags()
    {
        int totalFeatureCount = 0;
        int totalTagCount = 0;
        for(Feature f: features)
        {
            Tags tags = f.tags();
            int tagCount = 0;

            Map<String,Object> tagMap = tags.toMap();

            while(tags.next())
            {
                String k = tags.key();
                String v = tags.stringValue();
                /*
                if(!f.stringValue(k).equals(v))
                {
                    Log.debug("%s: %s should be %s, not %s", f, k, v, f.stringValue(k));
                }
                 */
                assertTrue(f.hasTag(k,v));
                assertTrue(f.stringValue(k).equals(v));
                assertTrue(f.hasTag(k));
                assertTrue(tagMap.containsKey(k));
                tagCount++;
            }
            assertEquals(tagCount, tags.size());
            assertEquals(tagCount, tagMap.size());

            totalFeatureCount++;
            totalTagCount += tagCount;
        }
        Log.debug("Tested %d features with %d tags", totalFeatureCount, totalTagCount);
    }


    @Test public void testSpecificWayTags()
    {
        for (Way way : features.ways("w[highway]"))
        {
            for(Node node: way.nodes(
                "n[!highway][!railway][!barrier][!entrance][!created_by]" +
                "[!traffic_sign][!crossing]"))
            {
                String v = node.stringValue("traffic_sign");
                assertNotTagged(node, "highway");
                assertNotTagged(node, "highway");
                assertNotTagged(node, "railway");
                assertNotTagged(node, "barrier");
                assertNotTagged(node, "entrance");
                assertNotTagged(node, "crossing");
                assertNotTagged(node, "created_by");
                assertNotTagged(node, "traffic_sign");
                // Log.debug("%s: %s", node, node.tags());
            }
        }
    }
}