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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import pl.lednica.arpark.R;
import pl.lednica.arpark.activities.object_explorer.ObjectExplorerTabActivity;
import pl.lednica.arpark.animations.ButtonAnimation;
import pl.lednica.arpark.helpers.MapObject;
import pl.lednica.arpark.helpers.MapPosition;
import pl.lednica.arpark.helpers.ObjectJsonUtils;
import pl.lednica.arpark.helpers.ObjectModel;

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

    // ustaw pozycje na ta w ktorej jestes - symulacja mapy
    double hereTestX = 16.911888;
    double hereTestY = 52.418429;
    double testDistanceX =  17.377807 - hereTestX;
    double testDistanceY = 52.52664 - hereTestY;

    MapObject[] mapObjectList = {
            new MapObject(17.377378, 52.5263, "churchButton", ChurchActivity.class),
            new MapObject(17.37759, 52.52590, "compostelaButton", CompostelaActivity.class),
            new MapObject(17.37668, 52.526548, "wallButton", WallActivity.class),
            new MapObject(17.378826, 52.526039, "palatiumButton", PalatiumActivity.class),
            new MapObject(17.378869, 52.527116, "boroughButton", BoroughActivity.class)
    };
    //Variables to Localisation
    private GoogleApiClient mGoogleApiClient;

    Button object3DViewButton;


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
        }

        setContentView(R.layout.activity_main);

        for(int i = 0; i < mapObjectList.length; i++){
            final int finalI = i;
            int oId = getResources().getIdentifier(mapObjectList[i].name, "id", getPackageName());

            mapObjectList[i].button = (Button) findViewById(oId);
            final ButtonAnimation buttAnim = new ButtonAnimation(getApplicationContext(), mapObjectList[i].button, goToAR);

            mapObjectList[i].button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    v.startAnimation(buttAnim.getAnimation());
                    mapObjectList[finalI].show = !mapObjectList[finalI].show;
                    drawPoints();

                }
            });

            mapObjectList[i].button.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mapObjectList[finalI].button.getText().toString().equals(goToAR)) {
                        Intent intent = new Intent(getApplicationContext(), mapObjectList[finalI].activityClass);
                        startActivity(intent);
                        overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
                        finish();
                    }
                    return true;
                }
            });
        }
        object3DViewButton = (Button) findViewById(R.id.object3DViewButton);
        object3DViewButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ObjectExplorerTabActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
                finish();
            }
        });
        drawPoints();
    }

    private void drawPoints(){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = true;
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.island,options);
        Bitmap workingBitmap = Bitmap.createBitmap(bitmap);
        Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);


        Canvas canvas = new Canvas(mutableBitmap);

        MapPosition posHere = new MapPosition(hereX, hereY);
        Bitmap bHere=BitmapFactory.decodeResource(getResources(), R.drawable.here_icon,options);
        canvas.drawBitmap(bHere,posHere.xOut-(bHere.getWidth()/2), posHere.yOut-(bHere.getHeight()/2), new Paint());

        Bitmap b=BitmapFactory.decodeResource(getResources(), R.drawable.object_icon,options);
        for (MapObject o:mapObjectList)
            if(o.show)
                canvas.drawBitmap(b, o.x-(b.getWidth()/2), o.y-(b.getHeight()/2) , new Paint());


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
        try{
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }catch (SecurityException e){
            Log.e("PERM ERR",e.toString());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("LocationFinder", "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        String mLastUpdateTime;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        hereX = location.getLongitude() + testDistanceX;
        hereY = location.getLatitude() + testDistanceY;
        //Toast.makeText(this, "Updated: " + mLastUpdateTime+" hereX: "+ hereX+" HereY: "+hereY, Toast.LENGTH_SHORT).show();
        drawPoints();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("LocationFinder", "Connection failed. Error: " + connectionResult.getErrorCode());
    }
}
