package com.geodesk.tests;

import com.geodesk.feature.*;
import com.geodesk.geom.Mercator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class ConcurTest
{
    Features world = new FeatureLibrary("c:\\geodesk\\tests\\monaco.gol");

    long italian_restaurant_count()
    {
        return world.select("na[amenity=restaurant][cuisine=italian]").count();
    }

    long member_count()
    {
        long count = 0;
        for (var parent : world)
        {
            count += parent.members().count();
        }
        return count;
    }

    long member_iter_count()
    {
        long count = 0;
        for (var parent : world)
        {
            for (var child : parent.members())
            {
                count += 1;
            }
        }
        return count;
    }

    long parent_count()
    {
        long count = 0;
        for (var child : world)
        {
            count += child.parents().count();
        }
        return count;
    }

    long parent_iter_count()
    {
        long count = 0;
        for (var child : world)
        {
            for (var parent : child.parents())
            {
                count += 1;
            }
        }
        return count;
    }

    long parents_of_count()
    {
        long count = 0;
        for (var child : world)
        {
            count += world.parentsOf(child).count();
        }
        return count;
    }

    long parent_relations_count()
    {
        long count = 0;
        for (var child : world)
        {
            count += child.parents().relations().count();
        }
        return count;
    }

    long parent_relations_of_count()
    {
        Features relations = world.relations();
        long count = 0;
        for (var child : world)
        {
            count += relations.parentsOf(child).count();
        }
        return count;
    }

    long parent_ways_count()
    {
        long count = 0;
        for (var child : world)
        {
            count += child.parents().ways().count();
        }
        return count;
    }

    long parent_ways_of_count()
    {
        Features ways = world.ways();
        long count = 0;
        for (var child : world)
        {
            count += ways.parentsOf(child).count();
        }
        return count;
    }

    public long waynode_parents_count()
    {
        long count = 0;
        for (var way : world.ways())
        {
            for (var node : way.nodes())
            {
                count += node.parents().count();
            }
        }
        return count;
    }

    public long waynode_parent_ways_count()
    {
        long count = 0;
        for (var way : world.ways())
        {
            for (var node : way.nodes())
            {
                count += node.parents().ways().count();
            }
        }
        return count;
    }

    public long waynode_count()
    {
        long count = 0;
        for (var way : world.ways())
        {
            count += way.nodes().count();
        }
        return count;
    }

    public long waynode_iter_count()
    {
        long count = 0;
        for (var way : world.ways())
        {
            for (var node : way.nodes())
            {
                count += 1;
            }
        }
        return count;
    }

    long nonsense_parent_count()
    {
        long count = 0;
        for (var child : world)
        {
            count += child.parents().nodes().count();
        }
        return count;
    }

    long nonsense_parents_of_count()
    {
        Features nodes = world.nodes();
        long count = 0;
        for (var child : world)
        {
            count += nodes.parentsOf(child).count();
        }
        return count;
    }


    public long street_crossing_count()
    {
        Features crossings = world.select("n[highway=crossing]");
        long count = 0;
        for (var street : world.select("w[highway]"))
        {
            count += crossings.nodesOf(street).count();
        }
        return count;
    }

    public long street_crossing_iter_count()
    {
        Features crossings = world.select("n[highway=crossing]");
        long count = 0;
        for (var street : world.select("w[highway]"))
        {
            for (var node : crossings.nodesOf(street))
            {
                count += 1;
            }
        }
        return count;
    }

    public long street_crossing_in_count()
    {
        Features crossings = world.select("n[highway=crossing]");
        long count = 0;
        for (var street : world.select("w[highway]"))
        {
            for (var node : street.nodes())
            {
                if (crossings.contains(node))
                {
                    count += 1;
                }
            }
        }
        return count;
    }

    public long tags_count()
    {
        long count = 0;
        for (var f: world)
        {
            count += f.tags().size();
        }
        return count;
    }

    public long tags_iter_count()
    {
        long count = 0;
        for (var f: world)
        {
            Tags tags = f.tags();
            while(tags.next()) count++;
        }
        return count;
    }

    public long tags_ken_len()
    {
        long totalLen = 0;
        for (var f: world)
        {
            Tags tags = f.tags();
            while(tags.next())
            {
                totalLen += tags.key().length();
            }
        }
        return totalLen;
    }

    public long tags_str_len()
    {
        long totalLen = 0;
        for (var f: world)
        {
            Tags tags = f.tags();
            while(tags.next())
            {
                totalLen += tags.stringValue().length();
            }
        }
        return totalLen;
    }

    public long tags_int_sum()
    {
        long sum = 0;
        for (var f: world)
        {
            Tags tags = f.tags();
            while(tags.next())
            {
                sum += tags.intValue();
            }
        }
        return sum;
    }

    public long xy_hash()
    {
        long hash = 0;
        for (var f: world)
        {
            hash ^= f.x();
            hash ^= f.y();
        }
        return hash;
    }

    public long lonlat_100nd_hash()
    {
        long hash = 0;
        for (var f: world)
        {
            // hash ^= (long)(f.lon() * 10000000);
            // hash ^= (long)(f.lat() * 10000000);
            hash ^= (long)(Mercator.lonPrecision7fromX(f.x()) * 10000000);
            hash ^= (long)(Mercator.latPrecision7fromY(f.y()) * 10000000);
        }
        return hash;
    }

    public long waynodes_lonlat_100nd_hash()
    {
        long hash = 0;
        for (var way: world.ways())
        {
            for(var node: way.nodes())
            {
                // hash ^= (long)(f.lon() * 10000000);
                // hash ^= (long)(f.lat() * 10000000);
                hash ^= (long) (Mercator.lonPrecision7fromX(node.x()) * 10000000);
                hash ^= (long) (Mercator.latPrecision7fromY(node.y()) * 10000000);
            }
        }
        return hash;
    }

    public long id_hash()
    {
        long hash = 0;
        for (var f: world)
        {
            hash ^= f.id();
        }
        return hash;
    }

    public static <E> void reportSetDifferences(Set<E> set1, Set<E> set2)
    {
        // Create a set for items in set1 but not in set2
        Set<E> inSet1Only = new HashSet<>(set1);
        inSet1Only.removeAll(set2);  // Remove all elements that are also in set2

        // Create a set for items in set2 but not in set1
        Set<E> inSet2Only = new HashSet<>(set2);
        inSet2Only.removeAll(set1);  // Remove all elements that are also in set1

        // Report differences
        if (inSet1Only.isEmpty() && inSet2Only.isEmpty())
        {
            System.out.println("Both sets are identical.");
        }
        else
        {
            if (!inSet1Only.isEmpty())
            {
                System.out.println("Items in set1 but not in set2: " + inSet1Only);
            }

            if (!inSet2Only.isEmpty())
            {
                System.out.println("Items in set2 but not in set1: " + inSet2Only);
            }
        }
    }

    public static void main(String[] args) throws InvocationTargetException, IllegalAccessException
    {
        ConcurTest test = new ConcurTest();
        Method[] methods = ConcurTest.class.getDeclaredMethods();

        // Filter for snake_case methods (non-static, no parameters, return long)
        List<Method> testMethods = new ArrayList<>();
        for (Method method : methods)
        {
            // Check if the method name is in snake_case (contains an underscore)
            if (method.getName().contains("_"))
            {
                testMethods.add(method);
            }
        }

        // Sort the methods by name
        testMethods.sort(Comparator.comparing(Method::getName));

        // Invoke each method and print the result
        for (Method method : testMethods)
        {
            long result = (long) method.invoke(test);  // Invoke the method
            System.out.println("Method: " + method.getName() + ", Result: " + result);
        }

        // reportSetDifferences(test.parents, test.parentsOf);
    }
}