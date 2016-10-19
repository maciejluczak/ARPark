package pl.lednica.arpark.opengl_based_3d_engine;

import java.util.Vector;

import pl.lednica.arpark.activities.Object3DViewActivity;

/**
 * Created by Maciej on 2016-10-19.
 */

public class ChurchObjectFiles extends LightTextureRenderer{
    public Vector<ObjectFiles> objectFilesVector = new Vector<>();

    public ChurchObjectFiles(Object3DViewActivity activity) {
        super(activity);
        String path = "Church";
        String model = "kosciol-lednica";
        String textureImage = "kosciol_tekstura.png";
        String[] modelObjects = new String[]{"Cube"};
        for(String obj : modelObjects) {
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
    }
}
