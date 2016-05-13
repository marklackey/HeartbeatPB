package org.marklackey.heartbeatpb;

import android.app.Application;

import com.pubnub.api.Pubnub;


/**
 * Created by marklackey on 4/30/16.
 */
public class HBApplication extends Application {

    public static String PREFS_FILE = "HBPrefsFile";
    private Pubnub pubnub;

    public Pubnub getPubNub() {
        return pubnub;
    }

    public void setPubnub(Pubnub pubnub) {
        this.pubnub = pubnub;
    }
    //return pubnub object
//    public Pubnub getPubnub() {
//        return pubnub;
//    }
//    final Pubnub pubnub =
//            new Pubnub("pub-c-384aeed2-e5cc-4799-9373-5425241c978f","sub-c-08b59cf6-0e6d-11e6-a5b5-0619f8945a4f");
    //new Pubnub(getResources().getString(R.string.pub_key), getResources().getString(R.string.sub_key));

    public void setUser(HBUser user) {
        this.user = user;
    }

    HBUser user;

    public HBUser getUser() {
        return user;
    }



}
