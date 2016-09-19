package pl.lednica.arpark.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


import pl.lednica.arpark.R;

public class CompostelaActivity extends Activity {

    private static final String TAG = "CompostelaActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compostela);


    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
        finish();
    }

}
