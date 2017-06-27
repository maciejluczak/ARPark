package pl.lednica.arpark.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.vuforia.CameraDevice;
import com.vuforia.DataSet;
import com.vuforia.ImageTarget;
import com.vuforia.Matrix34F;
import com.vuforia.MultiTarget;
import com.vuforia.MultiTargetResult;
import com.vuforia.ObjectTracker;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.Tracker;
import com.vuforia.TrackerManager;
import com.vuforia.Vec3F;
import com.vuforia.Vuforia;
import com.vuforia.STORAGE_TYPE;


import java.io.IOException;
import java.util.Vector;

import pl.lednica.arpark.R;
import pl.lednica.arpark.object_recognition_engine.CustomApplicationSession;
import pl.lednica.arpark.object_recognition_engine.LoadingDialogHandler;
import pl.lednica.arpark.object_recognition_engine.MultiTargetRenderer;
import pl.lednica.arpark.object_recognition_engine.PalatiumObject;
import pl.lednica.arpark.object_recognition_engine.CustomApplicationControl;
import pl.lednica.arpark.object_recognition_engine.CustomApplicationException;
import pl.lednica.arpark.object_recognition_engine.CustomApplicationGLView;
import pl.lednica.arpark.object_recognition_engine.Texture;

public class PalatiumActivity extends Activity implements CustomApplicationControl
{
    private static final String LOGTAG = "MultiTargets";

    CustomApplicationSession vuforiaAppSession;

    
    private CustomApplicationGLView mGlView;

    
    private MultiTargetRenderer mRenderer;

    private RelativeLayout mUILayout;

    private GestureDetector mGestureDetector;



    private LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(
            this);

    
    private Vector<Texture> mTextures;

    private MultiTarget mit = null;

    private DataSet dataSet = null;

    
    private AlertDialog mErrorDialog;

    boolean mIsDroidDevice = false;


    
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(LOGTAG, "onCreate");
        super.onCreate(savedInstanceState);

        vuforiaAppSession = new CustomApplicationSession(this);

        startLoadingAnimation();

        vuforiaAppSession
                .initAR(this, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        
        mTextures = new Vector<Texture>();
        loadTextures();

        mGestureDetector = new GestureDetector(this, new GestureListener());

        mIsDroidDevice = android.os.Build.MODEL.toLowerCase().startsWith(
                "droid");

    }

    
    private class GestureListener extends
            GestureDetector.SimpleOnGestureListener
    {
        
        private final Handler autofocusHandler = new Handler();


        @Override
        public boolean onDown(MotionEvent e)
        {
            return true;
        }


        @Override
        public boolean onSingleTapUp(MotionEvent e)
        {
            
            
            autofocusHandler.postDelayed(new Runnable()
            {
                public void run()
                {
                    boolean result = CameraDevice.getInstance().setFocusMode(
                            CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO);

                    if (!result)
                        Log.e("SingleTapUp", "Unable to trigger focus");
                }
            }, 1000L);

            return true;
        }
    }


    
    
    private void loadTextures()
    {
        mTextures.add(Texture.loadTextureFromApk(
                "Textures/palatium.png", getAssets()));

    }


    
    @Override
    protected void onResume()
    {
        Log.d(LOGTAG, "onResume");
        super.onResume();

        
        if (mIsDroidDevice)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        try
        {
            vuforiaAppSession.resumeAR();
        } catch (CustomApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }

        
        if (mGlView != null)
        {
            mGlView.setVisibility(View.VISIBLE);
            mGlView.onResume();
        }

    }


    @Override
    public void onConfigurationChanged(Configuration config)
    {
        Log.d(LOGTAG, "onConfigurationChanged");
        super.onConfigurationChanged(config);

        vuforiaAppSession.onConfigurationChanged();
    }


    
    @Override
    protected void onPause()
    {
        Log.d(LOGTAG, "onPause");
        super.onPause();

        if (mGlView != null)
        {
            mGlView.setVisibility(View.INVISIBLE);
            mGlView.onPause();
        }

        try
        {
            vuforiaAppSession.pauseAR();
        } catch (CustomApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }
    }


    
    @Override
    protected void onDestroy()
    {
        Log.d(LOGTAG, "onDestroy");
        super.onDestroy();

        try
        {
            vuforiaAppSession.stopAR();
        } catch (CustomApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }

        
        mTextures.clear();
        mTextures = null;

        System.gc();
    }

    
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        return mGestureDetector.onTouchEvent(event);
    }


    private void startLoadingAnimation()
    {
        LayoutInflater inflater = LayoutInflater.from(this);
        mUILayout = (RelativeLayout) inflater.inflate(R.layout.camera_overlay,
                null, false);

        mUILayout.setVisibility(View.VISIBLE);
        mUILayout.setBackgroundColor(Color.BLACK);

        
        loadingDialogHandler.mLoadingDialogContainer = mUILayout
                .findViewById(R.id.loading_indicator);

        
        loadingDialogHandler
                .sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);

        
        addContentView(mUILayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }


    
    private void initApplicationAR()
    {
        
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();

        
        mGlView = new CustomApplicationGLView(this);
        mGlView.init(translucent, depthSize, stencilSize);

        

        mRenderer = new MultiTargetRenderer(vuforiaAppSession, new PalatiumObject());
        mRenderer.setTextures(mTextures);
        mGlView.setRenderer(mRenderer);

    }

    
    
    void initMIT()
    {
        
        
        
        
        
        
        

        Log.d(LOGTAG, "Beginning to check the tracking setup");

        
        
        
        
        
        
        

        String names[] = { "wphmarker.Front", "wphmarker.Back",
                "wphmarker.Left", "wphmarker.Right", "wphmarker.Top",
                "wphmarker.Bottom" };
        float trans[] = { 0.0f, 0.0f, 30.0f, 0.0f, 0.0f, -30.0f, -45.0f, 0.0f,
                0.0f, 45.0f, 0.0f, 0.0f, 0.0f, 60.0f, 0.0f, 0.0f, -60.0f, 0.0f };
        float rots[] = { 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 180.0f,
                0.0f, 1.0f, 0.0f, -90.0f, 0.0f, 1.0f, 0.0f, 90.0f, 1.0f, 0.0f,
                0.0f, -90.0f, 1.0f, 0.0f, 0.0f, 90.0f };

        TrackerManager trackerManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) trackerManager
                .getTracker(ObjectTracker.getClassType());

        if (objectTracker == null || dataSet == null)
        {
            return;
        }

        
        
        for (int i = 0; i < dataSet.getNumTrackables(); i++)
        {
            if (dataSet.getTrackable(i).getType() == MultiTargetResult
                    .getClassType())
            {
                Log.d(LOGTAG, "MultiTarget exists -> no need to create one");
                mit = (MultiTarget) (dataSet.getTrackable(i));
                break;
            }
        }

        
        if (mit == null)
        {
            Log.d(LOGTAG, "No MultiTarget found -> creating one");
            mit = dataSet.createMultiTarget("wphmarker");

            if (mit == null)
            {
                Log.d(LOGTAG,
                        "ERROR: Failed to create the MultiTarget - probably the Tracker is running");
                return;
            }
        }

        
        
        
        
        
        
        
        int numAdded = 0;
        for (int i = 0; i < 6; i++)
        {
            ImageTarget it = findImageTarget(names[i]);
            if (it != null)
            {
                Log.d(LOGTAG,
                        "ImageTarget '%s' found -> adding it as to the MultiTarget"
                                + names[i]);

                int idx = mit.addPart(it);
                Vec3F t = new Vec3F(trans[i * 3], trans[i * 3 + 1],
                        trans[i * 3 + 2]);
                Vec3F a = new Vec3F(rots[i * 4], rots[i * 4 + 1],
                        rots[i * 4 + 2]);
                Matrix34F mat = new Matrix34F();

                Tool.setTranslation(mat, t);
                Tool.setRotation(mat, a, rots[i * 4 + 3]);
                mit.setPartOffset(idx, mat);
                numAdded++;
            }
        }

        Log.d(LOGTAG, "Added " + numAdded
                + " ImageTarget(s) to the MultiTarget");

        if (mit.getNumParts() != 6)
        {
            Log.d(LOGTAG,
                    "ERROR: The MultiTarget should have 6 parts, but it reports "
                            + mit.getNumParts() + " parts");
        }

        Log.d(LOGTAG, "Finished checking the tracking setup");
    }


    ImageTarget findImageTarget(String name)
    {
        TrackerManager trackerManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) trackerManager
                .getTracker(ObjectTracker.getClassType());

        if (objectTracker != null)
        {
            for (int i = 0; i < dataSet.getNumTrackables(); i++)
            {
                if (dataSet.getTrackable(i).getType() == MultiTargetResult
                        .getClassType())
                {
                    if (dataSet.getTrackable(i).getName().compareTo(name) == 0)
                        return (ImageTarget) (dataSet.getTrackable(i));
                }
            }
        }
        return null;
    }


    @Override
    public boolean doInitTrackers()
    {
        
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        Tracker tracker;

        tracker = tManager.initTracker(ObjectTracker.getClassType());
        if (tracker == null)
        {
            Log.e(
                    LOGTAG,
                    "Tracker not initialized. Tracker already initialized or the camera is already started");
            result = false;
        } else
        {
            Log.i(LOGTAG, "Tracker successfully initialized");
        }

        return result;
    }


    @Override
    public boolean doLoadTrackersData()
    {
        
        TrackerManager trackerManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) trackerManager
                .getTracker(ObjectTracker.getClassType());
        if (objectTracker == null)
        {
            Log.d(
                    LOGTAG,
                    "Failed to load tracking data set because the ObjectTracker has not been initialized.");
            return false;
        }

        
        dataSet = objectTracker.createDataSet();
        if (dataSet == null)
        {
            Log.d(LOGTAG, "Failed to create a new tracking data.");
            return false;
        }

        
        if (!dataSet.load("Target/WPH.xml",
                STORAGE_TYPE.STORAGE_APPRESOURCE))
        {
            Log.d(LOGTAG, "Failed to load data set.");
            return false;
        }

        Log.d(LOGTAG, "Successfully loaded the data set.");

        
        initMIT();

        if (!objectTracker.activateDataSet(dataSet))
            return false;

        return true;
    }


    @Override
    public boolean doStartTrackers()
    {
        
        boolean result = true;

        Tracker objectTracker = TrackerManager.getInstance().getTracker(
                ObjectTracker.getClassType());
        if (objectTracker != null)
            objectTracker.start();

        return result;
    }


    @Override
    public boolean doStopTrackers()
    {
        
        boolean result = true;

        Tracker objectTracker = TrackerManager.getInstance().getTracker(
                ObjectTracker.getClassType());
        if (objectTracker != null)
            objectTracker.stop();

        return result;
    }


    @Override
    public boolean doUnloadTrackersData()
    {
        
        boolean result = true;

        
        TrackerManager trackerManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) trackerManager
                .getTracker(ObjectTracker.getClassType());
        if (objectTracker == null)
        {
            Log.d(
                    LOGTAG,
                    "Failed to destroy the tracking data set because the ObjectTracker has not been initialized.");
            return false;
        }

        if (dataSet != null)
        {
            if (!objectTracker.deactivateDataSet(dataSet))
            {
                Log.d(
                        LOGTAG,
                        "Failed to destroy the tracking data set because the data set could not be deactivated.");
                result = false;
            } else if (!objectTracker.destroyDataSet(dataSet))
            {
                Log.d(LOGTAG, "Failed to destroy the tracking data set.");
                result = false;
            }

            if (result)
                Log.d(LOGTAG, "Successfully destroyed the data set.");

            dataSet = null;
            mit = null;
        }

        return result;
    }


    @Override
    public boolean doDeinitTrackers()
    {
        
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        tManager.deinitTracker(ObjectTracker.getClassType());

        return result;
    }


    @Override
    public void onInitARDone(CustomApplicationException exception)
    {

        if (exception == null)
        {
            initApplicationAR();

            mRenderer.mIsActive = true;

            
            
            
            
            addContentView(mGlView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

            
            mUILayout.bringToFront();

            
            loadingDialogHandler
                    .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);

            
            mUILayout.setBackgroundColor(Color.TRANSPARENT);

            try
            {
                vuforiaAppSession.startAR(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT);
            } catch (CustomApplicationException e)
            {
                Log.e(LOGTAG, e.getString());
            }

            boolean result = CameraDevice.getInstance().setFocusMode(
                    CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);

            if (!result)
                Log.e(LOGTAG, "Unable to enable continuous autofocus");

        } else
        {
            Log.e(LOGTAG, exception.getString());
            showInitializationErrorMessage(exception.getString());
        }

    }


    
    public void showInitializationErrorMessage(String message)
    {
        final String errorMessage = message;
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                if (mErrorDialog != null)
                {
                    mErrorDialog.dismiss();
                }

                
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        PalatiumActivity.this);
                builder
                        .setMessage(errorMessage)
                        .setTitle(getString(R.string.INIT_ERROR))
                        .setCancelable(false)
                        .setIcon(0)
                        .setPositiveButton(getString(R.string.button_OK),
                                new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        finish();
                                    }
                                });

                mErrorDialog = builder.create();
                mErrorDialog.show();
            }
        });
    }


    @Override
    public void onVuforiaUpdate(State state)
    {
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
        finish();
    }
}

