package pl.lednica.arpark.sensor_engine;

import android.location.Location;

/**
 * Created by Maciej on 2016-10-21.
 */

public class CompostelaGeo {
    private final static Location geoData = new Location("manual");
    static {
        geoData.setLatitude(42.880653d);
        geoData.setLongitude(-8.544430d);
        //geoData.setLatitude(52.4099868);
        //geoData.setLongitude(16.8683079);
        geoData.setAltitude(260.0d);
    }

    public static Location getGeoData() {
        return geoData;
    }
}
