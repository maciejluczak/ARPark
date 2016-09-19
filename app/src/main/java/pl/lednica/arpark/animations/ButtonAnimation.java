package pl.lednica.arpark.animations;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import pl.lednica.arpark.R;

/**
 * Created by stachu on 18.09.2016.
 */
public class ButtonAnimation {
    final String textOut;
    final Animation buttonOut;
    final Animation buttonIn;
    public ButtonAnimation(Context context, final Button button, final String textIn) {
        textOut = button.getText().toString();
        buttonOut = AnimationUtils.loadAnimation(context, R.anim.button_out);
        buttonIn = AnimationUtils.loadAnimation(context, R.anim.button_in);

        buttonOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //Log.v(TAG, "start");

            }
            @Override
            public void onAnimationEnd(Animation animation) {
                //Log.v(TAG, "stop");
                if(button.getText().equals(textIn))
                    button.setText(textOut);
                else
                    button.setText(textIn);

                button.startAnimation(buttonIn);

            }
            @Override
            public void onAnimationRepeat(Animation animation) {



            }
        });


    }

    public Animation getAnimation(){
        return buttonOut;
    }
}

