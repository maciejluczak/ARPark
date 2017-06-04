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
    private static String LOGTAG = "MeshLoader";

    private FloatBuffer mModelVertices;
    private FloatBuffer mModelColors;
    private FloatBuffer mModelNormals;
    private FloatBuffer mModelTextures;
    private ByteBuffer mModelIndices;

    private int mCountVertices=0;
    private int mCountColors=0;
    private int mCountNormals=0;
    private int mCountTextures=0;
    private int mCountIndices=0;

    private int mCubePositionsBufferIdx;
    private int mCubeNormalsBufferIdx;
    private int mCubeTexCoordsBufferIdx;
    private int mCubeIndicesBufferIdx;

    private final int mBytesPerFloat = 4;
    private final int mBytesPerShort = 2;

    private final int mPositionDataSize = 3;
    private final int mColorDataSize = 4;
    private final int mNormalDataSize = 3;
    private final int mTextureDataSize = 2;
    private final int mPositionOffset = 0;
    private final int mColorOffset = 3;
    private final int mStrideBytesColor = (mColorDataSize+mPositionDataSize) * mBytesPerFloat;

    public enum BUFFER_TYPE
    {
        BUFFER_TYPE_VERTEX, BUFFER_TYPE_TEXTURE_COORD, BUFFER_TYPE_NORMALS, BUFFER_TYPE_INDICES, BUFFER_TYPE_COLOR
    }
    public enum BUFFER_DATA_TYPE
    {
        DATA_FLOAT, DATA_SHORT
    }

    /**Ładuje dane graficzne do buforów*/
    public void loadToBuffer(String filename, BUFFER_TYPE buffer_type,
                                   BUFFER_DATA_TYPE buffer_data_type , AssetManager assetManager) throws IOException{
        InputStream is = null;
        DataInputStream dis = null;
        ByteBuffer vertices;
        int mCount=0;
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
            case BUFFER_TYPE_COLOR:
                mModelColors = vertices.asFloatBuffer();
                mCountColors = mCount / (mColorDataSize+mPositionDataSize);
            default:
                break;
        }
    }

    /**Wczytuje obraz tekstury z zasobów aplikacji - zwraca uchwyt z OpenGL do niej*/
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

    /**Ładuje wczytaną wcześniej teksturę do pomięci OpenGL i zwraca uchwyt*/
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
            mData.put(dataBytes, rowSize * (height - 1 - r), rowSize);
        mData.rewind();
        final int[] textureHandle = new int[1];
        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0)
        {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                    width, height, 0, GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE, mData);
            dataBytes = null;
            data = null;
        }

        if (textureHandle[0] == 0)
        {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];

    }
    public int getmPositionOffset() {
        return mPositionOffset;
    }

    public int getmColorOffset() {
        return mColorOffset;
    }

    public int getmPositionDataSize() {
        return mPositionDataSize;
    }

    public int getmStrideBytesColor() {
        return mStrideBytesColor;
    }

    public FloatBuffer getmModelColors() {
        return mModelColors;
    }

    public int getmColorDataSize() {
        return mColorDataSize;
    }

    public int getmCountColors() {
        return mCountColors;
    }

    public int getmBytesPerFloat() {
        return mBytesPerFloat;
    }

    public int getmBytesPerShort() {
        return mBytesPerShort;
    }

    public FloatBuffer getmModelVertices() {
        return mModelVertices;
    }

    public FloatBuffer getmModelNormals() {
        return mModelNormals;
    }

    public FloatBuffer getmModelTextures() {
        return mModelTextures;
    }

    public ByteBuffer getmModelIndices() {
        return mModelIndices;
    }

    public int getmNormalDataSize() {
        return mNormalDataSize;
    }

    public int getmTextureDataSize() {
        return mTextureDataSize;
    }

    public int getmCountIndices() {
        return mCountIndices;
    }

    public int getmCubePositionsBufferIdx() {
        return mCubePositionsBufferIdx;
    }

    public int getmCubeNormalsBufferIdx() {
        return mCubeNormalsBufferIdx;
    }

    public int getmCubeTexCoordsBufferIdx() {
        return mCubeTexCoordsBufferIdx;
    }

    public int getmCubeIndicesBufferIdx() {
        return mCubeIndicesBufferIdx;
    }

    public void setmCubePositionsBufferIdx(int mCubePositionsBufferIdx) {
        this.mCubePositionsBufferIdx = mCubePositionsBufferIdx;
    }

    public void setmCubeNormalsBufferIdx(int mCubeNormalsBufferIdx) {
        this.mCubeNormalsBufferIdx = mCubeNormalsBufferIdx;
    }

    public void setmCubeTexCoordsBufferIdx(int mCubeTexCoordsBufferIdx) {
        this.mCubeTexCoordsBufferIdx = mCubeTexCoordsBufferIdx;
    }

    public void setmCubeIndicesBufferIdx(int mCubeIndicesBufferIdx) {
        this.mCubeIndicesBufferIdx = mCubeIndicesBufferIdx;
    }

    public int getmCountVertices() {
        return mCountVertices;
    }
}
