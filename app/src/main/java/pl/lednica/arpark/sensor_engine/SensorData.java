package pl.lednica.arpark.sensor_engine;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.util.Log;

/**
 * Created by Maciej on 2016-10-21.
 */

public class SensorData {
    private final static String LOGTAG = "SensorData";

    /** Orginalne dane z sensorów*/
    private float[] accelerometrOrginalData = new float[3];
    private float[] geomagneticOrginalData= new float[3];
    private float[] gyroscopeOrginalData= new float[3];

    /** Doane s sensorów wyliczone na podstawie filtru */
    private float[] accelerometrMeanData = new float[3];
    private float[] geomagneticMeanData= new float[3];
    private float[] gyroscopeMeanData= new float[3];

    /**Lokalizajc rzadzenia*/
    private Location location;

    /**orientacja urzadzenia*/
    private float orientation[] = new float[3];

    public void setSensorData(float[] dataTab, int dataType){
        switch(dataType) {
            case Sensor.TYPE_ACCELEROMETER:
                accelerometrOrginalData = dataTab;
                break;
            case Sensor.TYPE_GYROSCOPE:
                gyroscopeOrginalData = dataTab;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                geomagneticOrginalData = dataTab;
                break;
        }
        calcOrientation();
    }

    public void calcOrientation() {
        float rotation[] = new float[9];
        float identity[] = new float[9];
        boolean gotRotation = SensorManager.getRotationMatrix(rotation,
                identity, accelerometrOrginalData, geomagneticOrginalData);
        if (gotRotation)
        {
            float cameraRotation[] = new float[9];
            /**Przestawienie osi by wynik oriencacji by kamera była skierowana w duł osi Y*/
            SensorManager.remapCoordinateSystem(rotation, SensorManager.AXIS_X,
                    SensorManager.AXIS_Z, cameraRotation);
            SensorManager.getOrientation(cameraRotation, orientation);
        }else{
            Log.e(LOGTAG,"Błąd wyliczania orientacji");
        }
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public float[] getOrientation() {
        return orientation;
    }
}
