package io.sekretess.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.sekretess.dto.MessageBriefDto;
import io.sekretess.model.MessageStoreEntity;

public class MessageRepository {
    private final DbHelper dbHelper;
    private final String TAG = MessageRepository.class.getName();

    public MessageRepository(DbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void storeDecryptedMessage(String sender, String message) {
        String username = getUserNameFromJwt();
        ContentValues values = new ContentValues();
        values.put(MessageStoreEntity.COLUMN_SENDER, sender);
        values.put(MessageStoreEntity.COLUMN_MESSAGE_BODY, message);
        values.put(MessageStoreEntity.COLUMN_USERNAME, username);
        values.put(MessageStoreEntity.COLUMN_CREATED_AT, System.currentTimeMillis());
        dbHelper.getWritableDatabase().insert(MessageStoreEntity.TABLE_NAME,
                null, values);


    }

    public List<MessageBriefDto> getMessageBriefs(String username) {
        try {

            List<MessageBriefDto> resultArray;
            try (SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();
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

}
