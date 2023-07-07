package com.dds.temple0.socket;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class MyWebSocket extends WebSocketClient {
    private IEvent iEvent;

    public MyWebSocket(URI serverUri, IEvent iEvent) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {

    }

    @Override
    public void onMessage(String message) {

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {

    }

    @Override
    public void onError(Exception ex) {

    }


    public interface IEvent {

        void onOpen();

        void logout(String str);

        void onMessage(String msg);
    }
}
