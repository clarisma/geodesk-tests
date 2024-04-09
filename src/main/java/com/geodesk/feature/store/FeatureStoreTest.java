//package com.geodesk.feature.store;
//
//import com.geodesk.geom.Tile;
//import com.geodesk.feature.*;
//import com.geodesk.feature.polygon.PolygonBuilder;
//import com.geodesk.geom.Box;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.eclipse.collections.api.set.primitive.MutableLongSet;
//import org.eclipse.collections.impl.set.mutable.primitive.LongHashSet;
//import org.junit.Before;
//import org.junit.Test;
//import org.locationtech.jts.geom.*;
//import org.locationtech.jts.operation.valid.IsValidOp;
//import org.locationtech.jts.operation.valid.TopologyValidationError;
//import org.locationtech.jts.util.Stopwatch;
//
//import static org.junit.Assert.*;
//
//import java.nio.file.Path;
//
//// old, delete
//
//public class FeatureStoreTest
//{
//    public static final Logger log = LogManager.getLogger();
//
//    TestFeatureStore features;
//    Box bbox;
//
//    @Before
//    public void setUp() throws Exception
//    {
//        // features = new TestFeatureStore(Path.of("c:\\geodesk\\tests\\de.gol"));
//        features = new TestFeatureStore(Path.of("/home/md/geodesk/tests/world2.gol"));
//        // features = new TestFeatureStore(Path.of("c:\\geodesk\\tests\\france.gol"));
//        bbox = Box.ofWorld();
//    }
//
//    @Test
//    public void testQuery()
//    {
//        Box world = Box.ofWorld();
//        for (Feature f : features.in(world))
//        {
//            log.debug(f);
//        }
//    }
//
//    @Test
//    public void testCount()
//    {
//        Box world = Box.ofWorld();
//        log.debug("{} features in {}", features.in(world).count(), world);
//    }
//
//    private void testCountTile(String s, int runs)
//    {
//        int tile = Tile.fromString(s);
//        Box bbox = Tile.bounds(tile);
//        long startTime = System.currentTimeMillis();
//        long total = 0;
//        for (int i = 0; i < runs; i++)
//        {
//            long count = features.in(bbox).count();
//            total += count;
//        }
//        long timeElapsed = System.currentTimeMillis() - startTime;
//        log.debug("{}: Found {} features in {} ms ({} per second)",
//            s, total, timeElapsed, (total * 1000) / timeElapsed);
//    }
//
//    @Test
//    public void testCountArea()
//    {
//        testCountTile("6/33/21", 100);
//        testCountTile("8/136/88", 1000);
//        testCountTile("12/2179/1421", 10_000);
//    }
//
//    @Test
//    public void testCountArea2()
//    {
//        // int tile = Tile.fromString("8/136/88");
//        int tile = Tile.fromString("12/2179/1421");
//        Box greaterMunich = Tile.bounds(tile);
//        long startTime = System.currentTimeMillis();
//        long total = 0;
//        int runs = 1_000;
//        for (int i = 0; i < runs; i++)
//        {
//            long count = features.in(greaterMunich).count();
//            total += count;
//        }
//        long timeElapsed = System.currentTimeMillis() - startTime;
//        log.debug("Found {} features in {} ms ({} per second)",
//            total, timeElapsed, (total * 1000) / timeElapsed);
//    }
//
//    @Test
//    public void testIterRelations()
//    {
//        Box world = Box.ofWorld();
//        for (Feature f : features.in(world))
//        {
//            if (f instanceof Relation)
//            {
//                Relation rel = (Relation) f;
//                log.debug(rel);
//                for (Feature member : rel)
//                {
//                    log.debug("- {} as \"{}\"", member, member.role());
//                }
//            }
//        }
//    }
//
//    @Test
//    public void checkRelationRoles()
//    {
//        long relCount = 0;
//        long memberCount = 0;
//        long suspiciousRoleCount = 0;
//        Box world = Box.ofWorld();
//        for (Feature f : features.in(world))
//        {
//            if (f instanceof Relation)
//            {
//                Relation rel = (Relation) f;
//                for (Feature member : rel)
//                {
//                    String role = member.role();
//                    if (role.length() > 30)
//                    {
//                        log.warn("{} in {} has a suspicious role: {} chars long",
//                            member, rel, role.length());
//                        suspiciousRoleCount++;
//                    }
//                    memberCount++;
//                }
//                relCount++;
//            }
//        }
//        log.info("Checked {} members in {} relations, {} suspicious roles found.",
//            memberCount, relCount, suspiciousRoleCount);
//    }
//
//    @Test
//    public void checkUniqueRelations()
//    {
//        long dupeCount = 0;
//        MutableLongSet rels = new LongHashSet();
//        Box world = Box.ofWorld();
//        for (Feature f : features.in(world))
//        {
//            if (f instanceof Relation)
//            {
//                if (rels.contains(f.id()))
//                {
//                    log.warn("Duplicate {}", f);
//                    dupeCount++;
//                }
//                else
//                {
//                    rels.add(f.id());
//                }
//            }
//        }
//        log.warn("{} duplicate features found.", dupeCount);
//    }
//
//    private String getTags(Feature f)
//    {
//        StringBuilder buf = new StringBuilder();
//        Tags tags = f.tags();
//        while (tags.next())
//        {
//            buf.append(tags.key());
//            buf.append('=');
//            buf.append(tags.value());
//            buf.append(';');
//        }
//        return buf.toString();
//    }
//
//    @Test
//    public void checkInvalidMembers()
//    {
//        long count = 0;
//        Box world = Box.ofWorld();
//        for (Feature f : features.in(world))
//        {
//            if (f instanceof Relation)
//            {
//                Relation rel = (Relation) f;
//                for (Feature m : rel)
//                {
//                    Tags tags = m.tags();
//                    while (tags.next())
//                    {
//                        if (tags.key().startsWith("geodesk:"))
//                        {
//                            // log.debug("{}: {}={}", m, tags.key(), tags.value());
//                            count++;
//                        }
//                    }
//                }
//            }
//        }
//        log.warn("{} invalid members found.", count);
//    }
//
//    /**
//     * Tests the referential integrity of relations: If a feature is member
//     * of a relation, the parent relation's member table must actually
//     * contain the feature.
//     */
//    @Test
//    public void testParentRelationContainsFeature()
//    {
//        for (Feature f : features.in(bbox))
//        {
//            for (Relation parent : f.parentRelations())
//            {
//                assertTrue(parent.members().contains(f));
//            }
//        }
//    }
//
//    /**
//     * Tests the referential integrity of relations.
//     *
//     * If a feature is member of a relation, its relation table must
//     * contain the relation.
//     */
//    @Test
//    public void testMemberHasRelationAsParent()
//    {
//        Box world = Box.ofWorld();
//        for (Feature f : features.in(world))
//        {
//            if (f instanceof Relation)
//            {
//                Relation rel = (Relation)f;
//                for (Feature member : rel.members())
//                {
//                    if (!member.parentRelations().contains(rel))
//                    {
//                        /*
//                        log.debug("Parents of {}", member);
//                        for(Relation parent: member.parentRelations())
//                        {
//                            log.debug("- {}: {}", parent, getTags(parent));
//                        }
//                         */
//                        /*
//                        fail(String.format("%s contains %s, but is not in reltable of %s",
//                            rel, member, member));
//
//                         */
//                        log.error(String.format("%s contains %s, but is not in reltable of %s",
//                            rel, member, member));
//
//                    }
//                }
//            }
//        }
//    }
//
//    @Test
//    public void testParentRelations()
//    {
//        Box world = Box.ofWorld();
//        for (Feature f : features.in(world))
//        {
//            if (f.belongsToRelation())
//            {
//                if (f instanceof Relation && f.id() == 374676)
//                {
//                    log.debug("!!! {}", f);
//                }
//                log.debug("Parents of {}:", f);
//                for (Relation parent : f.parentRelations())
//                {
//                    log.debug("- {}: {}", parent, getTags(parent));
//                }
//            }
//        }
//    }
//
//    @Test
//    public void testRelationBounds()
//    {
//        long errors = 0;
//        for (Feature f : features.in(bbox))
//        {
//            if (f instanceof Relation)
//            {
//                Relation rel = (Relation) f;
//                Box memberBounds = new Box();
//                for (Feature member : rel)
//                {
//                    memberBounds.expandToInclude(member.bounds());
//                }
//                if (!memberBounds.equals(rel.bounds()))
//                {
//                    log.error("BBox of {} does not match the bounds of its members", rel);
//                    log.error("  Relation bounds: {}", rel.bounds());
//                    log.error("  Member bounds:   {}", memberBounds);
//                    for (Feature member : rel)
//                    {
//                        log.error("  {}: {}", member, member.bounds());
//                    }
//                    errors++;
//                }
//            }
//        }
//        if (errors > 0) fail();
//    }
//
//    @Test
//    public void testRelTableDuplicates()
//    {
//        MutableLongSet rels = new LongHashSet();
//        for (Feature f : features.in(bbox))
//        {
//            if(f.belongsToRelation())
//            {
//                for(Relation rel: f.parentRelations())
//                {
//                    long relId = rel.id();
//                    assertFalse(rels.contains(relId));
//                    rels.add(relId);
//                }
//                rels.clear();
//            }
//        }
//    }
//
//    private void count(String q)
//    {
//        Stopwatch timer = new Stopwatch();
//        timer.start();
//        long count = features.features(q).in(bbox).count();
//        log.debug("{}: {} features found in {} ms.", q, count, timer.stop());
//    }
//
//    @Test
//    public void testTagQuery()
//    {
//        count("w[highway=motorway]");
//        count("w[highway=pedestrian][tunnel=building_passage][lit]");
//        count("w[railway]");
//        count("w[waterway]");
//        count("w[natural=coastline]");
//        count("na[man_made=lighthouse]");
//        count("n[place=city][name=Berlin]");
//        count("na[amenity=restaurant][cuisine=greek][name='Acro*','Akro*']");
//        count("na[amenity=restaurant][cuisine=greek][name='My*']");
//        count("na[amenity=restaurant][cuisine=italian][name='Dolce*']");
//        count("a[leisure=pitch][sport=tennis][lit]");
//    }
//
//    @Test
//    public void testTagQuery1()
//    {
//        String q = "na[amenity=restaurant][cuisine=greek][name='Acro*','Akro*']";
//        for(Feature f: features.features(q).in(bbox))
//        {
//            log.debug("{}: {}", f, getTags(f));
//        }
//    }
//
//    private int countPolygonsWithHoles(Geometry g)
//    {
//        int count = 0;
//        for(int i=0; i<g.getNumGeometries(); i++)
//        {
//            Geometry child = g.getGeometryN(i);
//            if(child instanceof Polygon p)
//            {
//                if (p.getNumInteriorRing() > 0) count++;
//            }
//            else if(child.getNumGeometries() > 1)
//            {
//                count += countPolygonsWithHoles(child);
//            }
//        }
//        return count;
//    }
//
//    @Test
//    public void testBuildGeometriesNew()
//    {
//        double area = 0;
//        GeometryFactory factory = new GeometryFactory();
//        long totalCount = 0;
//        long validCount = 0;
//        long cannotAssemble = 0;
//        for (Feature f : features.in(bbox))
//        {
//            if (f.isArea() && f instanceof Relation)
//            {
//                Relation rel = (Relation) f;
//                Geometry g = PolygonBuilder.build(factory, rel);
//                if(g == null)
//                {
//                    cannotAssemble++;
//                }
//                else
//                {
//                    IsValidOp valid = new IsValidOp(g);
//                    TopologyValidationError error = valid.getValidationError();
//                    if(error == null)
//                    {
//                        validCount++;
//                        area += g.getArea();
//                    }
//                    else
//                    {
//                        log.debug("Assembled, but invalid: {}", rel);
//                        log.debug("{}", error);
//                    }
//                }
//                totalCount++;
//            }
//        }
//        log.info("{} area relations", totalCount);
//        log.info("{} could not be assembled", cannotAssemble);
//        log.info("{} assembled & valid, area = {}", validCount, area);
//
//    }
//
//    @Test
//    public void testWayNodes()
//    {
//        for (Feature f : features.in(bbox))
//        {
//            if(f instanceof Way way)
//            {
//                for(Node node: way)
//                {
//                    log.debug("{}: {}", node, node.tags().toString());
//                }
//            }
//        }
//    }
//
//}
