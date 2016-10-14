package pl.lednica.arpark.activities;

/**
 * Created by Maciej on 2016-09-24.
 * Aktywność sprawdza czy jest odpowiednia wersja OpenGLES i ustawia klasę odpowiadającą za
 * renderowanie obiektów 3d
 */

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;

import pl.lednica.arpark.R;
import pl.lednica.arpark.opengl_based_3d_engine.SimpleLightColorRenderer;


public class Object3DViewActivity extends Activity {

    private GLSurfaceView mGLSurfaceView;
    private static final String LOGTAG = "Object3DViewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLSurfaceView = new GLSurfaceView(this);

        // Sprawdzenie czy użądzenie obsługuje OpenGL ES 2.0.
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;
        if (supportsEs2)
        {
            // Ustawienie wymaganej wersji OpenGL na 2
            mGLSurfaceView.setEGLContextClientVersion(2);

            //Ustawienie odpowiedniej klasy jako Renderera
            mGLSurfaceView.setRenderer( new SimpleLightColorRenderer(this) );

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
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
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
}
