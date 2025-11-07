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
import androidx.lifecycle.LiveData;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Date;
import java.util.List;

import io.sekretess.Constants;
import io.sekretess.R;
import io.sekretess.SekretessApplication;
import io.sekretess.dto.MessageBriefDto;
import io.sekretess.dto.MessageDto;
import io.sekretess.dto.MessageRecordDto;
import io.sekretess.enums.MessageType;
import io.sekretess.repository.MessageRepository;
import io.sekretess.utils.NotificationPreferencesUtils;
import kotlinx.coroutines.flow.StateFlow;

public class SekretessMessageService {
    private final MessageRepository messageRepository;
    private final String TAG = SekretessMessageService.class.getName();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SekretessCryptographicService sekretessCryptographicService;
    private final SekretessApplication sekretessApplication;

    public SekretessMessageService(MessageRepository messageRepository,
                                   SekretessCryptographicService sekretessCryptographicService,
                                   SekretessApplication sekretessApplication) {
        this.messageRepository = messageRepository;
        this.sekretessCryptographicService = sekretessCryptographicService;
        this.sekretessApplication = sekretessApplication;
    }


    public void handleMessage(String messageText) {
        try {
            String exchangeName = "";
            Log.i(TAG, "Received payload:" + messageText + " ExchangeName :");
            MessageDto message = objectMapper.readValue(messageText, MessageDto.class);
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

    private void processAdvertisementMessage(String base64Message, String sender) {
        sekretessCryptographicService.decryptGroupChatMessage(sender, base64Message).ifPresent(decryptedMessage -> {
            messageRepository.storeDecryptedMessage(sender, decryptedMessage);
            sekretessApplication.getMessageEventsLiveData().postValue("new-message");
            publishNotification(sender, decryptedMessage);
        });
    }

    private void processPrivateMessage(String base64Message, String sender, MessageType messageType) {
        sekretessCryptographicService.decryptPrivateMessage(sender, base64Message).ifPresent(decryptedMessage -> {
            if (messageType == MessageType.KEY_DISTRIBUTION) {
                sekretessCryptographicService.processKeyDistributionMessage(sender, decryptedMessage);
            } else {
                messageRepository.storeDecryptedMessage(sender, decryptedMessage);
                sekretessApplication.getMessageEventsLiveData().postValue("new-message");
                publishNotification(sender, decryptedMessage);
            }
        });
    }

    public List<MessageBriefDto> getMessageBriefs(String username) {
        return messageRepository.getMessageBriefs(username);
    }

    public List<String> getTopSenders() {
        return messageRepository.getTopSenders();
    }

    public List<MessageRecordDto> loadMessages(String from) {
        return messageRepository.loadMessages(from);
    }

    private void publishNotification(String sender, String text) {
        Intent intent = new Intent();
        var notification = new NotificationCompat
                .Builder(sekretessApplication.getApplicationContext(), Constants.SEKRETESS_NOTIFICATION_CHANNEL_NAME)
                .setContentTitle("Message from " + sender)
                .setSilent(false)
                .setLargeIcon(BitmapFactory
                        .decodeResource(sekretessApplication.getApplicationContext().getResources(), R.drawable.ic_notif_sekretess))
                .setContentText(text.substring(0, Math.min(10, text.length())).concat("..."))
                .setSmallIcon(R.drawable.ic_notif_sekretess)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(PendingIntent
                        .getActivity(sekretessApplication.getApplicationContext(), 0,
                                intent, PendingIntent.FLAG_IMMUTABLE))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        NotificationManagerCompat notificationManager = NotificationManagerCompat
                .from(sekretessApplication.getApplicationContext());
        int m = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);

        NotificationChannel channel = new NotificationChannel(Constants.SEKRETESS_NOTIFICATION_CHANNEL_NAME,
                "New message", NotificationManager.IMPORTANCE_HIGH);
        channel.setAllowBubbles(true);
        channel.enableVibration(NotificationPreferencesUtils
                .getVibrationPreferences(sekretessApplication.getApplicationContext(), sender));
        boolean soundAlerts = NotificationPreferencesUtils
                .getSoundAlertsPreferences(sekretessApplication.getApplicationContext(), sender);
        Log.i("SekretessRabbitMqService", "soundAlerts:" + soundAlerts + "sender:" + sender);
        if (!soundAlerts) {
            notification.setSilent(true);
            channel.setImportance(NotificationManager.IMPORTANCE_LOW);
        } else {
            notification.setDefaults(0);
            notification.setSilent(false);
        }
        notificationManager.createNotificationChannel(channel);


        if (ActivityCompat.checkSelfPermission(sekretessApplication.getApplicationContext(),
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(m, notification.build());
        }
    }
}
