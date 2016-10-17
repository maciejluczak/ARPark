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
    private int numVertex=0;
    public PalatiumObject(AssetManager inAssetManager) throws IOException {
        MeshLoader mesh = new MeshLoader();
        mesh.loadToBuffer("Palatium/v.dat", MeshLoader.BUFFER_TYPE.BUFFER_TYPE_VERTEX,
                MeshLoader.BUFFER_DATA_TYPE.DATA_FLOAT,inAssetManager);
        numVertex = mesh.mCountVertices;
        numIndexes = mesh.mCountVertices * mesh.mPositionDataSize;
        mVertBuff = mesh.mModelVertices;

        mesh.loadToBuffer("Palatium/t.dat", MeshLoader.BUFFER_TYPE.BUFFER_TYPE_VERTEX,
                MeshLoader.BUFFER_DATA_TYPE.DATA_FLOAT,inAssetManager);
        mTexCoordBuff = mesh.mModelVertices;

        mesh.loadToBuffer("Palatium/n.dat", MeshLoader.BUFFER_TYPE.BUFFER_TYPE_VERTEX,
                MeshLoader.BUFFER_DATA_TYPE.DATA_FLOAT,inAssetManager);
        mNormBuff = mesh.mModelVertices;

        mesh.loadToBuffer("Palatium/i.dat", MeshLoader.BUFFER_TYPE.BUFFER_TYPE_VERTEX,
                MeshLoader.BUFFER_DATA_TYPE.DATA_FLOAT,inAssetManager);
        mIndBuff = mesh.mModelVertices;
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
        return numVertex;
    }


    @Override
    public int getNumObjectIndex()
    {
        return numIndexes;
    }
}
