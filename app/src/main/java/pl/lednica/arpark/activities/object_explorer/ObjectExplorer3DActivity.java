package pl.lednica.arpark.activities.object_explorer;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;

import pl.lednica.arpark.R;
import pl.lednica.arpark.activities.MainActivity;
import pl.lednica.arpark.helpers.ObjectModel;
import pl.lednica.arpark.opengl_based_3d_engine.CottageObject;
import pl.lednica.arpark.opengl_based_3d_engine.LightColorRenderer;
import pl.lednica.arpark.opengl_based_3d_engine.LightTextureRenderer;
import pl.lednica.arpark.opengl_based_3d_engine.ObjectExplorerView;
import pl.lednica.arpark.opengl_based_3d_engine.UniversalColorObject;
import pl.lednica.arpark.opengl_based_3d_engine.UniversalTextureObject;

/**
 * Created by Maciej on 2016-09-24.
 * Aktywność sprawdza czy jest odpowiednia wersja OpenGLES, tworzy widok GLSurfaceView i przypisuje
 * do niego właściwą klasę odpowiadającą za renderowanie obiektów 3D
 */

public class ObjectExplorer3DActivity extends Activity {

    private GLSurfaceView mGLSurfaceView;
    private GLSurfaceView mGLSurfaceViewColor;
    private static final String LOGTAG = "ObjExp3DActivity";
    private LightTextureRenderer renderer;
    private LightColorRenderer rendererColor;
    private ObjectModel objectModel;
    private final static String INTENT_OBJECT_EXTRA = "object_3d_extra";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        objectModel = (ObjectModel) getIntent().getSerializableExtra(INTENT_OBJECT_EXTRA);
        Log.e(LOGTAG,objectModel.getDesc());
        //Stworzenie widoku który dziedziczy z GLSurfaceView i obsługuje dotyk
//        if(objectModel.getModelName().equals(new String("cross"))) {
//            mGLSurfaceView = new GLSurfaceView(this);
//        }else{
            mGLSurfaceView = new ObjectExplorerView(this);
//        }
        // Sprawdzenie czy użądzenie obsługuje OpenGL ES 2.0.
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;
        if (supportsEs2)
        {
            // Ustawienie wymaganej wersji OpenGL na 2
            mGLSurfaceView.setEGLContextClientVersion(2);

            //Ustawienie odpowiedniej klasy jako Renderera
            //renderer = new CottageObject(this);
            if(objectModel.getModelName().equals("cross")){
                renderer = new UniversalColorObject(this, objectModel);
                mGLSurfaceView.setRenderer( renderer );
            }else {
                renderer = new UniversalTextureObject(this, objectModel);
                mGLSurfaceView.setRenderer( renderer );
            }
            //renderer = new ChurchObjectFiles(this);
            //rendererColor = new LightColorRenderer(this);
            //mGLSurfaceView.setRenderer( renderer );
            //mGLSurfaceView.setRenderer( new DummRenderer(this) );
        }
        else
        {
            //Możłiwość dodania obsługi dla nirzszej wersji OpenGLES
            Log.e(LOGTAG,"Błędna wersja OpenGLES");
            return;
        }
        // Ustawienie widoku jako głównego
        setContentView( mGLSurfaceView );
    }

    @Override
    public void onBackPressed() {
        //Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        //startActivity(intent);
        overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
        finish();
    }

    @Override
    protected void onResume() {
        mGLSurfaceView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mGLSurfaceView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        if(renderer != null)
            renderer.releaseBuffers();
        super.onStop();
    }
}
