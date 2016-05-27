package org.marklackey.heartbeatpb;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
public class WaitingActivity extends Activity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        Button startOverButton = (Button) findViewById(R.id.start_over_button);
        startOverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getIntent().putExtra(MessagingActivity.RESPONSE, MessagingActivity.START_OVER);
                setResult(RESULT_OK, getIntent());
                finish();
            }
        });
        Button startChattingButton = (Button) findViewById(R.id.start_chatting_button);
        startChattingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getIntent().putExtra(MessagingActivity.RESPONSE, MessagingActivity.ACCEPT);
                setResult(RESULT_OK, getIntent());
                finish();
            }
        });

        final HBApplication app = (HBApplication) getApplication();
        //try {
            app.getPubNub().history(app.getUser().getPartnerOnlyChannelName(), 2, new Callback() {
                @Override
                public void successCallback(String channel, Object message) {
                    boolean messageReceived = false;

                    try {
                        JSONArray messages = (JSONArray) ((JSONArray) message).get(0);
                        messageReceived = setResultFromMessage(messages.getJSONObject(0));
                        if (messages.length() > 0 && !messageReceived)
                            if (messages.length() > 1)
                                messageReceived = setResultFromMessage(messages.getJSONObject(1));

                    } catch (JSONException e) {
                        Log.d("X", e.getLocalizedMessage());
                    }
                    if(!messageReceived) {
                        try {
                            app.getPubNub().subscribe(app.getUser().getPartnerOnlyChannelName(), new Callback() {
                                @Override
                                public void successCallback(String channel, Object message) {
                                    //examine messages to see if it's a accept or reject message
                                    setResultFromMessage((JSONObject) message);
                                }

                                @Override
                                public void errorCallback(String channel, PubnubError error) {
                                    Log.d("X", error.getErrorString());
                                    setResult(RESULT_CANCELED, getIntent());
                                }

                                @Override
                                public void connectCallback(String channel, Object message) {
                                    Log.d("X", "connceted on: " + channel);
                                }
                            });
                        }
                        catch(PubnubException e){
                            Log.d("X",e.getLocalizedMessage());
                        }
                    }
                }

                @Override
                public void errorCallback(String channel, PubnubError error) {
                    Log.d("X", error.getErrorString());
                }
            });
            /*app.getPubNub().subscribe(app.getUser().getPartnerOnlyChannelName(), new Callback() {
                @Override
                public void successCallback(String channel, Object message) {
                    //examine messages to see if it's a accept or reject message
                    setResultFromMessage((JSONObject) message);
                }

                @Override
                public void errorCallback(String channel, PubnubError error) {
                    Log.d("X", error.getErrorString());
                    setResult(RESULT_CANCELED, getIntent());
                }

                @Override
                public void connectCallback(String channel, Object message) {
                    Log.d("X","connceted on: " + channel);
                }
            });*/
/*        } catch (PubnubException e) {
            Log.d("X", e.getLocalizedMessage());
            setResult(RESULT_CANCELED, getIntent());
        }*/

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!NetworkAccesss.haveNetworkAccess(getApplicationContext())) {
            setResult(RESULT_OK);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED, getIntent());
        super.onBackPressed();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        HBApplication app = (HBApplication) getApplication();
        if (app != null && app.getPubNub() != null &&
                app.getUser() != null &&
                app.getUser().getPartnerOnlyChannelName() != null)
            app.getPubNub().unsubscribe(app.getUser().getPartnerOnlyChannelName());
    }

    boolean setResultFromMessage(JSONObject response) {

        boolean recognizedResponse = false;
        if (response.has(MessagingActivity.ACCEPT)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.start_over_button).setVisibility(View.GONE);
                    findViewById(R.id.start_chatting_button).setVisibility(View.VISIBLE);
                    ((TextView) findViewById(R.id.waitingMessage)).setText("Invitation Accepted.");
                    ((TextView) findViewById(R.id.startOverMessage)).setText("Click the button to start chatting:");
                    setResult(RESULT_OK, getIntent());

                }
            });
            recognizedResponse = true;

        } else if (response.has(MessagingActivity.REJECT)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((TextView) findViewById(R.id.waitingMessage)).setText("Invitation Declined.");
                    ((TextView) findViewById(R.id.startOverMessage)).setText("Click the button to start over:");
                    setResult(RESULT_OK, getIntent());
                }
            });
            recognizedResponse = true;
        }

        return recognizedResponse;
    }
}