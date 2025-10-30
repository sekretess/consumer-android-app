package io.sekretess.service;

import java.io.IOException;
import java.net.URL;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

public class SekretessWebSocketClient {
    private final SekretessCryptographicService sekretessCryptographicService;
    private WebSocket webSocket;
    private Thread t;

    public SekretessWebSocketClient(SekretessCryptographicService sekretessCryptographicService) {
        this.sekretessCryptographicService = sekretessCryptographicService;
    }

    public void startWebSocket(URL url) throws IOException {
        this.webSocket = new WebSocketFactory().createSocket(url)
                .addListener(new WebSocketAdapter() {
                    @Override
                    public void onTextMessage(WebSocket websocket, String text) throws Exception {
                        sekretessCryptographicService.decryptMessage(text);
                    }

                    @Override
                    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
                        startWebSocket(url);
                    }

                    @Override
                    public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
                        super.onError(websocket, cause);
                    }
                });
        t = new Thread(() -> {
            try {
                webSocket.connect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        t.start();
    }

    public void destroy() {
        if (t != null) {
            t.interrupt();
        }
        if (webSocket != null) {
            webSocket.sendClose();
            webSocket.disconnect();
        }
    }
}
