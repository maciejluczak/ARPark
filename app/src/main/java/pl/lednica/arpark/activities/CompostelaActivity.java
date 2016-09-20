package pl.lednica.arpark.activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ConfigurationInfo;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.FileOutputStream;
import java.text.DateFormat;
import java.util.Date;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

import pl.lednica.arpark.R;
import pl.lednica.arpark.sensor_engine.CameraView;
import pl.lednica.arpark.sensor_engine.SensorCameraViewRenderer;

public class CompostelaActivity extends Activity
        implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener,
        SensorEventListener {

    private static final String LOGTAG = "CompostelaActivity";


    //Variables to OpenGLESView with camera
    private GLSurfaceView glView;
    private CameraView cameraView;

    //Variables to Localisation
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationRequest mLocationRequest;
    private String mLastUpdateTime;

    //Variables to Orientation
    private SensorManager mSensorManager;
    private Sensor accelSensor;
    private Sensor compassSensor;
    private Sensor gyroSensor;
    private float[] accelerometrData = new float[3];
    private float[] geomagneticData= new float[3];
    private float[] gyroscopeData= new float[3];

    public static final String ACCELEROMETR_HISTORY_LOG_X = "accelerometr_history_x.csv";
    public static final String ACCELEROMETR_HISTORY_LOG_Y = "accelerometr_history_y.csv";
    public static final String ACCELEROMETR_HISTORY_LOG_Z = "accelerometr_history_z.csv";
    public static final String GEOMAGNETIC_HISTORY_LOG_X = "geomagnetic_history_x.csv";
    public static final String GEOMAGNETIC_HISTORY_LOG_Y = "geomagnetic_history_y.csv";
    public static final String GEOMAGNETIC_HISTORY_LOG_Z = "geomagnetic_history_z.csv";
    public static final String GYROSCOPE_HISTORY_LOG_X = "gyroscope_history_x.csv";
    public static final String GYROSCOPE_HISTORY_LOG_Y = "gyroscope_history_y.csv";
    public static final String GYROSCOPE_HISTORY_LOG_Z = "gyroscope_history_z.csv";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOGTAG, "onCreate");
        super.onCreate(savedInstanceState);

        //Create Google Localisation Service instance
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        //Get instance of SensorMenager
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        compassSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // When working with the camera, it's useful to stick to one orientation.
        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE );

        // Now also create a view which contains the camera preview...
        cameraView = new CameraView( this );

        // Next, we disable the application's title bar...
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        // ...and the notification bar. That way, we can use the full screen.
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN );

        // Now let's create an OpenGL surface.
        glView = new GLSurfaceView( this );

        // Check if the system supports OpenGL ES 2.0.
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;
        Log.i(LOGTAG,"supportsEs2 " + supportsEs2);
        if (supportsEs2)
        {
            // Request an OpenGL ES 2.0 compatible context.
            glView.setEGLContextClientVersion(2);

            // To see the camera preview, the OpenGL surface has to be created translucently.
            // See link above.
            glView.setEGLConfigChooser( new ConfigChooser( 8, 8, 8, 8, 16, 0) );
            glView.getHolder().setFormat( PixelFormat.TRANSLUCENT );
            // The renderer will be implemented in a separate class, GLView, which I'll show next.
            Log.i(LOGTAG,"Przed wywołaniem konstruktora SensorCameraViewRenderer");
            glView.setRenderer( new SensorCameraViewRenderer(this,cameraView) );
            // Now set this as the main view.
            setContentView( glView );
        }
        else
        {
            // This is where you could create an OpenGL ES 1.x compatible
            // renderer if you wanted to support both ES 1 and ES 2.
            Log.e(LOGTAG,"Błędna wersja OpenGLES");
            return;
        }


        // camera preview add and wrapping the full screen size.
        addContentView( cameraView, new WindowManager.LayoutParams( WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.
                LayoutParams.WRAP_CONTENT ) );
    }


    @Override
    protected void onResume() {
        //Connect to localisation
        mGoogleApiClient.connect();
        //Register Orientation sensor
        mSensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, compassSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);

        glView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        //Disconnect to localisation
        mGoogleApiClient.disconnect();
        //Disregister Orientation sensor
        mSensorManager.unregisterListener(this);

        glView.onPause();
        super.onPause();
    }

    @Override
    public void onConnected(Bundle bundle) {
        /*try{
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }catch (SecurityException e){
            Log.e("PERM ERR",e.toString());
        }
        if (mLastLocation != null) {
            mLatitudeTextView.setText(String.valueOf(mLastLocation.getLatitude()));
            mLongitudeTextView.setText(String.valueOf(mLastLocation.getLongitude()));
        }*/

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        try{
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }catch (SecurityException e){
            Log.e("PERM ERR",e.toString());
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("LocationFinderFragment", "Connection Suspended");
        mGoogleApiClient.connect();
    }
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i("LocationFinderFragment", "Connection failed. Error: " + result.getErrorCode());
    }
    ///data/data/magisterka.wph.wphfirst/files/accelerometr_history.log
    //C:\Users\Maciej\AppData\Local\Android\sdk\platform-tools
    @Override
    public void onLocationChanged(Location location) {
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        //mLatitudeTextView.setText(String.valueOf(location.getLatitude()));
        //mLongitudeTextView.setText(String.valueOf(location.getLongitude()));
        mLocation = location;
        Toast.makeText(this, "Updated: " + mLastUpdateTime, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        FileOutputStream outputStream;
        String tmpVal;
        //Toast.makeText(this, "Sensor: " + event.sensor.getName() +" Value: "+ event.values.toString(), Toast.LENGTH_LONG).show();
        switch(event.sensor.getType())
        {
            case Sensor.TYPE_ACCELEROMETER:
                accelerometrData = event.values;
                //accelerometrData[0] = 0.2f * event.values[0] + (1.0f - 0.2f) * accelerometrData[0];
                //accelerometrData[1] = 0.2f * event.values[1] + (1.0f - 0.2f) * accelerometrData[1];
                //accelerometrData[2] = 0.2f * event.values[2] + (1.0f - 0.2f) * accelerometrData[2];
                /*try {
                    outputStream = openFileOutput(ACCELEROMETR_HISTORY_LOG_X, Context.MODE_APPEND);
                    tmpVal = String.valueOf(accelerometrData[0]) + ";";
                    outputStream.write(tmpVal.getBytes());
                    outputStream.close();
                    outputStream = openFileOutput(ACCELEROMETR_HISTORY_LOG_Y, Context.MODE_APPEND);
                    tmpVal = String.valueOf(accelerometrData[1]) + ";";
                    outputStream.write(tmpVal.getBytes());
                    outputStream.close();
                    outputStream = openFileOutput(ACCELEROMETR_HISTORY_LOG_Z, Context.MODE_APPEND);
                    tmpVal = String.valueOf(accelerometrData[2]) + ";";
                    outputStream.write(tmpVal.getBytes());
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
                break;
            case Sensor.TYPE_GYROSCOPE:
                //gyroscopeData = event.values;
                gyroscopeData[0] = 0.2f * event.values[0] + (1.0f - 0.2f) * gyroscopeData[0];
                gyroscopeData[1] = 0.2f * event.values[1] + (1.0f - 0.2f) * gyroscopeData[1];
                gyroscopeData[2] = 0.2f * event.values[2] + (1.0f - 0.2f) * gyroscopeData[2];
                /*try {
                    outputStream = openFileOutput(GYROSCOPE_HISTORY_LOG_X, Context.MODE_APPEND);
                    tmpVal = String.valueOf(gyroscopeData[0]) + ";";
                    outputStream.write(tmpVal.getBytes());
                    outputStream.close();
                    outputStream = openFileOutput(GYROSCOPE_HISTORY_LOG_Y, Context.MODE_APPEND);
                    tmpVal = String.valueOf(gyroscopeData[1]) + ";";
                    outputStream.write(tmpVal.getBytes());
                    outputStream.close();
                    outputStream = openFileOutput(GYROSCOPE_HISTORY_LOG_Z, Context.MODE_APPEND);
                    tmpVal = String.valueOf(gyroscopeData[2]) + ";";
                    outputStream.write(tmpVal.getBytes());
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                geomagneticData = event.values;
                //geomagneticData[0] = 0.2f * event.values[0] + (1.0f - 0.2f) * geomagneticData[0];
                //geomagneticData[1] = 0.2f * event.values[1] + (1.0f - 0.2f) * geomagneticData[1];
                //geomagneticData[2] = 0.2f * event.values[2] + (1.0f - 0.2f) * geomagneticData[2];
               /* try {
                    outputStream = openFileOutput(GEOMAGNETIC_HISTORY_LOG_X, Context.MODE_APPEND);
                    tmpVal = String.valueOf(geomagneticData[0]) + ";";
                    outputStream.write(tmpVal.getBytes());
                    outputStream.close();
                    outputStream = openFileOutput(GEOMAGNETIC_HISTORY_LOG_Y, Context.MODE_APPEND);
                    tmpVal = String.valueOf(geomagneticData[1]) + ";";
                    outputStream.write(tmpVal.getBytes());
                    outputStream.close();
                    outputStream = openFileOutput(GEOMAGNETIC_HISTORY_LOG_Z, Context.MODE_APPEND);
                    tmpVal = String.valueOf(geomagneticData[2]) + ";";
                    outputStream.write(tmpVal.getBytes());
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Toast.makeText(this, "Accuracy: " + accuracy, Toast.LENGTH_LONG).show();
    }

    public float[] getAccelerometrData(){
        return accelerometrData;
    }
    public float[] getGeomagneticData(){
        return  geomagneticData;
    }
    public float[] getGyroscopeData(){
        return gyroscopeData;
    }
    public Location getmLocation(){
        return mLocation;
    }
    // The config chooser.
    private static class ConfigChooser implements
            GLSurfaceView.EGLConfigChooser
    {
        public ConfigChooser(int r, int g, int b, int a, int depth, int stencil)
        {
            mRedSize = r;
            mGreenSize = g;
            mBlueSize = b;
            mAlphaSize = a;
            mDepthSize = depth;
            mStencilSize = stencil;
        }


        private EGLConfig getMatchingConfig(EGL10 egl, EGLDisplay display,
                                            int[] configAttribs)
        {
            // Get the number of minimally matching EGL configurations
            int[] num_config = new int[1];
            egl.eglChooseConfig(display, configAttribs, null, 0, num_config);

            int numConfigs = num_config[0];
            if (numConfigs <= 0)
                throw new IllegalArgumentException("No matching EGL configs");

            // Allocate then read the array of minimally matching EGL configs
            EGLConfig[] configs = new EGLConfig[numConfigs];
            egl.eglChooseConfig(display, configAttribs, configs, numConfigs,
                    num_config);

            // Now return the "best" one
            return chooseConfig(egl, display, configs);
        }


        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display)
        {
            // This EGL config specification is used to specify 2.0
            // rendering. We use a minimum size of 4 bits for
            // red/green/blue, but will perform actual matching in
            // chooseConfig() below.
            final int EGL_OPENGL_ES2_BIT = 0x0004;
            final int[] s_configAttribs_gl20 = { EGL10.EGL_RED_SIZE, 4,
                    EGL10.EGL_GREEN_SIZE, 4, EGL10.EGL_BLUE_SIZE, 4,
                    EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                    EGL10.EGL_NONE };

            return getMatchingConfig(egl, display, s_configAttribs_gl20);
        }


        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
                                      EGLConfig[] configs)
        {
            for (EGLConfig config : configs)
            {
                int d = findConfigAttrib(egl, display, config,
                        EGL10.EGL_DEPTH_SIZE, 0);
                int s = findConfigAttrib(egl, display, config,
                        EGL10.EGL_STENCIL_SIZE, 0);

                // We need at least mDepthSize and mStencilSize bits
                if (d < mDepthSize || s < mStencilSize)
                    continue;

                // We want an *exact* match for red/green/blue/alpha
                int r = findConfigAttrib(egl, display, config,
                        EGL10.EGL_RED_SIZE, 0);
                int g = findConfigAttrib(egl, display, config,
                        EGL10.EGL_GREEN_SIZE, 0);
                int b = findConfigAttrib(egl, display, config,
                        EGL10.EGL_BLUE_SIZE, 0);
                int a = findConfigAttrib(egl, display, config,
                        EGL10.EGL_ALPHA_SIZE, 0);

                if (r == mRedSize && g == mGreenSize && b == mBlueSize
                        && a == mAlphaSize)
                    return config;
            }

            return null;
        }


        private int findConfigAttrib(EGL10 egl, EGLDisplay display,
                                     EGLConfig config, int attribute, int defaultValue)
        {

            if (egl.eglGetConfigAttrib(display, config, attribute, mValue))
                return mValue[0];

            return defaultValue;
        }

        // Subclasses can adjust these values:
        protected int mRedSize;
        protected int mGreenSize;
        protected int mBlueSize;
        protected int mAlphaSize;
        protected int mDepthSize;
        protected int mStencilSize;
        private int[] mValue = new int[1];
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
        finish();
    }

}
