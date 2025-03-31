package io.sekretess.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import io.sekretess.Constants;
import io.sekretess.R;
import io.sekretess.dto.MessageDto;
import io.sekretess.repository.DbHelper;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class SekretessRabbitMqService extends SekretessBackgroundService {

    public static final int RABBIT_MQ_NOTIFICATION = 1;
    private static final String TAG = "SekretessRabbitMqConsumer";
    private Channel rabbitMqChannel;
    private Connection rabbitMqConnection;
    private Future<?> future;
    private final ObjectMapper objectMapper = new ObjectMapper();
    public static final AtomicInteger serviceInstances = new AtomicInteger(0);
    private String queueName;

    private final BroadcastReceiver loggedInEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("SekretessRabbitMqService", "Login event received");
            queueName = intent.getStringExtra("queueName");
            future = Executors.newSingleThreadExecutor().submit(() -> startConsumeQueue(queueName));
        }
    };

    @Override
    public void destroyed() {
        super.onDestroy();
        serviceInstances.getAndSet(0);
        Executors.newSingleThreadExecutor().submit(this::closeRabbitMqConnections);
        future.cancel(true);
    }

    private void closeRabbitMqConnections() {
        if (rabbitMqChannel != null) {
            try {
                rabbitMqChannel.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
        }
        if (rabbitMqConnection != null) {
            try {
                rabbitMqConnection.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void started(Intent intent) {
        Log.i("SekretessRabbitMqService", "SekretessRabbitMqService started successfully");
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        getApplicationContext()
                .registerReceiver(loggedInEventReceiver, new IntentFilter(Constants.EVENT_LOGIN),
                        RECEIVER_EXPORTED);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private Connection createConnection() {
        DbHelper dbHelper = DbHelper.getInstance(getApplicationContext());
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setVirtualHost("sekretess");
        String amqpConnectionUrl = getString(R.string.rabbit_mq_uri);
        amqpConnectionUrl = String.format(amqpConnectionUrl, dbHelper.getUserNameFromJwt(), dbHelper.getAuthState().getAccessToken());
        try {
            connectionFactory.setUri(amqpConnectionUrl);
        } catch (Exception e) {
            Log.e("SekretessRabbitMqService", "URI setting problem", e);
        }
        connectionFactory.setAutomaticRecoveryEnabled(false);
        while (true) {

            try {
                Thread.sleep(3000);
                Connection connection = connectionFactory.newConnection();
                connection.addShutdownListener(cause -> {
                    try {
                        rabbitMqConnection = createConnection();
                        rabbitMqChannel = rabbitMqConnection.createChannel();
                    } catch (Exception e) {
                        Log.e("SekretessRabbitMqService", "AMQP Connection creation establishment failed", e);
                    }
                });
                return connection;
            } catch (Exception e) {
                Log.e("SekretessRabbitMqService", "Can not establish connection", e);
            }
        }
    }

    private Channel createChannel(Connection connection) {
        while (connection.isOpen()) {
            try {
                Channel channel = connection.createChannel();
                channel.addShutdownListener(cause -> {
                    rabbitMqChannel = createChannel(connection);
                });
                return channel;
            } catch (Exception e) {
                Log.e("SekretessRabbitMqService", "AMQP channel creation failed", e);
            }
        }
        return null;
    }

    private void startConsumeQueue(String queueName) {
        try {

            if (rabbitMqChannel == null || !rabbitMqChannel.getConnection().isOpen()) {
                rabbitMqConnection = createConnection();
                rabbitMqChannel = createChannel(rabbitMqConnection);
                rabbitMqChannel.confirmSelect();

                Log.i("SekretessRabbitMqService", "RabbitMq Consumer connection established.");
                rabbitMqChannel.basicConsume(queueName.concat(Constants.RABBIT_MQ_CONSUMER_QUEUE_SUFFIX),
                        true, new DefaultConsumer(rabbitMqChannel) {
                            @Override
                            public void handleDelivery(String consumerTag, Envelope envelope,
                                                       AMQP.BasicProperties properties, byte[] body) {
                                try {
                                    String exchangeName = envelope.getExchange();
                                    Log.i("SekretessRabbitMqService", "Received payload:" + new String(body));
                                    MessageDto message = objectMapper.readValue(body, MessageDto.class);
                                    String encryptedText = message.getText();
                                    String messageType = message.getType();
                                    String sender = "";
                                    switch (messageType.toLowerCase()) {
                                        case "advert":
                                            exchangeName = message.getBusinessExchange();
                                            break;
                                        case "key_dist":
                                            exchangeName = message.getConsumerExchange();
                                            sender = message.getSender();
                                            break;
                                        case "private":
                                            exchangeName = message.getConsumerExchange();
                                            break;
                                    }


                                    Log.i("SekretessRabbitMqService", "Encoded message received : " + message);
                                    broadcastNewMessageReceived(encryptedText, sender, exchangeName, messageType);

                                } catch (Exception e) {
                                    Log.e(TAG, e.getMessage(), e);
                                }
                            }
                        });
                Log.i("SekretessRabbitMqService", "RabbitMq Consumer started");
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void broadcastNewMessageReceived(String encryptedMessage, String sender, String exchangeName, String messageType) {
        Intent intent = new Intent(Constants.EVENT_NEW_INCOMING_ENCRYPTED_MESSAGE);
        intent.putExtra("encryptedMessage", encryptedMessage);
        intent.putExtra("exchangeName", exchangeName);
        intent.putExtra("messageType", messageType);
        intent.putExtra("sender", sender);
        sendBroadcast(intent);
    }

    @Override
    public String getChannelId() {
        return "sekretess:rb-service-channel";
    }

    @Override
    public int getNotificationId() {
        return RABBIT_MQ_NOTIFICATION;
    }
}
