/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.tests;

import com.clarisma.common.util.Log;
import com.geodesk.feature.store.StoredNode;
import com.geodesk.geom.Box;
import com.geodesk.feature.*;

import com.geodesk.feature.match.Matcher;
import com.geodesk.feature.match.RoleMatcher;
import com.geodesk.feature.match.TypeBits;
import com.geodesk.feature.query.EmptyView;
import com.geodesk.feature.query.WorldView;
import com.geodesk.feature.store.StoredFeature;
import com.geodesk.feature.store.StoredRelation;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Lineal;
import org.locationtech.jts.geom.Polygonal;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
        features = new FeatureLibrary(TestSettings.golFile());


        /*
        boxes = new BoxMaker(
            Box.ofWSEN(7.6872276841, 47.707433547, 12.6844378887, 53.8412264446));
         */
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
    @Test
    public void testNoDuplicates()
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
    @Test
    public void testTypeConstraint()
    {
        Set<Feature> set1 = new HashSet<>();
        Set<Feature> set2 = new HashSet<>();

        for (Feature f : features.select("ra"))
        {
            if (f instanceof Relation) set1.add(f);
        }

        for (Feature rel : features.relations())
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
    @Test
    public void testRelations()
    {
        Set<String> uniqueRoles = new HashSet<>();
        long relCount = 0;
        long memberCount = 0;
        long start = System.currentTimeMillis();
        for (Feature rel : features.relations())
        {
            relCount++;

            // Box memberBounds = new Box();

            for (Feature member : rel.members())
            {
                memberCount++;

                // Check referential integrity relation <---> member
                assertTrue(member.belongsToRelation());
                if (!member.belongsTo(rel))
                {
                    Assert.fail(String.format(
                        "Feature.belongsTo() false, but %s belongs to %s", member, rel));
                }
                // assertTrue(member.belongsTo(rel));
                assertTrue(member.parents().relations().contains(rel));
                uniqueRoles.add(member.role());
            }
        }
        long end = System.currentTimeMillis();
        Log.debug("Checked %d relations with %d members in %d ms.",
            relCount, memberCount, end - start);
        Log.debug("- %d unique roles", uniqueRoles.size());
        for (String role : COMMON_ROLES) assertTrue(uniqueRoles.contains(role));
    }

    @Test
    public void testMembersOf()
    {
        Features routes = features.select("r[route=bicycle]");
        Features primaryRoads = features.select("w[highway=primary]");

        for (Feature route : routes)
        {
            Features members1 = route.members("w[highway=primary]");
            Features members2 = primaryRoads.membersOf(route);
            for (Feature member : members1)
            {
                assert (members2.contains(member));
                assert (member.parents().contains(route));
                assert (routes.parentsOf(member).contains(route));
            }
            for (Feature member : members2)
            {
                assert (members1.contains(member));
            }
        }
    }

    /**
     * Ensures that typed queries return the same results as querying "by hand." Note that for simplicity, we count the
     * number of features found, rather than doing a proper set check; it is extremely unlikely that two different query
     * methods return sets with the same number of features, but different content.
     */
    @Test
    public void testTypedQueries()
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
        assertEquals(areas, features.select("a").count());
        assertEquals(allHighways, features.select("*[highway]").count());
        assertEquals(linealHighways, features.select("w[highway]").count());
        assertEquals(linealHighways, features.ways("w[highway]").count());
        assertEquals(linealRailways, features.ways("w[railway]").count());
        assertEquals(linealRailwayHighways, features.select("w[railway][highway]").count());
        assertEquals(linealRailwayHighways, features.select("w[railway]")
            .ways("*[highway]").count());
        assertEquals(linealRailwayHighways, features.select("w[highway]")
            .ways("*[railway]").count());

        WorldView linealWaysQuery = (WorldView) features.ways()
            .select("wa[highway]")
            .ways("*[railway][highway]")
            .select("w");
        assertEquals(TypeBits.NONAREA_WAYS, linealWaysQuery.types());

        assertEquals(linealRailwayHighways, features.ways()
            .select("wa[highway]")
            .ways("*[railway][highway]")
            .select("*[railway]")
            .select("w")
            .count());

        Features empty = features.ways()
            .select("wa[highway]")
            .ways("*[railway][highway]")
            .select("*[railway]")
            .nodes();
        assertTrue(empty instanceof EmptyView);
    }

    /**
     * Checks the referential integrity between relations and their members; this time from the perspective of the
     * members.
     */
    @Test
    public void testRelationMembers()
    {
        for (Feature f : features.in(Box.ofWorld()))
        {
            for (Feature rel : f.parents().relations())
            {
                assertTrue(rel.members().contains(f));
            }
        }
    }

    void testContainsQueries(Features features, Set<Feature> others)
    {
        testContains(features.select("a[landuse]"), others);
        testContains(features
                .nodes("na[shop]")
                .select("*[opening_hours]"),
            others);
    }

    @Test
    public void testContains()
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
    void testContains(Features features, Set<Feature> others)
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
    Set<Feature> randomSample(Features features, int sampleInterval)
    {
        Set<Feature> sample = new HashSet<>();
        Random random = new Random();
        int skip = random.nextInt(sampleInterval);
        for (Feature f : features)
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
     * <p>
     * - A way's bounding box must be the tightest bbox that includes all of its nodes
     * - If a way is an area, its first and last node must be the same
     * - An area must have at least 4 nodes; all others at least 2
     * - Coordinates of the way must match the nodes
     * - Geometry of an area must be polygonal
     * - Geometry of non-area must be lineal
     * - Nodes obtained via filters must match the results of checking nodes "manually"
     */
    @Test
    public void testWays()
    {
        long wayCount = 0;
        long totalNodeCount = 0;
        long totalFeatureNodeCount = 0;
        long totalHighwayNodeCount = 0;
        long totalEntranceNodeCount = 0;
        for (Feature way : features.ways())
        {
            Box wayBox = way.bounds();
            Box calculatedBox = new Box();

            Feature firstNode = null;
            Feature lastNode = null;
            int nodeCount = 0;
            int highwayNodeCount = 0;
            int entranceNodeCount = 0;

            int[] xy = way.toXY();

            for (Feature node : way.nodes())
            {
                if (firstNode == null) firstNode = node;
                lastNode = node;
                calculatedBox.expandToInclude(node.x(), node.y());
                assertEquals(xy[nodeCount * 2], node.x());
                assertEquals(xy[nodeCount * 2 + 1], node.y());
                nodeCount++;
                String v = node.stringValue("highway");
                if (!v.isEmpty() && !v.equals("no")) highwayNodeCount++;
                v = node.stringValue("entrance");
                if (!v.isEmpty() && !v.equals("no")) entranceNodeCount++;
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

            for (Feature node : way.nodes("*"))
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


    @Test
    public void testParentWays()
    {
        long nodeCount = 0;
        long parentWayCount = 0;
        long waysAtNodes = 0;
        Set<Feature> sample = randomSample(features.ways(), 1_000);
        long start = System.currentTimeMillis();
        for (Feature way : sample)
        {
            for (Feature node : way.nodes())
            {
                Features parentWays = node.parents().ways();
                if (!parentWays.contains(way))
                {
                    Log.debug("%s should have %s as a parent, but doesn't. It has %d parents.",
                        node.toString(), way.toString(), parentWays.count());
                    Log.debug("%s flags = %d, tags = %s", node.toString(),
                        ((StoredNode) node).flags(), node.tags().toString());
                    Features parentWays2 = node.parents().ways();
                    for (Feature p : parentWays2)
                    {
                        Log.debug(p.toString());
                    }
                }
                Assert.assertTrue(parentWays.contains(way));
                for (Feature parentWay : parentWays)
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
            sample.size(), nodeCount, parentWayCount, end - start);
        Log.debug("ParentWay queries consulted %d ways at node location.", waysAtNodes);
    }


    private void assertNotTagged(Feature f, String k)
    {
        String v = f.stringValue(k);
        assertTrue(v.isEmpty() || v.equals("no"));
        Tags tags = f.tags();
        while (tags.next())
        {
            if (tags.key().equals(k))
            {
                v = tags.stringValue();
                assertTrue(v.isEmpty() || v.equals("no"));
            }
        }
    }

    /**
     * This test gets the tags of all features and queries them in various ways:
     * <p>
     * - Directly
     * - Through the iterator
     * - Converting the tags to a HashMap and looking up each one
     * <p>
     * All methods of lookup must return the same results.
     */
    @Test
    public void testTags()
    {
        int totalFeatureCount = 0;
        int totalTagCount = 0;
        for (Feature f : features)
        {
            Tags tags = f.tags();
            int tagCount = 0;

            // Log.debug("%s: %s", f.toString(), tags.toString());

            Map<String, Object> tagMap = tags.toMap();

            while (tags.next())
            {
                String k = tags.key();
                String v = tags.stringValue();
                /*
                if(!f.stringValue(k).equals(v))
                {
                    Log.debug("%s: %s should be %s, not %s", f, k, v, f.stringValue(k));
                }
                 */
                if (!f.hasTag(k, v))
                {
                    Log.debug("%s: hasTag() did not find %s=%s", f.toString(), k, v);
                    String v2 = f.stringValue(k);
                    Log.debug("  Value of %s: is %s", k, v2);
                }

                if (!f.stringValue(k).equals(v))
                {
                    Log.debug("%s: %s=%s is not equal to %s", f.toString(),
                        k, f.stringValue(k), v);
                }

                if (!f.hasTag(k))
                {
                    Log.debug("%s should have tag %s", f.toString(), k);
                }

                assertTrue(f.hasTag(k, v));
                assertEquals(f.stringValue(k), v);
                assertTrue(f.hasTag(k) || k.isEmpty());
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


    @Test
    public void testSpecificWayTags()
    {
        for (Feature way : features.ways("w[highway]"))
        {
            for (var node : way.nodes(
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

    // delete
    public void testSuperRelations()
    {
        long relCount = 0;
        long superRelCount = 0;
        for (var rel : features.relations())
        {
            if (!rel.members().relations().isEmpty())
            {
                Feature firstMember = rel.members().relations().first();
                assertTrue(firstMember instanceof Relation);
                superRelCount++;
            }
            relCount++;
        }
        Log.debug("%d relations", relCount);
        Log.debug("  Of these, %d are super-relations", superRelCount);
    }

    /**
     * Tests relation member queries:
     * - based on primitive type
     * - based on conceptual type
     * - iteration
     * All approaches must yield the same counts.
     */
    @Test
    public void testSimpleMemberQueries()
    {
        long relations = 0;
        long members = 0;
        long memberNodes = 0;
        long memberWays = 0;
        long memberRelations = 0;
        long memberAreas = 0;
        long memberWayAreas = 0;
        long memberRelationAreas = 0;
        long membersManual = 0;
        long memberNodesManual = 0;
        long memberWaysManual = 0;
        long memberRelationsManual = 0;
        long memberAreasManual = 0;
        long memberWayAreasManual = 0;
        long memberRelationAreasManual = 0;

        for (var rel : features.relations())
        {
            long thisMemberRelations = rel.members().relations().count();
            long thisMemberWays = rel.members().ways().count();
            members += rel.members().count();
            memberNodes += rel.members().nodes().count();
            memberWays += thisMemberWays;
            memberRelations += thisMemberRelations;
            memberAreas += rel.members("a").count();
            memberWayAreas += rel.members().ways("a").count();
            memberRelationAreas += rel.members().relations("a").count();
            assertEquals(thisMemberRelations, rel.members().relations("ar").count());
            assertEquals(thisMemberWays, rel.members().ways("wa").count());
            assertEquals(thisMemberRelations, rel.members().relations("nar").count());
            assertEquals(thisMemberWays, rel.members().ways("nwa").count());

            for (Feature f : rel)
            {
                switch (f.type())
                {
                case NODE:
                    memberNodesManual++;
                    break;
                case WAY:
                    memberWaysManual++;
                    if (f.isArea())
                    {
                        memberWayAreasManual++;
                        memberAreasManual++;
                    }
                    break;
                case RELATION:
                    memberRelationsManual++;
                    if (f.isArea())
                    {
                        memberRelationAreasManual++;
                        memberAreasManual++;
                    }
                    break;
                }
                membersManual++;
            }
            relations++;
        }
        Log.debug("%d relations with %d members", relations, members);
        Log.debug("  (%d nodes, %d ways, %d relations)", memberNodes, memberWays, memberRelations);
        assertTrue(relations > 0);
        assertTrue(members > 0);
        assertEquals(membersManual, members);
        assertEquals(memberNodesManual, memberNodes);
        assertEquals(memberWaysManual, memberWays);
        assertEquals(memberRelationsManual, memberRelations);
        assertEquals(memberAreasManual, memberAreas);
        assertEquals(memberWayAreasManual, memberWayAreas);
        assertEquals(memberRelationAreasManual, memberRelationAreas);
    }


    public void testMemberQueriesX()
    {
        for (int run = 0; run < 10; run++)
        {
            long start = System.currentTimeMillis();
            long relCount = 0;
            long memberCount = 0;
            for (var rel : features.relations(/* "a[boundary]" */))
            {
                relCount++;
                for (var node : rel.members().nodes("n[place]"))
                {
                    // Log.debug("%s: %s", rel, node);
                    memberCount++;
                }
            }
            long end = System.currentTimeMillis();
            Log.debug("%d nodes in %d relations (%d ms)", memberCount, relCount, end - start);
        }
    }

    @Test
    public void testMemberRoleQueries()
    {
        Matcher matcher = new RoleMatcher(features.store(), "admin_centre");
        for (int run = 0; run < 10; run++)
        {
            long start = System.currentTimeMillis();
            long relCount = 0;
            long memberCount = 0;
            long memberCountSlow = 0;
            for (var rel : features.relations(/* "a[boundary]" */))
            {
                relCount++;
                Iterator<Feature> iter = ((StoredRelation) rel).iterator(
                    TypeBits.NODES, matcher);
                while (iter.hasNext())
                {
                    iter.next();
                    memberCount++;
                }

                for (var node : rel.members().nodes())
                {
                    if (node.role().equals("admin_centre")) memberCountSlow++;
                }
            }
            long end = System.currentTimeMillis();
            Log.debug("%d nodes in %d relations (%d ms)", memberCount, relCount, end - start);
            Log.debug("  (%d nodes using slow count)", memberCountSlow);
        }

        /*
        for(Relation rel: features.relations("r"))
        {
            for(Way way: rel.memberWays("w[highway]"))
            {
                Log.debug("%s: %s", rel, way);
            }
        }
         */
    }

    /*
    @Test public void testMemberQueries2()
    {
        for(Relation rel: features.relations())
        {
            for(Node node: rel.memberNodes("n[place]"))
            {
                if(!rel.hasTag("boundary"))
                {
                    Log.debug("%s: %s", rel, node);
                }
            }
        }

    }
     */

    @Test
    public void testValueStrings() throws IOException
    {
        List<String> strings = new ArrayList<>();

        for (Feature f : features)
        {
            Tags tags = f.tags();
            while (tags.next())
            {
                strings.add(f + ": " + tags.stringValue());
            }
        }

        Collections.sort(strings);

        // Write to file
        try (BufferedWriter writer = new BufferedWriter(
            new FileWriter("d:\\geodesk\\tests\\monaco-java.txt",
                StandardCharsets.UTF_8)))
        {
            for (String s : strings)
            {
                writer.write(s);
                writer.newLine();
            }
        }
    }
}