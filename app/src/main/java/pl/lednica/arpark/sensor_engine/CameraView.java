package pl.lednica.arpark.sensor_engine;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by Maciej on 2016-07-05.
 */
public class CameraView extends SurfaceView implements SurfaceHolder.Callback {
    private Camera camera;
    private float horizontalFOV = 0.0f;
    private float verticalFOV = 0.0f;

    public CameraView(Context context ) {
        super( context );
        // We're implementing the Callback interface and want to get notified
        // about certain surface events.
        getHolder().addCallback( this );
        // We're changing the surface to a PUSH surface, meaning we're receiving
        // all buffer data from another component - the camera, in this case.
        getHolder().setType( SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS );
    }

    public void surfaceCreated( SurfaceHolder holder ) {
        // Once the surface is created, simply open a handle to the camera hardware.
        camera = Camera.open();
        verticalFOV = camera.getParameters().getVerticalViewAngle();
        horizontalFOV = camera.getParameters().getHorizontalViewAngle();

    }

    public void surfaceChanged( SurfaceHolder holder, int format, int width, int height ) {
        // This method is called when the surface changes, e.g. when it's size is set.
        // We use the opportunity to initialize the camera preview display dimensions.
        Log.e("FOV_R_C","w "+ width+" h "+ height);
        Camera.Parameters p = camera.getParameters();
        p.setPreviewSize( width, height );
        camera.setParameters( p );
        verticalFOV = camera.getParameters().getVerticalViewAngle();
        horizontalFOV = camera.getParameters().getHorizontalViewAngle();
        // We also assign the preview display to this surface...
        try {
            camera.setPreviewDisplay( holder );
        } catch( IOException e ) {
            e.printStackTrace();
        }
        // ...and start previewing. From now on, the camera keeps pushing preview
        // images to the surface.
        camera.startPreview();
    }

    public void surfaceDestroyed( SurfaceHolder holder ) {
        // Once the surface gets destroyed, we stop the preview mode and release
        // the whole camera since we no longer need it.
        camera.stopPreview();
        camera.release();
        camera = null;
    }
    public float getHorizontalFOV(){
        return horizontalFOV;
    }
    public  float getVerticalFOV(){
        return verticalFOV;
    }
}
