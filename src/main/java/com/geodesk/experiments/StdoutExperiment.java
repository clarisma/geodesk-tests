/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.experiments;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;

public class StdoutExperiment
{
    public static void main(String[] args) throws Exception
    {
        Class<?> c = System.out.getClass();
        System.out.println("Class: " + c.getName());
        for (Field field : FieldUtils.getAllFields(c))
        {
            field.setAccessible(true);
            System.out.println("> " + field.getType().getSimpleName() + ' ' + field.getName() + " = " + field.get(System.out));
        }
    }
}
