package pl.lednica.arpark.object_recognition_engine;

/**
 * Created by stachu on 13.08.2016.
 */
/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other
countries.
===============================================================================*/

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.vuforia.Matrix44F;
import com.vuforia.MultiTargetResult;
import com.vuforia.Renderer;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.TrackableResult;
import com.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.vuforia.Vuforia;


public class MultiTargetRenderer implements GLSurfaceView.Renderer
{
    private static final String LOGTAG = "MultiTargetRenderer";

    private CustomApplicationSession vuforiaAppSession;

    public boolean mIsActive = false;

    private int shaderProgramID;

    private int vertexHandle;
    private int normalHandle;
    private int textureCoordHandle;
    private int mvpMatrixHandle;
    private int texSampler2DHandle;

    private Vector<Texture> mTextures;

    private double prevTime;
    private float rotateAngle;

    private MeshObject bowlAndSpoonObject;

    final static float kCubeScaleX = 120.0f * 0.75f / 2.0f;
    final static float kCubeScaleY = 120.0f * 1.00f / 2.0f;
    final static float kCubeScaleZ = 120.0f * 0.50f / 2.0f;
    final static float scaleRate = 8 * 1.5f;
    final static float kBowlScaleX = 120.0f * scaleRate;
    final static float kBowlScaleY = 120.0f * scaleRate;
    final static float kBowlScaleZ = 120.0f * scaleRate;
    int widthT, heightT;



    public MultiTargetRenderer(CustomApplicationSession session, MeshObject object)
    {
        vuforiaAppSession = session;
        bowlAndSpoonObject = object;
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");

        initRendering();

        vuforiaAppSession.onSurfaceCreated();
    }


    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");
        Log.d(LOGTAG, "Stanley.onSurfaceChanged");
        vuforiaAppSession.onSurfaceChanged(width, height);
        Log.d(LOGTAG, "GLRenderer.onSurfaceChangedExtra");
    }


    @Override
    public void onDrawFrame(GL10 gl)
    {
        if (!mIsActive)
            return;

        renderFrame();
    }


    private void initRendering()
    {
        Log.d(LOGTAG, "MultiTargetsRenderer.initRendering");

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f
                : 1.0f);

        for (Texture t : mTextures)
        {
            GLES20.glGenTextures(1, t.mTextureID, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.mTextureID[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                    t.mWidth, t.mHeight, 0, GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE, t.mData);
        }

        shaderProgramID = CustomUtils.createProgramFromShaderSrc(
                CubeShaders.CUBE_MESH_VERTEX_SHADER,
                CubeShaders.CUBE_MESH_FRAGMENT_SHADER);

        vertexHandle = GLES20.glGetAttribLocation(shaderProgramID,
                "vertexPosition");
        normalHandle = GLES20.glGetAttribLocation(shaderProgramID,
                "vertexNormal");
        textureCoordHandle = GLES20.glGetAttribLocation(shaderProgramID,
                "vertexTexCoord");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID,
                "modelViewProjectionMatrix");
        texSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID,
                "texSampler2D");
    }


    private void renderFrame()
    {
        float ratio = (float)  widthT/ heightT;

        CustomUtils.checkGLError("Check gl errors prior render Frame");

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        State state = Renderer.getInstance().begin();

        Renderer.getInstance().drawVideoBackground();

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        int[] viewport = vuforiaAppSession.getViewport();
        GLES20.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);

        if (state.getNumTrackableResults() != 0)
        {
            Log.d("renderFrame", "znalazlem");
            TrackableResult result = null;
            int numResults = state.getNumTrackableResults();

            for (int j = 0; j < numResults; j++)
            {
                result = state.getTrackableResult(j);
                if (result.isOfType(MultiTargetResult.getClassType()))
                    break;
                result = null;


            }



            if (result == null)
            {
                GLES20.glDisable(GLES20.GL_BLEND);
                GLES20.glDisable(GLES20.GL_DEPTH_TEST);

                Renderer.getInstance().end();
                return;
            }else
            {
                Log.d("stachu", "wyszukalem " + result.getTrackable().getName() );

            }

            Matrix44F modelViewMatrix_Vuforia = Tool
                    .convertPose2GLMatrix(result.getPose());
            float[] modelViewMatrix = modelViewMatrix_Vuforia.getData();

            float[] modelViewProjection = new float[16];

            Matrix.scaleM(modelViewMatrix, 0, kCubeScaleX, kCubeScaleY,
                    kCubeScaleZ);
            Matrix.multiplyMM(modelViewProjection, 0, vuforiaAppSession
                    .getProjectionMatrix().getData(), 0, modelViewMatrix, 0);

            GLES20.glUseProgram(shaderProgramID);

            GLES20.glEnable(GLES20.GL_CULL_FACE);
            GLES20.glCullFace(GLES20.GL_BACK);
            if (Renderer.getInstance().getVideoBackgroundConfig()
                    .getReflection() == VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON)
                GLES20.glFrontFace(GLES20.GL_CW); // Front camera
            else
                GLES20.glFrontFace(GLES20.GL_CCW); // Back camera

            GLES20.glEnableVertexAttribArray(vertexHandle);
            GLES20.glEnableVertexAttribArray(normalHandle);
            GLES20.glEnableVertexAttribArray(textureCoordHandle);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                    mTextures.get(0).mTextureID[0]);
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
                    modelViewProjection, 0);
            GLES20.glUniform1i(texSampler2DHandle, 0);


            GLES20.glDisable(GLES20.GL_CULL_FACE);

            modelViewMatrix = modelViewMatrix_Vuforia.getData();


            Matrix.translateM(modelViewMatrix, 0, 0, 0,
                    0.0f);
            Matrix.rotateM(modelViewMatrix, 0, 90.0f, 0, 1.0f, 0);

            Matrix.scaleM(modelViewMatrix, 0, kBowlScaleX, kBowlScaleY,
                    kBowlScaleZ);
            Matrix.multiplyMM(modelViewProjection, 0, vuforiaAppSession
                    .getProjectionMatrix().getData(), 0, modelViewMatrix, 0);

            GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
                    false, 0, bowlAndSpoonObject.getVertices());
            GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT,
                    false, 0, bowlAndSpoonObject.getNormals());
            GLES20.glVertexAttribPointer(textureCoordHandle, 2,
                    GLES20.GL_FLOAT, false, 0, bowlAndSpoonObject.getTexCoords());

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                    mTextures.get(0).mTextureID[0]);
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
                    modelViewProjection, 0);
            GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                    bowlAndSpoonObject.getNumObjectIndex(),
                    GLES20.GL_UNSIGNED_SHORT, bowlAndSpoonObject.getIndices());

            GLES20.glDisableVertexAttribArray(vertexHandle);
            GLES20.glDisableVertexAttribArray(normalHandle);
            GLES20.glDisableVertexAttribArray(textureCoordHandle);

            CustomUtils.checkGLError("MultiTargets renderFrame");

        }

        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        Renderer.getInstance().end();

    }


    private void animateBowl(float[] modelViewMatrix)
    {
        double time = System.currentTimeMillis();
        float dt = (float) (time - prevTime) / 1000;

        rotateAngle += dt * 180.0f / 3.1415f;
        rotateAngle %= 360;
        Log.d(LOGTAG, "Delta animation time: " + rotateAngle);

        Matrix.rotateM(modelViewMatrix, 0, rotateAngle, 0.0f, 1.0f, 0.0f);

        prevTime = time;
    }


    public void setTextures(Vector<Texture> textures)
    {
        mTextures = textures;

    }

}

