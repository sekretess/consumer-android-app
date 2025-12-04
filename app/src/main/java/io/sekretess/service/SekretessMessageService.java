package io.sekretess.service;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.ViewModelProvider;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Date;
import java.util.List;

import io.sekretess.Constants;
import io.sekretess.R;
import io.sekretess.dependency.SekretessDependencyProvider;
import io.sekretess.dto.MessageBriefDto;
import io.sekretess.dto.MessageDto;
import io.sekretess.dto.MessageRecordDto;
import io.sekretess.enums.MessageType;
import io.sekretess.repository.MessageRepository;
import io.sekretess.utils.NotificationPreferencesUtils;

public class SekretessMessageService {
    private final MessageRepository messageRepository;
    private final String TAG = SekretessMessageService.class.getName();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SekretessMessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }


    public void handleMessage(String jsonPayload) {
        try {
            MessageDto message = objectMapper.readValue(jsonPayload, MessageDto.class);
            String sender = message.getSender();
            Log.i(TAG, "Received payload: " + jsonPayload + " sender:");
            String encryptedText = message.getText();
            MessageType messageType = MessageType.getInstance(message.getType());
            switch (messageType) {
                case ADVERTISEMENT:
                    processAdvertisementMessage(encryptedText, sender);
                    break;
                case KEY_DISTRIBUTION:
                case PRIVATE:
                    sender = message.getSender();
                    Log.i(TAG, "Private message received. Sender: " + sender + " sender: " + sender);
                    processPrivateMessage(encryptedText, sender, messageType);
                    break;
            }
            Log.i(TAG, "Encoded message received : " + message);
        } catch (Throwable e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void processAdvertisementMessage(String base64Message, String sender) {
        String username = SekretessDependencyProvider.authService().getUsername();
        SekretessDependencyProvider.cryptographicService()
                .decryptGroupChatMessage(sender, base64Message)
                .ifPresent(decryptedMessage -> {
                    messageRepository.storeDecryptedMessage(sender, decryptedMessage, username);
                    SekretessDependencyProvider.messageEventStream().postValue("new-message");
                    publishNotification(sender, decryptedMessage);
                });
    }

    private void processPrivateMessage(String base64Message, String sender, MessageType messageType) {
        SekretessCryptographicService sekretessCryptographicService = SekretessDependencyProvider.cryptographicService();
        sekretessCryptographicService
                .decryptPrivateMessage(sender, base64Message)
                .ifPresent(decryptedMessage -> {
                    if (messageType == MessageType.KEY_DISTRIBUTION) {
                        sekretessCryptographicService.processKeyDistributionMessage(sender, decryptedMessage);
                    } else {
                        String username = SekretessDependencyProvider.authService().getUsername();
                        messageRepository.storeDecryptedMessage(sender, decryptedMessage, username);
                        SekretessDependencyProvider.messageEventStream().postValue("new-message");
                        publishNotification(sender, decryptedMessage);
                    }
                });
    }

    public List<MessageBriefDto> getMessageBriefs() {
        String username = SekretessDependencyProvider.authService().getUsername();
        return messageRepository.getMessageBriefs(username);
    }

    public List<String> getTopSenders() {
        return messageRepository.getTopSenders();
    }

    public List<MessageRecordDto> loadMessages(String from) {
        return messageRepository.loadMessages(from);
    }


    public void deleteMessage(Long messageId) {
        messageRepository.deleteMessage(messageId);
    }

    private void publishNotification(String sender, String text) {
        Intent intent = new Intent();
        var notification = new NotificationCompat
                .Builder(SekretessDependencyProvider.applicationContext(), Constants.SEKRETESS_NOTIFICATION_CHANNEL_NAME)
                .setContentTitle("Message from " + sender)
                .setSilent(false)
                .setLargeIcon(BitmapFactory
                        .decodeResource(SekretessDependencyProvider.applicationContext().getResources(), R.drawable.ic_notif_sekretess))
                .setContentText(text.substring(0, Math.min(10, text.length())).concat("..."))
                .setSmallIcon(R.drawable.ic_notif_sekretess)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(PendingIntent
                        .getActivity(SekretessDependencyProvider.applicationContext(), 0,
                                intent, PendingIntent.FLAG_IMMUTABLE))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        NotificationManagerCompat notificationManager = NotificationManagerCompat
                .from(SekretessDependencyProvider.applicationContext());
        int m = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);

        NotificationChannel channel = new NotificationChannel(Constants.SEKRETESS_NOTIFICATION_CHANNEL_NAME,
                "New message", NotificationManager.IMPORTANCE_HIGH);
        channel.setAllowBubbles(true);
        channel.enableVibration(NotificationPreferencesUtils
                .getVibrationPreferences(SekretessDependencyProvider.applicationContext(), sender));
        boolean soundAlerts = NotificationPreferencesUtils
                .getSoundAlertsPreferences(SekretessDependencyProvider.applicationContext(), sender);
        Log.i("SekretessRabbitMqService", "soundAlerts:" + soundAlerts + "sender:" + sender);
        if (!soundAlerts) {
            notification.setSilent(true);
            channel.setImportance(NotificationManager.IMPORTANCE_LOW);
        } else {
            notification.setDefaults(0);
            notification.setSilent(false);
        }
        notificationManager.createNotificationChannel(channel);


        if (ActivityCompat.checkSelfPermission(SekretessDependencyProvider.applicationContext(),
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(m, notification.build());
        }
    }
}
