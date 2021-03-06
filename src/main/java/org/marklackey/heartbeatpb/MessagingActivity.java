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
import org.marklackey.heartbeatpb.util.CommonUtils;
import org.marklackey.heartbeatpb.util.NetworkAccesss;

import java.util.HashMap;
import java.util.Map;

public class MessagingActivity extends ActionBarActivity {

    private static final int REQUEST_CODE_LOGIN = 2;
    private static final int REQUEST_CODE_INVITE = 3;
    private static final int REQUEST_CODE_ACCEPTREJECT = 4;
    private static final int REQUEST_CODE_WAITING = 5;
    public static final int REQUEST_CODE_NO_INTERNET = 6;

    private static final String TAG = "MessagingActivity";
    public static final String SENDER_ID = "senderId";
    public static final String HEARTBEAT = "heartbeat";
    public static final String MESSAGE_TEXT = "messageText";
    public static final String ACCEPT = "acceptedBy";
    public static final String REJECT = "rejectedBy";
    public static final String INVITED = "invitedBy";
    public static final String RESPONSE = "response";
    public static final String START_OVER = "startOver";
    public static final String CLOSE_APP = "closeApp";

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
                new Pubnub("pub-c-384aeed2-e5cc-4799-9373-5425241c978f", "sub-c-08b59cf6-0e6d-11e6-a5b5-0619f8945a4f", true);
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
            }
        });

        //just like send button, but this is a special heartbeat messsage
        findViewById(R.id.loveButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendHeartbeat(OverlayActivity.HEART);
            }
        });
        //just like send button, but this is a special heartbeat messsage
        findViewById(R.id.energyButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendHeartbeat(OverlayActivity.ENERGY);
            }
        });
        //just like send button, but this is a special heartbeat messsage
        findViewById(R.id.peaceButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendHeartbeat(OverlayActivity.PEACE);
            }
        });
        //just like send button, but this is a special heartbeat messsage
        findViewById(R.id.spaceButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendHeartbeat(OverlayActivity.SPACE);
            }
        });
        //just like send button, but this is a special heartbeat messsage
        findViewById(R.id.healingButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendHeartbeat(OverlayActivity.HEALING);
            }
        });


    }

    void sendHeartbeat(String effectChoice) {
        Map<String, String> heartbeat = new HashMap<String, String>();
        heartbeat.put(HEARTBEAT, effectChoice);
        heartbeat.put(SENDER_ID, getUser().getUserEmailAddress());
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

    //when messaging activity is displayed, take the right action based on user state
    //and network access
    @Override
    protected void onResume() {
        super.onResume();
        if (!NetworkAccesss.haveNetworkAccess(getApplicationContext())) {
            Intent noInternetIntent = new Intent(this, NoInternetActivity.class);
            startActivityForResult(noInternetIntent, REQUEST_CODE_NO_INTERNET);
        } else
            init();

    }

    //unxubscribe, since
    @Override
    protected void onPause() {
        super.onPause();
        CommonUtils.unsubscribeAllChannels(app);
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
        String userStateSetting = settings.getString(HBUser.USER_STATE, null);

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
        Intent waitingIntent = new Intent(getApplicationContext(), WaitingActivity.class);
        startActivityForResult(waitingIntent, REQUEST_CODE_WAITING);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            //return from LoginActivity
            if (requestCode == REQUEST_CODE_LOGIN && data != null) {
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
            }
            //invited someone, wait for them to accept or reject us
            else if (requestCode == REQUEST_CODE_INVITE && data != null) {
                if (data.hasExtra(RESPONSE) && data.getStringExtra(RESPONSE).equals(START_OVER)) {
                    app.setUser(new HBUser());
                    getUser().persistUser(MessagingActivity.this);
                } else {
                    app.getUser().setPartnerEmailAddress(data.getStringExtra(HBUser.PARTNER_EMAIL_ADDRESS));
                    getUser().setUserState(HBUser.UserState.INITIATOR);
                    getUser().persistUser(this);
                    Log.d("X", "sent invite to partner email adddress: " + getUser().getPartnerEmailAddress());
                }
            //we were invited and responded to the invite
            } else if (requestCode == REQUEST_CODE_ACCEPTREJECT) {
                if (data != null && data.hasExtra(RESPONSE)) {
                    // If accepts:
                    if (data.getStringExtra(RESPONSE).equals(ACCEPT)) {
                        getUser().setSharedChannelName(CommonUtils.createPubNubSafeBase64Hash(getUser().getUserEmailAddress() + getUser().getPartnerEmailAddress()));
                        getUser().setUserState(HBUser.UserState.CONNECTED_TO_PARTNER);
                        getUser().persistUser(this);
                    } else if (data.getStringExtra(RESPONSE).equals(REJECT)) {
                        app.setUser(new HBUser());
                        getUser().persistUser(MessagingActivity.this);
                    }
                }
            } else if (requestCode == REQUEST_CODE_WAITING) {
                if (data != null && data.hasExtra(RESPONSE)) {
                    if (data.getStringExtra(RESPONSE).equals(ACCEPT)) {
                        getUser().setSharedChannelName(CommonUtils.createPubNubSafeBase64Hash(getUser().getPartnerEmailAddress() + getUser().getUserEmailAddress()));
                        getUser().setUserState(HBUser.UserState.CONNECTED_TO_PARTNER);
                        getUser().persistUser(MessagingActivity.this);
                        //rejected invite, so start over
                    } else if (data.getStringExtra(RESPONSE).equals(START_OVER)) {
                        app.setUser(new HBUser());
                        getUser().persistUser(MessagingActivity.this);
                    }
                }
            } else if (requestCode == REQUEST_CODE_NO_INTERNET) {
                if (data != null && data.hasExtra(RESPONSE) && data.getStringExtra(RESPONSE).equals(CLOSE_APP)) {
                    finish();
                }
            } else {
                Log.d("X", "Result OK, Unknown Request Code:" + requestCode);
            }
        } else {
            finish();
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
                                if (senderId != null && singleMessage.has(MESSAGE_TEXT)) {
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
                                if (singleMessage.has(HEARTBEAT)) {
                                    Intent overlay = new Intent(getApplicationContext(), OverlayActivity.class);
                                    overlay.putExtra(OverlayActivity.EFFECT, singleMessage.getString(HEARTBEAT));
                                    startActivity(overlay);
                                } else {
                                    String senderId = singleMessage.getString(SENDER_ID);
                                    //only regular messages for now. heartbeats soon.
                                    if (senderId.equals(getUser().getPartnerEmailAddress())) {
                                        messageAdapter.addMessage(new WritableMessage(senderId, singleMessage.getString(MESSAGE_TEXT)), MessageAdapter.DIRECTION_OUTGOING);
                                    } else if (senderId.equals(getUser().getUserEmailAddress())) {
                                        messageAdapter.addMessage(new WritableMessage(senderId, singleMessage.getString(MESSAGE_TEXT)), MessageAdapter.DIRECTION_INCOMING);
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
