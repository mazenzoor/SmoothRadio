package cloud.thecode.smoothradio;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import cloud.thecode.library.radio.RadioListener;
import cloud.thecode.library.radio.RadioManager;
import cloud.thecode.library.radio.RadioPlayerService;

/**
 * Created by mertsimsek on 04/11/15.
 */
public class RadioActivity extends Activity implements RadioListener{

    private final String[] RADIO_URL = {"http://in2streaming.com:9999"};
    TextView song_title_view;
    ImageButton mButtonControlStart, fb, tw;
    RadioManager mRadioManager;
    ProgressBar loader;
    final int darkBlue = Color.parseColor("#222939");
    final int pink = Color.parseColor("#e26860");
    RelativeLayout parent_layout;
    String fullString;
    Window window;
    LinearLayout promotion;

    PhoneStateListener phoneStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio);

        window = getWindow();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setNavigationBarColor(darkBlue);
        }

        loader = (ProgressBar) findViewById(R.id.loading);
        song_title_view = (TextView) findViewById(R.id.song_title);
        mRadioManager = RadioManager.with(getApplicationContext());
        mRadioManager.registerListener(this);
        mRadioManager.setLogging(true);
        parent_layout = (RelativeLayout) findViewById(R.id.parent_layout);
        promotion = (LinearLayout) findViewById(R.id.promotions);
        fb = (ImageButton) findViewById(R.id.fb);
        tw = (ImageButton) findViewById(R.id.tw);
        initializeUI();

        GetMetadata();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                GetMetadata();
            }
        }, 0, 6000);

        // Promotions open intent
        promotion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent promo = new Intent(RadioActivity.this, promotions.class);
                startActivity(promo);
            }
        });


        // SOcial links
        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uriFB = Uri.parse("https://www.facebook.com/smoothradiocanada/");
                Intent intentFB = new Intent(Intent.ACTION_VIEW, uriFB);
                startActivity(intentFB);
            }
        });

        tw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uriFB = Uri.parse("https://www.twitter.com/smoothradioca/"); // missing 'http://' will cause crashed
                Intent intentTW = new Intent(Intent.ACTION_VIEW, uriFB);
                startActivity(intentTW);
            }
        });

    }

    public void initializeUI() {
        mButtonControlStart = (ImageButton) findViewById(R.id.buttonControlStart);

        mButtonControlStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mRadioManager.isPlaying())
                    mRadioManager.startRadio(RADIO_URL[0]);
                else
                    mRadioManager.stopRadio();
            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
        mRadioManager.connect();
    }

    @Override
    public void onRadioLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //TODO Do UI works here.
                loader.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onRadioConnected() {

    }

    @Override
    public void onRadioStarted() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loader.setVisibility(View.INVISIBLE);
                playing();

                phoneStateListener = new PhoneStateListener() {
                    @Override
                    public void onCallStateChanged(int state, String incomingNumber) {
                        if (state == TelephonyManager.CALL_STATE_RINGING) {
                            //Incoming call: Pause music
                            mRadioManager.stopRadio();
                        } else if(state == TelephonyManager.CALL_STATE_OFFHOOK) {
                            //A call is dialing, active or on hold
                            mRadioManager.stopRadio();
                        }
                        super.onCallStateChanged(state, incomingNumber);
                    }
                };

                TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                if(mgr != null) {
                    mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
                }
            }
        });
    }

    @Override
    public void onRadioStopped() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                paused();
                TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                if(mgr != null) {
                    mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
                }
            }
        });
    }

    @Override
    public void onMetaDataReceived(String s, String s2) {
        // Do nothing I have my own metadata reciever
    }

    @Override
    public void onError() {

    }


    public void playing() {
        //playing state
        mButtonControlStart.setImageResource(R.drawable.ic_pause);
        ValueAnimator anim = new ValueAnimator();
        anim.setIntValues(darkBlue, pink);
        anim.setEvaluator(new ArgbEvaluator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                parent_layout.setBackgroundColor((Integer)valueAnimator.getAnimatedValue());
            }
        });

        anim.setDuration(300);
        anim.start();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setNavigationBarColor(pink);
        }
    }

    public void paused() {
        //pause
        mButtonControlStart.setImageResource(R.drawable.ic_play);

        ValueAnimator anim = new ValueAnimator();
        anim.setIntValues(pink, darkBlue);
        anim.setEvaluator(new ArgbEvaluator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                parent_layout.setBackgroundColor((Integer)valueAnimator.getAnimatedValue());
            }
        });

        anim.setDuration(300);
        anim.start();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setNavigationBarColor(darkBlue);
        }
    }


    public void GetMetadata() {
        String text = fullString;
        try
        {
            URL url = new URL("http://in2streaming.com:9999/7.html");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            fullString = "";
            String line;
            while ((line = reader.readLine()) != null) {
                fullString += line;
            }
            reader.close();

        } catch (Exception ex) {
        }


        // Text contains html body tag
        if (text != null && text != "") {
            String[] values;
            values = text.split(",");
            text = values[6].replaceAll("</body></html>", "");
            if (Build.VERSION.SDK_INT >= 24) {
                text = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString();
            } else {
                text = Html.fromHtml(text).toString();
            }

            final String d = text;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    song_title_view.setText(d);
                }
            });


        }

    }

}
