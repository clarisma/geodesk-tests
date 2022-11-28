/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.experiments;

import com.geodesk.benchmark.Benchmark;
import com.geodesk.benchmark.BenchmarkRunner;
import com.geodesk.benchmark.SimpleBenchmark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Studies the effects of polymorphic dispatch.
 *
 * Conclusion:
 * - The JVM is great at reducing vtable dispatch; can forget about the issue
 * - Peeling does very little (if anything) to improve performance
 */
public class PolymorphicExperiment
{
    static Random random = new Random();

    static class Fruit
    {
        int a;

        Fruit()
        {
            a = random.nextInt();
        }

        int a() { return a; }
        int b() { return 37; }
    }

    static class Apple extends Fruit
    {
        int b() { return 53; }
    }

    static class Banana extends Fruit
    {
        int b() { return 65; }
    }

    static class Cherry extends Fruit
    {
        int b() { return 76; }
    }

    static class Durian extends Fruit
    {
        int b() { return 81; }
    }

    static class CalculateA extends SimpleBenchmark
    {
        final Fruit[] fruits;
        long result;

        CalculateA(String name, Fruit[] fruits)
        {
            super("monomorphic-" + name, null);
            this.fruits = fruits;
        }

        @Override protected void perform()
        {
            long sum = 0;
            for(int i=0; i<20; i++)
            {
                for(Fruit f: fruits) sum += f.a();
            }
            result = sum;
        }
    }

    static class CalculateB extends SimpleBenchmark
    {
        final Fruit[] fruits;
        long result;

        CalculateB(String name, Fruit[] fruits)
        {
            super("polymorphic-" + name, null);
            this.fruits = fruits;
        }

        @Override protected void perform()
        {
            long sum = 0;
            for(int i=0; i<20; i++)
            {
                for(Fruit f: fruits) sum += f.b();
            }
            result = sum;
        }
    }

    // Need separate class, or else we won't see the effect of
    // call target prediction
    static class CalculateB_separate extends SimpleBenchmark
    {
        final Fruit[] fruits;
        long result;

        CalculateB_separate(String name, Fruit[] fruits)
        {
            super("polymorphic-separate-" + name, null);
            this.fruits = fruits;
        }

        @Override protected void perform()
        {
            long sum = 0;
            for(int i=0; i<20; i++)
            {
                for(Fruit f: fruits) sum += f.b();
            }
            result = sum;
        }
    }

    static class CalculateC extends SimpleBenchmark
    {
        final Fruit[] fruits;
        long result;

        CalculateC(String name, Fruit[] fruits)
        {
            super("peeled-" + name, null);
            this.fruits = fruits;
        }

        @Override protected void perform()
        {
            long sum = 0;
            for(int i=0; i<20; i++)
            {
                for(Fruit f: fruits)
                {
                    if(f instanceof Banana banana)
                    {
                        sum += banana.b();
                    }
                    else
                    {
                        sum += f.b();
                    }
                }
            }
            result = sum;
        }
    }

    public static void main(String[] args)
    {
        List<Fruit> fruits  =new ArrayList<>();
        for(int i=0; i<250_000; i++) fruits.add(new Apple());
        for(int i=0; i<250_000; i++) fruits.add(new Banana());
        for(int i=0; i<250_000; i++) fruits.add(new Cherry());
        for(int i=0; i<250_000; i++) fruits.add(new Durian());
        Collections.shuffle(fruits);
        Fruit[] randomFruits = fruits.toArray(new Fruit[0]);
        Fruit[] bananas = new Fruit[1_000_000];
        for(int i=0; i<1_000_000; i++) bananas[i] = new Banana();

        fruits.clear();
        for(int i=0; i<20_000; i++) fruits.add(new Apple());
        for(int i=0; i<950_000; i++) fruits.add(new Banana());
        for(int i=0; i<20_000; i++) fruits.add(new Cherry());
        for(int i=0; i<10_000; i++) fruits.add(new Durian());
        Collections.shuffle(fruits);
        Fruit[] mostlyBananas = fruits.toArray(new Fruit[0]);

        var a1 = new CalculateA("fruits", randomFruits);
        var a2 = new CalculateA("bananas", bananas);
        var a3 = new CalculateA("mostly-bananas", mostlyBananas);
        var b1 = new CalculateB("fruits", randomFruits);
        var b2 = new CalculateB("bananas", bananas);
        var b3 = new CalculateB("mostly-bananas", mostlyBananas);
        // var bs1 = new CalculateB_separate("bananas", bananas);
        var bs2 = new CalculateB_separate("mostly-bananas", mostlyBananas);
        var c1 = new CalculateC("fruits", randomFruits);
        var c2 = new CalculateC("bananas", bananas);
        var c3 = new CalculateC("mostly-bananas", mostlyBananas);
        a1.run(10);
        a2.run(10);
        a3.run(10);
        b1.run(10);
        b2.run(10);
        b3.run(10);
        // bs1.run(10);
        bs2.run(10);
        c1.run(10);
        c2.run(10);
        c3.run(10);
    }

}
