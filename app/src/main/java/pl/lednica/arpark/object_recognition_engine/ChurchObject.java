package pl.lednica.arpark.object_recognition_engine;

import java.nio.Buffer;

public class ChurchObject extends MeshObject
{
    protected static final double cubeVertices[] =  ChurchObjectModel.VERTS;

    protected static final double cubeNormals[] = ChurchObjectModel.NORMS;

    protected static final double cubeTexcoords[] = ChurchObjectModel.TEX_COORDS;

    protected static final short cubeIndices[] = ChurchObjectModel.INDICES;

    protected Buffer mVertBuff;
    protected Buffer mTexCoordBuff;
    protected Buffer mNormBuff;
    protected Buffer mIndBuff;

    public ChurchObject()
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