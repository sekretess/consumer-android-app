package io.sekretess.service;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.impl.CredentialsProvider;

import org.signal.libsignal.protocol.DuplicateMessageException;
import org.signal.libsignal.protocol.InvalidKeyException;
import org.signal.libsignal.protocol.InvalidKeyIdException;
import org.signal.libsignal.protocol.InvalidMessageException;
import org.signal.libsignal.protocol.InvalidVersionException;
import org.signal.libsignal.protocol.LegacyMessageException;
import org.signal.libsignal.protocol.NoSessionException;
import org.signal.libsignal.protocol.SessionCipher;
import org.signal.libsignal.protocol.SignalProtocolAddress;
import org.signal.libsignal.protocol.UntrustedIdentityException;
import org.signal.libsignal.protocol.UsePqRatchet;
import org.signal.libsignal.protocol.message.PreKeySignalMessage;

import io.sekretess.BuildConfig;
import io.sekretess.Constants;
import io.sekretess.R;
import io.sekretess.dto.MessageDto;
import io.sekretess.enums.MessageType;
import io.sekretess.repository.DbHelper;
import io.sekretess.ui.LoginActivity;
import io.sekretess.utils.NotificationPreferencesUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SekretessRabbitMqService {

    private static final String TAG = "SekretessRabbitMqConsumer";
    private Channel rabbitMqChannel;
    private Connection rabbitMqConnection;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Thread rabbitMqConnectorThread;
    private ScheduledExecutorService rabbitMqConnectionGuard;

    private final Context context;
    private final SekretessCryptographicService sekretessCryptographicService;


    public SekretessRabbitMqService(Context context, SekretessCryptographicService sekretessCryptographicService) {
        this.context = context;
        this.sekretessCryptographicService = sekretessCryptographicService;
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private void closeRabbitMqConnections() {
        try {
            Executors.newSingleThreadExecutor().submit(() -> {
                if (rabbitMqChannel != null) {
                    try {
                        rabbitMqChannel.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (TimeoutException e) {
                        Log.i("SekretessRabbitMqService", "RabbitMqChannel close failed", e);
                    }
                }
                if (rabbitMqConnection != null) {
                    try {
                        rabbitMqConnection.close();
                    } catch (IOException e) {
                        Log.i("SekretessRabbitMqService", "RabbitMqConnection close failed", e);
                    }


                }
            }).get();
        } catch (Throwable e) {
            Log.i("SekretessRabbitMqService", "RabbitMqConnection close failed", e);
        }


        if (rabbitMqConnectorThread != null)
            rabbitMqConnectorThread.interrupt();
        rabbitMqConnectorThread = null;

        if (rabbitMqConnectionGuard != null)
            rabbitMqConnectionGuard.shutdownNow();
        rabbitMqConnectionGuard = null;


    }

    public void startRabbitMqConnectionGuard() {
        if (rabbitMqConnectionGuard == null || rabbitMqConnectionGuard.isShutdown()
                || rabbitMqConnectionGuard.isTerminated()) {
            rabbitMqConnectionGuard = Executors.newScheduledThreadPool(1);
            rabbitMqConnectionGuard.scheduleWithFixedDelay(() -> {
                Log.i("SekretessRabbitMqService", "rabbitMqConnectionGuard...");
                if (isRabbitMqConnectionClose()) {
                    Log.i("SekretessRabbitMqService",
                            "rabbitMqConnectionGuard - RabbitMq connection not established connecting...");
                    startRabbitMqConnection();
                }
            }, 20, 20, TimeUnit.SECONDS);
        }
    }


    private boolean isRabbitMqConnectionClose() {
        return rabbitMqConnection == null || rabbitMqChannel == null || !rabbitMqConnection.isOpen() || !rabbitMqConnection.isOpen();
    }

    private void startRabbitMqConnection() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setVirtualHost("sekretess");
        try (DbHelper dbHelper = new DbHelper(context)) {
            String amqpConnectionUrl = BuildConfig.RABBIT_MQ_URI;

            String userName = dbHelper.getUserNameFromJwt();
            amqpConnectionUrl = String.format(amqpConnectionUrl, userName, dbHelper.getAuthState().getAccessToken());
            Log.i(TAG, "Connecting with URI: " + amqpConnectionUrl);
            connectionFactory.setUri(amqpConnectionUrl);
            connectionFactory.setCredentialsProvider(new CredentialsProvider() {
                @Override
                public String getUsername() {
                    return dbHelper.getUserNameFromJwt();
                }

                @Override
                public String getPassword() {
                    return dbHelper.getAuthState().getAccessToken();
                }
            });
            connectionFactory.setAutomaticRecoveryEnabled(true);
            connectionFactory.useSslProtocol();
            rabbitMqConnection = connectionFactory.newConnection();
            if (rabbitMqConnection == null) {
                Log.i(TAG, "RabbitMq Consumer connection NOT established.");
                return;
            }
            rabbitMqChannel = rabbitMqConnection.createChannel();
            if (rabbitMqChannel == null) {
                Log.i(TAG, "RabbitMq Consumer connection channel NOT established.");
                return;
            }
            rabbitMqChannel.confirmSelect();

            Log.i(TAG, "RabbitMq Consumer connection established.");
            rabbitMqChannel.basicConsume(userName.concat(Constants.RABBIT_MQ_CONSUMER_QUEUE_SUFFIX), true, new DefaultConsumer(rabbitMqChannel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
                    try {
                        String exchangeName = envelope.getExchange();
                        Log.i(TAG, "Received payload:" + new String(body) + " ExchangeName :" + exchangeName);
                        MessageDto message = objectMapper.readValue(body, MessageDto.class);
                        String encryptedText = message.getText();
                        MessageType messageType = MessageType.getInstance(message.getType());
                        String sender = "";
                        switch (messageType) {
                            case ADVERTISEMENT:
                                exchangeName = message.getBusinessExchange();
                                processAdvertisementMessage(encryptedText, exchangeName);
                                break;
                            case KEY_DISTRIBUTION:
                            case PRIVATE:
                                exchangeName = message.getConsumerExchange();
                                sender = message.getSender();
                                Log.i(TAG, "Private message received. Sender:" + sender + " Exchange:" + exchangeName);
                                processPrivateMessage(encryptedText, sender, messageType);
                                break;
                        }
                        Log.i(TAG, "Encoded message received : " + message);
                    } catch (Throwable e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
            });
            Log.i(TAG, "RabbitMq Consumer started");
        } catch (TimeoutException | NoSuchAlgorithmException | KeyManagementException |
                 URISyntaxException e) {
            Log.e(TAG, "Can not establish connection", e);
        } catch (IOException io) {
            Log.e(TAG, "Can not establish connection", io);
            closeRabbitMqConnections();
            context.sendBroadcast(new Intent(Constants.EVENT_TOKEN_ISSUE));
        }
    }


    private void processAdvertisementMessage(String base64Message, String exchangeName) throws NoSessionException, InvalidMessageException, DuplicateMessageException, LegacyMessageException {
        String sender = exchangeName.split("_")[0];
        sekretessCryptographicService.handleAdvertisementMessage(sender, base64Message, exchangeName, (message) -> {
            Log.i("SignalProtocolService", "Decrypted advertisement message: " + message);
            try (DbHelper dbHelper = new DbHelper(context)) {
                dbHelper.storeDecryptedMessage(sender, message);
                broadcastNewMessageReceived();
                publishNotification(sender, message);
            }
        });
    }

    private void processPrivateMessage(String base64Message, String sender, MessageType messageType) throws InvalidMessageException, InvalidVersionException, LegacyMessageException, InvalidKeyException, UntrustedIdentityException, DuplicateMessageException, InvalidKeyIdException {
        sekretessCryptographicService.handlePrivateMessage(sender, base64Message, (message) -> {
            try (DbHelper dbHelper = new DbHelper(context)) {
                if (messageType == MessageType.KEY_DISTRIBUTION) {
                    sekretessCryptographicService.processKeyDistributionMessage(sender, message);
                    //Store group chat info
                    dbHelper.storeGroupChatInfo(message, sender);
                } else {
                    Log.i("SignalProtocolService", "Decrypted private message: " + message);
                    dbHelper.storeDecryptedMessage(sender, message);
                    publishNotification(sender, message);
                    broadcastNewMessageReceived();
                }
            }
        });
    }

    private void broadcastNewMessageReceived() {
        Log.i("SignalProtocolService", "Sending new-incoming-message event");
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);
        intent.setAction(Constants.EVENT_NEW_INCOMING_MESSAGE);
        context.sendBroadcast(intent);
    }

    private void publishNotification(String sender, String text) {
        Intent intent = new Intent();
        var notification = new NotificationCompat
                .Builder(context, Constants.SEKRETESS_NOTIFICATION_CHANNEL_NAME)
                .setContentTitle("Message from " + sender)
                .setSilent(false)
                .setLargeIcon(BitmapFactory
                        .decodeResource(context.getResources(), R.drawable.ic_notif_sekretess))
                .setContentText(text.substring(0, Math.min(10, text.length())).concat("..."))
                .setSmallIcon(R.drawable.ic_notif_sekretess)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(PendingIntent
                        .getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        int m = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);

        NotificationChannel channel = new NotificationChannel(Constants.SEKRETESS_NOTIFICATION_CHANNEL_NAME, "New message", NotificationManager.IMPORTANCE_HIGH);
        channel.setAllowBubbles(true);
        channel.enableVibration(NotificationPreferencesUtils.getVibrationPreferences(context, sender));
        boolean soundAlerts = NotificationPreferencesUtils.getSoundAlertsPreferences(context, sender);
        Log.i("SekretessRabbitMqService", "soundAlerts:" + soundAlerts + "sender:" + sender);
        if (!soundAlerts) {
            notification.setSilent(true);
            channel.setImportance(NotificationManager.IMPORTANCE_LOW);
        } else {
            notification.setDefaults(0);
            notification.setSilent(false);
        }
        notificationManager.createNotificationChannel(channel);


        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(m, notification.build());
        }
    }

}
