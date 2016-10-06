package pl.lednica.arpark.opengl_based_3d_engine;

import android.content.res.AssetManager;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by Maciej on 2016-10-04.
 * Klasa ładująca i przechowujące obiek Mesh(obiekt 3d) float buffer
 */

public class MeshLoader {

    private String mFileName;
    private AssetManager mAssetManager;
    private FloatBuffer mModel;

    private final int mBytesPerFloat = 4;

    private final int mStrideBytes = 7 * mBytesPerFloat;

    private int mCount=0;


    public MeshLoader(String filename, AssetManager assetManager) {
        mFileName = filename;
        mAssetManager = assetManager;
    }

    public FloatBuffer loadToFloatBuffer() throws IOException{
        InputStream is = null;
        DataInputStream dis = null;
        ByteBuffer verts;
        try {
            is = mAssetManager.open(mFileName);
            dis = new DataInputStream(is);
            int count = is.available();

            int floatsToRead = count / mBytesPerFloat;
            mCount = count;
            verts = ByteBuffer.allocateDirect(floatsToRead * 4);
            verts.order(ByteOrder.nativeOrder());

            for (int i = 0; i < floatsToRead; i++) {
                float readTmp = dis.readFloat();
                verts.putFloat(readTmp);
            }
            verts.rewind();
            mModel = verts.asFloatBuffer();
        }
        finally {
            if (is != null)
                is.close();
            if (dis != null)
                dis.close();
        }
        return mModel;
    }

    public int getmCount() {
        return mCount;
    }

    public FloatBuffer getmModel() {
        return mModel;
    }
}
