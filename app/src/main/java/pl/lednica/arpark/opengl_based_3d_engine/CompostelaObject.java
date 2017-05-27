package pl.lednica.arpark.opengl_based_3d_engine;

import java.util.Vector;

import pl.lednica.arpark.activities.object_explorer.ObjectExplorer3DActivity;

/**
 * Created by Maciej on 2016-10-18.
 */

public class CompostelaObject extends LightTextureRenderer{
    public Vector<ObjectFiles> objectFilesVector = new Vector<>();

    public CompostelaObject(ObjectExplorer3DActivity activity) {
        super(activity);
        String path = "Compostela";
        String model = "cross";
        String[] modelObjects = new String[]{"cross"};
        for(String obj : modelObjects) {
            objectFilesVector.add(new ObjectFiles(
                    path + "/" + model + "_1_v_Model.dat",
                    path + "/" + model + "_1_n_Model.dat",
                    path + "/" + model + "_1_i_Model.dat",
                    path + "/" + model + "_1_c_Model.dat"
            ));
        }
        for(int i=0;i<objectFilesVector.size();i++){
            loadMesh(i,objectFilesVector.get(i));
        }
    }
}
