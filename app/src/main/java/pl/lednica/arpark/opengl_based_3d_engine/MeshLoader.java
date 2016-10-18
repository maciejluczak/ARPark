package pl.lednica.arpark.opengl_based_3d_engine;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static java.sql.Types.NULL;

/**
 * Created by Maciej on 2016-10-04.
 * Klasa ładująca i przechowujące obiek Mesh(obiekt 3d) float buffer
 */

public class MeshLoader {

    public FloatBuffer mModelVertices;
    public FloatBuffer mModelColors;
    public FloatBuffer mModelNormals;
    public FloatBuffer mModelTextures;
    public ByteBuffer mModelIndices;

    public int mCountVertices=0;
    public int mCountColors=0;
    public int mCountNormals=0;
    public int mCountTextures=0;
    public int mCountIndices=0;

    int mCubePositionsBufferIdx;
    int mCubeNormalsBufferIdx;
    int mCubeTexCoordsBufferIdx;
    int mCubeIndicesBufferIdx;

    public final int mBytesPerFloat = 4;
    public final int mBytesPerShort = 2;

    public final int mPositionDataSize = 3;
    public final int mColorDataSize = 4;
    public final int mNormalDataSize = 3;
    public final int mTextureDataSize = 2;



    public final int mStrideBytesVertex = mPositionDataSize * mBytesPerFloat;
    public final int mStrideBytesColor = mColorDataSize * mBytesPerFloat;
    public final int mStrideBytesNormal = mNormalDataSize * mBytesPerFloat;
    public final int mStrideBytesTexture = mTextureDataSize * mBytesPerFloat;


    private int mCount=0;

    private static String LOGTAG = "MeshLoader";


    public enum BUFFER_TYPE
    {
        BUFFER_TYPE_VERTEX, BUFFER_TYPE_TEXTURE_COORD, BUFFER_TYPE_NORMALS, BUFFER_TYPE_INDICES
    }
    public enum BUFFER_DATA_TYPE
    {
        DATA_FLOAT, DATA_SHORT
    }

    public void loadToBuffer(String filename, BUFFER_TYPE buffer_type,
                                   BUFFER_DATA_TYPE buffer_data_type , AssetManager assetManager) throws IOException{
        InputStream is = null;
        DataInputStream dis = null;
        ByteBuffer vertices;

        try {
            is = assetManager.open(filename);
            dis = new DataInputStream(is);
            int count = is.available();
            int numbersToRead = NULL;
            if(buffer_data_type == BUFFER_DATA_TYPE.DATA_FLOAT) {
                numbersToRead = count / mBytesPerFloat;
            }
            if(buffer_data_type == BUFFER_DATA_TYPE.DATA_SHORT) {
                numbersToRead = count / mBytesPerShort;
            }
            mCount =numbersToRead;
            vertices = ByteBuffer.allocateDirect(count);
            vertices.order(ByteOrder.nativeOrder());

            for (int i = 0; i < numbersToRead; i++) {
                if(buffer_data_type == BUFFER_DATA_TYPE.DATA_FLOAT) {
                    float readTmp = dis.readFloat();
                    vertices.putFloat(readTmp);
                }
                if(buffer_data_type == BUFFER_DATA_TYPE.DATA_SHORT) {
                    int readTmp = dis.readUnsignedShort();
                    vertices.putChar((char) readTmp);
                }
            }
            vertices.rewind();
        }
        finally {
            if (is != null)
                is.close();
            if (dis != null)
                dis.close();
        }

        switch (buffer_type)
        {
            case BUFFER_TYPE_VERTEX:
                mModelVertices = vertices.asFloatBuffer();
                mCountVertices = mCount / mPositionDataSize;
                Log.e(LOGTAG,"Count V: "+ mCountVertices);
                break;
            case BUFFER_TYPE_TEXTURE_COORD:
                mModelTextures = vertices.asFloatBuffer();
                mCountTextures = mCount / mTextureDataSize;
                Log.e(LOGTAG,"Count T: "+ mCountTextures);
                break;
            case BUFFER_TYPE_INDICES:
                mModelIndices = vertices;
                mCountIndices = mCount;
                Log.e(LOGTAG,"Count I: "+ mCountIndices);
                break;
            case BUFFER_TYPE_NORMALS:
                mModelNormals = vertices.asFloatBuffer();
                mCountNormals = mCount / mNormalDataSize;
                Log.e(LOGTAG,"Count N: "+ mCountNormals);
            default:
                break;
        }
    }

    public int loadTexture(final Context context, final int resourceId) {
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0)
        {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;   // No pre-scaling
            // Read in the resource
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        }

        if (textureHandle[0] == 0)
        {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }

    public int loadTextureFromApk(String fileName, AssetManager assets) {
        InputStream inputStream = null;
        try
        {
            inputStream = assets.open(fileName, AssetManager.ACCESS_BUFFER);

            BufferedInputStream bufferedStream = new BufferedInputStream(
                    inputStream);
            Bitmap bitMap = BitmapFactory.decodeStream(bufferedStream);

            int[] data = new int[bitMap.getWidth() * bitMap.getHeight()];
            bitMap.getPixels(data, 0, bitMap.getWidth(), 0, 0,
                    bitMap.getWidth(), bitMap.getHeight());

            return loadTextureFromIntBuffer(data, bitMap.getWidth(),
                    bitMap.getHeight());
        } catch (IOException e)
        {
            Log.e(LOGTAG, "Failed to log texture '" + fileName + "' from APK");
            Log.i(LOGTAG, e.getMessage());
            return 0;
        }
    }

    private int loadTextureFromIntBuffer(int[] data, int width, int height) {
        ByteBuffer mData;
        int numPixels = width * height;
        byte[] dataBytes = new byte[numPixels * 4];

        for (int p = 0; p < numPixels; ++p)
        {
            int colour = data[p];
            dataBytes[p * 4] = (byte) (colour >>> 16); // R
            dataBytes[p * 4 + 1] = (byte) (colour >>> 8); // G
            dataBytes[p * 4 + 2] = (byte) colour; // B
            dataBytes[p * 4 + 3] = (byte) (colour >>> 24); // A
        }


        mData = ByteBuffer.allocateDirect(dataBytes.length).order(
                ByteOrder.nativeOrder());
        int rowSize = width * 4;
        for (int r = 0; r < height; r++)
            mData.put(dataBytes, rowSize * (height - 1 - r),
                    rowSize);

        mData.rewind();

        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0)
        {
            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            // Load the bitmap into the bound texture.
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                    width, height, 0, GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE, mData);



            // Cleans variables
            dataBytes = null;
            data = null;
        }

        if (textureHandle[0] == 0)
        {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];

    }
}
