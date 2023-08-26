/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.debug;

//import com.geodesk.feature.FeatureLibrary;
//import com.geodesk.feature.Features;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Issue91
{
    /*

    public static void main2(String[] args)
    {
        FeatureLibrary library = new FeatureLibrary("c:\\geodesk\\tests\\de.gol"); //this is okay
        Features<?> movieTheaters = library.select("na[amenity=cinema]"); //throws an error
    }
*/

    public static void main(String[] args)
    {
        Locale jvmLocale = Locale.getDefault();
        System.out.println("Default JVM Locale:");
        System.out.println("Language: " + jvmLocale.getLanguage());
        System.out.println("Country: " + jvmLocale.getCountry());
        System.out.println("Display Name: " + jvmLocale.getDisplayName());

        String input = "na[xxx";
        Pattern pattern = Pattern.compile("[a-zA-Z_]\\w*");
        Matcher matcher = pattern.matcher(input);

        if(matcher.lookingAt())
		{
			System.out.println("Matched: " + input.substring(0, matcher.end()));
        }
    }
}

