package com.dds.java;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.dds.webrtc.R;
import com.dds.webrtclib.ws.JavaWebSocket;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public class JavaActivity extends AppCompatActivity {
    private static WebSocketClient mWebSocketClient;
    private final static String TAG = "JavaActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_java);
    }


    /*----------------------------java版本服务器测试--------------------------------------------*/

    // 测试连接webSocket
    public void connect(View view) {

    }

    // test wss
    public static void testWs(String wss) {
        URI uri;
        try {
            uri = new URI(wss);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshake) {
                Log.e(TAG, "onOpen:");
                mWebSocketClient.send("");
            }

            @Override
            public void onMessage(String message) {
                Log.e(TAG, "onMessage:" + message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.e(TAG, "onClose:" + reason);
            }

            @Override
            public void onError(Exception ex) {
                Log.e(TAG, "onError:");
                Log.e(TAG, ex.toString());
            }
        };

        if (wss.startsWith("wss")) {
            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                if (sslContext != null) {
                    sslContext.init(null, new TrustManager[]{new JavaWebSocket.TrustManagerTest()}, new SecureRandom());
                }

                SSLSocketFactory factory = null;
                if (sslContext != null) {
                    factory = sslContext.getSocketFactory();
                }

                if (factory != null) {
                    mWebSocketClient.setSocket(factory.createSocket());
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mWebSocketClient.connect();
    }

}
