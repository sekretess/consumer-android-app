package io.sekretess.websocket;

import android.util.Log;

import org.apache.commons.lang3.ThreadUtils;

import java.time.Duration;

import io.sekretess.dependency.SekretessDependencyProvider;

public class WebSocketMonitor {
    private Thread webSocketMonitorThread;

    public WebSocketMonitor() {

    }


    public void start() {
        webSocketMonitorThread = new Thread(() -> {
            while (!webSocketMonitorThread.isInterrupted()) {
                try {
                    ThreadUtils.sleep(Duration.ofSeconds(10));
                    SekretessDependencyProvider.authenticatedWebSocket().ping();
                    Log.i("SekretessWebSocketListener", "Ping sent");
                } catch (InterruptedException e) {
                    Log.e("SekretessWebSocketListener", "Error occurred during WebSocket monitoring", e);
                    webSocketMonitorThread.interrupt();
                }
            }
        });
        webSocketMonitorThread.start();
    }

    public void stop() {
        if (webSocketMonitorThread != null) {
            webSocketMonitorThread.interrupt();
        }
    }
}
