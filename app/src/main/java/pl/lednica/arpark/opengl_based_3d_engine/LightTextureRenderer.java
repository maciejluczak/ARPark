package pl.lednica.arpark.opengl_based_3d_engine;

import android.app.Activity;
import android.content.res.AssetManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Maciej on 2016-09-24.
 * Klasa renderujaca obraz 3d
 */
public class LightTextureRenderer implements GLSurfaceView.Renderer {

    /**macierz modelu transformuje obiekt z przestrzeni modelu do przestrzeni świata
     * macierz widoku transformuje macierz świata w macierz widoku czyli kamery
     * macierz projekcji używana do rzutowania modelu 3d na ekran 2d
     * kombinacja poprzednich trzech macierzy*/
    protected float[] modelMatrix = new float[16];
    protected float[] viewMatrix = new float[16];
    private float[] projectionMatrix = new float[16];
    private float[] MVPMatrix = new float[16];
    private float[] lightModelMatrix = new float[16];

    /**Oznaczenie wersji renderere:tekstury czy kolor*/
    private static boolean TEXTURE_RENDERER=true;
    private boolean isTextureRenderer=false;

    /**Wektor przechowujący obiekty geometrii*/
    private Vector<MeshLoader> meshObjects = new Vector<>();

    private AssetManager assetMenager;

    private int MVPMatrixHandle;
    private int MVMatrixHandle;
    private int positionHandle;
    private int colorHandle;
    private int normalHandle;
    private int lightPosHandle;
    private int textureDataHandle;
    private int textureCoordinateHandle;
    private int textureUniformHandle;

    private String textureImageFile;

    private final float[] lightPosInModelSpace = new float[] {0.0f, 0.0f, 0.0f, 1.0f};
    private final float[] lightPosInWorldSpace = new float[4];
    private final float[] lightPosInEyeSpace = new float[4];

    private int perVertexProgramHandle;
    private int pointProgramHandle;
    private volatile float angle =0;
    private float scale;


    public LightTextureRenderer(Activity activity, boolean textureRenderer) {
        assetMenager = activity.getResources().getAssets();
        this.isTextureRenderer = textureRenderer;
    }


    void loadMesh(int i, ObjectFiles objectFiles){
        textureImageFile = objectFiles.file_texture_image;
        meshObjects.add(i,new MeshLoader());
        try {
            if (isTextureRenderer==TEXTURE_RENDERER) {
                meshObjects.get(i).loadToBuffer(objectFiles.file_v, MeshLoader.BUFFER_TYPE.BUFFER_TYPE_VERTEX,
                        MeshLoader.BUFFER_DATA_TYPE.DATA_FLOAT, assetMenager);
                meshObjects.get(i).loadToBuffer(objectFiles.file_t, MeshLoader.BUFFER_TYPE.BUFFER_TYPE_TEXTURE_COORD,
                        MeshLoader.BUFFER_DATA_TYPE.DATA_FLOAT, assetMenager);
                meshObjects.get(i).loadToBuffer(objectFiles.file_n, MeshLoader.BUFFER_TYPE.BUFFER_TYPE_NORMALS,
                        MeshLoader.BUFFER_DATA_TYPE.DATA_FLOAT, assetMenager);
                meshObjects.get(i).loadToBuffer(objectFiles.file_i, MeshLoader.BUFFER_TYPE.BUFFER_TYPE_INDICES,
                        MeshLoader.BUFFER_DATA_TYPE.DATA_SHORT, assetMenager);
            }else {
                meshObjects.get(i).loadToBuffer(objectFiles.file_c, MeshLoader.BUFFER_TYPE.BUFFER_TYPE_COLOR,
                        MeshLoader.BUFFER_DATA_TYPE.DATA_FLOAT, assetMenager);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 10.0f;

        Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = 0.0f;

        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -100.0f;

        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        Matrix.setLookAtM(viewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        final String vertexShader;
        final String fragmentShader;
        if(isTextureRenderer==TEXTURE_RENDERER) {
            vertexShader = SimpleLightTextureShaders.DEFFUSE_POINT_LIGHT_VERTEX_SHADER;
            fragmentShader = SimpleLightTextureShaders.DEFFUSE_POINT_LIGHT_FRAGMENT_SHADER;
            textureDataHandle = meshObjects.get(0).loadTextureFromApk(textureImageFile, assetMenager);
        }else {
            vertexShader = SimpleLightColorShaders.DEFFUSE_POINT_LIGHT_VERTEX_SHADER;
            fragmentShader = SimpleLightColorShaders.DEFFUSE_POINT_LIGHT_FRAGMENT_SHADER;
        }

        final int vertexShaderHandle = compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);
        final int fragmentShaderHandle = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);

        if(isTextureRenderer==TEXTURE_RENDERER) {
            perVertexProgramHandle = createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle,
                    new String[]{"a_Position", "a_Normal", "a_TexCoordinate"});
        }else{
            perVertexProgramHandle = createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle,
                    new String[]{"a_Position", "a_Color"});
        }
        // Define a simple shader program for our point.
        final String pointVertexShader =
                "uniform mat4 u_MVPMatrix;      \n"
                        + "attribute vec4 a_Position;     \n"
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
        pointProgramHandle = createAndLinkProgram(pointVertexShaderHandle, pointFragmentShaderHandle,
                new String[]{"a_Position"});

        if(isTextureRenderer==TEXTURE_RENDERER) {
            for (MeshLoader mesh : meshObjects) {
                int buffers[] = new int[3];
                GLES20.glGenBuffers(3, buffers, 0);

                int ibo[] = new int[1];
                GLES20.glGenBuffers(1, ibo, 0);

                mesh.getmModelVertices().position(0);
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[0]);
                GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mesh.getmModelVertices().capacity() * mesh.getmBytesPerFloat(),
                        mesh.getmModelVertices(), GLES20.GL_STATIC_DRAW);
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
                mesh.setmCubePositionsBufferIdx(buffers[0]);

                mesh.getmModelNormals().position(0);
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[1]);
                GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mesh.getmModelNormals().capacity() * mesh.getmBytesPerFloat(),
                        mesh.getmModelNormals(), GLES20.GL_STATIC_DRAW);
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
                mesh.setmCubeNormalsBufferIdx(buffers[1]);

                mesh.getmModelTextures().position(0);
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[2]);
                GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mesh.getmModelTextures().capacity() * mesh.getmBytesPerFloat(),
                        mesh.getmModelTextures(), GLES20.GL_STATIC_DRAW);
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
                mesh.setmCubeTexCoordsBufferIdx(buffers[2]);


                mesh.getmModelIndices().position(0);
                GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
                GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, mesh.getmCountIndices() * mesh.getmBytesPerShort(),
                        mesh.getmModelIndices().asShortBuffer(), GLES20.GL_STATIC_DRAW);
                GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
                mesh.setmCubeIndicesBufferIdx(ibo[0]);

                buffers[0] = 0;
                buffers[1] = 0;
                buffers[2] = 0;
                ibo[0] = 0;
            }
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //Ustawienia OpenGL
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(perVertexProgramHandle);
        GLES20.glFrontFace(GLES20.GL_CCW);
        GLES20.glDisable(GLES20.GL_CULL_FACE);

        //załadowanie uchwytów do atrybutów i związanie tekstury
        if(isTextureRenderer==TEXTURE_RENDERER) {
            MVPMatrixHandle = GLES20.glGetUniformLocation(perVertexProgramHandle, "u_MVPMatrix");
            MVMatrixHandle = GLES20.glGetUniformLocation(perVertexProgramHandle, "u_MVMatrix");
            lightPosHandle = GLES20.glGetUniformLocation(perVertexProgramHandle, "u_LightPos");
            positionHandle = GLES20.glGetAttribLocation(perVertexProgramHandle, "a_Position");
            normalHandle = GLES20.glGetAttribLocation(perVertexProgramHandle, "a_Normal");
            textureUniformHandle = GLES20.glGetUniformLocation(perVertexProgramHandle, "u_Texture");
            textureCoordinateHandle = GLES20.glGetAttribLocation(perVertexProgramHandle, "a_TexCoordinate");

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureDataHandle);
            GLES20.glUniform1i(textureUniformHandle, 0);
        }else {
            MVPMatrixHandle = GLES20.glGetUniformLocation(perVertexProgramHandle, "u_MVPMatrix");
            positionHandle = GLES20.glGetAttribLocation(perVertexProgramHandle, "a_Position");
            colorHandle = GLES20.glGetAttribLocation(perVertexProgramHandle, "a_Color");
            //GLES20.glUseProgram(perVertexProgramHandle);
        }

        Matrix.setIdentityM(lightModelMatrix, 0);
        Matrix.translateM(lightModelMatrix, 0, 0.0f, 0.0f, -scale);
        Matrix.multiplyMV(lightPosInWorldSpace, 0, lightModelMatrix, 0, lightPosInModelSpace, 0);
        Matrix.multiplyMV(lightPosInEyeSpace, 0, viewMatrix, 0, lightPosInWorldSpace, 0);

        for (MeshLoader mesh : meshObjects) {
            Matrix.setIdentityM(modelMatrix, 0);
            translateM();
            drawModel(mesh, mesh.getmCubePositionsBufferIdx(), mesh.getmCubeNormalsBufferIdx(),
                    mesh.getmCubeTexCoordsBufferIdx(), mesh.getmCubeIndicesBufferIdx());
        }

        GLES20.glUseProgram(pointProgramHandle);
        drawLight();
    }

    protected void translateM(){
        Matrix.translateM(modelMatrix, 0, 0.0f, -1.0f, -scale);
        Matrix.rotateM(modelMatrix, 0, angle, 0.0f, 1.0f, 0.0f);
    }

    private void drawModel(MeshLoader meshLoc, int bufVert, int bufNorms,int bufText,int bufInd)
    {
        if(isTextureRenderer==TEXTURE_RENDERER) {
            //IDX_CPU
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufVert);
            GLES20.glEnableVertexAttribArray(positionHandle);
            GLES20.glVertexAttribPointer(positionHandle, meshLoc.getmPositionDataSize(), GLES20.GL_FLOAT, false, 0, 0);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufNorms);
            GLES20.glEnableVertexAttribArray(normalHandle);
            GLES20.glVertexAttribPointer(normalHandle, meshLoc.getmNormalDataSize(), GLES20.GL_FLOAT, false, 0, 0);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufText);
            GLES20.glEnableVertexAttribArray(textureCoordinateHandle);
            GLES20.glVertexAttribPointer(textureCoordinateHandle, meshLoc.getmTextureDataSize(), GLES20.GL_FLOAT, false, 0, 0);

            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, bufInd);

            // Utworzenie macierzy Model x View i przekazanie jej do OpenGL w celu kalkulacji światła
            Matrix.multiplyMM(MVPMatrix, 0, viewMatrix, 0, modelMatrix, 0);
            GLES20.glUniformMatrix4fv(MVMatrixHandle, 1, false, MVPMatrix, 0);

            // Utworzenie macierzy Model x View x Projection i przekazanie do OpenGl
            Matrix.multiplyMM(MVPMatrix, 0, projectionMatrix, 0, MVPMatrix, 0);
            GLES20.glUniformMatrix4fv(MVPMatrixHandle, 1, false, MVPMatrix, 0);

            // Przekazanie pozycji światła w przestrzeni widoku
            GLES20.glUniform3f(lightPosHandle, lightPosInEyeSpace[0], lightPosInEyeSpace[1], lightPosInEyeSpace[2]);

            //Rysowanie modelu bez indeksu
            //GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mesh.mCountVertices);

            //IDX_CPU Rysowanie modelu z indeksem,z ładowaymi buforami do opengl
            GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                    meshLoc.getmCountIndices(),
                    GLES20.GL_UNSIGNED_SHORT, 0);

            //IDX Rysowanie modelu z indeksem, bez ładowania buforów do opengl
            /*GLES20.glDrawElements(GLES20.GL_TRIANGLES,
              mesh.mCountIndices,
              GLES20.GL_UNSIGNED_SHORT, mesh.mModelIndices);*/

            //IDX_CPU
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        }else{
            meshLoc.getmModelColors().position(meshLoc.getmPositionOffset());
            GLES20.glVertexAttribPointer(positionHandle, meshLoc.getmPositionDataSize(), GLES20.GL_FLOAT, false,
                    meshLoc.getmStrideBytesColor(), meshLoc.getmModelColors());
            GLES20.glEnableVertexAttribArray(positionHandle);

            meshLoc.getmModelColors().position(meshLoc.getmColorOffset());
            GLES20.glVertexAttribPointer(colorHandle, meshLoc.getmColorDataSize(), GLES20.GL_FLOAT, false,
                    meshLoc.getmStrideBytesColor(), meshLoc.getmModelColors());
            GLES20.glEnableVertexAttribArray(colorHandle);

            Matrix.multiplyMM(MVPMatrix, 0, viewMatrix, 0, modelMatrix, 0);
            Matrix.multiplyMM(MVPMatrix, 0, projectionMatrix, 0, MVPMatrix, 0);

            GLES20.glUniformMatrix4fv(MVPMatrixHandle, 1, false, MVPMatrix, 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, meshLoc.getmCountColors());
        }
    }

    /**
     * Draws a point representing the position of the light.
     */
    private void drawLight()
    {
        final int pointMVPMatrixHandle = GLES20.glGetUniformLocation(pointProgramHandle, "u_MVPMatrix");
        final int pointPositionHandle = GLES20.glGetAttribLocation(pointProgramHandle, "a_Position");
        GLES20.glVertexAttrib3f(pointPositionHandle, lightPosInModelSpace[0], lightPosInModelSpace[1], lightPosInModelSpace[2]);
        GLES20.glDisableVertexAttribArray(pointPositionHandle);
        Matrix.multiplyMM(MVPMatrix, 0, viewMatrix, 0, lightModelMatrix, 0);
        Matrix.multiplyMM(MVPMatrix, 0, projectionMatrix, 0, MVPMatrix, 0);
        GLES20.glUniformMatrix4fv(pointMVPMatrixHandle, 1, false, MVPMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);
    }

    /**
     * Helper function to compile a shader.
     */
    private int compileShader(final int shaderType, final String shaderSource)
    {
        int shaderHandle = GLES20.glCreateShader(shaderType);
        if (shaderHandle != 0)
        {
            GLES20.glShaderSource(shaderHandle, shaderSource);
            GLES20.glCompileShader(shaderHandle);
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
            if (compileStatus[0] == 0)
            {
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
     * Tworzy i podłącza program zawierający vertex,fragment-shader
     */
    private int createAndLinkProgram(final int vertexShaderHandle, final int fragmentShaderHandle, final String[] attributes)
    {
        int programHandle = GLES20.glCreateProgram();

        if (programHandle != 0)
        {
            GLES20.glAttachShader(programHandle, vertexShaderHandle);
            GLES20.glAttachShader(programHandle, fragmentShaderHandle);
            if (attributes != null)
            {
                final int size = attributes.length;
                for (int i = 0; i < size; i++)
                {
                    GLES20.glBindAttribLocation(programHandle, i, attributes[i]);
                }
            }
            GLES20.glLinkProgram(programHandle);
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);
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
        return programHandle;
    }

    public void releaseBuffers() {
        for(MeshLoader mesh : meshObjects) {
            final int[] buffersToDelete = new int[]{mesh.getmCubeIndicesBufferIdx(), mesh.getmCubeNormalsBufferIdx(),
                    mesh.getmCubePositionsBufferIdx(), mesh.getmCubePositionsBufferIdx()};
            GLES20.glDeleteBuffers(buffersToDelete.length, buffersToDelete, 0);
            mesh.setmCubeIndicesBufferIdx(0);
            mesh.setmCubeNormalsBufferIdx(0);
            mesh.setmCubePositionsBufferIdx(0);
            mesh.setmCubePositionsBufferIdx(0);
        }
    }

    float getAngle() {
        return angle;
    }

    void setAngle(float angle) {
        this.angle = angle;
    }

    void setScale(float scale){
        this.scale = scale;
    }

}
