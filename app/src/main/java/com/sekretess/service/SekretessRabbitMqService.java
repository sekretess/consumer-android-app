package com.sekretess.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.sekretess.Constants;
import com.sekretess.R;
import com.sekretess.dto.MessageDto;

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

    private final BroadcastReceiver loggedInEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String queueName = intent.getStringExtra("queueName");
            future = Executors.newSingleThreadExecutor().submit(() -> startConsumeQueue(queueName, intent));
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
        getApplicationContext()
                .registerReceiver(loggedInEventReceiver, new IntentFilter(Constants.EVENT_LOGIN),
                        RECEIVER_EXPORTED);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startConsumeQueue(String queueName, Intent intent) {
        try {
            if (rabbitMqChannel == null || !rabbitMqChannel.getConnection().isOpen()) {
                ConnectionFactory connectionFactory = new ConnectionFactory();
                connectionFactory.setUri(getString(R.string.rabbit_mq_uri));
                connectionFactory.setAutomaticRecoveryEnabled(true);
                rabbitMqConnection = connectionFactory.newConnection();
                rabbitMqChannel = rabbitMqConnection.createChannel();
                rabbitMqChannel.confirmSelect();
                Log.i("SekretessRabbitMqService", "RabbitMq Consumer connection established.");
                rabbitMqChannel.basicConsume(queueName, true, new DefaultConsumer(rabbitMqChannel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope,
                                               AMQP.BasicProperties properties, byte[] body) {
                        try {
                            MessageDto messageDto = objectMapper.readValue(body, MessageDto.class);
                            Log.i("SekretessRabbitMqService", "Encoded message received");
                            broadcastNewMessageReceived(messageDto.getText(), messageDto.getSender(),
                                    messageDto.getDeviceId());

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

    private void broadcastNewMessageReceived(String encryptedMessage, String name, int deviceId) {
        Intent intent = new Intent(Constants.EVENT_NEW_INCOMING_ENCRYPTED_MESSAGE);
        intent.putExtra("encryptedMessage", encryptedMessage);
        intent.putExtra("name", name);
        intent.putExtra("deviceId", deviceId);
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
