package io.sekretess.websocket;

import android.util.Log;

import androidx.annotation.Nullable;

import io.sekretess.BuildConfig;
import io.sekretess.dependency.SekretessDependencyProvider;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class SekretessAuthenticatedWebSocket extends WebSocketListener {
    private final WebSocketMonitor webSocketMonitor;
    private okhttp3.WebSocket webSocket;
    private ConnectionState connectionState;


    public SekretessAuthenticatedWebSocket() {
        this.webSocketMonitor = new WebSocketMonitor();
        this.connectionState = ConnectionState.DISCONNECTED;
    }

    @Override
    public void onMessage(okhttp3.WebSocket webSocket, String text) {
        SekretessDependencyProvider.messageService().handleMessage(text);
    }

    @Override
    public void onClosed(okhttp3.WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
        connectionState = ConnectionState.DISCONNECTED;
        webSocketMonitor.stop();
        Log.i("SekretessWebSocketClient", "WebSocket disconnected");
    }

    @Override
    public void onClosing(okhttp3.WebSocket webSocket, int code, String reason) {
        super.onClosing(webSocket, code, reason);
        connectionState = ConnectionState.DISCONNECTED;
        Log.i("SekretessWebSocketClient", "WebSocket closing");
    }

    @Override
    public void onOpen(okhttp3.WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
        Log.i("SekretessWebSocketClient", "WebSocket connected");
        try {
            webSocket.send(SekretessDependencyProvider.authService().getAccessToken().toString());
        } catch (Exception e) {
            Log.e("SekretessWebSocketListener", "Error occurred during send auth token to WebSocket", e);
            connectionState = ConnectionState.DISCONNECTED;
        }
    }


    @Override
    public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
        super.onFailure(webSocket, t, response);
        this.connectionState = ConnectionState.DISCONNECTED;
        webSocketMonitor.stop();
        webSocket.close(1000, "Error connecting to WebSocket");
        Log.e("SekretessWebSocketClient", "Error connecting to WebSocket", t);
    }

    public void connect() {
        if (connectionState == ConnectionState.CONNECTED) {
            Log.i("SekretessWebSocketClient", "WebSocket is already connected");
            return;
        }
        OkHttpClient client = new OkHttpClient();
        Request request = new Request
                .Builder()
                .addHeader("Origin", "https://consumer.sekretess.io")
                .url(BuildConfig.WEB_SOCKET_URL)
                .build();
        this.webSocket = client.newWebSocket(request, this);
        this.connectionState = ConnectionState.CONNECTED;
        webSocketMonitor.start();
    }

    public void ping() {
        if (connectionState == ConnectionState.CONNECTED) {
            webSocket.send("sekretess-ping:" + System.currentTimeMillis());
        } else {
            Log.e("SekretessWebSocketClient", "WebSocket is not connected");
            throw new IllegalStateException("WebSocket is not connected");
        }
    }

    public void disconnect() {
        if (webSocketMonitor != null) {
            webSocketMonitor.stop();
        }
        this.connectionState = ConnectionState.DISCONNECTED;

        if (webSocket != null) {
            webSocket.close(1000, "Normal close");
        }
        this.onFailure(webSocket, new Exception("WebSocket closed"), null);
    }

    public enum ConnectionState {
        CONNECTED,
        DISCONNECTED,
        RECOVERING
    }
}
