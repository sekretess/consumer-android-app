package com.sekretess.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.sekretess.R;
import com.sekretess.dto.MessageDto;
import com.sekretess.model.MessageEntity;
import com.sekretess.repository.DbHelper;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

public class SekretessRabbitMqService extends Service {

    private static final String TAG = "SekretessRabbitMqConsumer";
    private Channel rabbitMqChannel;
    private Connection rabbitMqConnection;
    private Future<?> future;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onDestroy() {
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
        future.cancel(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();
        String queueName = extras.getString("queueName");
        future = Executors.newSingleThreadExecutor().submit(() -> startConsumeQueue(queueName, intent));
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startConsumeQueue(String queueName, Intent intent) {
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setUri(getString(R.string.rabbit_mq_uri));
            rabbitMqConnection = connectionFactory.newConnection();
            rabbitMqChannel = rabbitMqConnection.createChannel();
            rabbitMqChannel.confirmSelect();

            rabbitMqChannel.basicConsume(queueName, true, new DefaultConsumer(rabbitMqChannel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope,
                                           AMQP.BasicProperties properties, byte[] body)
                        throws IOException {
                    MessageDto messageDto = objectMapper.readValue(body, MessageDto.class);

                    try {
                        broadcastNewMessageReceived(messageDto.getText(), messageDto.getSender(),
                                messageDto.getDeviceId());

                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void broadcastNewMessageReceived(String encryptedMessage, String name, int deviceId) {
        Intent intent = new Intent("new-incoming-encrypted-message");
        intent.putExtra("encryptedMessage", encryptedMessage);
        intent.putExtra("name", name);
        intent.putExtra("deviceId", deviceId);
        LocalBroadcastManager.getInstance(SekretessRabbitMqService.this).sendBroadcast(intent);
    }


}
