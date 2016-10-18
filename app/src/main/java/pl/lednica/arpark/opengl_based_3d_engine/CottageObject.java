package pl.lednica.arpark.opengl_based_3d_engine;

import java.util.Vector;

import pl.lednica.arpark.activities.Object3DViewActivity;

/**
 * Created by Maciej on 2016-10-18.
 */

public class CottageObject extends LightTextureRenderer {

    public Vector<ObjectFiles> objectFilesVector = new Vector<>();

    public CottageObject(Object3DViewActivity activity) {
        super(activity);
        String path = "Cottage";
        String model = "cottageClear";
        String textureImage = "cottage_texture.png";
        String[] modelObjects = new String[]{"parter","Belki","dach1","dach2"};
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
