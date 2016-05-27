package org.marklackey.heartbeatpb.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import org.marklackey.heartbeatpb.HBApplication;

/**
 * Created by vijay on 5/16/15.
 * Modified by mlackey 5/16.
 */
public class CommonUtils {

    private static final String TAG = "CommonUtils";

    private CommonUtils() {
    }

    //Leaving this here for now, as I may go back to a form structure.
    //Also may want to save this method for other projects
    public static String loadJSONFromAsset(Context context, String fileName) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            Log.d(TAG, "Exception Occurred : " + ex.getMessage());
            return null;
        }
        return json;

    }

    //This returns a legal PubNub channel name, which is very similar to a legal URL
    //I think it's no coincidence, as pubnub framework seems to put channel names in URLs
    public static String createPubNubSafeBase64Hash(String input)
    {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(input.getBytes());
            String encodedChannelName = Base64.encodeToString( messageDigest.digest(),Base64.URL_SAFE);

            //pubnub channel names cannot be more than 92 characters
            if(encodedChannelName.length()>92){
                encodedChannelName = encodedChannelName.substring(0,91);
            }
            //pubnub channel names cannot have whitespace characters
            return encodedChannelName.trim();
        } catch (Exception e) {
            Log.d("X", "Error in encoding: " + e.getMessage());
            return null;
        }
    }

    //PubNub util method to unsubscribe all channel when necessary
    //Might be better for clarity to do them one at a time in code. We consider refactor.
    public static void unsubscribeAllChannels(HBApplication app) {
        if (app != null && app.getPubNub() != null) {
            String[] subscribedChannels = app.getPubNub().getSubscribedChannelsArray();
            for (int i = 0; i < subscribedChannels.length; i++)
                app.getPubNub().unsubscribe(subscribedChannels[i]);
        }
    }
}
