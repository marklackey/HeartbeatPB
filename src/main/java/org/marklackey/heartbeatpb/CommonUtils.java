package org.marklackey.heartbeatpb;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

/**
 * Created by vijay on 5/16/15.
 */
public class CommonUtils {

    private static final String TAG = "CommonUtils";

    private CommonUtils() {
    }

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

    public static String createURLSafeBase64Hash(String input)
    {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(input.getBytes());
            String encodedChannelName = Base64.encodeToString( messageDigest.digest(),Base64.URL_SAFE);
            if(encodedChannelName.length()>92){
                encodedChannelName = encodedChannelName.substring(0,91);
            }
            return encodedChannelName.trim();
        } catch (Exception e) {
            Log.d("X", "Error in encoding: " + e.getMessage());
            return null;
        }
    }
}
