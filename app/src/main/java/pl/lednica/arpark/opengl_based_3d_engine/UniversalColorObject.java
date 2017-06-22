package pl.lednica.arpark.opengl_based_3d_engine;

import android.app.Activity;
import android.util.Log;

import java.util.Vector;

import pl.lednica.arpark.activities.object_explorer.ObjectExplorer3DActivity;
import pl.lednica.arpark.helpers.ObjectModel;

/**
 * Created by admin on 27.05.2017.
 */

public class UniversalColorObject extends LightTextureRenderer {
    public Vector<ObjectFiles> objectFilesVector = new Vector<>();
    public UniversalColorObject(Activity activity, ObjectModel objectModel) {
        super(activity,false);
        String path = objectModel.getModelPath();
        String model = objectModel.getModelName();
        Log.e("UniversalCol", "Size "+objectModel.getMeshs().size());
        for(String obj : objectModel.getMeshs()) {
            objectFilesVector.add(new ObjectFiles(
                    path + "/" + obj + "Model.dat"
            ));
        }
        for(int i=0;i<objectFilesVector.size();i++){
            loadMesh(i,objectFilesVector.get(i));
        }
        setScale(objectModel.getScale());
    }
}
