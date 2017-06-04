package pl.lednica.arpark.opengl_based_3d_engine;

import java.util.Vector;

import pl.lednica.arpark.activities.object_explorer.ObjectExplorer3DActivity;

/**
 * Created by Maciej on 2016-10-19.
 */

public class ChurchObjectFiles extends LightTextureRenderer{
    public Vector<ObjectFiles> objectFilesVector = new Vector<>();

    public ChurchObjectFiles(ObjectExplorer3DActivity activity) {
        super(activity,true);
        String path = "Church";
        String model = "kosciol-lednica";
        String textureImage = "kosciol_tekstura.png";
        String[] modelObjects = new String[]{"Cube"};
        for(String obj : modelObjects) {
            objectFilesVector.add(new ObjectFiles(
                    path + "/" + model + "_" + obj + "_1_v_Model.dat",
                    path + "/" + model + "_" + obj + "_1_n_Model.dat",
                    path + "/" + model + "_" + obj + "_1_t_Model.dat",
                    path + "/" + model + "_" + obj + "_1_i_Model.dat",
                    path+"/"+textureImage
            ));
        }
        for(int i=0;i<objectFilesVector.size();i++){
            loadMesh(i,objectFilesVector.get(i));
        }
    }
}
