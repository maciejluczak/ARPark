package pl.lednica.arpark.object_recognition_engine;


/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2015 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other
countries.
===============================================================================*/


import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.WindowManager;

import com.vuforia.CameraCalibration;
import com.vuforia.CameraDevice;
import com.vuforia.Matrix44F;
import com.vuforia.Renderer;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.Vec2I;
import com.vuforia.VideoBackgroundConfig;
import com.vuforia.VideoMode;
import com.vuforia.Vuforia;
import com.vuforia.Vuforia.UpdateCallbackInterface;

import pl.lednica.arpark.R;


public class CustomApplicationSession implements UpdateCallbackInterface
{

    private static final String LOGTAG = "Lednica";

    private Activity mActivity;
    private CustomApplicationControl mSessionControl;

    private boolean mStarted = false;
    private boolean mCameraRunning = false;

    private int mScreenWidth = 0;
    private int mScreenHeight = 0;

    private InitVuforiaTask mInitVuforiaTask;
    private LoadTrackerTask mLoadTrackerTask;

    private Object mShutdownLock = new Object();

    private int mVuforiaFlags = 0;

    private int mCamera = CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT;

    private Matrix44F mProjectionMatrix;

    private int[] mViewport;

    private boolean mIsPortrait = false;


    public CustomApplicationSession(CustomApplicationControl sessionControl)
    {
        mSessionControl = sessionControl;
    }


    public void initAR(Activity activity, int screenOrientation)
    {
        CustomApplicationException vuforiaException = null;
        mActivity = activity;

        if ((screenOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR)
                && (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO))
            screenOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;

        OrientationEventListener orientationEventListener = new OrientationEventListener(mActivity) {
            @Override
            public void onOrientationChanged(int i) {
                int activityRotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
                if(mLastRotation != activityRotation)
                {
                    setProjectionMatrix();
                    mLastRotation = activityRotation;
                }
            }

            int mLastRotation = -1;
        };

        if(orientationEventListener.canDetectOrientation())
            orientationEventListener.enable();

        mActivity.setRequestedOrientation(screenOrientation);

        updateActivityOrientation();

        storeScreenDimensions();

        mActivity.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mVuforiaFlags = Vuforia.GL_20;

        if (mInitVuforiaTask != null)
        {
            String logMessage = "Cannot initialize SDK twice";
            vuforiaException = new CustomApplicationException(
                    CustomApplicationException.VUFORIA_ALREADY_INITIALIZATED,
                    logMessage);
            Log.e(LOGTAG, logMessage);
        }

        if (vuforiaException == null)
        {
            try
            {
                mInitVuforiaTask = new InitVuforiaTask();
                mInitVuforiaTask.execute();
            } catch (Exception e)
            {
                String logMessage = "Initializing Vuforia SDK failed";
                vuforiaException = new CustomApplicationException(
                        CustomApplicationException.INITIALIZATION_FAILURE,
                        logMessage);
                Log.e(LOGTAG, logMessage);
            }
        }

        if (vuforiaException != null)
            mSessionControl.onInitARDone(vuforiaException);
    }


    public void startAR(int camera) throws CustomApplicationException
    {
        String error;
        if(mCameraRunning)
        {
            error = "Camera already running, unable to open again";
            Log.e(LOGTAG, error);
            throw new CustomApplicationException(
                    CustomApplicationException.CAMERA_INITIALIZATION_FAILURE, error);
        }

        mCamera = camera;
        if (!CameraDevice.getInstance().init(camera))
        {
            error = "Unable to open camera device: " + camera;
            Log.e(LOGTAG, error);
            throw new CustomApplicationException(
                    CustomApplicationException.CAMERA_INITIALIZATION_FAILURE, error);
        }

        if (!CameraDevice.getInstance().selectVideoMode(
                CameraDevice.MODE.MODE_DEFAULT))
        {
            error = "Unable to set video mode";
            Log.e(LOGTAG, error);
            throw new CustomApplicationException(
                    CustomApplicationException.CAMERA_INITIALIZATION_FAILURE, error);
        }

        configureVideoBackground();

        if (!CameraDevice.getInstance().start())
        {
            error = "Unable to start camera device: " + camera;
            Log.e(LOGTAG, error);
            throw new CustomApplicationException(
                    CustomApplicationException.CAMERA_INITIALIZATION_FAILURE, error);
        }

        setProjectionMatrix();

        mSessionControl.doStartTrackers();

        mCameraRunning = true;

        if(!CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO))
        {
            if(!CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO))
                CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_NORMAL);
        }
    }


    public void stopAR() throws CustomApplicationException
    {
        if (mInitVuforiaTask != null
                && mInitVuforiaTask.getStatus() != InitVuforiaTask.Status.FINISHED)
        {
            mInitVuforiaTask.cancel(true);
            mInitVuforiaTask = null;
        }

        if (mLoadTrackerTask != null
                && mLoadTrackerTask.getStatus() != LoadTrackerTask.Status.FINISHED)
        {
            mLoadTrackerTask.cancel(true);
            mLoadTrackerTask = null;
        }

        mInitVuforiaTask = null;
        mLoadTrackerTask = null;

        mStarted = false;

        stopCamera();

        synchronized (mShutdownLock)
        {

            boolean unloadTrackersResult;
            boolean deinitTrackersResult;

            unloadTrackersResult = mSessionControl.doUnloadTrackersData();

            deinitTrackersResult = mSessionControl.doDeinitTrackers();

            Vuforia.deinit();

            if (!unloadTrackersResult)
                throw new CustomApplicationException(
                        CustomApplicationException.UNLOADING_TRACKERS_FAILURE,
                        "Failed to unload trackers\' data");

            if (!deinitTrackersResult)
                throw new CustomApplicationException(
                        CustomApplicationException.TRACKERS_DEINITIALIZATION_FAILURE,
                        "Failed to deinitialize trackers");

        }
    }


    public void resumeAR() throws CustomApplicationException
    {
        Vuforia.onResume();

        if (mStarted)
        {
            startAR(mCamera);
        }
    }


    public void pauseAR() throws CustomApplicationException
    {
        if (mStarted)
        {
            stopCamera();
        }

        Vuforia.onPause();
    }


    public Matrix44F getProjectionMatrix()
    {
        return mProjectionMatrix;
    }

    public int[] getViewport()
    {
        return mViewport;
    }

    @Override
    public void Vuforia_onUpdate(State s)
    {
        mSessionControl.onVuforiaUpdate(s);
    }


    public void onConfigurationChanged()
    {
        updateActivityOrientation();

        storeScreenDimensions();

        if (isARRunning())
        {
            // configure video background
            configureVideoBackground();

            // Update projection matrix:
            setProjectionMatrix();
        }

    }


    public void onResume()
    {
        Vuforia.onResume();
    }


    public void onPause()
    {
        Vuforia.onPause();
    }


    public void onSurfaceChanged(int width, int height)
    {
        Vuforia.onSurfaceChanged(width, height);
    }


    public void onSurfaceCreated()
    {
        Vuforia.onSurfaceCreated();
    }

    private class InitVuforiaTask extends AsyncTask<Void, Integer, Boolean>
    {
        private int mProgressValue = -1;


        protected Boolean doInBackground(Void... params)
        {
            synchronized (mShutdownLock)
            {
                Vuforia.setInitParameters(mActivity, mVuforiaFlags, "AQMYjfP/////AAAAAXcAIJ4ND0EhgeByI//+HwIOPpWSL1HB8xtx2szAG7HUGpuNpLN/nLSq1Dai7fnzDuSAUNJvnyyRrA//7C7Dzyy0owSYZ15qWJ4O6tGGoDC68PfKOmO9mc5YE/wJgU4VSWNU9JRdA7bdHmj8QEDBoAfYN6Ue0DWjTi8jf49RP5S4oF6bssiYvviz+D1eAyVujW+wvwtJZUHrjEmEmHGH/GhtDAw66p9AEPHE7+DSjxwwCHOPHcDstylSLxyvjfMaQ81pz74QZXZJny9ENlt8LDED53ovmWyMVkhD06+f+7KLQpNF+QKtLCbs6F6ErYfERrHVmaILhtcyzHQR0wFLVHpHdULPocxbRfYiCOhfrwey");

                do
                {
                    mProgressValue = Vuforia.init();

                    publishProgress(mProgressValue);

                } while (!isCancelled() && mProgressValue >= 0
                        && mProgressValue < 100);

                return (mProgressValue > 0);
            }
        }


        protected void onProgressUpdate(Integer... values)
        {

        }


        protected void onPostExecute(Boolean result)
        {


            CustomApplicationException vuforiaException = null;

            if (result)
            {
                Log.d(LOGTAG, "InitVuforiaTask.onPostExecute: Vuforia "
                        + "initialization successful");

                boolean initTrackersResult;
                initTrackersResult = mSessionControl.doInitTrackers();

                if (initTrackersResult)
                {
                    try
                    {
                        mLoadTrackerTask = new LoadTrackerTask();
                        mLoadTrackerTask.execute();
                    } catch (Exception e)
                    {
                        String logMessage = "Loading tracking data set failed";
                        vuforiaException = new CustomApplicationException(
                                CustomApplicationException.LOADING_TRACKERS_FAILURE,
                                logMessage);
                        Log.e(LOGTAG, logMessage);
                        mSessionControl.onInitARDone(vuforiaException);
                    }

                } else
                {
                    vuforiaException = new CustomApplicationException(
                            CustomApplicationException.TRACKERS_INITIALIZATION_FAILURE,
                            "Failed to initialize trackers");
                    mSessionControl.onInitARDone(vuforiaException);
                }
            } else
            {
                String logMessage;

                logMessage = getInitializationErrorString(mProgressValue);

                Log.e(LOGTAG, "InitVuforiaTask.onPostExecute: " + logMessage
                        + " Exiting.");

                vuforiaException = new CustomApplicationException(
                        CustomApplicationException.INITIALIZATION_FAILURE,
                        logMessage);
                mSessionControl.onInitARDone(vuforiaException);
            }
        }
    }

    private class LoadTrackerTask extends AsyncTask<Void, Integer, Boolean>
    {
        protected Boolean doInBackground(Void... params)
        {
            synchronized (mShutdownLock)
            {
                return mSessionControl.doLoadTrackersData();
            }
        }


        protected void onPostExecute(Boolean result)
        {

            CustomApplicationException vuforiaException = null;

            Log.d(LOGTAG, "LoadTrackerTask.onPostExecute: execution "
                    + (result ? "successful" : "failed"));

            if (!result)
            {
                String logMessage = "Failed to load tracker data.";
                Log.e(LOGTAG, logMessage);
                vuforiaException = new CustomApplicationException(
                        CustomApplicationException.LOADING_TRACKERS_FAILURE,
                        logMessage);
            } else
            {
                System.gc();

                Vuforia.registerCallback(CustomApplicationSession.this);

                mStarted = true;
            }

            mSessionControl.onInitARDone(vuforiaException);
        }
    }


    private String getInitializationErrorString(int code)
    {
        if (code == Vuforia.INIT_DEVICE_NOT_SUPPORTED)
            return mActivity.getString(R.string.INIT_ERROR_DEVICE_NOT_SUPPORTED);
        if (code == Vuforia.INIT_NO_CAMERA_ACCESS)
            return mActivity.getString(R.string.INIT_ERROR_NO_CAMERA_ACCESS);
        if (code == Vuforia.INIT_LICENSE_ERROR_MISSING_KEY)
            return mActivity.getString(R.string.INIT_LICENSE_ERROR_MISSING_KEY);
        if (code == Vuforia.INIT_LICENSE_ERROR_INVALID_KEY)
            return mActivity.getString(R.string.INIT_LICENSE_ERROR_INVALID_KEY);
        if (code == Vuforia.INIT_LICENSE_ERROR_NO_NETWORK_TRANSIENT)
            return mActivity.getString(R.string.INIT_LICENSE_ERROR_NO_NETWORK_TRANSIENT);
        if (code == Vuforia.INIT_LICENSE_ERROR_NO_NETWORK_PERMANENT)
            return mActivity.getString(R.string.INIT_LICENSE_ERROR_NO_NETWORK_PERMANENT);
        if (code == Vuforia.INIT_LICENSE_ERROR_CANCELED_KEY)
            return mActivity.getString(R.string.INIT_LICENSE_ERROR_CANCELED_KEY);
        if (code == Vuforia.INIT_LICENSE_ERROR_PRODUCT_TYPE_MISMATCH)
            return mActivity.getString(R.string.INIT_LICENSE_ERROR_PRODUCT_TYPE_MISMATCH);
        else
        {
            return mActivity.getString(R.string.INIT_LICENSE_ERROR_UNKNOWN_ERROR);
        }
    }


    private void storeScreenDimensions()
    {
        DisplayMetrics metrics = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
    }


    private void updateActivityOrientation()
    {
        Configuration config = mActivity.getResources().getConfiguration();

        switch (config.orientation)
        {
            case Configuration.ORIENTATION_PORTRAIT:
                mIsPortrait = true;
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                mIsPortrait = false;
                break;
            case Configuration.ORIENTATION_UNDEFINED:
            default:
                break;
        }

        Log.i(LOGTAG, "Activity is in "
                + (mIsPortrait ? "PORTRAIT" : "LANDSCAPE"));
    }


    public void setProjectionMatrix()
    {
        CameraCalibration camCal = CameraDevice.getInstance()
                .getCameraCalibration();
        mProjectionMatrix = Tool.getProjectionGL(camCal, 10.0f, 1000000.0f);
    }


    public void stopCamera()
    {
        if(mCameraRunning)
        {
            mSessionControl.doStopTrackers();
            CameraDevice.getInstance().stop();
            CameraDevice.getInstance().deinit();
            mCameraRunning = false;
        }
    }


    private void configureVideoBackground()
    {
        CameraDevice cameraDevice = CameraDevice.getInstance();
        VideoMode vm = cameraDevice.getVideoMode(CameraDevice.MODE.MODE_DEFAULT);

        VideoBackgroundConfig config = new VideoBackgroundConfig();
        config.setEnabled(true);
        config.setPosition(new Vec2I(0, 0));

        int xSize = 0, ySize = 0;
        if (mIsPortrait)
        {
            xSize = (int) (vm.getHeight() * (mScreenHeight / (float) vm
                    .getWidth()));
            ySize = mScreenHeight;

            if (xSize < mScreenWidth)
            {
                xSize = mScreenWidth;
                ySize = (int) (mScreenWidth * (vm.getWidth() / (float) vm
                        .getHeight()));
            }
        } else
        {
            xSize = mScreenWidth;
            ySize = (int) (vm.getHeight() * (mScreenWidth / (float) vm
                    .getWidth()));

            if (ySize < mScreenHeight)
            {
                xSize = (int) (mScreenHeight * (vm.getWidth() / (float) vm
                        .getHeight()));
                ySize = mScreenHeight;
            }
        }

        config.setSize(new Vec2I(xSize, ySize));

        mViewport = new int[4];
        mViewport[0] = ((mScreenWidth - xSize) / 2) + config.getPosition().getData()[0];
        mViewport[1] = ((mScreenHeight - ySize) / 2) + config.getPosition().getData()[1];
        mViewport[2] = xSize;
        mViewport[3] = ySize;

        Log.i(LOGTAG, "Configure Video Background : Video (" + vm.getWidth()
                + " , " + vm.getHeight() + "), Screen (" + mScreenWidth + " , "
                + mScreenHeight + "), mSize (" + xSize + " , " + ySize + ")");

        Renderer.getInstance().setVideoBackgroundConfig(config);

    }

    private boolean isARRunning()
    {
        return mStarted;
    }

}
