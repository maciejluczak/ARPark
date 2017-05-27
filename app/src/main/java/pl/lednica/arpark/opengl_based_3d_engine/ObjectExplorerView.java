package pl.lednica.arpark.opengl_based_3d_engine;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

/**
 * Created by Maciej on 23.03.2017.
 * Class extends GLSurfaceView and handling:
 * touch events - rotation of 3D object
 */

public class ObjectExplorerView extends android.opengl.GLSurfaceView {

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;
    private LightTextureRenderer renderer;

    public ObjectExplorerView(Context context) {
        super(context);
    }


    @Override
    public boolean onTouchEvent(MotionEvent e) {

        // MotionEvent obsługuje dane wejściowe na ekran dotykowy
        // i innych kontrolerów. Funkcje e.getX() e.getY()
        // pokazują zmianę pozycji dotyku.
        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = x - mPreviousX;
                float dy = y - mPreviousY;

                // odwrócenie rotacji w przypadku dotyku powyżej lini środka ekranu
                if (y > getHeight() / 2) {
                    dx = dx * -1 ;
                }

                // odwrócenie rotacji w przypadku dotyku na lewo od środka ekranu
                if (x < getWidth() / 2) {
                    dy = dy * -1 ;
                }

                renderer.setAngle(
                        renderer.getAngle() +
                                ((dx + dy) * TOUCH_SCALE_FACTOR));
                requestRender();
        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }

    @Override
    public void setRenderer(Renderer renderer) {
        super.setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        this.renderer = (LightTextureRenderer) renderer;
    }
}
