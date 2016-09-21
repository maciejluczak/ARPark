package pl.lednica.arpark.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.Date;

import pl.lednica.arpark.R;
import pl.lednica.arpark.animations.ButtonAnimation;
import pl.lednica.arpark.helpers.MapPosition;

public class MainActivity extends Activity
        implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener{

    Button churchButton;
    Button compostelaButton;
    private static final String TAG = "MainActivity";
    private static final String goToAR = "Przytrzmaj dłużej >>>";
    boolean showChurch = false;
    boolean showCompostela = false;
    
    

    double hereX = 17.377807;
    double hereY = 52.52664;

    double hereTestX = 16.911888;
    double hereTestY = 52.418429;
    double testDistanceX =  17.377807 - hereTestX;
    double testDistanceY = 52.52664 - hereTestY;


    //Variables to Localisation
    private GoogleApiClient mGoogleApiClient;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Create Google Localisation Service instance
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            Log.d(TAG, "jestem 1");
        }

        setContentView(R.layout.activity_main);



        churchButton = (Button) findViewById(R.id.churchButton);
        final ButtonAnimation churchButtAnim = new ButtonAnimation(getApplicationContext(), churchButton,  goToAR);

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
                if(churchButton.getText().toString().equals(goToAR) ) {
                    Intent intent = new Intent(getApplicationContext(), ChurchActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
                    finish();
                }
                return true;
            }
        });

        compostelaButton = (Button) findViewById(R.id.compostelaButton);
        final ButtonAnimation compButtAnim = new ButtonAnimation(getApplicationContext(), compostelaButton, goToAR);

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
                if(compostelaButton.getText().toString().equals(goToAR) ) {
                    Intent intent = new Intent(getApplicationContext(), CompostelaActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
                    finish();
                }
                return true;
            }
        });

        drawPoint();
    }

    private void drawPoint(){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = true;
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.island,options);
        Bitmap workingBitmap = Bitmap.createBitmap(bitmap);
        Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);


        Canvas canvas = new Canvas(mutableBitmap);



        Log.d(TAG, "wysokosc: " + String.valueOf(canvas.getHeight()));
        Log.d(TAG, "szerokosc: " + String.valueOf(canvas.getWidth()));
        Log.d(TAG, "wysokosc bit: " + String.valueOf(canvas.getMaximumBitmapHeight()));
        Log.d(TAG, "szerokosc bit: " + String.valueOf(canvas.getMaximumBitmapWidth()));

        if(showChurch) {
            MapPosition posCh = new MapPosition(17.377378, 52.5263);

//            x: 158
//            y: 293
//
//            x: 86
//            y: 221

            //  x: 230 72
            // y: 365 72

            //x: 230 144
            //y: 365 144
//
//            x: 230 36
//            y: 365 36

            Bitmap bCh=BitmapFactory.decodeResource(getResources(), R.drawable.church_icon,options);
            Log.d(TAG, "x: " + String.valueOf(posCh.xOut) + " " + String.valueOf((bCh.getWidth()/2)));
            Log.d(TAG, "y: " + String.valueOf(posCh.yOut) + " " + String.valueOf(bCh.getHeight()/2));
            canvas.drawBitmap(bCh,posCh.xOut-(bCh.getWidth()/2), posCh.yOut-(bCh.getHeight()/2), new Paint());
        }
        if(showCompostela) {
            MapPosition posCo = new MapPosition(17.37759, 52.52590);
            Bitmap bCo=BitmapFactory.decodeResource(getResources(), R.drawable.compostela_icon, options);
            canvas.drawBitmap(bCo,posCo.xOut-(bCo.getWidth()/2), posCo.yOut-(bCo.getHeight()/2), new Paint());
        }

        MapPosition posHere = new MapPosition(hereX, hereY);
        Bitmap bHere=BitmapFactory.decodeResource(getResources(), R.drawable.here_icon,options);
        canvas.drawBitmap(bHere,posHere.xOut-(bHere.getWidth()/2), posHere.yOut-(bHere.getHeight()/2), new Paint());

        ImageView imageView = (ImageView)findViewById(R.id.island_map);
        imageView.setAdjustViewBounds(true);
        imageView.setImageBitmap(mutableBitmap);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onResume() {
        //Connect to localisation
        mGoogleApiClient.connect();
        super.onResume();
    }

    @Override
    protected void onPause() {
        //Disconnect to localisation
        mGoogleApiClient.disconnect();
        super.onPause();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest mLocationRequest;
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        Log.d(TAG, "jestem 2");
        try{
            Log.d(TAG, "jestem 3");
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }catch (SecurityException e){
            Log.e("PERM ERR",e.toString());
            Log.d(TAG, "jestem 4");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("LocationFinderFragment", "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "jestem 5");

        String mLastUpdateTime;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        hereX = location.getLongitude() + testDistanceX;
        hereY = location.getLatitude() + testDistanceY;
        Toast.makeText(this, "Updated: " + mLastUpdateTime+" hereX: "+ hereX+" HereY: "+hereY, Toast.LENGTH_SHORT).show();
        drawPoint();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "jestem 6");

        Log.i("LocationFinderFragment", "Connection failed. Error: " + connectionResult.getErrorCode());
    }
}
