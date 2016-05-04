package org.marklackey.heartbeatpb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;
import com.vijay.jsonwizard.activities.JsonFormActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class MessagingActivity extends ActionBarActivity {

    HBApplication app;
    private static final int REQUEST_CODE_GET_JSON = 1;
    private static final String TAG = "MessagingActivity";
    private static final String DATA_JSON_PATH = "data.json";
    private Pubnub pubnub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        pubnub =
                new Pubnub("pub-c-384aeed2-e5cc-4799-9373-5425241c978f", "sub-c-08b59cf6-0e6d-11e6-a5b5-0619f8945a4f");
        app = ((HBApplication) getApplication());

        Intent intent = new Intent(this, JsonFormActivity.class);
        String json = CommonUtils.loadJSONFromAsset(getApplicationContext(), DATA_JSON_PATH);
        intent.putExtra("json", json);
        startActivityForResult(intent, REQUEST_CODE_GET_JSON);

        //testing purposes


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {
            Log.d(TAG, data.getStringExtra("json"));
        }
        try {
            JSONObject jsonObject = new JSONObject(data.getStringExtra("json"));
            String userEmail = jsonObject.getJSONObject("step1").getJSONArray("fields").getJSONObject(0).getString("value");
            Log.i("X", userEmail);
            String partnerEmail = jsonObject.getJSONObject("step2").getJSONArray("fields").getJSONObject(0).getString("value");
            app.getUser().setPartnerEmailAddress("SETVSFSDFDSF");
            Log.i("X", partnerEmail);
            sendMessage(new JSONObject().put("text","test message"));
        } catch (JSONException e) {
            Log.d("ERR", e.getLocalizedMessage());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void sendMessage(final JSONObject hbMessage) {

        try {
            pubnub.subscribe(app.getUser().getPartnerOnlyChannelName(), new Callback() {
                        @Override
                        public void connectCallback(String channel, Object message) {
                            Log.i("X:", "connected to channel: " + channel);

                            pubnub.publish(app.getUser().getPartnerOnlyChannelName(), hbMessage, new Callback() {
                                @Override
                                public void successCallback(String channel, Object message) {
                                    Log.i("X", channel + " : PUBLISH : " + message.toString());
                                }

                                @Override
                                public void errorCallback(String channel, PubnubError error) {
                                    Log.d("X", "PUBLISH : ERROR on channel " + channel
                                            + " : " + error.toString());
                                }
                            });
                        }
                            @Override
                        public void disconnectCallback(String channel, Object message) {
                            System.out.println("SUBSCRIBE : DISCONNECT on channel:" + channel
                                    + " : " + message.getClass() + " : "
                                    + message.toString());
                        }

                        public void reconnectCallback(String channel, Object message) {
                            System.out.println("SUBSCRIBE : RECONNECT on channel:" + channel
                                    + " : " + message.getClass() + " : "
                                    + message.toString());
                        }

                        @Override
                        public void successCallback(String channel, Object message) {
                            Log.d("X", message.toString());

                            pubnub.history(app.getUser().getPartnerOnlyChannelName(), 100, true, new Callback() {
                                @Override
                                public void errorCallback(String channel, PubnubError error) {
                                    Log.d("X", "HISTORY : ERROR on channel " + channel
                                            + " : " + error.getErrorString());
                                }

                                @Override
                                public void successCallback(String channel, Object response) {
                                    Log.i("X", channel + " : " + response.toString() );
                                }
                            });
                        }

                        @Override
                        public void errorCallback(String channel, PubnubError error) {
                            System.out.println("SUBSCRIBE : ERROR on channel " + channel
                                    + " : " + error.toString());
                        }
                    }
            );

        } catch (PubnubException e) {
            System.out.println(e.toString());
        }

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_messaging, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
}
