package org.marklackey.heartbeatpb;

/**
 * Created by marklackey on 5/9/16.
 */
public class WritableMessage {
private String messageText;
    private String senderId;
    public WritableMessage(String senderId, String messageText){
        this.senderId = senderId;
        this.messageText = messageText;
    }
    public String getTextBody(){
        return messageText;
    }
}
