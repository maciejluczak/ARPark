package pl.lednica.arpark.helpers;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Button;

import pl.lednica.arpark.R;

/**
 * Created by stachu on 21.09.2016.
 */
public class MapObject {
    public int x;
    public int y;
    public boolean show=false;
    public String name;
    public Button button;
    public Class activityClass;

    public MapObject(double xIn, double yIn, String nameIn, Class classIn) {
        MapPosition pos = new MapPosition(xIn, yIn);
        x = pos.xOut;
        y = pos.yOut;
        name = nameIn;
        activityClass = classIn;
    }
}
