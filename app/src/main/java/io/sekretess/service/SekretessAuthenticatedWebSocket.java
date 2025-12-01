package io.sekretess.service;

import android.util.Log;

import io.sekretess.listeners.SekretessWebSocketListener;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class SekretessAuthenticatedWebSocket {
    private final SekretessMessageService sekretessMessageService;
    private okhttp3.WebSocket webSocket;
    private final AuthService authService;
    private SekretessWebSocketListener sekretessWebSocketListener;


    public SekretessAuthenticatedWebSocket(SekretessMessageService sekretessMessageService, AuthService authService) {
        this.sekretessMessageService = sekretessMessageService;
        this.authService = authService;
    }

    public void startWebSocket(String url) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request
                .Builder()
                .addHeader("Origin", "https://consumer.sekretess.io")
                .url(url)
                .build();
        this.sekretessWebSocketListener = new SekretessWebSocketListener(sekretessMessageService, authService);
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
