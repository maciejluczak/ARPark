package pl.lednica.arpark.helpers;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Maciej Łuczak on 07.05.2017.
 * Clasa ładuje deklaracje modeli z zasobów i przechowuje w postaci listy.
 */

public class ObjectJsonUtils {
    private final static String ARR_OBJECTS = "objects";
    private final static String VAR_NAME = "name";
    private final static String OBJ_POS= "position";
    private final static String VAR_POSITION_LAT = "lat";
    private final static String VAR_POSITION_LONG = "long";
    private final static String OBJ_MODEL= "model";
    private final static String OBJ_MODEL_META= "meta";
    private final static String VAR_MODEL_META_NAME = "name";
    private final static String VAR_MODEL_META_PATH = "path";
    private final static String VAR_MODEL_META_TEXTUREIMAGE = "textureImage";
    private final static String VAR_MODEL_META_SCALE = "scale";
    private final static String ARR_MODEL_MESH= "mesh";
    private final static String VAR_MODEL_MESH_NAME= "name";
    private final static String OBJ_DETAILS = "details";
    private final static String VAR_DETAILS_DESC = "description";
    private final static String VAR_DETAILS_ICON = "icon";
    private final static String ARR_DETAILS_INFO = "information";
    private final static String VAR_DETAILS_INFO_NAME = "name";
    private final static String VAR_DETAILS_INFO_TEXT = "text";
    private final static String VAR_DETAILS_INFO_IMAGE = "image";

    private ArrayList<ObjectModel> objectsList;
    private Context context;
    public ObjectJsonUtils(Context con) {
        this.context = con;
        objectsList = new ArrayList<>();
        try {
            objectsList = getObjectsDataFromJson(loadJSON());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<ObjectModel> getObjectsDataFromJson(String objectDefinitionJsonStr) throws JSONException {
        ArrayList<ObjectModel> objectModels = new ArrayList<>();
        JSONObject objectsDefinitionJson = new JSONObject(objectDefinitionJsonStr);
        if (objectsDefinitionJson.has(ARR_OBJECTS)) {
            JSONArray objectsJson = objectsDefinitionJson.getJSONArray(ARR_OBJECTS);
            for (int i = 0; i< objectsJson.length();i++){
                ObjectModel modelDefinition = new ObjectModel();
                JSONObject objectJson = objectsJson.getJSONObject(i);
                if(objectJson.has(VAR_NAME)){
                    modelDefinition.setName(objectJson.getString(VAR_NAME));
                }
                if (objectJson.has(OBJ_POS)){
                    JSONObject position = objectJson.getJSONObject(OBJ_POS);
                    if(position.has(VAR_POSITION_LAT)){
                        modelDefinition.setLatitude(position.getDouble(VAR_POSITION_LAT));
                    }
                    if(position.has(VAR_POSITION_LONG)){
                        modelDefinition.setLongitude(position.getDouble(VAR_POSITION_LONG));
                    }
                }
                if(objectJson.has(OBJ_MODEL)){
                    JSONObject model = objectJson.getJSONObject(OBJ_MODEL);
                    if(model.has(OBJ_MODEL_META)){
                        JSONObject meta = model.getJSONObject(OBJ_MODEL_META);
                        if(meta.has(VAR_MODEL_META_NAME)){
                            modelDefinition.setModelName(meta.getString(VAR_MODEL_META_NAME));
                        }
                        if(meta.has(VAR_MODEL_META_PATH)){
                            modelDefinition.setModelPath(meta.getString(VAR_MODEL_META_PATH));
                        }
                        if (meta.has(VAR_MODEL_META_TEXTUREIMAGE)){
                            modelDefinition.setTextureImage(meta.getString(VAR_MODEL_META_TEXTUREIMAGE));
                        }
                        if (meta.has(VAR_MODEL_META_SCALE)){
                            modelDefinition.setScale((float) meta.getDouble(VAR_MODEL_META_SCALE));
                        }
                    }
                    if(model.has(ARR_MODEL_MESH)){
                        Log.e("LoadMesh", modelDefinition.getName());
                        modelDefinition.setMeshs(new ArrayList<String>());
                        JSONArray meshs = model.getJSONArray(ARR_MODEL_MESH);
                        for (int j = 0; j< meshs.length();j++){
                            JSONObject mesh = meshs.getJSONObject(j);
                            if(mesh.has(VAR_MODEL_MESH_NAME)){
                                Log.e("LoadMesh", mesh.getString(VAR_MODEL_MESH_NAME));
                                modelDefinition.addMeshs(mesh.getString(VAR_MODEL_MESH_NAME));
                            }
                        }
                    }
                }
                if(objectJson.has(OBJ_DETAILS)){
                    JSONObject details = objectJson.getJSONObject(OBJ_DETAILS);
                    if(details.has(VAR_DETAILS_DESC)){
                        modelDefinition.setDesc(details.getString(VAR_DETAILS_DESC));
                    }
                    if(details.has(VAR_DETAILS_ICON)){
                        modelDefinition.setIconPath(details.getString(VAR_DETAILS_ICON));
                    }
                    if(details.has(ARR_DETAILS_INFO)){
                        modelDefinition.setInformations(new ArrayList<ObjectModel.ObjectModelInformation>());
                        JSONArray infos = details.getJSONArray(ARR_DETAILS_INFO);
                        for (int k=0;k<infos.length();k++){
                            JSONObject info = infos.getJSONObject(k);
                            if(info.has(VAR_DETAILS_INFO_NAME) && info.has(VAR_DETAILS_INFO_TEXT) && info.has(VAR_DETAILS_INFO_IMAGE)){
                                modelDefinition.addInformation(info.getString(VAR_DETAILS_INFO_NAME),
                                                                info.getString(VAR_DETAILS_INFO_TEXT),
                                                                info.getString(VAR_DETAILS_INFO_IMAGE));
                            }else{
                                if(info.has(VAR_DETAILS_INFO_NAME) && info.has(VAR_DETAILS_INFO_TEXT)){
                                    modelDefinition.addInformation(info.getString(VAR_DETAILS_INFO_NAME),
                                                                    info.getString(VAR_DETAILS_INFO_TEXT));
                                }
                            }
                        }
                    }
                }
                objectModels.add(modelDefinition);
            }
        }
        return  objectModels;
    }

    public String loadJSON() {
        String json = null;
        try {
            InputStream is = context.getAssets().open("General/ModelsDefinition.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public ArrayList<ObjectModel> getObjectsList() {
        return objectsList;
    }
}
