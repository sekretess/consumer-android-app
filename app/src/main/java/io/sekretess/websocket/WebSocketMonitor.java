package io.sekretess.websocket;

import android.util.Log;

import org.apache.commons.lang3.ThreadUtils;

import java.time.Duration;

public class WebSocketMonitor {
    private Thread webSocketMonitorThread;
    private SekretessAuthenticatedWebSocket webSocket;

    public WebSocketMonitor(SekretessAuthenticatedWebSocket webSocket) {
        this.webSocket = webSocket;
    }


    public void start() {
        webSocketMonitorThread = new Thread(() -> {
            while (!webSocketMonitorThread.isInterrupted()) {
                try {
                    ThreadUtils.sleep(Duration.ofSeconds(30));
                    webSocket.ping();
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
