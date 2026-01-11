package io.sekretess.websocket;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

import io.sekretess.BuildConfig;
import io.sekretess.SekretessApplication;
import io.sekretess.dependency.SekretessDependencyProvider;
import io.sekretess.dto.MessageAckDto;
import io.sekretess.dto.MessageDto;
import io.sekretess.enums.SekretessEvent;
import io.sekretess.exception.IncorrectTokenSyntaxException;
import io.sekretess.exception.TokenExpiredException;
import io.sekretess.exception.TokenNotFoundException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class SekretessAuthenticatedWebSocket extends WebSocketListener {
    private final WebSocketMonitor webSocketMonitor;
    private okhttp3.WebSocket webSocket;
    private ConnectionState connectionState;
    public final int MESSAGE_HANDLING_SUCCESS = 2;
    public final int MESSAGE_HANDLING_FAILED = 3;
    private final ObjectMapper objectMapper = new ObjectMapper();


    public SekretessAuthenticatedWebSocket() {
        this.webSocketMonitor = new WebSocketMonitor();
        this.connectionState = ConnectionState.DISCONNECTED;
    }

    @Override
    public void onMessage(okhttp3.WebSocket webSocket, String text) {
        Log.i("SekretessWebSocketClient", "Received message: " + text);
        MessageDto message = null;
        try {
            message = objectMapper.readValue(text, MessageDto.class);
            SekretessDependencyProvider.messageService().handleMessage(message);
            webSocket.send(new MessageAckDto(message.getMessageId(), MESSAGE_HANDLING_SUCCESS).jsonString());
        } catch (Exception e) {
            Log.e("SekretessWebSocketClient", "Error occurred during handle message", e);
            if (message != null) {
                webSocket.send(new MessageAckDto(message.getMessageId(), MESSAGE_HANDLING_FAILED).jsonString());
            }
        }
    }

    @Override
    public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
        Log.i("SekretessWebSocketClient", "Received message: " + bytes.string(StandardCharsets.UTF_8));
        MessageDto message = null;
        try {
            message = objectMapper.readValue(bytes.string(StandardCharsets.UTF_8), MessageDto.class);
            SekretessDependencyProvider.messageService().handleMessage(message);
            webSocket.send(new MessageAckDto(message.getMessageId(), MESSAGE_HANDLING_SUCCESS).jsonString());
        } catch (Exception e) {
            Log.e("SekretessWebSocketClient", "Error occurred during handle message", e);
            if (message != null) {
                webSocket.send(new MessageAckDto(message.getMessageId(), MESSAGE_HANDLING_FAILED).jsonString());
            }
        }
    }

    @Override
    public void onClosed(okhttp3.WebSocket webSocket, int code, String reason) {
//        super.onClosed(webSocket, code, reason);
        notify(SekretessEvent.WEBSOCKET_CONNECTION_LOST);
        this.connectionState = ConnectionState.DISCONNECTED;
        webSocketMonitor.stop();
        Log.i("SekretessWebSocketClient", "WebSocket disconnected");
    }

    @Override
    public void onClosing(okhttp3.WebSocket webSocket, int code, String reason) {
//        super.onClosing(webSocket, code, reason);
        Log.i("SekretessWebSocketClient", "WebSocket closing");
    }

    @Override
    public void onOpen(okhttp3.WebSocket webSocket, Response response) {
//        super.onOpen(webSocket, response);
        Log.i("SekretessWebSocketClient", "WebSocket connected");
        try {
            webSocket.send(SekretessDependencyProvider.authService().getAccessToken().toString());
            connectionState = ConnectionState.CONNECTED;
            webSocketMonitor.start();
            notify(SekretessEvent.WEBSOCKET_CONNECTION_ESTABLISHED);
        } catch (TokenExpiredException tokenNotFoundException) {
            Log.e("SekretessWebSocketListener",
                    "Error occurred during send auth token to WebSocket", tokenNotFoundException);
        } catch (Exception e) {
            Log.e("SekretessWebSocketListener", "Error occurred during send auth token to WebSocket", e);
            connectionState = ConnectionState.DISCONNECTED;
            notify(SekretessEvent.WEBSOCKET_CONNECTION_LOST);
        }

    }


    @Override
    public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
//        super.onFailure(webSocket, t, response);
        notify(SekretessEvent.WEBSOCKET_CONNECTION_LOST);
        connectionState = ConnectionState.DISCONNECTED;
        webSocketMonitor.stop();
        webSocket.close(1000, "Error connecting to WebSocket");
        Log.e("SekretessWebSocketClient", "Error occurred on websocket:" + t);
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
    }

    public boolean ping() {
        if (connectionState == ConnectionState.CONNECTED) {
            webSocket.send("sekretess-ping:" + System.currentTimeMillis());
            Log.i("SekretessWebSocketListener", "Ping sent");
            return true;
        } else {
            Log.e("SekretessWebSocketClient", "WebSocket is not connected");
            return false;
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

    private void notify(SekretessEvent sekretessEvent) {
        SekretessDependencyProvider.getSekretessEventMutableLiveData().postValue(sekretessEvent);
    }

    public enum ConnectionState {
        CONNECTED,
        DISCONNECTED,
    }
}
