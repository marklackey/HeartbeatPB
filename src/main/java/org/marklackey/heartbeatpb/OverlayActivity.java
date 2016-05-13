package org.marklackey.heartbeatpb;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by marklackey on 4/21/16.
 */
public class OverlayActivity extends Activity {

    int DELAY = 40;
    long MAX = 0x88ff1100L;
    long MIN = 0x20ff1100L;
    long INTERVAL = 0x04000000;
    long ALPHA_PART_MAX = 0x88000000L;
    long ALPHA_PART_MIN = 0x20000000L;

    public final static String EFFECT = "effect";
    public final static String HEART = "heart";
    public final static String HEALING = "healing";
    public final static String PEACE = "peace";
    public final static String ENERGY = "energy";
    public final static String SPACE = "space";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.overlay);
        String effectType = getIntent().getStringExtra(EFFECT);
        switch (effectType) {
            case HEART:
                this.r1 = new BackgroundChanger(DELAY, ALPHA_PART_MAX+0xB83343L, MIN+0xB83343L, INTERVAL);
                pulse();
                break;
            case HEALING:
                this.r1 = new BackgroundChanger(DELAY, ALPHA_PART_MAX+0x90c3d4L, MIN+0x90c3d4L, INTERVAL);
                pulse();
                break;
            case PEACE:
                this.r1 = new BackgroundChanger(DELAY, ALPHA_PART_MAX+0xC9A918L, MIN+0xC9A918L, INTERVAL);
                pulse();
                break;
            case ENERGY:
                this.r1 = new BackgroundChanger(DELAY, ALPHA_PART_MAX+0x45DE40, MIN+0x45DE40, INTERVAL);
                pulse();
                break;
            case SPACE:
                this.r1 = new BackgroundChanger(DELAY, ALPHA_PART_MAX+0xeeeeeeL, MIN+0xeeeeeeL, INTERVAL);
                pulse();
                break;
        }

    }


    Runnable r1;

    public void pulse() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(r1);
    }

    class BackgroundChanger implements Runnable {

        int delay;
        long maxColorValue;
        long minColorValue;
        long colorInterval;

        public BackgroundChanger(int delay, long maxColorValue, long minColorValue, long colorInterval) {
            this.delay = delay;
            this.maxColorValue = maxColorValue;
            this.minColorValue = minColorValue;
            this.colorInterval = colorInterval;
        }

        //for loop 'i' -- easier to have here, since otherwise inner class complains below
        long i;

        public void run() {
            DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
            Date dateobj = new Date();
            Log.d("X", df.format(dateobj));
            for (int j = 0; j < 2; j++) {
                for (i = minColorValue; i <= maxColorValue; i += colorInterval) {
                    try {
                        Thread.sleep(delay);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                if (i > 0)
                                    findViewById(R.id.background)
                                            .setBackgroundColor((int) i);
                            }
                        });
                    } catch (InterruptedException e) {
                        Log.d("x", e.getMessage());
                    }
                }
                for (i = maxColorValue; i > minColorValue; i -= colorInterval) {
                    try {
                        Thread.sleep(delay);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                //int backgroundColor = 0x33000000 + i;
                                //Log.d("X", String.valueOf(i));
                                if (i > 0)
                                    findViewById(R.id.background)
                                            .setBackgroundColor((int) i);
                            }
                        });
                    } catch (InterruptedException e) {
                        Log.d("x", e.getMessage());
                    }
                }

            }
            dateobj = new Date();
            Log.d("X", df.format(dateobj));
            finish();
        }
    }

    ;
}