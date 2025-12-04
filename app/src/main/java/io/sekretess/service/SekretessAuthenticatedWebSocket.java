package io.sekretess.service;

import android.util.Log;

import io.sekretess.listeners.SekretessWebSocketListener;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class SekretessAuthenticatedWebSocket {

    private okhttp3.WebSocket webSocket;

    private SekretessWebSocketListener sekretessWebSocketListener;


    public SekretessAuthenticatedWebSocket() {

    }

    public void startWebSocket(String url) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request
                .Builder()
                .addHeader("Origin", "https://consumer.sekretess.io")
                .url(url)
                .build();
        this.sekretessWebSocketListener = new SekretessWebSocketListener();
        this.webSocket = client.newWebSocket(request, sekretessWebSocketListener);
    }

    public void destroy() {
        if (webSocket != null) {
            webSocket.close(0, "Normal close");
        }
        if (sekretessWebSocketListener != null) {
            sekretessWebSocketListener.onFailure(webSocket, new Exception("WebSocket closed"), null);
        }
    }
}
