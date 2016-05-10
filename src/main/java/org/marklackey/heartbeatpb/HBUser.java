package org.marklackey.heartbeatpb;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by marklackey on 5/1/16.
 */
public class HBUser {

    //user states
    public enum UserState {
        NEW, INITIATOR, INVITEE, CONNECTED_TO_PARTNER, REGISTERED
    }

    private UserState userState;
    private String userEmailAddress;
    private String partnerEmailAddress;
    private String sharedChannelName;

    public static final String USER_EMAIL_ADDRESS = "userEmailAddress";
    public static final String PARTNER_EMAIL_ADDRESS = "partnerEmailAddress";
    public static final String SHARED_CHANNEL_NAME = "sharedChannelName";
    public static final String USER_STATE = "userState";

    public UserState getUserState() {
        return userState;
    }

    public void setUserState(UserState userState) {
        this.userState = userState;
    }

    public String getUserEmailAddress() {
        return userEmailAddress;
    }

    public void setUserEmailAddress(String userEmailAddress) {
        this.userEmailAddress = userEmailAddress;
    }

    public String getPartnerEmailAddress() {
        return partnerEmailAddress;
    }

    public void setPartnerEmailAddress(String partnerEmailAddress) {
        this.partnerEmailAddress = partnerEmailAddress;
    }

    public String getSharedChannelName() {
        return sharedChannelName;
    }

    public void setSharedChannelName(String sharedChannelName) {
        this.sharedChannelName = sharedChannelName;
    }

    public String getPartnerOnlyChannelName() {
        return CommonUtils.createURLSafeBase64Hash(partnerEmailAddress);
    }

    public void persistUser(Activity activity) {
        SharedPreferences.Editor settingsEditor = activity.getPreferences(Context.MODE_PRIVATE).edit();
        settingsEditor.putString(PARTNER_EMAIL_ADDRESS, partnerEmailAddress);
        settingsEditor.putString(USER_STATE, (userState == null) ? null : userState.name());
        settingsEditor.putString(USER_EMAIL_ADDRESS, userEmailAddress);
        settingsEditor.putString(SHARED_CHANNEL_NAME, sharedChannelName);
        settingsEditor.commit();

    }

}
