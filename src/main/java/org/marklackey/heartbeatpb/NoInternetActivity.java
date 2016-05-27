package org.marklackey.heartbeatpb;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.pubnub.api.Callback;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.marklackey.heartbeatpb.util.NetworkAccesss;

/**
 * Created by marklackey on 5/11/16.
 */
public class NoInternetActivity extends Activity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internet);

        Button startOverButton = (Button) findViewById(R.id.close_app_button);
        startOverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeApp();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (NetworkAccesss.haveNetworkAccess(getApplicationContext())) {
            setResult(RESULT_OK, getIntent());
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        closeApp();
        super.onBackPressed();
    }

    private void closeApp() {
        getIntent().putExtra(MessagingActivity.RESPONSE, MessagingActivity.CLOSE_APP);
        setResult(RESULT_OK, getIntent());
        finish();
    }
}