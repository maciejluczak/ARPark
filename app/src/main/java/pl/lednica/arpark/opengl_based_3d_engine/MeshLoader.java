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

/**
 * Created by Maciej on 2016-10-04.
 * Klasa ładująca i przechowujące obiek Mesh(obiekt 3d) float buffer
 */

public class MeshLoader {

    private String mFileName;
    private AssetManager mAssetManager;
    private FloatBuffer mModel;

    public ByteBuffer mData;

    private int mBytesPerFloat = 4;

    private int mStrideBytes = 7 * mBytesPerFloat;

    private int mCount=0;

    private static String LOGTAG = "MeshLoader";
    private static int mtype;

    public enum BUFFER_TYPE
    {
        VERTEX, TEXTURE_COORD, NORMALS, INDICES
    }

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
            mCount =floatsToRead;// count / mStrideBytes;
            verts = ByteBuffer.allocateDirect(floatsToRead * mBytesPerFloat);
            verts.order(ByteOrder.nativeOrder());

            for (int i = 0; i < floatsToRead; i++) {
                float   readTmp = dis.readFloat();
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

    /*private ByteBuffer fillBuffer(DataInputStream dis, ){
        verts = ByteBuffer.allocateDirect(floatsToRead * mBytesPerFloat);
        verts.order(ByteOrder.nativeOrder());

        for (int i = 0; i < floatsToRead; i++) {
            float   readTmp = dis.readFloat();
            verts.putFloat(readTmp);
        }
        verts.rewind();
    }*/

    public FloatBuffer loadDummTextureCordinate(){
        final float[] cubeTextureCoordinateData =
                {
                        // Front face
                        0.0f, 1-0.0f,
                        0.0f, 1-1.0f,
                        1.0f, 1-0.0f,
                        1.0f, 1-0.0f,
                        0.0f, 1-1.0f,
                        1.0f, 1-1.0f,

                        // Right face
                        0.0f, 1-0.0f,
                        0.0f, 1-1.0f,
                        1.0f, 1-0.0f,
                        1.0f, 1-0.0f,
                        0.0f, 1-1.0f,
                        1.0f, 1-1.0f,

                        // Back face
                        0.0f, 1-0.0f,
                        0.0f, 1-1.0f,
                        1.0f, 1-0.0f,
                        1.0f, 1-0.0f,
                        0.0f, 1-1.0f,
                        1.0f, 1-1.0f,

                        // Left face
                        0.0f, 1-0.0f,
                        0.0f, 1-1.0f,
                        1.0f, 1-0.0f,
                        1.0f, 1-0.0f,
                        0.0f, 1-1.0f,
                        1.0f, 1-1.0f,

                        // Top face
                        0.0f, 1-0.0f,
                        0.0f, 1-1.0f,
                        1.0f, 1-0.0f,
                        1.0f, 1-0.0f,
                        0.0f, 1-1.0f,
                        1.0f, 1-1.0f,

                        // Bottom face
                        0.0f, 1-0.0f,
                        0.0f, 1-1.0f,
                        1.0f, 1-0.0f,
                        1.0f, 1-0.0f,
                        0.0f, 1-1.0f,
                        1.0f, 1-1.0f,
                };
        mModel = ByteBuffer.allocateDirect(cubeTextureCoordinateData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mModel.put(cubeTextureCoordinateData).position(0);
        return mModel;
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

    public int loadTextureFromApk(String fileName,
                                             AssetManager assets) {
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

    public int loadTextureFromIntBuffer(int[] data, int width,
                                                   int height)
    {
        // Convert:
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

    public int getmCount() {
        return mCount;
    }

    public void setmBytesPerFloat(int mBytesPerFloat) {
        this.mBytesPerFloat = mBytesPerFloat;
    }

    public void setmStrideBytes(int mStrideBytes) {
        this.mStrideBytes = mStrideBytes;
    }
}
