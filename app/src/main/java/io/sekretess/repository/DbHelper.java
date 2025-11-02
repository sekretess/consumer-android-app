package io.sekretess.repository;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


import com.auth0.android.jwt.JWT;

import io.sekretess.Constants;
import io.sekretess.cryptography.storage.SekretessSignalProtocolStore;
import io.sekretess.dto.GroupChatDto;
import io.sekretess.dto.MessageRecordDto;
import io.sekretess.enums.ItemType;
import io.sekretess.model.AuthStateStoreEntity;
import io.sekretess.model.GroupChatEntity;
import io.sekretess.model.IdentityKeyEntity;
import io.sekretess.model.IdentityKeyPairStoreEntity;
import io.sekretess.model.JwtStoreEntity;
import io.sekretess.model.KyberPreKeyRecordsEntity;
import io.sekretess.model.MessageStoreEntity;
import io.sekretess.model.PreKeyRecordStoreEntity;
import io.sekretess.model.RegistrationIdStoreEntity;
import io.sekretess.model.SenderKeyEntity;
import io.sekretess.model.SessionStoreEntity;
import io.sekretess.model.SignedPreKeyRecordStoreEntity;

import net.openid.appauth.AuthState;

import org.signal.libsignal.protocol.SignalProtocolAddress;
import org.signal.libsignal.protocol.groups.state.SenderKeyRecord;
import org.signal.libsignal.protocol.state.SessionRecord;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DbHelper extends SQLiteOpenHelper {
    public static final DateTimeFormatter dateTimeFormatter
            = DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter WEEK_FORMATTER = DateTimeFormatter.ofPattern("EEEE");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM");
    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    public static final int DATABASE_VERSION = 19;
    public static final String DATABASE_NAME = "sekretessencrypt.db";
    public static final Base64.Encoder base64Encoder = Base64.getEncoder();
    public static final Base64.Decoder base64Decoder = Base64.getDecoder();
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();


    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public boolean clearKeyData() {
        try {
            return executorService.submit(() -> {
                try (SQLiteDatabase db = getWritableDatabase()) {
                    db.beginTransaction();
                    db.delete(IdentityKeyPairStoreEntity.TABLE_NAME, null, null);
                    db.delete(RegistrationIdStoreEntity.TABLE_NAME, null, null);
                    db.delete(SignedPreKeyRecordStoreEntity.TABLE_NAME, null, null);
                    db.delete(PreKeyRecordStoreEntity.TABLE_NAME, null, null);
                    db.delete(KyberPreKeyRecordsEntity.TABLE_NAME, null, null);
                    db.setTransactionSuccessful();
                    db.endTransaction();
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }).get();
        } catch (Exception e) {
            Log.e("DbHelper", "Error occurred during clear key data", e);
            return false;
        }
    }

    public boolean clearUserData() {
        try {
            return executorService.submit(() -> {
                try (SQLiteDatabase db = getWritableDatabase()) {
                    db.beginTransaction();
                    db.delete(MessageStoreEntity.TABLE_NAME, null, null);
                    db.delete(IdentityKeyPairStoreEntity.TABLE_NAME, null, null);
                    db.delete(RegistrationIdStoreEntity.TABLE_NAME, null, null);
                    db.delete(SignedPreKeyRecordStoreEntity.TABLE_NAME, null, null);
                    db.delete(PreKeyRecordStoreEntity.TABLE_NAME, null, null);
                    db.delete(JwtStoreEntity.TABLE_NAME, null, null);
                    db.delete(SessionStoreEntity.TABLE_NAME, null, null);
                    db.delete(AuthStateStoreEntity.TABLE_NAME, null, null);
                    db.delete(KyberPreKeyRecordsEntity.TABLE_NAME, null, null);
                    db.delete(SenderKeyEntity.TABLE_NAME, null, null);
                    db.setTransactionSuccessful();
                    db.endTransaction();
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }).get();
        } catch (Exception e) {
            Log.e("DbHelper", "Error occurred during clear user data", e);
            return false;
        }

    }

    public void logout() {

        executorService.submit(() -> {
            try (SQLiteDatabase db = getWritableDatabase()) {
                db.beginTransaction();
                db.delete(AuthStateStoreEntity.TABLE_NAME, null, null);
                db.setTransactionSuccessful();
                db.endTransaction();
            }
        });
    }

    public void storeAuthState(String authState) {
        ContentValues values = new ContentValues();
        values.put(AuthStateStoreEntity.COLUMN_AUTH_STATE, authState);

        executorService.submit(() -> {
            getWritableDatabase().delete(AuthStateStoreEntity.TABLE_NAME, null, null);
            getWritableDatabase().insert(AuthStateStoreEntity.TABLE_NAME, null, values);

        });

    }

    public void removeAuthState() {
        executorService.submit(() -> {
            getWritableDatabase().delete(AuthStateStoreEntity.TABLE_NAME, null, null);
        });
    }

    @SuppressLint("Range")
    public AuthState getAuthState() {
        try {
            return executorService.submit(() -> {
                Cursor result = null;
                SQLiteDatabase db = null;
                try {
                    db = getWritableDatabase();
                    result = db
                            .query(AuthStateStoreEntity.TABLE_NAME,
                                    null,
                                    null, null, null, null, null);
                    if (result.moveToNext()) {
                        return AuthState.jsonDeserialize(result
                                .getString(result.getColumnIndex(AuthStateStoreEntity.COLUMN_AUTH_STATE)));
                    }
                } catch (Throwable e) {
                    Log.e("DbHelper", "Getting AuthState failed", e);
                    return null;
                } finally {
                    if (result != null) {
                        result.close();
                    }
                    if (db != null) {
                        db.close();
                    }
                }
                return null;
            }).get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.i("DbHelper", "Getting AuthState failed", e);
            return null;
        }

    }








    public void storeGroupChatInfo(String distributionKey, String sender) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(GroupChatEntity.COLUMN_SENDER, sender);
        contentValues.put(GroupChatEntity.COLUMN_DISTRIBUTION_KEY, distributionKey);
        executorService.submit(() -> {
            getWritableDatabase().replace(GroupChatEntity.TABLE_NAME, null, contentValues);
        });
    }

    public List<GroupChatDto> getGroupChatsInfo() {
        try {
            return executorService.submit(() -> {
                List<GroupChatDto> groupChatsInfo = new ArrayList<>();
                try (Cursor cursor = getReadableDatabase()
                        .query(GroupChatEntity.TABLE_NAME, new String[]{
                                GroupChatEntity.COLUMN_SENDER, GroupChatEntity.COLUMN_DISTRIBUTION_KEY
                        }, null, null, null, null, null)) {

                    while (cursor.moveToNext()) {
                        String sender = cursor.getString(0);
                        String distributionKey = cursor.getString(1);
                        groupChatsInfo.add(new GroupChatDto(sender, distributionKey));
                    }
                }
                return groupChatsInfo;
            }).get();
        } catch (Exception e) {
            Log.e("DbHelper", "Getting GroupChatsInfo failed", e);
            return Collections.emptyList();
        }
    }

    public List<String> getTopSenders() {
        try {
            return executorService.submit(() -> {
                try (SQLiteDatabase db = getWritableDatabase(); Cursor resultCursor = db
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
            }).get();
        } catch (Exception e) {
            Log.e("DbHelper", "Getting top senders failed", e);
            return Collections.emptyList();
        }
    }

    public List<MessageRecordDto> loadMessages(String from) {
        try {
            return executorService.submit(() -> {

                try (Cursor resultCursor = getReadableDatabase()
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
            }).get();

        } catch (Exception e) {
            Log.e("DbHelper", "Getting messages failed", e);
            return Collections.emptyList();
        }
    }

    public static String dateTimeText(LocalDate dateTime) {

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

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("DbHelper", "OnCreate called. Creating tables");
        db.execSQL(MessageStoreEntity.SQL_CREATE_TABLE);
        db.execSQL(IdentityKeyPairStoreEntity.SQL_CREATE_TABLE);
        db.execSQL(RegistrationIdStoreEntity.SQL_CREATE_TABLE);
        db.execSQL(SignedPreKeyRecordStoreEntity.SQL_CREATE_TABLE);
        db.execSQL(PreKeyRecordStoreEntity.SQL_CREATE_TABLE);
        db.execSQL(JwtStoreEntity.SQL_CREATE_TABLE);
        db.execSQL(SessionStoreEntity.SQL_CREATE_TABLE);
        db.execSQL(AuthStateStoreEntity.SQL_CREATE_TABLE);
        db.execSQL(KyberPreKeyRecordsEntity.SQL_CREATE_TABLE);
        db.execSQL(SenderKeyEntity.SQL_CREATE_TABLE);
        db.execSQL(GroupChatEntity.SQL_CREATE_TABLE);
        db.execSQL(IdentityKeyEntity.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(MessageStoreEntity.SQL_DROP_TABLE);
        db.execSQL(IdentityKeyPairStoreEntity.SQL_DROP_TABLE);
        db.execSQL(RegistrationIdStoreEntity.SQL_DROP_TABLE);
        db.execSQL(SignedPreKeyRecordStoreEntity.SQL_DROP_TABLE);
        db.execSQL(PreKeyRecordStoreEntity.SQL_DROP_TABLE);
        db.execSQL(JwtStoreEntity.SQL_DROP_TABLE);
        db.execSQL(SessionStoreEntity.SQL_DROP_TABLE);
        db.execSQL(AuthStateStoreEntity.SQL_DROP_TABLE);
        db.execSQL(KyberPreKeyRecordsEntity.SQL_DROP_TABLE);
        db.execSQL(SenderKeyEntity.SQL_DROP_TABLE);
        db.execSQL(GroupChatEntity.SQL_DROP_TABLE);
        db.execSQL(IdentityKeyEntity.SQL_DROP_TABLE);


        Log.i("DbHelper", "OnUpgrade called. Creating tables");
        db.execSQL(MessageStoreEntity.SQL_CREATE_TABLE);
        db.execSQL(IdentityKeyPairStoreEntity.SQL_CREATE_TABLE);
        db.execSQL(RegistrationIdStoreEntity.SQL_CREATE_TABLE);
        db.execSQL(SignedPreKeyRecordStoreEntity.SQL_CREATE_TABLE);
        db.execSQL(PreKeyRecordStoreEntity.SQL_CREATE_TABLE);
        db.execSQL(JwtStoreEntity.SQL_CREATE_TABLE);
        db.execSQL(SessionStoreEntity.SQL_CREATE_TABLE);
        db.execSQL(AuthStateStoreEntity.SQL_CREATE_TABLE);
        db.execSQL(KyberPreKeyRecordsEntity.SQL_CREATE_TABLE);
        db.execSQL(SenderKeyEntity.SQL_CREATE_TABLE);
        db.execSQL(GroupChatEntity.SQL_CREATE_TABLE);
        db.execSQL(IdentityKeyEntity.SQL_CREATE_TABLE);
    }

    public String getUserNameFromJwt() {
        return new JWT(getAuthState().getIdToken()).getClaim(Constants.USERNAME_CLAIM).asString();
    }

    public void deleteMessage(Long messageId) {
        executorService.submit(() -> {
            try (SQLiteDatabase db = getWritableDatabase()) {
                db.delete(MessageStoreEntity.TABLE_NAME,
                        MessageStoreEntity._ID + "=?", new String[]{String.valueOf(messageId)});
            }
        });

    }
}
