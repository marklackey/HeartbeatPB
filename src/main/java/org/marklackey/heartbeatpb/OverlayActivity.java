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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Window window = getWindow();

        // Let touches go through to apps/activities underneath.
        //window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        // Now set up content view
        setContentView(R.layout.main);
//        if (getIntent().getBooleanExtra(HeartbeatMessagingActivity.PULSE_NOW, false))
//            pulse();
        pulse();
        //go to login
        /*startActivity(new Intent(getApplicationContext(),
                org.marklackey.heartbeat.LoginActivity.class));*/

//        myThread.start();
//
//        Thread myThread2 = new Thread(r2);
//        myThread2.start();

        //executor.execute(r2);
        //executor.execute(r1);
        //executor.execute(r2);
//        final EditText colorvalueField = (EditText) findViewById(R.id.colorvalue);
//        Button submitColor = (Button) findViewById(R.id.colorsubmit);
//        submitColor.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                findViewById(R.id.background)
//                        .setBackgroundColor(Color.parseColor(colorvalueField.getText().toString()));
//            }
//        });
//        submitColor.bringToFront();
//        colorvalueField.bringToFront();

//

    }

    public void pulse() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(r1);
    }

    Runnable r1 = new Runnable() {
        int DELAY = 40;
        long MAX = 0x88ff1100L;
        long MIN = 0x20ff1100L;
        long INTERVAL = 0x04000000;
        long i;

        public void run() {
            DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
            Date dateobj = new Date();
            Log.d("X", df.format(dateobj));
            for (int j = 0; j < 2; j++) {
                for (
                        i = MIN; i <= MAX; i += INTERVAL) {
                    try {
                        Thread.sleep(DELAY);
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
                for (i = MAX; i > MIN; i -= INTERVAL) {
                    try {
                        Thread.sleep(DELAY);
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
    };
}