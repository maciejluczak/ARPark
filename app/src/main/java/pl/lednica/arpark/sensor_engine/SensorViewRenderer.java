package pl.lednica.arpark.sensor_engine;
import android.location.Location;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.opengles.GL10;

import pl.lednica.arpark.activities.CompostelaActivity;
import pl.lednica.arpark.helpers.ObjectModel;
import pl.lednica.arpark.opengl_based_3d_engine.LightTextureRenderer;
import pl.lednica.arpark.opengl_based_3d_engine.UniversalColorObject;

/**
 * Created by admin on 31.05.2017.
 */

public class SensorViewRenderer extends UniversalColorObject{

    private CompostelaActivity mActivity;
    private CameraView mCameraView;

    private static final String LOGTAG = "SensorCameraViewRendere";

    private float wynik=0.0f;
    private float wynikLoad=0.0f;
    private float delta =0.0f;
    private boolean isInTheView=false;

    public SensorViewRenderer(CompostelaActivity activity, CameraView camera,ObjectModel objectModel){
        super(activity,objectModel);
        mActivity = activity;
        mCameraView = camera;
    }

    @Override
    public void onDrawFrame( GL10 gl ) {
        float deltaTmp;
        float orientation[] = mActivity.getSensorData().getOrientation();
        Location location = mActivity.getSensorData().getLocation();

        Location targetLocation = CompostelaGeo.getGeoData();

        float currentBearingToTarget = 0.0f;

        try {
            currentBearingToTarget = location.bearingTo(targetLocation);
            Log.e(LOGTAG,"Pozycja "+ currentBearingToTarget);
        }catch(NullPointerException e){
            Log.e(LOGTAG,"Brak wyznaczonej pozycji");
            return;
        }

        float d = 1.5f;
        float angleHFOV = mCameraView.getHorizontalFOV()/2;
        double xTan =(float) Math.tan(Math.toRadians(angleHFOV))*d*4.0f;
        if(currentBearingToTarget <= (float)((Math.toDegrees(orientation[0]) +angleHFOV+20) )){
            if(currentBearingToTarget >= (float)((Math.toDegrees(orientation[0]) -angleHFOV-20))){
                if((Math.toDegrees(orientation[0])>90.0f)&(currentBearingToTarget<-90.0f)){
                    deltaTmp = (float)(Math.toDegrees(orientation[0]) + currentBearingToTarget);
                }else{
                    if((Math.toDegrees(orientation[0]) < -90.0f)&(currentBearingToTarget>90.0f)) {
                        deltaTmp = (float)(Math.toDegrees(orientation[0]) + currentBearingToTarget);
                    }else{
                        deltaTmp = (float)(Math.toDegrees(orientation[0]) - currentBearingToTarget);
                    }
                }

                if (!Float.isNaN(deltaTmp)) {
                    if (Math.abs(delta - deltaTmp) > 10) {
                        delta = 0.5f * deltaTmp + (1.0f - 0.5f) * delta;
                        wynikLoad = (delta / (angleHFOV)) * (float) xTan;
                    }else{
                        if (Math.abs(delta - deltaTmp) > 5) {
                            delta = 0.2f * deltaTmp + (1.0f - 0.2f) * delta;
                            wynikLoad = (delta / (angleHFOV)) * (float) xTan;
                        } else {
                            if (Math.abs(delta - deltaTmp) >2 ) {
                                wynikLoad = (delta / (angleHFOV)) * (float) xTan;
                                delta = 0.1f * deltaTmp + (1.0f - 0.1f) * delta;
                            }
                        }
                    }
                }
                wynik = wynikLoad;
                isInTheView=true;

            }else{
                isInTheView=false;
            }
        }else{
            isInTheView=false;
        }
        Log.e(LOGTAG,"SuperOnDrawFrame");
        super.onDrawFrame(gl);
    }

    @Override
    protected void translateM() {
        Log.e(LOGTAG,"translateM");
        if (isInTheView) {
            Matrix.translateM(modelMatrix, 0, 0.0f - wynik, 0.0f, 0.0f);
            Matrix.translateM(modelMatrix, 0, 0.0f, 0.0f, -1.5f);
        }else {
            Matrix.translateM(modelMatrix,0,0.0f,-500.0f, 0.0f);
            delta=0;
        }
    }
}
