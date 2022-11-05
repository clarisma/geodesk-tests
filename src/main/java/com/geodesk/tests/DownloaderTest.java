/*
 * Copyright (c) Clarisma / GeoDesk contributors
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.geodesk.tests;

import com.clarisma.common.util.Log;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class DownloaderTest
{
    @Test public void testDownload() throws Exception
    {
        URL sourceUrl = new URL("http://data.geodesk.com/switzerland");
        HttpURLConnection conn = (HttpURLConnection)sourceUrl.openConnection();
        conn.setInstanceFollowRedirects(false);

        Log.debug("Original URL: %s", conn.getURL());
        conn.connect();

        Log.debug("Response code: %d", conn.getResponseCode());
        Log.debug("Response msg:  %s", conn.getResponseMessage());
        Log.debug("Connected URL: %s", conn.getURL());
        Map<String, List<String>> map = conn.getHeaderFields();
        for (Map.Entry<String, List<String>> entry : map.entrySet())
        {
            System.out.println("Key : " + entry.getKey() +
                " ,Value : " + entry.getValue());
        }

        /*
        log.info("Server response: {} {}",
            ((HttpURLConnection)conn).getResponseCode(),
            ((HttpURLConnection)conn).getResponseMessage());
        */

        // TODO: get Content-Length and Last-Modified

        InputStream in = conn.getInputStream();
        Log.debug("Redirected URL: %s", conn.getURL());
        ReadableByteChannel rbc = Channels.newChannel(in);
        FileOutputStream out = new FileOutputStream("c:\\geodesk\\debug\\download.html");
        out.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        in.close();
        out.close();
    }
}
