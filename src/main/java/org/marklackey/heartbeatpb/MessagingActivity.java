package org.marklackey.heartbeatpb;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MessagingActivity extends ActionBarActivity {

    private static final int REQUEST_CODE_LOGIN = 2;
    private static final int REQUEST_CODE_INVITE = 3;
    private static final int REQUEST_CODE_ACCEPTREJECT = 4;
    private static final String TAG = "MessagingActivity";
    public static final String SENDER_ID = "senderId";
    public static final String HEARTBEAT = "heartbeat";
    public static final String MESSAGE_TEXT = "messageText";
    public static final String ACCEPT = "acceptedBy";
    public static final String REJECT = "rejectedBy";
    public static final String INVITED = "invitedBy";
    public static final String RESPONSE = "response";

    protected MessageAdapter messageAdapter;


    HBApplication app;
    ListView messagesList;

    HBUser getUser() {
        return ((HBApplication) getApplication()).getUser();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heartbeat_messaging);

        this.messagesList = (ListView) findViewById(R.id.listMessages);
        messageAdapter = new MessageAdapter(this);
        messagesList.setAdapter(messageAdapter);

        Pubnub pubnub =
                new Pubnub("pub-c-384aeed2-e5cc-4799-9373-5425241c978f", "sub-c-08b59cf6-0e6d-11e6-a5b5-0619f8945a4f");
        app = ((HBApplication) getApplication());
        app.setPubnub(pubnub);

        final EditText messageBodyField = (EditText) findViewById(R.id.messageBodyField);
        //listen for a click on the send button. Send message from message text box
        findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //create the JSON object to send
                Map<String, String> message = new HashMap<String, String>();
                String messageText = messageBodyField.getText().toString();
                if (messageText.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter a message", Toast.LENGTH_LONG).show();
                    return;
                }
                message.put("messageText", messageText);
                message.put("senderId", getUser().getUserEmailAddress());
                app.getPubNub().publish(getUser().getSharedChannelName(), new JSONObject(message), new Callback() {
                    @Override
                    public void successCallback(String channel, Object message) {
                        super.successCallback(channel, message);
                    }

                    @Override
                    public void errorCallback(String channel, PubnubError error) {
                        Log.d("X", error.getErrorString());
                    }
                });
                messageBodyField.setText("");
                //((EditText) findViewById(R.id.messageBodyField)).setText("");
            }
        });

        //just like send button, but this is a special heartbeat messsage
        findViewById(R.id.heartbeatButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, String> heartbeat = new HashMap<String, String>();
                heartbeat.put("heartbeat", getUser().getUserEmailAddress());
                heartbeat.put("senderId", getUser().getUserEmailAddress());
                app.getPubNub().publish(getUser().getSharedChannelName(), new JSONObject(heartbeat), new Callback() {
                    @Override
                    public void successCallback(String channel, Object message) {
                        super.successCallback(channel, message);
                    }

                    @Override
                    public void errorCallback(String channel, PubnubError error) {
                        super.errorCallback(channel, error);
                    }
                });
            }
        });


    }

    //when messaging activity is displayed, take the right action based on user state
    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    void init() {
        initializeUser();
        switch (app.getUser().getUserState()) {
            case CONNECTED_TO_PARTNER: {
                startChatting();
                break;
            }
            case INITIATOR: {
                waitForResponse();
                break;
            }
            case INVITEE: {
                askForResponse();
                break;
            }
            case REGISTERED: {
                //show invite activity
                Intent inviteIntent = new Intent(getApplicationContext(), InviteActivity.class);
                startActivityForResult(inviteIntent, REQUEST_CODE_INVITE);
                break;
            }
            case NEW: {
                //show Login
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivityForResult(intent, REQUEST_CODE_LOGIN);
                break;
            }
        }
    }

    //create an HBUser object that reflects current status
    private void initializeUser() {
        //Initiate User
        HBUser user = new HBUser();
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        String userStateSetting = settings.getString("userState", null);

        if (userStateSetting != null) {
            user.setUserState(HBUser.UserState.valueOf(userStateSetting));
            if (user.getUserState() != HBUser.UserState.NEW) {
                user.setUserEmailAddress(settings.getString(HBUser.USER_EMAIL_ADDRESS, null));
                user.setPartnerEmailAddress(settings.getString(HBUser.PARTNER_EMAIL_ADDRESS, null));
                user.setSharedChannelName(settings.getString(HBUser.SHARED_CHANNEL_NAME, null));
            }
        } else {
            user.setUserState(HBUser.UserState.NEW);
            user.persistUser(this);
        }
        app.setUser(user);
    }

    //show dialog here to accept or go to activity to  invite someone else
    private void askForResponse() {
        Intent acceptOrRejectIntent = new Intent(getApplicationContext(), AcceptOrRejectActivity.class);
        startActivityForResult(acceptOrRejectIntent, REQUEST_CODE_ACCEPTREJECT);
    }

    //show waiting screen for INITIATOR, wait for response message
    private void waitForResponse() {
        Log.d("X", "Waiting for response.");
        try {
            app.getPubNub().subscribe(getUser().getPartnerOnlyChannelName(), new Callback() {
                @Override
                public void successCallback(String channel, Object message) {
                    //examine messages to see if it's a accept or reject message
                    try {
                        JSONObject response = (JSONObject) message;
                        if (response.has(ACCEPT)) {
                            String partnerEmailAddress = response.getString(ACCEPT);
                            //Toast.makeText(getApplicationContext(), partnerEmailAddress + " accepted.", Toast.LENGTH_LONG).show();
                            getUser().setSharedChannelName(CommonUtils.createURLSafeBase64Hash(getUser().getPartnerEmailAddress() + getUser().getUserEmailAddress()));
                            getUser().setUserState(HBUser.UserState.CONNECTED_TO_PARTNER);
                            getUser().persistUser(MessagingActivity.this);
                            startChatting();
                        } else if (response.has(REJECT)) {
                            app.setUser(new HBUser());
                            getUser().persistUser(MessagingActivity.this);
                            init();
                        } else
                            Log.d("X", message.toString());
                    } catch (JSONException e) {
                        Log.d("X", e.getLocalizedMessage());
                    }
                }

                @Override
                public void errorCallback(String channel, PubnubError error) {
                    Log.d("X", error.getErrorString());
                }
            });
        } catch (PubnubException e) {
            Log.d("X", e.getLocalizedMessage());

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //return from LoginActivity
        if (requestCode == REQUEST_CODE_LOGIN && resultCode == RESULT_OK) {

            getUser().setUserEmailAddress(data.getStringExtra(HBUser.USER_EMAIL_ADDRESS));
            getUser().persistUser(this);
            Log.d("X", "logged in: " + getUser().getUserEmailAddress());
            //if we weren't invited, then give option to invite someone else
            String partnerEmailAddress = data.getStringExtra(HBUser.PARTNER_EMAIL_ADDRESS);
            if (partnerEmailAddress == null || partnerEmailAddress.isEmpty()) {
                getUser().setUserState(HBUser.UserState.REGISTERED);
                getUser().persistUser(this);
            } else {
                //we found that we had been invited, ask to accept or reject
                Log.d("X", "invited by partner email adddress: " + partnerEmailAddress);
                getUser().setPartnerEmailAddress(partnerEmailAddress);
                getUser().setUserState(HBUser.UserState.INVITEE);
                getUser().persistUser(this);
            }
            //invited someone, wait for them to accept or reject us
        } else if (requestCode == REQUEST_CODE_INVITE && resultCode == RESULT_OK) {
            Log.d("X", "sent invite to partner email adddress: " + getUser().getPartnerEmailAddress());
            app.getUser().setPartnerEmailAddress(data.getStringExtra(HBUser.PARTNER_EMAIL_ADDRESS));
            getUser().setUserState(HBUser.UserState.INITIATOR);
            getUser().persistUser(this);
            //we were invited and responded to the invite
        } else if (requestCode == REQUEST_CODE_ACCEPTREJECT && resultCode == RESULT_OK) {
            // If accepts:
            if (data.getStringExtra("response").equals(ACCEPT)) {
                getUser().setSharedChannelName(CommonUtils.createURLSafeBase64Hash(getUser().getUserEmailAddress() + getUser().getPartnerEmailAddress()));
                getUser().setUserState(HBUser.UserState.CONNECTED_TO_PARTNER);
                getUser().persistUser(this);
                //rejected invite, so start over
            } else if (data.getStringExtra("response").equals(REJECT)) {
                app.setUser(new HBUser());
                getUser().persistUser(this);
            }
        } else {
            Log.d("X", "Request:" + requestCode + "Result:" + resultCode);
        }
    }

    public void onIncomingMessage(WritableMessage w, int direction) {
        messageAdapter.addMessage(w, direction);

    }

    void startChatting() {

        //populate message history
        app.getPubNub().history(getUser().getSharedChannelName(), 100, new Callback() {
            @Override
            public void successCallback(String channel, final Object message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            messageAdapter.resetMessageList();
                            JSONArray messages = (JSONArray) ((JSONArray) message).get(0);
                            for (int i = 0; i < messages.length(); i++) {
                                JSONObject singleMessage = messages.getJSONObject(i);
                                String senderId = singleMessage.getString(SENDER_ID);
                                if (senderId != null) {
                                    //for now, only add regular text messages to the message history
                                    if (senderId.equals(getUser().getPartnerEmailAddress())) {
                                        onIncomingMessage(new WritableMessage(senderId, singleMessage.getString(MESSAGE_TEXT)), MessageAdapter.DIRECTION_OUTGOING);
                                    } else if (senderId.equals(getUser().getUserEmailAddress())) {
                                        onIncomingMessage(new WritableMessage(senderId, singleMessage.getString(MESSAGE_TEXT)), MessageAdapter.DIRECTION_INCOMING);
                                    }
                                }
                            }

                        } catch (JSONException e) {
                            Log.d("X", e.getLocalizedMessage());
                        }
                    }
                });
            }

            @Override
            public void errorCallback(String channel, PubnubError error) {
                Log.d("X", error.getErrorString());
            }
        });

        //wait for new messages
        try {
            app.getPubNub().subscribe(getUser().getSharedChannelName(), new Callback() {
                @Override
                public void successCallback(String channel, final Object message) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                JSONObject singleMessage = (JSONObject) message;
                                String senderId = singleMessage.getString(SENDER_ID);
                                //only regular messages for now. heartbeats soon.
                                if (senderId.equals(getUser().getPartnerEmailAddress())) {
                                    messageAdapter.addMessage(new WritableMessage(senderId, singleMessage.getString(MESSAGE_TEXT)), MessageAdapter.DIRECTION_OUTGOING);
                                } else if (senderId.equals(getUser().getUserEmailAddress())) {
                                    messageAdapter.addMessage(new WritableMessage(senderId, singleMessage.getString(MESSAGE_TEXT)), MessageAdapter.DIRECTION_INCOMING);
                                }

                            } catch (JSONException e) {
                                Log.d("X", e.getLocalizedMessage());
                            }

                        }
                    });

                }

                @Override
                public void errorCallback(String channel, PubnubError error) {
                    Log.d("X", error.getErrorString());
                }

                @Override
                public void connectCallback(String channel, Object message) {
                    Log.d("X", "Connected on channel: " + channel);
                }
            });
        } catch (PubnubException e) {
            Log.d("X", e.getErrorResponse());
        }

    }

}
