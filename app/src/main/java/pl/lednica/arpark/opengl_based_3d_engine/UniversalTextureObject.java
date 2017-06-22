package pl.lednica.arpark.opengl_based_3d_engine;

import android.util.Log;

import java.util.Vector;

import pl.lednica.arpark.activities.object_explorer.ObjectExplorer3DActivity;
import pl.lednica.arpark.helpers.ObjectModel;

/**
 * Created by admin on 27.05.2017.
 */

public class UniversalTextureObject extends LightTextureRenderer {
    public Vector<ObjectFiles> objectFilesVector = new Vector<>();
    public UniversalTextureObject(ObjectExplorer3DActivity activity, ObjectModel objectModel) {
        super(activity,true);
        String path = objectModel.getModelPath();
        String model = objectModel.getModelName();
        String textureImage = objectModel.getTextureImage();
        Log.e("UniversalText", "Size "+objectModel.getMeshs().size());
        for(String obj : objectModel.getMeshs()) {
            objectFilesVector.add(new ObjectFiles(
                    path + "/" + model + "_" + obj + "_v_Model.dat",
                    path + "/" + model + "_" + obj + "_n_Model.dat",
                    path + "/" + model + "_" + obj + "_t_Model.dat",
                    path + "/" + model + "_" + obj + "_i_Model.dat",
                    path+"/"+textureImage
            ));
        }
        for(int i=0;i<objectFilesVector.size();i++){
            loadMesh(i,objectFilesVector.get(i));
        }
        setScale(objectModel.getScale());
    }
}
