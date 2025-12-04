package io.sekretess.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.sekretess.dto.MessageBriefDto;
import io.sekretess.dto.MessageRecordDto;
import io.sekretess.enums.ItemType;
import io.sekretess.model.MessageStoreEntity;

public class MessageRepository {
    private final SekretessDatabase sekretessDatabase;
    private final String TAG = MessageRepository.class.getName();
    private static final DateTimeFormatter WEEK_FORMATTER = DateTimeFormatter.ofPattern("EEEE");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM");
    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    public MessageRepository(SekretessDatabase sekretessDatabase) {
        this.sekretessDatabase = sekretessDatabase;
    }

    public void storeDecryptedMessage(String sender, String message, String username) {
        ContentValues values = new ContentValues();
        values.put(MessageStoreEntity.COLUMN_SENDER, sender);
        values.put(MessageStoreEntity.COLUMN_MESSAGE_BODY, message);
        values.put(MessageStoreEntity.COLUMN_USERNAME, username);
        values.put(MessageStoreEntity.COLUMN_CREATED_AT, System.currentTimeMillis());
        sekretessDatabase.getWritableDatabase().insert(MessageStoreEntity.TABLE_NAME,
                null, values);
    }

    public List<MessageBriefDto> getMessageBriefs(String username) {
        try {
            List<MessageBriefDto> resultArray;
            try (SQLiteDatabase sqLiteDatabase = sekretessDatabase.getWritableDatabase();
                 Cursor resultCursor = sqLiteDatabase
                         .query(MessageStoreEntity.TABLE_NAME,
                                 new String[]{MessageStoreEntity.COLUMN_SENDER,
                                         MessageStoreEntity.COLUMN_MESSAGE_BODY},
                                 MessageStoreEntity.COLUMN_USERNAME + "=?",
                                 new String[]{username},
                                 null,
                                 null,
                                 MessageStoreEntity.COLUMN_CREATED_AT + " DESC",
                                 "1")) {
                sqLiteDatabase.disableWriteAheadLogging();
                resultArray = new ArrayList<>();

                while (resultCursor.moveToNext()) {
                    String senderName = resultCursor.getString(0);
                    String messageText = resultCursor.getString(1);
                    resultArray.add(new MessageBriefDto(senderName, messageText));
                }
                return resultArray;
            } catch (Throwable e) {
                Log.e(TAG, "Getting MessageBriefs failed", e);
                return new ArrayList<MessageBriefDto>();
            }
        } catch (Exception e) {
            Log.e(TAG, "Getting MessageBriefs failed:", e);
            return new ArrayList<>();
        }
    }

    public List<String> getTopSenders() {
        try {
            try (SQLiteDatabase db = sekretessDatabase.getWritableDatabase(); Cursor resultCursor = db
                    .query(MessageStoreEntity.TABLE_NAME, new String[]{
                                    MessageStoreEntity.COLUMN_SENDER
                            }, null, null, null, null,
                            MessageStoreEntity.COLUMN_CREATED_AT + " DESC", "4")) {
                List<String> topSenders = new ArrayList<>();
                while (resultCursor.moveToNext()) {
                    topSenders.add(resultCursor.getString(0));
                }
                return topSenders;
            }
        } catch (Exception e) {
            Log.e("DbHelper", "Getting top senders failed", e);
            return Collections.emptyList();
        }
    }


    public List<MessageRecordDto> loadMessages(String from) {
        try {
            try (Cursor resultCursor = sekretessDatabase.getReadableDatabase()
                    .query(MessageStoreEntity.TABLE_NAME, new String[]{MessageStoreEntity._ID,
                                    MessageStoreEntity.COLUMN_SENDER,
                                    MessageStoreEntity.COLUMN_MESSAGE_BODY,
                                    MessageStoreEntity.COLUMN_CREATED_AT
                            },
                            "sender=?",
                            new String[]{from}, null, null, "created_at ASC")) {
                List<MessageRecordDto> resultArray = new ArrayList<>();
                String dateTimeText = "";
                while (resultCursor.moveToNext()) {

                    Long id = resultCursor.getLong(0);
                    String sender = resultCursor.getString(1);
                    String messageBody = resultCursor.getString(2);
                    long createdAt = resultCursor.getLong(3);

                    LocalDateTime messageDateTime = LocalDateTime
                            .ofInstant(Instant.ofEpochMilli(createdAt),
                                    ZoneId.systemDefault());

                    String dateTimeAsText = dateTimeText(messageDateTime.toLocalDate());
                    if (dateTimeText.equals(dateTimeAsText)) {
                        resultArray.add(new MessageRecordDto(id, sender, messageBody, createdAt,
                                dateTimeAsText, ItemType.ITEM));
                    } else {
                        resultArray.add(new MessageRecordDto(id, sender, null, createdAt,
                                dateTimeAsText, ItemType.HEADER));
//                            resultArray.add(new MessageRecordDto(id, sender, messageBody, createdAt,
//                                    dateTimeAsText, ItemType.ITEM));
                        dateTimeText = dateTimeAsText;
                    }
                }
                return resultArray;
            }
        } catch (Exception e) {
            Log.e("DbHelper", "Getting messages failed", e);
            return Collections.emptyList();
        }
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

    public void deleteMessage(Long messageId) {
        SQLiteDatabase db = sekretessDatabase.getWritableDatabase();
        db.delete(MessageStoreEntity.TABLE_NAME,
                MessageStoreEntity._ID + "=?", new String[]{String.valueOf(messageId)});
    }
}
