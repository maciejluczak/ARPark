/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package pl.lednica.arpark.object_recognition_engine;;

import java.nio.Buffer;


public class BowlAndSpoonObject extends MeshObject
{
    // Data for drawing the 3D plane as overlay
    protected static final double cubeVertices[] =  BowlAndSpoonObjectModel.TEAPOT_VERTS;// BowlAndSpoonObjectModel.TEAPOT_VERTS;

    protected static final double cubeNormals[] = BowlAndSpoonObjectModel.TEAPOT_NORMS;

    protected static final double cubeTexcoords[] = BowlAndSpoonObjectModel.TEAPOT_TEX_COORDS;

    protected static final short cubeIndices[] = BowlAndSpoonObjectModel.TEAPOT_INDICES;

    protected Buffer mVertBuff;
    protected Buffer mTexCoordBuff;
    protected Buffer mNormBuff;
    protected Buffer mIndBuff;
    
    
    public BowlAndSpoonObject()
    {
        mVertBuff = fillBuffer(cubeVertices);
        mTexCoordBuff = fillBuffer(cubeTexcoords);
        mNormBuff = fillBuffer(cubeNormals);
        mIndBuff = fillBuffer(cubeIndices);
    }
    
    
    @Override
    public Buffer getBuffer(BUFFER_TYPE bufferType)
    {
        Buffer result = null;
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
        return cubeVertices.length / 3;
    }
    
    
    @Override
    public int getNumObjectIndex()
    {
        return cubeIndices.length;
    }
}
