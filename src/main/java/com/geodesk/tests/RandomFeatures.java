package com.geodesk.tests;

import com.clarisma.common.math.MathUtils;
import com.clarisma.common.text.Strings;
import com.clarisma.common.util.Log;
import com.geodesk.core.Box;
import com.geodesk.feature.Feature;
import com.geodesk.feature.FeatureLibrary;
import com.geodesk.feature.Features;
import com.geodesk.feature.Tags;
import com.geodesk.geom.Bounds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * In this test, we pick random features out of a GOL, and then we try to
 * re-create specific queries that should pick up those features.
 *
 * For every feature:
 *
 * - We create a random bbox that overlaps the feature
 * - For every tag, we randomly decide to include a clause:
 *   - exact match
 *   - For strings:
 *     - starts with string
 *     - ends with string
 *   - for numbers:
 *     range
 *   - We randomly add multiple clauses for the same key
 * - Then we add any number of negative clauses that don't apply to the feature
 *
 * The goal of the test is to smoke out bugs caused by weird combinations of
 * query clauses, and to also detect any structural problems in the GOL file
 * (buffer overruns, etc.)
 */
public class RandomFeatures
{
    Random random = new Random();
    Features<?> features;

    RandomFeatures(Features<?> features)
    {
        this.features = features;
    }

    String equalsString(String s)
    {
        return "=\"" + Strings.escape(s) + "\"";
    }

    String makeMatchClause(String value)
    {
        switch(random.nextInt(5))
        {
        case 0:     // nothing, just a key match
            return "";
        case 1:     // exact string match
            double num = MathUtils.doubleFromString(value);
            if(!Double.isNaN(num)) return "";
            // TODO: don't do string match for numbers for now, because
            //  this does not work yet
            return equalsString(value);
        case 2:     // start
            if(value.isEmpty()) return "=*";
            int len = random.nextInt(value.length());
            return equalsString(value.substring(0,len) + "*");
        case 3:     // end
            if(value.isEmpty()) return "=*";
            len = random.nextInt(value.length());
            return equalsString("*" + value.substring(len));
        case 4:     // numeric
            num = MathUtils.doubleFromString(value);
            if(Double.isNaN(num)) return "";
            if(num > 100_000_000d) return ""; // large number formatting: avoid E notation for now
            String ns = String.valueOf(num);
            switch(random.nextInt(4))
            {
            case 0:
                return ">=" + ns;
            case 1:
                return "<=" + ns;
            case 2:
                num -= random.nextDouble(100);
                num = Math.round(num * 1_000_000d) / 1_000_000d;
                return ">" + num;
            case 3:
                num += random.nextDouble(100);
                num = Math.round(num * 1_000_000d) / 1_000_000d;
                return "<" + num;
            }
        }
        return "";
    }

    static boolean mustBeQuoted(String s)
    {
        if(Character.isDigit(s.charAt(0))) return true;
        for(int i=0; i<s.length(); i++)
        {
            char ch = s.charAt(i);
            if(!Character.isLetterOrDigit(ch) && ch != ':' && ch != '_') return true;
        }
        return false;
    }

    String makeKey(String k)
    {
        if(!mustBeQuoted(k))
        {
            if(random.nextBoolean()) return k;
        }
        return String.format("\"%s\"", k);
    }

    String makeQuery(Tags tags)
    {
        List<String> clauses = new ArrayList<>();
        while(tags.next())
        {
            String k = tags.key();
            String v = tags.stringValue();
            int count = random.nextInt(10) - 5;
            for(int i=0; i<count; i++)
            {
                if(v.equals("no"))
                {
                    if(random.nextBoolean()) clauses.add(String.format("[!%s]",
                        makeKey(k)));
                }
                else
                {
                    String clause = String.format("[%s%s]", makeKey(k),
                        makeMatchClause(v));
                    clauses.add(clause);
                }
            }
        }
        Collections.shuffle(clauses);
        StringBuilder sb = new StringBuilder();
        sb.append("*");
        for(String clause: clauses) sb.append(clause);
        return sb.toString();
    }

    Box makeRandomBox(Bounds b)
    {
        long minX = b.minX();
        long maxX = b.maxX();
        long minY = b.minY();
        long maxY = b.maxY();
        long w = maxX - minX;
        long h = maxY - minY;
        int x1 = (int)Math.max(maxX - random.nextLong(4 * w + 1), Integer.MIN_VALUE);
        int x2 = (int)Math.min(minX + random.nextLong(4 * w + 1), Integer.MAX_VALUE);
        int y1 = (int)Math.max(maxY - random.nextLong(4 * h + 1), Integer.MIN_VALUE);
        int y2 = (int)Math.min(minY + random.nextLong(4 * h + 1), Integer.MAX_VALUE);
        return Box.ofXYXY(x1,y1,Math.max(x1,x2),Math.max(y1,y2));
    }

    void queryFeature(Feature f)
    {
        String query = makeQuery(f.tags());
        Log.debug("Query %s:", query);
        Box bbox = makeRandomBox(f.bounds());

        for(Feature candidate: features.select(query).in(bbox))
        {
            if(candidate.equals(f)) return;
        }

        Log.debug("Failed to find: %s", f);
        Log.debug("  Tags:  %s", f.tags());
        Log.debug("  Query: %s", query);

        boolean foundPlain = false;
        for(Feature candidate: features.in(bbox))
        {
            if(candidate.equals(f))
            {
                foundPlain = true;
            }
        }

        if(foundPlain)
        {
            Log.debug("However, found feature using plain bbox query.");
        }
        else
        {
            Log.debug("Plain bbox query didn't find it either!");
        }

        throw new RuntimeException("Test failed");
    }

    public void test()
    {
        int sampleRate = 10_000;
        int skip = random.nextInt(sampleRate);
        for(Feature f: features.in(Box.ofWorld()))
        {
            if(skip == 0)
            {
                queryFeature(f);
                skip = random.nextInt(sampleRate);
                continue;   // don't decrement, could already be 0
            }
            skip--;
        }
    }

    public static void main(String[] args)
    {
        FeatureLibrary features = new FeatureLibrary(TestSettings.golFile());
        RandomFeatures test = new RandomFeatures(features);
        test.test();
    }

}
