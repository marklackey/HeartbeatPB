package org.marklackey.heartbeatpb;

import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by marklackey on 5/1/16.
 */
public class HBUser {

    //user states
    final static int NEW = 0;
    final static int INITIATOR = 1;
    final static int INVITEE = 2;
    final static int CONNECTED_TO_PARTNER = 3;

    public int getUserState() {
        return userState;
    }

    public void setUserState(int userState) {
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

    private int userState = NEW;
    private String userEmailAddress;
    private String partnerEmailAddress;
    private String sharedChannelName;

    public String getPartnerOnlyChannelName() {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(partnerEmailAddress.getBytes());
            String encodedChannelName = Base64.encodeToString( messageDigest.digest(),Base64.URL_SAFE);
            if(encodedChannelName.length()>92){
                encodedChannelName = encodedChannelName.substring(0,91);
            }
            return encodedChannelName;
        } catch (NoSuchAlgorithmException e) {
            Log.d("X", e.getMessage());
            return null;
        }
    }

    private String partnerOnlyChannelName;

}
