/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.experiments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class GenericExperiment
{
    static class Fruit implements Iterable<Fruit>
    {
        List<Fruit> fruits = new ArrayList<>();

        Fruit peel()
        {
            return this;
        }

        @Override public Iterator<Fruit> iterator()
        {
            return fruits.iterator();
        }
    }

    static class Banana extends Fruit
    {
        @Override Banana peel()
        {
            return this;
        }

        /*
        // Covariant return does not work here; Iterator<Banana> is not a
        // subtype of Iterator<Fruit>

        @Override public Iterator<Banana> iterator()
        {
            return fruits.iterator();
        }
         */
    }

    static class Apple extends Fruit
    {
    }

    interface Basket<T extends Fruit> extends Iterable<T>
    {
        Basket<T> select(String q);
        default <U extends Fruit> Basket<U> select(Class<U> c)
        {
            return (Basket<U>)this;
        }
    }

    interface Fruits extends Basket<Fruit>
    {
        Fruits select(String q);
    }

    static class FruitView implements Fruits
    {
        final List<Fruit> xxx = new ArrayList<>();

        FruitView()
        {
            xxx.add(new Banana());
        }

        @Override public Fruits select(String q)
        {
            return this;
        }

        @Override public Iterator<Fruit> iterator()
        {
            return xxx.iterator();
        }
    }

    public static void main(String[] args)
    {
        Fruits fruits = new FruitView();
        Fruits otherFruits = fruits.select("bananas");
        Basket<Fruit> basket = otherFruits;
        Basket<Apple> basket2 = basket.select(Apple.class);
        Basket<Banana> basket3 = basket.select(Banana.class);
        for(Banana a : basket3) System.out.println(a);
        Basket<Banana> bananas = fruits.select(Banana.class);
        for(Banana b : bananas) System.out.println(b);
    }
}
