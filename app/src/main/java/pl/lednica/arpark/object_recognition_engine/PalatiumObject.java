package pl.lednica.arpark.object_recognition_engine;

import android.content.res.AssetManager;

import java.io.IOException;
import java.nio.Buffer;

import pl.lednica.arpark.opengl_based_3d_engine.MeshLoader;

/**
 * Created by stachu on 06.10.2016.
 */
public class PalatiumObject extends BowlAndSpoonObject {

    private int numIndexes=0;
    public PalatiumObject(AssetManager inAssetManager) throws IOException {
        MeshLoader vertLoader = new MeshLoader("Palatium/v.dat", inAssetManager);
        numIndexes = vertLoader.getmCount();
        mVertBuff = vertLoader.loadToFloatBuffer();

        MeshLoader texLoader = new MeshLoader("Palatium/t.dat", inAssetManager);
        mTexCoordBuff = texLoader.loadToFloatBuffer();

        MeshLoader normLoader = new MeshLoader("Palatium/n.dat", inAssetManager);
        mNormBuff = normLoader.loadToFloatBuffer();

        MeshLoader indLoader = new MeshLoader("Palatium/i.dat", inAssetManager);
        mIndBuff = indLoader.loadToFloatBuffer();
    }
    @Override
    public Buffer getBuffer(BUFFER_TYPE bufferType)
    {
        Buffer result = null;


        //MeshLoader meshLoader = new MeshLoader("Palatium/i.dat");
        switch (bufferType)
        {
            case BUFFER_TYPE_VERTEX:
                result = mVertBuff;
                break;
            case BUFFER_TYPE_TEXTURE_COORD:
                result = mTexCoordBuff;
                break;
            case BUFFER_TYPE_INDICES:
                result = mIndBuff;
                break;
            case BUFFER_TYPE_NORMALS:
                result = mNormBuff;
            default:
                break;
        }
        return result;
    }


    @Override
    public int getNumObjectVertex()
    {
        return numIndexes / 3;
    }


    @Override
    public int getNumObjectIndex()
    {
        return numIndexes;
    }
}
