package io.sekretess.websocket;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.sekretess.dependency.SekretessDependencyProvider;

public class WebSocketMonitor {
    private final String PINGER_TASK_NAME = "SekretessWebSocketPingWorker";
    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private Future<?> pingTask;
    public WebSocketMonitor() {

    }


    public void start() {
        if(this.pingTask != null){
            this.pingTask.cancel(true);
        }

        this.pingTask = scheduledExecutorService.scheduleAtFixedRate(() -> {
            SekretessDependencyProvider.authenticatedWebSocket().ping();
        }, 10, 10,TimeUnit.SECONDS);
    }

    public void stop() {
        if (this.pingTask != null) {
            this.pingTask.cancel(true);
        }
    }
}
