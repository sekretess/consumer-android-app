package io.sekretess.listeners;

import android.util.Log;

import androidx.annotation.Nullable;

import org.apache.commons.lang3.ThreadUtils;

import java.time.Duration;

import io.sekretess.service.AuthService;
import io.sekretess.service.SekretessMessageService;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class SekretessWebSocketListener extends WebSocketListener {
    private final SekretessMessageService sekretessMessageService;
    private Thread webSocketMonitorThread;
    private AuthService authService;

    public SekretessWebSocketListener(SekretessMessageService sekretessMessageService,
                                      AuthService authService) {
        this.sekretessMessageService = sekretessMessageService;
        this.authService = authService;
    }

    @Override
    public void onMessage(okhttp3.WebSocket webSocket, String text) {
        sekretessMessageService.handleMessage(text);
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
            webSocket.send(authService.getAccessToken().toString());
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
