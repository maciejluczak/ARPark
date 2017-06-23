package pl.lednica.arpark.helpers;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by admin on 07.05.2017.
 */

public class ObjectModel implements Serializable{
    private String name, modelName, modelPath, textureImage, desc, foto, iconPath;
    private Double latitude, longitude;
    private float scale;
    private ArrayList<String> meshs;
    public class ObjectModelInformation implements Serializable{
        public String name, desc, image;
        public ObjectModelInformation(String name, String desc, String image) {
            this.name = name;
            this.desc = desc;
            this.image = image;
        }
        public ObjectModelInformation(String name, String desc) {
            this.name = name;
            this.desc = desc;
        }
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    private ArrayList<ObjectModelInformation> informations;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelPath() {
        return modelPath;
    }

    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
    }

    public String getTextureImage() {
        return textureImage;
    }

    public void setTextureImage(String textureImage) {
        this.textureImage = textureImage;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public ArrayList<String> getMeshs() {
        return meshs;
    }

    public void setMeshs(ArrayList<String> meshs) {
        this.meshs = meshs;
    }

    public void addMeshs(String mesh) {
        this.meshs.add(mesh);
    }

    public ArrayList<ObjectModelInformation> getInformations() {
        return informations;
    }

    public void addInformation(String name, String desc) {
        informations.add(new ObjectModelInformation(name, desc));
    }
    public void addInformation(String name, String desc, String image) {
        informations.add(new ObjectModelInformation(name, desc, image));
    }

    public void setInformations(ArrayList<ObjectModelInformation> informations) {
        this.informations = informations;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }
}
