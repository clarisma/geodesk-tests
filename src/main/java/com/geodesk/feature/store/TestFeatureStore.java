package com.geodesk.feature.store;

import java.nio.ByteBuffer;
import java.nio.file.Path;

public class TestFeatureStore extends FeatureStore
{
    public TestFeatureStore(Path path)
    {
        setPath(path);
        open();
        enableQueries();
    }

    public ByteBuffer baseMapping()
    {
        return baseMapping;
    }

    public int tileIndexPointer()
    {
        return super.tileIndexPointer();
    }

    public int zoomLevels()
    {
        return super.zoomLevels();
    }

}
