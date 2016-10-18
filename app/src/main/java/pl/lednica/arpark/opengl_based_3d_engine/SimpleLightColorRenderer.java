package pl.lednica.arpark.opengl_based_3d_engine;

import android.content.res.AssetManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import pl.lednica.arpark.activities.Object3DViewActivity;

/**
 * Created by Maciej on 2016-09-24.
 * Klasa renderujaca obraz 3d
 */

public class SimpleLightColorRenderer implements GLSurfaceView.Renderer {

    private static final String LOGTAG = "SLCRenderer";

    /** macierz modelu transformuje obiekt z przestrzeni modelu do przestrzeni świata */
    private float[] mModelMatrix = new float[16];

    /** macierz widoku transformuje macierz świata w macierz widoku czyli kamery */
    private float[] mViewMatrix = new float[16];

    /** macierz projekcji używana do rzutowania modelu 3d na ekran 2d */
    private float[] mProjectionMatrix = new float[16];

    /**kombinacja poprzednich trzech macierzy */
    private float[] mMVPMatrix = new float[16];

    /** Stores a copy of the model matrix specifically for the light position */
    private float[] mLightModelMatrix = new float[16];

    /** Dane modelu będą przechowywane w postaci float buffer. */
    private MeshLoader mesh;

    int mCubePositionsBufferIdx;
    int mCubeNormalsBufferIdx;
    int mCubeTexCoordsBufferIdx;
    int mCubeIndicesBufferIdx;

    private Object3DViewActivity mActivity;
    private AssetManager mAssetMenager;
    /*private static String file_cross_v = "Compostela/cubeText_Cube_v_Model.dat";
    //private static String file_cross_c = "Compostela/kosciol-lednica_Cube_c_Model.dat";
    private static String file_cross_n = "Compostela/cubeText_Cube_n_Model.dat";
    private static String file_cross_t = "Compostela/cubeText_Cube_t_Model.dat";
    private static String file_cross_i = "Compostela/cubeText_Cube_i_Model.dat";*/

    private static String file_cross_v = "Compostela/kosciol-lednica_Cube_v_Model.dat";
    //private static String file_cross_c = "Compostela/kosciol-lednica_Cube_c_Model.dat";
    private static String file_cross_n = "Compostela/kosciol-lednica_Cube_n_Model.dat";
    private static String file_cross_t = "Compostela/kosciol-lednica_Cube_t_Model.dat";
    private static String file_cross_i = "Compostela/kosciol-lednica_Cube_i_Model.dat";

    private int mMVPMatrixHandle;
    private int mMVMatrixHandle;
    private int mPositionHandle;
    private int mColorHandle;
    private int mNormalHandle;
    private int mLightPosHandle;
    private int mTextureDataHandle;
    private int mTextureCoordinateHandle;
    private int mTextureUniformHandle;


    private final float[] mLightPosInModelSpace = new float[] {0.0f, 0.0f, 0.0f, 1.0f};
    private final float[] mLightPosInWorldSpace = new float[4];
    private final float[] mLightPosInEyeSpace = new float[4];

    /** This is a handle to our per-vertex cube shading program. */
    private int mPerVertexProgramHandle;

    /** This is a handle to our light point program. */
    private int mPointProgramHandle;

    public SimpleLightColorRenderer(Object3DViewActivity activity) {
        mActivity = activity;
        mAssetMenager = mActivity.getResources().getAssets();
        mesh = new MeshLoader();
        try {
            mesh.loadToBuffer(file_cross_v, MeshLoader.BUFFER_TYPE.BUFFER_TYPE_VERTEX,
                                MeshLoader.BUFFER_DATA_TYPE.DATA_FLOAT, mAssetMenager);

            mesh.loadToBuffer(file_cross_t, MeshLoader.BUFFER_TYPE.BUFFER_TYPE_TEXTURE_COORD,
                                MeshLoader.BUFFER_DATA_TYPE.DATA_FLOAT, mAssetMenager);

            mesh.loadToBuffer(file_cross_n, MeshLoader.BUFFER_TYPE.BUFFER_TYPE_NORMALS,
                                MeshLoader.BUFFER_DATA_TYPE.DATA_FLOAT, mAssetMenager);
            //IDX
            mesh.loadToBuffer(file_cross_i, MeshLoader.BUFFER_TYPE.BUFFER_TYPE_INDICES,
                                MeshLoader.BUFFER_DATA_TYPE.DATA_SHORT, mAssetMenager);
        }catch (IOException e){
            Log.e(LOGTAG,"Loading models error!");
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Set the background clear color to black.
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // Use culling to remove back faces.
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);


        // Position the eye behind the origin.
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = 0.0f;

        // We are looking toward the distance
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -100.0f;

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        // Set the view matrix. This matrix can be said to represent the camera position.
        // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
        // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        //final String vertexShader = SimpleLightColorShaders.DEFFUSE_POINT_LIGHT_VERTEX_SHADER;
        //final String fragmentShader = SimpleLightColorShaders.DEFFUSE_POINT_LIGHT_FRAGMENT_SHADER;
        final String vertexShader = SimpleLightTextureShaders.DEFFUSE_POINT_LIGHT_VERTEX_SHADER;
        final String fragmentShader = SimpleLightTextureShaders.DEFFUSE_POINT_LIGHT_FRAGMENT_SHADER;

        final int vertexShaderHandle = compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);
        final int fragmentShaderHandle = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);

        //mPerVertexProgramHandle = createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle,
        //        new String[] {"a_Position",  "a_Color", "a_Normal","a_TexCoordinate"});
        mPerVertexProgramHandle = createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle,
                new String[] {"a_Position", "a_Normal","a_TexCoordinate"});

        //mTextureDataHandle = meshTextures.loadTexture(mActivity, R.drawable.kosciol_tekstura);
        mTextureDataHandle = mesh.loadTextureFromApk("Compostela/kosciol_tekstura.jpg",mAssetMenager);
        // Define a simple shader program for our point.
        final String pointVertexShader =
                "uniform mat4 u_MVPMatrix;      \n"
                        +	"attribute vec4 a_Position;     \n"
                        + "void main()                    \n"
                        + "{                              \n"
                        + "   gl_Position = u_MVPMatrix   \n"
                        + "               * a_Position;   \n"
                        + "   gl_PointSize = 5.0;         \n"
                        + "}                              \n";

        final String pointFragmentShader =
                "precision mediump float;       \n"
                        + "void main()                    \n"
                        + "{                              \n"
                        + "   gl_FragColor = vec4(1.0,    \n"
                        + "   1.0, 1.0, 1.0);             \n"
                        + "}                              \n";

        final int pointVertexShaderHandle = compileShader(GLES20.GL_VERTEX_SHADER, pointVertexShader);
        final int pointFragmentShaderHandle = compileShader(GLES20.GL_FRAGMENT_SHADER, pointFragmentShader);
        mPointProgramHandle = createAndLinkProgram(pointVertexShaderHandle, pointFragmentShaderHandle,
                new String[] {"a_Position"});


        //IDX
        int buffers[] = new int[3];
        GLES20.glGenBuffers(3, buffers, 0);

        int ibo[] = new int[1];
        GLES20.glGenBuffers(1, ibo, 0);

        mesh.mModelVertices.position(0);
       GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mesh.mModelVertices.capacity() * mesh.mBytesPerFloat,
                mesh.mModelVertices, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        mesh.mModelNormals.position(0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[1]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mesh.mModelNormals.capacity() * mesh.mBytesPerFloat,
                mesh.mModelNormals, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        mesh.mModelTextures.position(0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[2]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mesh.mModelTextures.capacity() * mesh.mBytesPerFloat,
                mesh.mModelTextures, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        mesh.mModelIndices.position(0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, mesh.mCountIndices * mesh.mBytesPerShort,
                mesh.mModelIndices.asShortBuffer(), GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        mCubePositionsBufferIdx = buffers[0];
        mCubeNormalsBufferIdx = buffers[1];
        mCubeTexCoordsBufferIdx = buffers[2];
        mCubeIndicesBufferIdx = ibo[0];
        buffers[0]=0;
        buffers[1]=0;
        buffers[2]=0;
        ibo[0]=0;

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // Set the OpenGL viewport to the same size as the surface.
        GLES20.glViewport(0, 0, width, height);

        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;

        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 10.0f;

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        //GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        //GLES20.glEnable(GLES20.GL_BLEND);
        //GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        // Do a complete rotation every 10 seconds.
        long time = SystemClock.uptimeMillis() % 10000L;
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);

        // Set our per-vertex lighting program.
        GLES20.glUseProgram(mPerVertexProgramHandle);

        GLES20.glFrontFace(GLES20.GL_CCW);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        // Set program handles for cube drawing.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mPerVertexProgramHandle, "u_MVPMatrix");
        mMVMatrixHandle = GLES20.glGetUniformLocation(mPerVertexProgramHandle, "u_MVMatrix");
        mLightPosHandle = GLES20.glGetUniformLocation(mPerVertexProgramHandle, "u_LightPos");
        mTextureUniformHandle = GLES20.glGetUniformLocation(mPerVertexProgramHandle, "u_Texture");
        mPositionHandle = GLES20.glGetAttribLocation(mPerVertexProgramHandle, "a_Position");
        //mColorHandle = GLES20.glGetAttribLocation(mPerVertexProgramHandle, "a_Color");
        mNormalHandle = GLES20.glGetAttribLocation(mPerVertexProgramHandle, "a_Normal");
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mPerVertexProgramHandle, "a_TexCoordinate");

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);


        // Calculate position of the light. Rotate and then push into the distance.
        Matrix.setIdentityM(mLightModelMatrix, 0);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, -8.0f);

        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0, mLightPosInWorldSpace, 0);



        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -1.0f);
        Matrix.rotateM(mModelMatrix,0,angleInDegrees,0.0f,1.0f,0.0f);
        drawModel();

        // Draw a point to indicate the light.
        GLES20.glUseProgram(mPointProgramHandle);
        drawLight();
    }

    private void drawModel()
    {
        //przekazanie informacji o wierzchołkach do OpenGL
        //są osobne bufory dla każdego rodzaju danych więc  offset zawsze jest 0
      /* mesh.mModelVertices.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, mesh.mPositionDataSize, GLES20.GL_FLOAT, false,
                mesh.mStrideBytesVertex, mesh.mModelVertices);
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        //przekazanie informacji o normalnych do OpenGL
        mesh.mModelNormals.position(0);
        GLES20.glVertexAttribPointer(mNormalHandle, mesh.mNormalDataSize, GLES20.GL_FLOAT, false,
                mesh.mStrideBytesNormal, mesh.mModelNormals);
        GLES20.glEnableVertexAttribArray(mNormalHandle);

        //przekazanie informacji o teksturach
        mesh.mModelTextures.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mesh.mTextureDataSize, GLES20.GL_FLOAT, false,
                0, mesh.mModelTextures);
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);*/

        //przekazanie informacji o kolorach do OpenGL
        /*mModelColors.position(0);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
                mStrideBytesColor, mModelColors);
        GLES20.glEnableVertexAttribArray(mColorHandle);*/

        //IDX_CPU
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mCubePositionsBufferIdx);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, mesh.mPositionDataSize, GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mCubeNormalsBufferIdx);
        GLES20.glEnableVertexAttribArray(mNormalHandle);
        GLES20.glVertexAttribPointer(mNormalHandle, mesh.mNormalDataSize, GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mCubeTexCoordsBufferIdx);
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mesh.mTextureDataSize, GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mCubeIndicesBufferIdx);

        // Utworzenie macierzy Model x View i przekazanie jej do OpenGL w celu kalkulacji światła
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);

        // Utworzenie macierzy Model x View x Projection i przekazanie do OpenGl
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // Przekazanie pozycji światła w przestrzeni widoku
        GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

        //Rysowanie modelu bez indeksu
        //GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mesh.mCountVertices);

        //IDX_CPU Rysowanie modelu z indeksem,z ładowaymi buforami do opengl
        GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                mesh.mCountIndices,
                GLES20.GL_UNSIGNED_SHORT, 0);

        //IDX Rysowanie modelu z indeksem, bez ładowania buforów do opengl
        /*GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                mesh.mCountIndices,
                GLES20.GL_UNSIGNED_SHORT, mesh.mModelIndices);*/

        //IDX_CPU
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    /**
     * Draws a point representing the position of the light.
     */
    private void drawLight()
    {
        final int pointMVPMatrixHandle = GLES20.glGetUniformLocation(mPointProgramHandle, "u_MVPMatrix");
        final int pointPositionHandle = GLES20.glGetAttribLocation(mPointProgramHandle, "a_Position");

        // Pass in the position.
        GLES20.glVertexAttrib3f(pointPositionHandle, mLightPosInModelSpace[0], mLightPosInModelSpace[1], mLightPosInModelSpace[2]);

        // Since we are not using a buffer object, disable vertex arrays for this attribute.
        GLES20.glDisableVertexAttribArray(pointPositionHandle);

        // Pass in the transformation matrix.
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mLightModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(pointMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // Draw the point.
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);
    }

    /**
     * Helper function to compile a shader.
     *
     * @param shaderType The shader type.
     * @param shaderSource The shader source code.
     * @return An OpenGL handle to the shader.
     */
    private int compileShader(final int shaderType, final String shaderSource)
    {
        int shaderHandle = GLES20.glCreateShader(shaderType);
        if (shaderHandle != 0)
        {
            // Pass in the shader source.
            GLES20.glShaderSource(shaderHandle, shaderSource);

            // Compile the shader.
            GLES20.glCompileShader(shaderHandle);

            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0)
            {
                Log.e(LOGTAG, "Error compiling program: " + GLES20.glGetProgramInfoLog(shaderHandle));
                GLES20.glDeleteShader(shaderHandle);
                shaderHandle = 0;
            }
        }

        if (shaderHandle == 0)
        {
            throw new RuntimeException("Error creating shader.");
        }

        return shaderHandle;
    }

    /**
     * Helper function to compile and link a program.
     *
     * @param vertexShaderHandle An OpenGL handle to an already-compiled vertex shader.
     * @param fragmentShaderHandle An OpenGL handle to an already-compiled fragment shader.
     * @param attributes Attributes that need to be bound to the program.
     * @return An OpenGL handle to the program.
     */
    private int createAndLinkProgram(final int vertexShaderHandle, final int fragmentShaderHandle, final String[] attributes)
    {
        int programHandle = GLES20.glCreateProgram();

        if (programHandle != 0)
        {
            // Bind the vertex shader to the program.
            GLES20.glAttachShader(programHandle, vertexShaderHandle);

            // Bind the fragment shader to the program.
            GLES20.glAttachShader(programHandle, fragmentShaderHandle);

            // Bind attributes
            if (attributes != null)
            {
                final int size = attributes.length;
                for (int i = 0; i < size; i++)
                {
                    GLES20.glBindAttribLocation(programHandle, i, attributes[i]);
                }
            }

            // Link the two shaders together into a program.
            GLES20.glLinkProgram(programHandle);

            // Get the link status.
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

            // If the link failed, delete the program.
            if (linkStatus[0] == 0)
            {
                Log.e(LOGTAG, "Error compiling program: " + GLES20.glGetProgramInfoLog(programHandle));
                GLES20.glDeleteProgram(programHandle);
                programHandle = 0;
            }
        }

        if (programHandle == 0)
        {
            throw new RuntimeException("Error creating program.");
        }

        return programHandle;
    }

    public void releaseBuffers() {
        final int[] buffersToDelete = new int[] { mCubeIndicesBufferIdx, mCubeNormalsBufferIdx,
                mCubePositionsBufferIdx, mCubePositionsBufferIdx };
        GLES20.glDeleteBuffers(buffersToDelete.length, buffersToDelete, 0);
        mCubeIndicesBufferIdx = 0;
        mCubeNormalsBufferIdx = 0;
        mCubePositionsBufferIdx = 0;
        mCubePositionsBufferIdx = 0;
    }
}
