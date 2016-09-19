package pl.lednica.arpark.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import pl.lednica.arpark.R;
import pl.lednica.arpark.animations.ButtonAnimation;

public class MainActivity extends Activity {

    Button churchButton;
    Button compostelaButton;
    private static final String TAG = "MainActivity";
    boolean showChurch = false;
    boolean showCompostela = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        churchButton = (Button) findViewById(R.id.churchButton);
        final ButtonAnimation churchButtAnim = new ButtonAnimation(getApplicationContext(), churchButton, "Włącz AR");

        churchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                    v.startAnimation(churchButtAnim.getAnimation());
                    showChurch=!showChurch;

                    drawPoint();

            }
        });

        churchButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(churchButton.getText().toString().equals("Włącz AR") ) {
                    Intent intent = new Intent(getApplicationContext(), ChurchActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
                    finish();
                }
                return true;
            }
        });

        compostelaButton = (Button) findViewById(R.id.compostelaButton);
        final ButtonAnimation compButtAnim = new ButtonAnimation(getApplicationContext(), compostelaButton, "Włącz AR");

        compostelaButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                v.startAnimation(compButtAnim.getAnimation());
                showCompostela=!showCompostela;
                drawPoint();
            }
        });

        compostelaButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(compostelaButton.getText().toString().equals("Włącz AR") ) {
                    Intent intent = new Intent(getApplicationContext(), CompostelaActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
                    finish();
                }
                return true;
            }
        });


    }

    private void drawPoint(){
        BitmapFactory.Options myOptions = new BitmapFactory.Options();
        myOptions.inDither = true;
        myOptions.inScaled = false;
        myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// important
        myOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.island,myOptions);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLUE);


        Bitmap workingBitmap = Bitmap.createBitmap(bitmap);
        Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);


        Canvas canvas = new Canvas(mutableBitmap);
        if(showChurch)
            canvas.drawCircle(60, 50, 25, paint);
        if(showCompostela)
            canvas.drawCircle(60, 100, 25, paint);

        ImageView imageView = (ImageView)findViewById(R.id.island_map);
        imageView.setAdjustViewBounds(true);
        imageView.setImageBitmap(mutableBitmap);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}
