package pl.lednica.arpark.sensor_engine;

import android.content.res.AssetManager;
import android.location.Location;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import pl.lednica.arpark.activities.CompostelaActivity;

/**
 * Created by Maciej on 2016-07-05.
 */
public class SensorCameraViewRenderer implements GLSurfaceView.Renderer{

    private CompostelaActivity mActivity;
    private CameraView mCameraView;
    private int translateFactor = 0;

    private static final String LOGTAG = "SensorCameraViewRendere";
    public static final String ORIENTATION_ORGINAL = "orientation_orginal.csv";
    public static final String ORIENTATION_FILTERED = "orientation_filtered";

    /*public final static Location mountWashington = new Location("manual");
    static {
        mountWashington.setLatitude(42.869466d);
        mountWashington.setLongitude(-8.547657d);
        mountWashington.setAltitude(50.0d);
    }*/

    private int widthT,heightT;
    private float glFOV;
    private float wynik=0.0f;
    private float wynikLoad=0.0f;
    float delta =0.0f;
    float deltaTmp =0.0f;
    float delta2 =0.0f;
    float delta3 =0.0f;
    float delta4 =0.0f;
    float delta5 =0.0f;
    /**
     * Store the model matrix. This matrix is used to move models from object space (where each model can be thought
     * of being located at the center of the universe) to world space.
     */
    private float[] mModelMatrix = new float[16];

    /**
     * Store the view matrix. This can be thought of as our camera. This matrix transforms world space to eye space;
     * it positions things relative to our eye.
     */
    private float[] mViewMatrix = new float[16];

    /** Store the projection matrix. This is used to project the scene onto a 2D viewport. */
    private float[] mProjectionMatrix = new float[16];

    /** Allocate storage for the final combined matrix. This will be passed into the shader program. */
    private float[] mMVPMatrix = new float[16];

    /** Store our model data in a float buffer. */
    private FloatBuffer mTriangle1Vertices;

    /** This will be used to pass in the transformation matrix. */
    private int mMVPMatrixHandle;

    /** This will be used to pass in model position information. */
    private int mPositionHandle;

    /** This will be used to pass in model color information. */
    private int mColorHandle;

    /** How many bytes per float. */
    private final int mBytesPerFloat = 4;

    /** How many elements per vertex. */
    private final int mStrideBytes = 7 * mBytesPerFloat;

    /**How many vertex to print */
    private int mVertexCount=0;

    /** Offset of the position data. */
    private final int mPositionOffset = 0;

    /** Size of the position data in elements. */
    private final int mPositionDataSize = 3;

    /** Offset of the color data. */
    private final int mColorOffset = 3;

    /** Size of the color data in elements. */
    private final int mColorDataSize = 4;


    public SensorCameraViewRenderer(CompostelaActivity activity, CameraView camera){

        mActivity = activity;
        mCameraView = camera;
        // Define points for equilateral triangles.
        // This trisangle is red, green, and blue.
        //strzalka trojkatna i domyslny bufor
       /* final float[] triangle1VerticesData = {
                // X, Y, Z,
                // R, G, B, A
                -0.5f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f, 1.0f,

                0.5f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 1.0f,

                0.0f, 0.5f, 0.0f,
                0.0f, 1.0f, 0.0f, 1.0f};
        // Initialize the buffers.
        mTriangle1Vertices = ByteBuffer.allocateDirect(triangle1VerticesData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangle1Vertices.put(triangle1VerticesData).position(0);*/

        AssetManager assetManager = mActivity.getResources().getAssets();
        String filename = "Compostela/crossModel.dat";
        InputStream is = null;
        DataInputStream dis = null;
        ByteBuffer verts;
        try
        {
            is = assetManager.open(filename);
            dis = new DataInputStream(is);
            int count = is.available();

            int floatsToRead = count / mBytesPerFloat;
            mVertexCount = count / mStrideBytes;
            //Log.e("HEJ","Liczba: "+ floatsToRead);
            verts = ByteBuffer.allocateDirect(floatsToRead * 4);
            verts.order(ByteOrder.nativeOrder());

            for (int i = 0; i < floatsToRead; i++)
            {
                float readTmp = dis.readFloat();
                //Log.e("HEJ","ReadVal: "+ readTmp);
                verts.putFloat(readTmp);
            }
            verts.rewind();
            mTriangle1Vertices  = verts.asFloatBuffer();
        } catch (IOException e){

        }
        finally {
            if (is != null)
                try {
                    is.close();
                }
                catch (IOException e){

                }
            if (dis != null)
                try {
                    dis.close();
                }
                catch (IOException e){

                }
        }

        /*ByteBuffer bb = ByteBuffer.allocateDirect(mBytesPerFloat * CrossModel.triangle1VerticesData.length);
        bb.order(ByteOrder.nativeOrder());
        for (double d : CrossModel.triangle1VerticesData)
            bb.putFloat((float) d);
        bb.rewind();
        mTriangle1Vertices  = verts.asFloatBuffer();*/



    }
    public void onDrawFrame( GL10 gl ) {
        // compute rotation matrix
        /*float rotation[] = new float[9];
        float identity[] = new float[9];
        float orientation[] = new float[3];
        boolean gotRotation = SensorManager.getRotationMatrix(rotation,
                identity, mActivity.getAccelerometrData(), mActivity.getGeomagneticData());
        if (gotRotation) {
            float cameraRotation[] = new float[9];
            // remap such that the camera is pointing straight down the Y axis
            SensorManager.remapCoordinateSystem(rotation, SensorManager.AXIS_X,
                    SensorManager.AXIS_Z, cameraRotation);

            // orientation vector

            SensorManager.getOrientation(cameraRotation, orientation);
            Log.e(LOGTAG,"x: "+ (float)Math.toDegrees(orientation[0]) +" y "+
                    (float)Math.toDegrees(orientation[1]) +" z "+ (float)Math.toDegrees(orientation[2]));
        }*/

        float orientation[] = mActivity.getSensorData().getOrientation();
        Location location = mActivity.getSensorData().getLocation();

        Location targetLocation = CompostelaGeo.getGeoData();

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        // Do a complete rotation every 10 seconds.
        long time = SystemClock.uptimeMillis() % 10000L;
        //float angleInDegrees = (360.0f / 10000.0f) * ((int) time);

        // Draw the triangle facing straight on.
        float curBearingToMW = 0.0f;
        float dx = 0.0f;
        float dz = 0.0f;
        try {
            curBearingToMW = location.bearingTo(targetLocation);

            double latitudeDelta = targetLocation.getLatitude() - location.getLatitude();
            double longitudeDelta = targetLocation.getLongitude() - location.getLongitude();
            double a =  Math.sin(latitudeDelta/2)*Math.sin(latitudeDelta/2) +
                       Math.sin(longitudeDelta/2)*Math.sin(longitudeDelta/2) *
                       Math.cos(targetLocation.getLatitude()) *
                       Math.cos(location.getLatitude());
            double b  = 2* Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
            double d = 6471 * b;
            dx = (float)(d * Math.cos(curBearingToMW));
            dz = (float)(d *  Math.sin(curBearingToMW));
        }catch(NullPointerException e){
            Log.e(LOGTAG,"Brak wyznaczonej pozycji");
        }
        Matrix.setIdentityM(mModelMatrix, 0);
        //Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);
        //Matrix.translateM(mModelMatrix,0,2.0f,2.0f,0.0f);

        //float dxT = (float) ( (widthT/ mCameraView.getHorizontalFOV()) * (Math.toDegrees(orientation[0])-curBearingToMW));
        //float dyT = (float) ( (heightT/ mCameraView.getVerticalFOV()) * Math.toDegrees(orientation[1]));
        //Matrix.rotateM(mModelMatrix,0,(float)(0.0f- Math.toDegrees(orientation[2])),0.0f,1.0f,0.0f);
        //Matrix.translateM(mModelMatrix,0,0.0f - dxT, 0.0f -dyT,0.0f);

        //fajny efekt ale ucieka
        //Matrix.translateM(mModelMatrix,0,0.0f- (float)(Math.toDegrees(orientation[0])-curBearingToMW),0.0f ,0.0f);
        //Matrix.rotateM(mModelMatrix,0,0.0f-(float)Math.toDegrees(orientation[1]),0.0f,1.0f,0.0f);

        //Matrix.translateM(mModelMatrix,0,0.0f,0.0f ,-1.0f);

        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = 0.0f;//1.5f;
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -100.0f;//-5.0f;
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        float d = 1.5f;//doświadczalnie do pierwszej metody 6.0f;
        //float x = (float) Math.tan(mCameraView.getHorizontalFOV()/2)*d;
        //tangens źle podaje więc podajemy z stałej
        double x = 1.73205 * d;
        double xTan =(float) Math.tan(Math.toRadians(mCameraView.getHorizontalFOV()/2))*d*4.0f;
        //zastąpienie konta z kamery kontem z opengles (mCameraView.getHorizontalFOV()/2)-> glFOV
        if(curBearingToMW <= (float)((Math.toDegrees(orientation[0]) +glFOV+20) )){
            if(curBearingToMW >= (float)((Math.toDegrees(orientation[0]) -glFOV-20))){
                if((Math.toDegrees(orientation[0])>90.0f)&(curBearingToMW<-90.0f)){
                    deltaTmp = (float)(Math.toDegrees(orientation[0]) + curBearingToMW);
                }else{
                    if((Math.toDegrees(orientation[0]) < -90.0f)&(curBearingToMW>90.0f)) {
                        deltaTmp = (float)(Math.toDegrees(orientation[0]) + curBearingToMW);
                    }else{
                        deltaTmp = (float)(Math.toDegrees(orientation[0]) - curBearingToMW);
                    }
                }
                //delta = (float)(Math.toDegrees(orientation[0]) - curBearingToMW);

                //używając procenta szerokości ekranu przesuwanie obiektu
                //działa ale trochę bez sensu
                float deltaCompTreshold = delta;

                if (!Float.isNaN(deltaTmp)) {
                    if (Math.abs(delta - deltaTmp) > 5) {
                        wynikLoad = (delta / (mCameraView.getHorizontalFOV() / 2)) * (float) xTan;
                        delta = 0.1f * deltaTmp + (1.0f - 0.1f) * delta;
                    } else {
                        delta = 0.2f * deltaTmp + (1.0f - 0.2f) * delta;
                        wynikLoad = (delta / (mCameraView.getHorizontalFOV() / 2)) * (float) xTan;
                    }
                }
                if(!Float.isNaN(deltaTmp)) {
                    //delta = 0.2f * deltaTmp + (1.0f - 0.2f) * delta;
                    delta2 = 0.2f * deltaTmp + (1.0f - 0.2f) * delta2;
                    delta3 = 0.1f * deltaTmp + (1.0f - 0.1f) * delta3;
                }
                Log.e(LOGTAG,"wynik przypisanie : "+ wynik);

                /*FileOutputStream outputStream;
                String tmpVal;
                try {
                    outputStream = mActivity.getApplicationContext().openFileOutput(ORIENTATION_ORGINAL, Context.MODE_APPEND);
                    tmpVal = String.valueOf(deltaTmp) + ";";
                    outputStream.write(tmpVal.getBytes());
                    outputStream.close();
                    outputStream = mActivity.getApplicationContext().openFileOutput(ORIENTATION_FILTERED+"_delta.csv", Context.MODE_APPEND);
                    tmpVal = String.valueOf(delta) + ";";
                    outputStream.write(tmpVal.getBytes());
                    outputStream.close();
                    outputStream = mActivity.getApplicationContext().openFileOutput(ORIENTATION_FILTERED+"_wynik.csv", Context.MODE_APPEND);
                    tmpVal = String.valueOf(wynikLoad) + ";";
                    outputStream.write(tmpVal.getBytes());
                    outputStream.close();
                    outputStream = mActivity.getApplicationContext().openFileOutput(ORIENTATION_FILTERED+"2.csv", Context.MODE_APPEND);
                    tmpVal = String.valueOf(delta2) + ";";
                    outputStream.write(tmpVal.getBytes());
                    outputStream.close();
                    outputStream = mActivity.getApplicationContext().openFileOutput(ORIENTATION_FILTERED+"1.csv", Context.MODE_APPEND);
                    tmpVal = String.valueOf(delta3) + ";";
                    outputStream.write(tmpVal.getBytes());
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
                wynik = wynikLoad;

                //if(translateFactor==0) wynik = wynikLoad;
                //if((wynikLoad - wynik)>0.1f) wynik = wynikLoad;

                Matrix.translateM(mModelMatrix,0,0.0f-wynik,0.0f ,0.0f);
                translateFactor++;
                if (translateFactor==15)translateFactor=0;
                Log.e(LOGTAG,"delta: "+delta + " wynik: " + wynik +" x_szerokość: "+ xTan);

                //próba przesunięcie + obrót względem y, nieudana obraca model według osi y położonej wewnątrz modelu
                Matrix.translateM(mModelMatrix,0,0.0f,0.0f ,-1.5f);
                //Matrix.rotateM(mModelMatrix,0,delta,0.0f,1.0f ,0.0f);

                //przesuwanie widoku względem osi x funkcja cos
                //x = Math.sqrt(((1.5/Math.cos(Math.toRadians(Math.abs(delta))))*(1.5/Math.cos(Math.toRadians(Math.abs(delta))))) -(d*d) );
                if(delta<0){
                    x = 0.0f - x;
                }
                //Matrix.translateM(mViewMatrix,0,0.0f - (float)x,0.0f,0.0f);
                //Log.e(LOGTAG,"FOVOPENGL: "+glFOV +"delta: "+delta +" abs(delta): "+Math.abs(delta)+" cos: "+Math.cos(Math.toRadians(Math.abs(delta)))+ " x: " + x +" ---------- ");

            }else{
                Matrix.translateM(mModelMatrix,0,0.0f,-500.0f, 0.0f);
            }
        }else{
            Matrix.translateM(mModelMatrix,0,0.0f,-500.0f, 0.0f);
        }
        float tmp  = 0.0f - (float)(Math.toDegrees(orientation[0]));
        //Log.e(LOGTAG,"tmp: "+tmp+
                //widthT+" HorizontFOV: "+mCameraView.getHorizontalFOV() +
           //     " orientation[0]: " +
          //      Math.toDegrees(orientation[0])+" curBearingToMW: "+curBearingToMW+
                //" dxT: "+ dxT +" dyT "+dyT+
             //   " rot: "+ Math.toDegrees(orientation[2]));


       /* // Position the eye behind the origin.
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = 0.0f;//1.5f;

        // We are looking toward the distance
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -100.0f;//-5.0f;

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        // Set the view matrix. This matrix can be said to represent the camera position.
        // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
        // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
       Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);*/


        //najfajniejsze
        //Matrix.rotateM(mViewMatrix,0,0.0f - (float)Math.toDegrees(orientation[0]),1.0f,0.0f,0.0f);
        //Matrix.translateM(mViewMatrix,0,0.0f,0.0f -(float)Math.toDegrees(orientation[1]),0.0f);

        //Matrix.rotateM(mViewMatrix,0,0.0f -(float)Math.toDegrees(orientation[0]),0.0f,1.0f,0.0f);

        //Matrix.rotateM(mViewMatrix,0,(float)Math.toDegrees(orientation[1]),0.0f,1.0f,0.0f);
        //Matrix.rotateM(mViewMatrix,0,(float)Math.toDegrees(orientation[2]),0.0f,0.0f,1.0f);
        //Matrix.rotateM(mViewMatrix,0,(float)(0.0f- Math.toDegrees(orientation[2])),0.0f,1.0f,0.0f);

        //Matrix.rotateM(mViewMatrix,0,(float)(0.0f- Math.toDegrees(orientation[2])),0.0f,1.0f,0.0f);
        //Matrix.translateM(mViewMatrix,0,0.0f - dxT, 0.0f -dyT,0.0f);


        drawTriangle(mTriangle1Vertices);

    }
    /**
     * Draws a triangle from the given vertex data.
     *
     * @param aTriangleBuffer The buffer containing the vertex data.
     */
    private void drawTriangle(final FloatBuffer aTriangleBuffer)
    {
        // Pass in the position information
        aTriangleBuffer.position(mPositionOffset);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, aTriangleBuffer);

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Pass in the color information
        aTriangleBuffer.position(mColorOffset);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, aTriangleBuffer);

        GLES20.glEnableVertexAttribArray(mColorHandle);

        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVertexCount);
        //GLES20.glDrawElements(GLES20.GL_TRIANGLES,4,GLES20.GL_FLOAT,0);
    }

    public void onSurfaceChanged( GL10 gl, int width, int height ) {
        // Set the OpenGL viewport to the same size as the surface.
        GLES20.glViewport(0, 0, width, height);
        widthT = width;
        heightT = height;
        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;

        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 10.0f;

        glFOV = (float)Math.toDegrees(Math.atan2(near,ratio));
        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    public void onSurfaceCreated( GL10 gl, EGLConfig config ) {
        // Set the background clear color to gray.
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);

        // Position the eye behind the origin.
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = 0.0f;//1.5f;

        // We are looking toward the distance
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = 0.0f;//-5.0f;

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        final float upX = 0.0f;
        final float upY = 0.0f;//1.0f;
        final float upZ = 0.0f;

        // Set the view matrix. This matrix can be said to represent the camera position.
        // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
        // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
        final String vertexShader =
                        "uniform mat4 u_MVPMatrix;      \n"		// A constant representing the combined model/view/projection matrix.

                        + "attribute vec4 a_Position;     \n"		// Per-vertex position information we will pass in.
                        + "attribute vec4 a_Color;        \n"		// Per-vertex color information we will pass in.

                        + "varying vec4 v_Color;          \n"		// This will be passed into the fragment shader.

                        + "void main()                    \n"		// The entry point for our vertex shader.
                        + "{                              \n"
                        + "   v_Color = a_Color;          \n"		// Pass the color through to the fragment shader.
                        // It will be interpolated across the triangle.
                        + "   gl_Position = u_MVPMatrix   \n" 	// gl_Position is a special variable used to store the final position.
                        + "               * a_Position;   \n"     // Multiply the vertex by the matrix to get the final point in
                        + "}                              \n";    // normalized screen coordinates.

        final String fragmentShader =
                        "precision mediump float;       \n"		// Set the default precision to medium. We don't need as high of a
                        // precision in the fragment shader.
                        + "varying vec4 v_Color;          \n"		// This is the color from the vertex shader interpolated across the
                        // triangle per fragment.
                        + "void main()                    \n"		// The entry point for our fragment shader.
                        + "{                              \n"
                        + "   gl_FragColor = v_Color;     \n"		// Pass the color directly through the pipeline.
                        + "}                              \n";
        // Load in the vertex shader.
        int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);

        if (vertexShaderHandle != 0)
        {
            // Pass in the shader source.
            GLES20.glShaderSource(vertexShaderHandle, vertexShader);

            // Compile the shader.
            GLES20.glCompileShader(vertexShaderHandle);

            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0)
            {
                GLES20.glDeleteShader(vertexShaderHandle);
                vertexShaderHandle = 0;
            }
        }

        if (vertexShaderHandle == 0)
        {
            throw new RuntimeException("Error creating vertex shader.");
        }

        // Load in the fragment shader shader.
        int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);

        if (fragmentShaderHandle != 0)
        {
            // Pass in the shader source.
            GLES20.glShaderSource(fragmentShaderHandle, fragmentShader);

            // Compile the shader.
            GLES20.glCompileShader(fragmentShaderHandle);

            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0)
            {
                GLES20.glDeleteShader(fragmentShaderHandle);
                fragmentShaderHandle = 0;
            }
        }

        if (fragmentShaderHandle == 0)
        {
            throw new RuntimeException("Error creating fragment shader.");
        }

        // Create a program object and store the handle to it.
        int programHandle = GLES20.glCreateProgram();

        if (programHandle != 0)
        {
            // Bind the vertex shader to the program.
            GLES20.glAttachShader(programHandle, vertexShaderHandle);

            // Bind the fragment shader to the program.
            GLES20.glAttachShader(programHandle, fragmentShaderHandle);

            // Bind attributes
            GLES20.glBindAttribLocation(programHandle, 0, "a_Position");
            GLES20.glBindAttribLocation(programHandle, 1, "a_Color");

            // Link the two shaders together into a program.
            GLES20.glLinkProgram(programHandle);

            // Get the link status.
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

            // If the link failed, delete the program.
            if (linkStatus[0] == 0)
            {
                GLES20.glDeleteProgram(programHandle);
                programHandle = 0;
            }
        }

        if (programHandle == 0)
        {
            throw new RuntimeException("Error creating program.");
        }

        // Set program handles. These will later be used to pass in values to the program.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");

        // Tell OpenGL to use this program when rendering.
        GLES20.glUseProgram(programHandle);
    }
}
