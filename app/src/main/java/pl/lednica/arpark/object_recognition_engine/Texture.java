/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package pl.lednica.arpark.object_recognition_engine;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;


public class Texture
{
    private static final String LOGTAG = "Vuforia_Texture";
    
    public int mWidth;
    public int mHeight;
    public int mChannels;
    public ByteBuffer mData;
    public int[] mTextureID = new int[1];
    public boolean mSuccess = false;
    
    
    public static Texture loadTextureFromApk(String fileName,
        AssetManager assets)
    {
        Log.e("Tex", fileName);
        Log.e("Tex", "load");

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
            return null;
        }
    }
    
    
    public static Texture loadTextureFromIntBuffer(int[] data, int width,
        int height)
    {
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
        
        Texture texture = new Texture();
        texture.mWidth = width;
        texture.mHeight = height;
        texture.mChannels = 4;
        
        texture.mData = ByteBuffer.allocateDirect(dataBytes.length).order(
            ByteOrder.nativeOrder());
        int rowSize = texture.mWidth * texture.mChannels;
        for (int r = 0; r < texture.mHeight; r++)
            texture.mData.put(dataBytes, rowSize * (texture.mHeight - 1 - r),
                rowSize);
        
        texture.mData.rewind();
        
        dataBytes = null;
        data = null;
        
        texture.mSuccess = true;
        return texture;
    }
}
