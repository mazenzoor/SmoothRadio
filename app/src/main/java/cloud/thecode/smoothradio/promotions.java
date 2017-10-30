package cloud.thecode.smoothradio;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

public class promotions extends Activity implements View.OnTouchListener {

    ImageButton a,b,c;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotions);

        a = (ImageButton) findViewById(R.id.a);
        b = (ImageButton) findViewById(R.id.b);
        c = (ImageButton) findViewById(R.id.c);

        a.setOnTouchListener(this);
        b.setOnTouchListener(this);
        c.setOnTouchListener(this);



    }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ImageButton temp = (ImageButton) v;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(temp,
                            "scaleX", 0.98f);
                    ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(temp,
                            "scaleY", 0.98f);
                    scaleDownX.setDuration(80);
                    scaleDownY.setDuration(80);

                    AnimatorSet scaleDown = new AnimatorSet();
                    scaleDown.play(scaleDownX).with(scaleDownY);

                    scaleDown.start();

                    break;

                case MotionEvent.ACTION_UP:
                    ObjectAnimator scaleDownX2 = ObjectAnimator.ofFloat(
                            temp, "scaleX", 1f);
                    ObjectAnimator scaleDownY2 = ObjectAnimator.ofFloat(
                            temp, "scaleY", 1f);
                    scaleDownX2.setDuration(190);
                    scaleDownY2.setDuration(190);

                    AnimatorSet scaleDown2 = new AnimatorSet();
                    scaleDown2.play(scaleDownX2).with(scaleDownY2);

                    scaleDown2.start();

                    if(temp == a) {
                        Uri link = Uri.parse("http://in2hosting.com/business-hosting.php");
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(link);
                        startActivity(i);
                    } else if(temp == b) {
                        Uri link = Uri.parse("https://in2streaming.com/");
                        Intent i2 = new Intent(Intent.ACTION_VIEW);
                        i2.setData(link);
                        startActivity(i2);
                    } else {
                        Uri link = Uri.parse("http://secure.in2hosting.com/access/index.php");
                        Intent i3 = new Intent(Intent.ACTION_VIEW);
                        i3.setData(link);
                        startActivity(i3);
                    }

                    break;


                case MotionEvent.ACTION_MOVE:
                    ObjectAnimator scaleDownX3 = ObjectAnimator.ofFloat(
                            temp, "scaleX", 1f);
                    ObjectAnimator scaleDownY3 = ObjectAnimator.ofFloat(
                            temp, "scaleY", 1f);
                    scaleDownX3.setDuration(190);
                    scaleDownY3.setDuration(190);

                    AnimatorSet scaleDown3 = new AnimatorSet();
                    scaleDown3.play(scaleDownX3).with(scaleDownY3);

                    scaleDown3.start();

                    break;

                case MotionEvent.ACTION_CANCEL:
                    ObjectAnimator scaleDownX4 = ObjectAnimator.ofFloat(
                            temp, "scaleX", 1f);
                    ObjectAnimator scaleDownY4 = ObjectAnimator.ofFloat(
                            temp, "scaleY", 1f);
                    scaleDownX4.setDuration(190);
                    scaleDownY4.setDuration(190);

                    AnimatorSet scaleDown4 = new AnimatorSet();
                    scaleDown4.play(scaleDownX4).with(scaleDownY4);

                    scaleDown4.start();

                    break;

            }
            return true;
        }



}
