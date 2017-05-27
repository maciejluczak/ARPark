package pl.lednica.arpark.object_recognition_engine;

import android.content.res.AssetManager;

import java.io.IOException;

import pl.lednica.arpark.opengl_based_3d_engine.MeshLoader;

/**
 * Created by stachu on 06.10.2016.
 */
public class ChurchObject extends BowlAndSpoonObject {

    MeshLoader meshLoader;
    public ChurchObject(AssetManager inAssetManager) throws IOException
    {
        meshLoader = new MeshLoader();
        meshLoader.loadToBuffer("Compostela/cubeText_Cube_1_v_Model.dat",
                MeshLoader.BUFFER_TYPE.BUFFER_TYPE_VERTEX, MeshLoader.BUFFER_DATA_TYPE.DATA_FLOAT, inAssetManager);
        meshLoader.loadToBuffer("Compostela/cubeText_Cube_1_t_Model.dat",
                MeshLoader.BUFFER_TYPE.BUFFER_TYPE_TEXTURE_COORD, MeshLoader.BUFFER_DATA_TYPE.DATA_FLOAT, inAssetManager);
        meshLoader.loadToBuffer("Compostela/cubeText_Cube_1_n_Model.dat",
                MeshLoader.BUFFER_TYPE.BUFFER_TYPE_NORMALS, MeshLoader.BUFFER_DATA_TYPE.DATA_FLOAT, inAssetManager);
        meshLoader.loadToBuffer("Compostela/cubeText_Cube_1_i_Model.dat",
                MeshLoader.BUFFER_TYPE.BUFFER_TYPE_INDICES, MeshLoader.BUFFER_DATA_TYPE.DATA_SHORT, inAssetManager);
        mVertBuff = meshLoader.mModelVertices;
        mTexCoordBuff = meshLoader.mModelTextures;
        mNormBuff = meshLoader.mModelNormals;
        mIndBuff = meshLoader.mModelIndices;
    }

    @Override
    public int getNumObjectVertex()
    {
        return meshLoader.mCountVertices;
    }


    @Override
    public int getNumObjectIndex()
    {
        return meshLoader.mCountIndices;
    }
}
