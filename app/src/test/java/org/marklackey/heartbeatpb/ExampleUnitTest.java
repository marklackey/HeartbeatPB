package org.marklackey.heartbeatpb;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import org.json.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

//    @Before
//    public void setUpStreams() {
//        System.setOut(new PrintStream(outContent));
//        System.setErr(new PrintStream(errContent));
//    }
//    @Test
//    public void out() {
//        System.out.print("hello");
//        assertEquals("hello", outContent.toString());
//    }
//
//    @Test
//    public void err() {
//        System.err.print("hello again");
//        assertEquals("hello again", errContent.toString());
//    }
    @Test
    public void can_send_message() throws Exception {
        final Pubnub pubnub = new Pubnub("pub-c-384aeed2-e5cc-4799-9373-5425241c978f", "sub-c-08b59cf6-0e6d-11e6-a5b5-0619f8945a4f");
        try {

            pubnub.subscribe("my_channel", new Callback() {
                        @Override
                        public void connectCallback(String channel, Object message) {
                            pubnub.publish("my_channel", "Hello from the PubNub Java SDK", new Callback() {
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
                            System.out.print(message.toString());
                        }

                        @Override
                        public void errorCallback(String channel, PubnubError error) {
                            System.out.println("SUBSCRIBE : ERROR on channel " + channel
                                    + " : " + error.toString());
                        }
                    }
            );
        }
        catch (PubnubException e) {
            System.out.println(e.toString());
        }
        Thread.sleep(5000);
        //System.out.print("Hello from the PubNub Java SDK");
        assertEquals("Hello from the PubNub Java SDK", outContent.toString());
    }
    @After
    public void cleanUpStreams() {
/*        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        System.setOut(null);
        System.setErr(null);
    }


}