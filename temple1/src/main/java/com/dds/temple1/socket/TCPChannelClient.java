package com.dds.temple1.socket;

import android.util.Log;

import androidx.annotation.Nullable;

import org.webrtc.ThreadUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;


public class TCPChannelClient {
    private static final String TAG = "TCPChannelClient";

    private final ExecutorService executor;
    private final ThreadUtils.ThreadChecker executorThreadCheck;
    private final TCPChannelEvents eventListener;
    private TCPSocket socket;


    public interface TCPChannelEvents {
        void onTCPConnected(boolean server);

        void onTCPMessage(String message);

        void onTCPError(String description);

        void onTCPClose();
    }

    /**
     * Initializes the TCPChannelClient. If IP is a local IP address, starts a listening server on
     * that IP. If not, instead connects to the IP.
     *
     * @param eventListener Listener that will receive events from the client.
     * @param ip            IP address to listen on or connect to.
     * @param port          Port to listen on or connect to.
     */
    public TCPChannelClient(ExecutorService executor, TCPChannelEvents eventListener, String ip, int port) {
        this.executor = executor;
        executorThreadCheck = new ThreadUtils.ThreadChecker();
        executorThreadCheck.detachThread();
        this.eventListener = eventListener;

        InetAddress address;
        try {
            address = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            reportError("Invalid IP address.");
            return;
        }

        if (address.isAnyLocalAddress()) {
            socket = new TCPSocketServer(address, port);
        } else {
            socket = new TCPSocketClient(address, port);
        }

        socket.start();
    }

    /**
     * Disconnects the client if not already disconnected. This will fire the onTCPClose event.
     */
    public void disconnect() {
        executorThreadCheck.checkIsOnValidThread();

        socket.disconnect();
    }

    /**
     * Sends a message on the socket.
     *
     * @param message Message to be sent.
     */
    public void send(String message) {
        executorThreadCheck.checkIsOnValidThread();

        socket.send(message);
    }


    private void reportError(final String message) {
        Log.e(TAG, "TCP Error: " + message);
        executor.execute(() -> eventListener.onTCPError(message));
    }

    private abstract class TCPSocket extends Thread {
        // Lock for editing out and rawSocket
        protected final Object rawSocketLock;
        @Nullable
        private PrintWriter out;
        @Nullable
        private Socket rawSocket;

        @Nullable
        public abstract Socket connect();

        public abstract boolean isServer();

        TCPSocket() {
            rawSocketLock = new Object();
        }

        @Override
        public void run() {
            Log.d(TAG, "Listening thread started...");

            // Receive connection to temporary variable first, so we don't block.
            Socket tempSocket = connect();
            BufferedReader in;

            Log.d(TAG, "TCP connection established.");

            synchronized (rawSocketLock) {
                if (rawSocket != null) {
                    Log.e(TAG, "Socket already existed and will be replaced.");
                }

                rawSocket = tempSocket;

                // Connecting failed, error has already been reported, just exit.
                if (rawSocket == null) {
                    return;
                }

                try {
                    out = new PrintWriter(
                            new OutputStreamWriter(rawSocket.getOutputStream(), StandardCharsets.UTF_8), true);
                    in = new BufferedReader(
                            new InputStreamReader(rawSocket.getInputStream(), StandardCharsets.UTF_8));
                } catch (IOException e) {
                    reportError("Failed to open IO on rawSocket: " + e.getMessage());
                    return;
                }
            }

            Log.v(TAG, "Execute onTCPConnected");
            executor.execute(() -> {
                Log.v(TAG, "Run onTCPConnected");
                eventListener.onTCPConnected(isServer());
            });

            while (true) {
                final String message;
                try {
                    message = in.readLine();
                } catch (IOException e) {
                    synchronized (rawSocketLock) {
                        // If socket was closed, this is expected.
                        if (rawSocket == null) {
                            break;
                        }
                    }

                    reportError("Failed to read from rawSocket: " + e.getMessage());
                    break;
                }

                // No data received, rawSocket probably closed.
                if (message == null) {
                    break;
                }

                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        Log.v(TAG, "Receive: " + message);
                        eventListener.onTCPMessage(message);
                    }
                });
            }

            Log.d(TAG, "Receiving thread exiting...");

            // Close the rawSocket if it is still open.
            disconnect();
        }

        public void disconnect() {
            try {
                synchronized (rawSocketLock) {
                    if (rawSocket != null) {
                        rawSocket.close();
                        rawSocket = null;
                        out = null;

                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                eventListener.onTCPClose();
                            }
                        });
                    }
                }
            } catch (IOException e) {
                reportError("Failed to close rawSocket: " + e.getMessage());
            }
        }

        public void send(String message) {
            Log.v(TAG, "Send: " + message);

            synchronized (rawSocketLock) {
                if (out == null) {
                    reportError("Sending data on closed socket.");
                    return;
                }

                out.write(message + "\n");
                out.flush();
            }
        }
    }

    private class TCPSocketServer extends TCPSocket {
        // Server socket is also guarded by rawSocketLock.
        @Nullable
        private ServerSocket serverSocket;

        final private InetAddress address;
        final private int port;

        public TCPSocketServer(InetAddress address, int port) {
            this.address = address;
            this.port = port;
        }

        /**
         * Opens a listening socket and waits for a connection.
         */
        @Nullable
        @Override
        public Socket connect() {
            Log.d(TAG, "Listening on [" + address.getHostAddress() + "]:" + Integer.toString(port));

            final ServerSocket tempSocket;
            try {
                tempSocket = new ServerSocket(port, 0, address);
            } catch (IOException e) {
                reportError("Failed to create server socket: " + e.getMessage());
                return null;
            }

            synchronized (rawSocketLock) {
                if (serverSocket != null) {
                    Log.e(TAG, "Server rawSocket was already listening and new will be opened.");
                }

                serverSocket = tempSocket;
            }

            try {
                return tempSocket.accept();
            } catch (IOException e) {
                reportError("Failed to receive connection: " + e.getMessage());
                return null;
            }
        }

        /**
         * Closes the listening socket and calls super.
         */
        @Override
        public void disconnect() {
            try {
                synchronized (rawSocketLock) {
                    if (serverSocket != null) {
                        serverSocket.close();
                        serverSocket = null;
                    }
                }
            } catch (IOException e) {
                reportError("Failed to close server socket: " + e.getMessage());
            }

            super.disconnect();
        }

        @Override
        public boolean isServer() {
            return true;
        }
    }

    private class TCPSocketClient extends TCPSocket {
        final private InetAddress address;
        final private int port;

        public TCPSocketClient(InetAddress address, int port) {
            this.address = address;
            this.port = port;
        }

        /**
         * Connects to the peer.
         */
        @Nullable
        @Override
        public Socket connect() {
            Log.d(TAG, "Connecting to [" + address.getHostAddress() + "]:" + Integer.toString(port));

            try {
                return new Socket(address, port);
            } catch (IOException e) {
                reportError("Failed to connect: " + e.getMessage());
                return null;
            }
        }

        @Override
        public boolean isServer() {
            return false;
        }
    }
}
