package io.sekretess.listeners;

import android.util.Log;

import androidx.annotation.Nullable;

import org.apache.commons.lang3.ThreadUtils;

import java.time.Duration;

import io.sekretess.dependency.SekretessDependencyProvider;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class SekretessWebSocketListener extends WebSocketListener {
    private Thread webSocketMonitorThread;

    public SekretessWebSocketListener() {

    }

    @Override
    public void onMessage(okhttp3.WebSocket webSocket, String text) {
        SekretessDependencyProvider.messageService().handleMessage(text);
    }

    @Override
    public void onClosed(okhttp3.WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
    }

    @Override
    public void onClosing(okhttp3.WebSocket webSocket, int code, String reason) {
        super.onClosing(webSocket, code, reason);
    }

    @Override
    public void onOpen(okhttp3.WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
        Log.i("SekretessWebSocketClient", "WebSocket connected");
        try {
            webSocket.send(SekretessDependencyProvider.authService().getAccessToken().toString());
            webSocketMonitorThread = new Thread(() -> {
                while (!webSocketMonitorThread.isInterrupted()) {
                    try {
                        ThreadUtils.sleep(Duration.ofSeconds(30));
                        webSocket.send("ping:" + System.currentTimeMillis());
                        Log.i("SekretessWebSocketListener", "Ping sent");
                    } catch (Exception e) {
                        Log.e("SekretessWebSocketListener", "Error occurred during WebSocket monitoring", e);
                    }
                }
            });
            webSocketMonitorThread.start();
        } catch (Exception e) {
            Log.e("SekretessWebSocketListener", "Error occurred during send auth token to WebSocket", e);
        }
    }


    @Override
    public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
        super.onFailure(webSocket, t, response);
        if (webSocketMonitorThread != null) {
            webSocketMonitorThread.interrupt();
        }
        Log.e("SekretessWebSocketClient", "Error connecting to WebSocket", t);
    }
}
