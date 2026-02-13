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

import org.signal.libsignal.protocol.DuplicateMessageException;
import org.signal.libsignal.protocol.InvalidKeyException;
import org.signal.libsignal.protocol.InvalidKeyIdException;
import org.signal.libsignal.protocol.InvalidMessageException;
import org.signal.libsignal.protocol.InvalidVersionException;
import org.signal.libsignal.protocol.LegacyMessageException;
import org.signal.libsignal.protocol.NoSessionException;
import org.signal.libsignal.protocol.UntrustedIdentityException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import io.sekretess.Constants;
import io.sekretess.R;
import io.sekretess.db.model.MessageEntity;
import io.sekretess.dependency.SekretessDependencyProvider;
import io.sekretess.dto.MessageBriefDto;
import io.sekretess.dto.MessageDto;
import io.sekretess.dto.MessageRecordDto;
import io.sekretess.enums.ItemType;
import io.sekretess.enums.MessageType;
import io.sekretess.db.repository.MessageRepository;
import io.sekretess.utils.NotificationPreferencesUtils;

public class SekretessMessageService {
    private final MessageRepository messageRepository;
    private final String TAG = SekretessMessageService.class.getName();

    private static final DateTimeFormatter WEEK_FORMATTER = DateTimeFormatter.ofPattern("EEEE");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM");
    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    private String dateTimeText = "";
    private final String username;

    public SekretessMessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
        String resolvedUsername = "";
        try {
            resolvedUsername = SekretessDependencyProvider.authService().getUsername();
        } catch (Exception e) {
            Log.e(TAG, "Getting username failed:", e);
        }
        this.username = resolvedUsername;
    }


    public void handleMessage(MessageDto message) throws InvalidMessageException,
            UntrustedIdentityException, DuplicateMessageException, InvalidVersionException,
            InvalidKeyIdException, LegacyMessageException, InvalidKeyException, NoSessionException {
        String sender = message.getSender();
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
    }

    private void processAdvertisementMessage(String base64Message, String sender) throws NoSessionException, InvalidMessageException, DuplicateMessageException, LegacyMessageException {

        SekretessDependencyProvider.cryptographicService()
                .decryptGroupChatMessage(sender, base64Message)
                .ifPresent(decryptedMessage -> {
                    messageRepository.storeDecryptedMessage(sender, decryptedMessage, username);
                    SekretessDependencyProvider.messageEventStream().postValue("new-message");
                    publishNotification(sender, decryptedMessage);
                });
    }

    private void processPrivateMessage(String encryptedText, String sender, MessageType messageType) throws InvalidMessageException, UntrustedIdentityException, DuplicateMessageException, InvalidVersionException, InvalidKeyIdException, LegacyMessageException, InvalidKeyException {
        SekretessCryptographicService sekretessCryptographicService = SekretessDependencyProvider.cryptographicService();
        sekretessCryptographicService
                .decryptPrivateMessage(sender, encryptedText)
                .ifPresent(decryptedMessage -> {
                    if (messageType == MessageType.KEY_DISTRIBUTION) {
                        sekretessCryptographicService.processKeyDistributionMessage(sender, decryptedMessage);
                    } else {
                        messageRepository.storeDecryptedMessage(sender, decryptedMessage, username);
                        SekretessDependencyProvider.messageEventStream().postValue("new-message");
                        publishNotification(sender, decryptedMessage);
                    }
                });
    }


    public List<MessageBriefDto> getMessageBriefs() {
        try {
            List<MessageEntity> messageStoreEntities = messageRepository.getMessages(username);
            return messageStoreEntities
                    .stream()
                    .map(messageStoreEntity ->
                            new MessageBriefDto(messageStoreEntity.getSender(),
                                    messageStoreEntity.getMessageBody()))
                    .toList();
        } catch (Exception e) {
            Log.e(TAG, "Getting MessageBriefs failed:", e);
            return new ArrayList<>();
        }
    }

    public List<String> getTopSenders() {
        return messageRepository.getTopSenders();
    }

    public List<MessageRecordDto> loadMessages(String from) {
        this.dateTimeText = "";
        return messageRepository.getMessages(username, from)
                .stream()
                .map(this::messageRecordDto)
                .collect(Collectors.toList());
    }

    public String dateTimeText(LocalDate dateTime) {

        LocalDate today = LocalDate.now();
        long daysBetween = ChronoUnit.DAYS.between(dateTime, today);
        long monthsBetween = ChronoUnit.MONTHS.between(dateTime, today);

        if (daysBetween == 0) {
            return "Today";
        }
        if (daysBetween <= 7) {
            return WEEK_FORMATTER.format(dateTime);
        } else if (monthsBetween >= 12) {
            return YEAR_FORMATTER.format(dateTime);
        } else {
            return MONTH_FORMATTER.format(dateTime);
        }
    }

    private MessageRecordDto messageRecordDto(MessageEntity messageEntity) {
        LocalDateTime messageDateTime = LocalDateTime
                .ofInstant(Instant.ofEpochMilli(messageEntity.getCreatedAt()),
                        ZoneId.systemDefault());

        String dateTimeAsText = dateTimeText(messageDateTime.toLocalDate());
        if (dateTimeText.equals(dateTimeAsText)) {
            return new MessageRecordDto(messageEntity.getId(), messageEntity.getSender(),
                    messageEntity.getMessageBody(), messageEntity.getCreatedAt(),
                    dateTimeAsText, ItemType.ITEM);
        } else {
            dateTimeText = dateTimeAsText;
            return new MessageRecordDto(messageEntity.getId(), messageEntity.getSender(),
                    messageEntity.getMessageBody(), messageEntity.getCreatedAt(),
                    dateTimeAsText, ItemType.HEADER);
        }
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

    public void insertTestData() {
        messageRepository.clearDatabase();
        messageRepository.storeDecryptedMessage("test_business", "TestMesssage1", "elnur");
        messageRepository.storeDecryptedMessage("test_business", "TestMesssage2", "elnur");
        messageRepository.storeDecryptedMessage("test_business", "TestMesssage3", "elnur");
        messageRepository.storeDecryptedMessage("test_business", "TestMesssage4", "elnur");

        messageRepository.storeDecryptedMessage("test_business1", "TestMesssage1", "elnur");
        messageRepository.storeDecryptedMessage("test_business1", "TestMesssage2", "elnur");
        messageRepository.storeDecryptedMessage("test_business1", "TestMesssage3", "elnur");
    }
}
