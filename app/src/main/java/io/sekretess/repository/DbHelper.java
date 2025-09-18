package io.sekretess.repository;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


import com.auth0.android.jwt.JWT;

import io.sekretess.Constants;
import io.sekretess.dto.GroupChatDto;
import io.sekretess.dto.MessageBriefDto;
import io.sekretess.dto.MessageRecordDto;
import io.sekretess.dto.RegistrationAndDeviceId;
import io.sekretess.enums.ItemType;
import io.sekretess.model.AuthStateStoreEntity;
import io.sekretess.model.GroupChatEntity;
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

import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONException;
import org.signal.libsignal.protocol.IdentityKeyPair;
import org.signal.libsignal.protocol.InvalidMessageException;
import org.signal.libsignal.protocol.SignalProtocolAddress;
import org.signal.libsignal.protocol.groups.state.SenderKeyRecord;
import org.signal.libsignal.protocol.state.KyberPreKeyRecord;
import org.signal.libsignal.protocol.state.PreKeyRecord;
import org.signal.libsignal.protocol.state.SessionRecord;
import org.signal.libsignal.protocol.state.SignalProtocolStore;
import org.signal.libsignal.protocol.state.SignedPreKeyRecord;

import java.io.File;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DbHelper extends SQLiteOpenHelper {
    private final DateTimeFormatter dateTimeFormatter
            = DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter WEEK_FORMATTER = DateTimeFormatter.ofPattern("EEEE");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM");
    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    public static final int DATABASE_VERSION = 19;
    public static final String DATABASE_NAME = "sekretessencrypt.db";
    private static final Base64.Encoder base64Encoder = Base64.getEncoder();
    private static final Base64.Decoder base64Decoder = Base64.getDecoder();
    private Context mContext;
    private static volatile DbHelper mInstance;
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
//        SQLiteDatabase.loadLibs(this.mContext);
        try {
//            String p = p();
//            Log.i("DbHelper", "db path: " + mContext.getDatabasePath(DATABASE_NAME));

//            SQLiteDatabase.openOrCreateDatabase(mContext.getDatabasePath(DATABASE_NAME), null);
            Log.i("DbHelper", "DbCreated");
        } catch (Exception e) {
            Log.e("DBHelper", "Db initialization failed. ", e);
        }
    }

//    public static synchronized DbHelper getInstance(Context context) {
//        synchronized (DbHelper.class) {
//            if (mInstance == null) {
//                mInstance = new DbHelper(context.getApplicationContext());
//            }
//            return mInstance;
//        }
//    }

    @SuppressLint("Range")
    public IdentityKeyPair getIdentityKeyPair() {
        try {
            return executorService.submit(() -> {
                Cursor cursor = null;
                try {

                    cursor = getWritableDatabase().query(IdentityKeyPairStoreEntity.TABLE_NAME,
                            new String[]{IdentityKeyPairStoreEntity._ID, IdentityKeyPairStoreEntity.COLUMN_IKP},
                            null, null, null, null, null);
                    if (cursor.moveToNext()) {
                        String ikp = cursor.getString(cursor.getColumnIndex(IdentityKeyPairStoreEntity.COLUMN_IKP));
                        return new IdentityKeyPair(base64Decoder.decode(ikp));
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
                return null;
            }).get();
        } catch (Exception e) {
            Log.e("DbHelper", "Error occurred during get ikp from database", e);
            return null;
        }
    }

    public void storeIdentityKeyPair(IdentityKeyPair identityKeyPair) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(IdentityKeyPairStoreEntity.COLUMN_IKP,
                base64Encoder.encodeToString(identityKeyPair.serialize()));
        contentValues.put(IdentityKeyPairStoreEntity.COLUMN_CREATED_AT, dateTimeFormatter.format(Instant.now()));

        executorService.submit(() -> {
            getWritableDatabase().insert(IdentityKeyPairStoreEntity.TABLE_NAME, null, contentValues);
        });


    }

    @SuppressLint("Range")
    public RegistrationAndDeviceId getRegistrationId() {
        try {
            return executorService.submit(() -> {
                Cursor cursor = null;
                try {
                    cursor = getReadableDatabase().query(RegistrationIdStoreEntity.TABLE_NAME,
                            new String[]{RegistrationIdStoreEntity._ID, RegistrationIdStoreEntity.COLUMN_REG_ID, RegistrationIdStoreEntity.COLUMN_DEVICE_ID},
                            null, null, null, null, null);
                    if (cursor.getCount() == 0) {
                        Log.e("DbHelper", " No Registration id found");
                    }
                    if (cursor.moveToNext()) {
                        String[] columnNames = cursor.getColumnNames();
                        for (String columnName : columnNames) {
                            Log.i("DbHelper", "ColumnName: " + columnName);
                        }
                        return new RegistrationAndDeviceId(cursor
                                .getInt(cursor.getColumnIndex(RegistrationIdStoreEntity.COLUMN_REG_ID)),
                                cursor.getInt(cursor.getColumnIndex(RegistrationIdStoreEntity.COLUMN_DEVICE_ID)));
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
                return null;
            }).get();
        } catch (Exception e) {
            Log.e("DbHelper", "Error occurred during get registration id from database", e);
            return null;
        }
    }

    public void storeRegistrationId(Integer registrationId, int deviceId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(RegistrationIdStoreEntity.COLUMN_REG_ID, registrationId);
        contentValues.put(RegistrationIdStoreEntity.COLUMN_DEVICE_ID, deviceId);
        contentValues.put(RegistrationIdStoreEntity.COLUMN_CREATED_AT, dateTimeFormatter.format(Instant.now()));
        executorService.submit(() -> {
            getWritableDatabase().insert(RegistrationIdStoreEntity.TABLE_NAME, null, contentValues);
        });


    }

    @SuppressLint("Range")
    public SignedPreKeyRecord getSignedPreKeyRecord() {
        try {
            return executorService.submit(() -> {
                Cursor cursor = null;
                try {
                    cursor = getReadableDatabase().query(SignedPreKeyRecordStoreEntity.TABLE_NAME,
                            new String[]{SignedPreKeyRecordStoreEntity._ID, SignedPreKeyRecordStoreEntity.COLUMN_SPK_RECORD},
                            null, null, null, null, null);
                    if (cursor.moveToNext()) {
                        try {
                            return new SignedPreKeyRecord(base64Decoder.decode(cursor.getString(cursor
                                    .getColumnIndex(SignedPreKeyRecordStoreEntity.COLUMN_SPK_RECORD))));
                        } catch (Exception e) {
                            Log.e("DbHelper", "Error occurred during get spk from database");
                            return null;
                        }
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
                return null;
            }).get();
        } catch (Exception e) {
            Log.e("DbHelper", "Error occurred during get spk from database", e);
            return null;
        }
    }

    public void storeSignedPreKeyRecord(SignedPreKeyRecord signedPreKeyRecord) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SignedPreKeyRecordStoreEntity.COLUMN_SPK_RECORD, base64Encoder
                .encodeToString(signedPreKeyRecord.serialize()));
        contentValues.put(SignedPreKeyRecordStoreEntity.COLUMN_CREATED_AT, dateTimeFormatter.format(Instant.now()));
        executorService.submit(() -> {
            getWritableDatabase().insert(SignedPreKeyRecordStoreEntity.TABLE_NAME, null, contentValues);
        });
    }

    public boolean clearKeyData() {
        try {
            return executorService.submit(() -> {
                try {
                    getWritableDatabase().beginTransaction();
                    getWritableDatabase().delete(IdentityKeyPairStoreEntity.TABLE_NAME, null, null);
                    getWritableDatabase().delete(RegistrationIdStoreEntity.TABLE_NAME, null, null);
                    getWritableDatabase().delete(SignedPreKeyRecordStoreEntity.TABLE_NAME, null, null);
                    getWritableDatabase().delete(PreKeyRecordStoreEntity.TABLE_NAME, null, null);
                    getWritableDatabase().delete(KyberPreKeyRecordsEntity.TABLE_NAME, null, null);
                    getWritableDatabase().setTransactionSuccessful();
                    return true;
                } catch (Exception e) {
                    return false;
                } finally {
                    getWritableDatabase().endTransaction();
//            db.close();
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
                try {
                    getWritableDatabase().beginTransaction();
                    getWritableDatabase().delete(MessageStoreEntity.TABLE_NAME, null, null);
                    getWritableDatabase().delete(IdentityKeyPairStoreEntity.TABLE_NAME, null, null);
                    getWritableDatabase().delete(RegistrationIdStoreEntity.TABLE_NAME, null, null);
                    getWritableDatabase().delete(SignedPreKeyRecordStoreEntity.TABLE_NAME, null, null);
                    getWritableDatabase().delete(PreKeyRecordStoreEntity.TABLE_NAME, null, null);
                    getWritableDatabase().delete(JwtStoreEntity.TABLE_NAME, null, null);
                    getWritableDatabase().delete(SessionStoreEntity.TABLE_NAME, null, null);
                    getWritableDatabase().delete(AuthStateStoreEntity.TABLE_NAME, null, null);
                    getWritableDatabase().delete(KyberPreKeyRecordsEntity.TABLE_NAME, null, null);
                    getWritableDatabase().delete(SenderKeyEntity.TABLE_NAME, null, null);
                    getWritableDatabase().setTransactionSuccessful();
                    return true;
                } catch (Exception e) {
                    return false;
                } finally {
                    getWritableDatabase().endTransaction();
//            db.close();
                }
            }).get();
        } catch (Exception e) {
            Log.e("DbHelper", "Error occurred during clear user data", e);
            return false;
        }

    }

    public void logout() {

        executorService.submit(() -> {
            try {
                getWritableDatabase().beginTransaction();
                getWritableDatabase().delete(AuthStateStoreEntity.TABLE_NAME, null, null);
                getWritableDatabase().setTransactionSuccessful();
            } finally {
                getWritableDatabase().endTransaction();
//            db.close();
            }
        });
    }

    @SuppressLint("Range")
    public void loadPreKeyRecords(SignalProtocolStore signalProtocolStore) throws InvalidMessageException {
        executorService.submit(() -> {
            Cursor cursor = null;
            try {
                cursor = getReadableDatabase().query(PreKeyRecordStoreEntity.TABLE_NAME,
                        new String[]{PreKeyRecordStoreEntity._ID, PreKeyRecordStoreEntity.COLUMN_PREKEY_RECORD},
                        null, null, null, null, null);
                while (cursor.moveToNext()) {
                    PreKeyRecord preKeyRecord = new PreKeyRecord(base64Decoder.decode(cursor
                            .getString(cursor.getColumnIndex(PreKeyRecordStoreEntity.COLUMN_PREKEY_RECORD))));
                    signalProtocolStore.storePreKey(preKeyRecord.getId(), preKeyRecord);
                }
            } catch (InvalidMessageException e) {
                throw new RuntimeException(e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        });
    }

    public void removePreKeyRecord(int prekeyId) {
        executorService.submit(() -> {
            getWritableDatabase().delete(PreKeyRecordStoreEntity.TABLE_NAME,
                    PreKeyRecordStoreEntity.COLUMN_PREKEY_ID + "=?", new String[]{String.valueOf(prekeyId)});
        });

    }

    public void storePreKeyRecord(PreKeyRecord preKeyRecord) {
        executorService.submit(() -> {
            ContentValues contentValues = new ContentValues();
            contentValues.put(PreKeyRecordStoreEntity.COLUMN_PREKEY_ID, preKeyRecord.getId());
            contentValues.put(PreKeyRecordStoreEntity.COLUMN_PREKEY_RECORD,
                    base64Encoder.encodeToString(preKeyRecord.serialize()));
            contentValues.put(PreKeyRecordStoreEntity.COLUMN_CREATED_AT,
                    dateTimeFormatter.format(Instant.now()));
            getWritableDatabase().insert(PreKeyRecordStoreEntity.TABLE_NAME, null, contentValues);
        });

    }

    public void markKyberPreKeyUsed(int kyberPreKeyId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KyberPreKeyRecordsEntity.COLUMN_USED, 1);
        executorService.submit(() -> {
            getWritableDatabase().updateWithOnConflict(KyberPreKeyRecordsEntity.TABLE_NAME,
                    contentValues, KyberPreKeyRecordsEntity.COLUMN_PREKEY_ID + "=?", new String[]{String.valueOf(kyberPreKeyId)}, SQLiteDatabase.CONFLICT_REPLACE);
        });


    }

    public void storeKyberPreKey(KyberPreKeyRecord kyberPreKeyRecord) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KyberPreKeyRecordsEntity.COLUMN_PREKEY_ID, kyberPreKeyRecord.getId());
        contentValues.put(KyberPreKeyRecordsEntity.COLUMN_KPK_RECORD,
                base64Encoder.encodeToString(kyberPreKeyRecord.serialize()));
        contentValues.put(KyberPreKeyRecordsEntity.COLUMN_USED, 0);
        contentValues.put(KyberPreKeyRecordsEntity.COLUMN_CREATED_AT,
                dateTimeFormatter.format(Instant.now()));
        executorService.submit(() -> {
            getWritableDatabase().insert(KyberPreKeyRecordsEntity.TABLE_NAME, null, contentValues);
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


    public void storeDecryptedMessage(String sender, String message) {
        String username = getUserNameFromJwt();
        ContentValues values = new ContentValues();
        values.put(MessageStoreEntity.COLUMN_SENDER, sender);
        values.put(MessageStoreEntity.COLUMN_MESSAGE_BODY, message);
        values.put(MessageStoreEntity.COLUMN_USERNAME, username);
        values.put(MessageStoreEntity.COLUMN_CREATED_AT, System.currentTimeMillis());
        executorService.submit(() -> {
            getWritableDatabase().insert(MessageStoreEntity.TABLE_NAME,
                    null, values);
        });

    }

    public List<MessageBriefDto> getMessageBriefs(String username) {
        try {
            return executorService.submit(() -> {
                List<MessageBriefDto> resultArray;
                Cursor resultCursor = null;
                SQLiteDatabase sqLiteDatabase = null;
                try {
                    sqLiteDatabase = getWritableDatabase();
                    sqLiteDatabase.disableWriteAheadLogging();
                    resultCursor = sqLiteDatabase
                            .query(MessageStoreEntity.TABLE_NAME,
                                    new String[]{MessageStoreEntity.COLUMN_SENDER,
                                            MessageStoreEntity.COLUMN_MESSAGE_BODY},
                                    MessageStoreEntity.COLUMN_USERNAME + "=?",
                                    new String[]{username},
                                    null,
                                    null,
                                    MessageStoreEntity.COLUMN_CREATED_AT + " DESC",
                                    "1"
                            );
                    resultArray = new ArrayList<>();

                    while (resultCursor.moveToNext()) {
                        String senderName = resultCursor.getString(0);
                        String messageText = resultCursor.getString(1);
                        resultArray.add(new MessageBriefDto(senderName, messageText));
                    }
                    return resultArray;
                } catch (Throwable e) {
                    Log.e("DbHelper", "Getting MessageBriefs failed", e);
                    return new ArrayList<MessageBriefDto>();
                } finally {
                    if (resultCursor != null) {
                        resultCursor.close();
                    }
                    if (sqLiteDatabase != null) {
                        sqLiteDatabase.close();
                    }
                }

            }).get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e("DbHelper", "Getting MessageBriefs failed:", e);
            return new ArrayList<MessageBriefDto>();
        }
    }

    public void storeSenderKey(SignalProtocolAddress sender, UUID distributionId, SenderKeyRecord record) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SenderKeyEntity.COLUMN_ADDRESS_NAME, sender.getName());
        contentValues.put(SenderKeyEntity.COLUMN_ADDRESS_DEVICE_ID, sender.getDeviceId());
        contentValues.put(SenderKeyEntity.COLUMN_DISTRIBUTION_UUID, distributionId.toString());
        contentValues.put(SenderKeyEntity.COLUMN_SENDER_KEY_RECORD, base64Encoder.encodeToString(record.serialize()));
        executorService.submit(() -> {
            getWritableDatabase().insert(SenderKeyEntity.TABLE_NAME, null, contentValues);
        });

    }

    public void storeSession(SignalProtocolAddress address, SessionRecord sessionRecord) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SessionStoreEntity.COLUMN_ADDRESS_NAME, address.getName());
        contentValues.put(SessionStoreEntity.COLUMN_ADDRESS_DEVICE_ID, address.getDeviceId());
        if (address.getServiceId() != null) {
            contentValues.put(SessionStoreEntity.COLUMN_SERVICE_ID,
                    base64Encoder.encodeToString(address.getServiceId().toServiceIdBinary()));
        }
        contentValues.put(SessionStoreEntity.COLUMN_SESSION,
                base64Encoder.encodeToString(sessionRecord.serialize()));
        executorService.submit(() -> {
            getWritableDatabase().insert(SessionStoreEntity.TABLE_NAME, null, contentValues);
        });

    }

    @SuppressLint("Range")
    public void loadKyberPreKeys(SekretessSignalProtocolStore signalProtocolStore) throws InvalidMessageException {

        executorService.submit(() -> {
            Cursor result = null;
            try {
                result = getReadableDatabase()
                        .query(KyberPreKeyRecordsEntity.TABLE_NAME, new String[]{
                                        KyberPreKeyRecordsEntity.COLUMN_PREKEY_ID, KyberPreKeyRecordsEntity.COLUMN_KPK_RECORD,
                                        KyberPreKeyRecordsEntity.COLUMN_USED},
                                null, null, null, null, null);
                while (result.moveToNext()) {
                    int prekeyId = result.getInt(result.getColumnIndex(KyberPreKeyRecordsEntity.COLUMN_PREKEY_ID));
                    String kpkRecordBase64 = result.getString(result.getColumnIndex(KyberPreKeyRecordsEntity.COLUMN_KPK_RECORD));
                    int used = result.getInt(result.getColumnIndex(KyberPreKeyRecordsEntity.COLUMN_USED));

                    signalProtocolStore.loadKyberPreKey(prekeyId, new KyberPreKeyRecord(base64Decoder
                            .decode(kpkRecordBase64)), used == 1);
                }
            } catch (Exception e) {
                Log.e("DbHelper", "Error occurred during load kyber prekeys", e);
            } finally {
                if (result != null) {
                    result.close();
                }
            }
        });

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
                Cursor resultCursor = null;
                try {
                    resultCursor = getReadableDatabase()
                            .query(GroupChatEntity.TABLE_NAME, new String[]{
                                    GroupChatEntity.COLUMN_SENDER, GroupChatEntity.COLUMN_DISTRIBUTION_KEY
                            }, null, null, null, null, null);
                    while (resultCursor.moveToNext()) {
                        String sender = resultCursor.getString(0);
                        String distributionKey = resultCursor.getString(1);
                        groupChatsInfo.add(new GroupChatDto(sender, distributionKey));
                    }
                } finally {
                    if (resultCursor != null) {
                        resultCursor.close();
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
                Cursor resultCursor = null;
                SQLiteDatabase db = getWritableDatabase();
                try {
                    resultCursor = db
                            .query(MessageStoreEntity.TABLE_NAME, new String[]{
                                            MessageStoreEntity.COLUMN_SENDER
                                    }, null, null, null, null,
                                    MessageStoreEntity.COLUMN_CREATED_AT + " DESC", "4");
                    List<String> topSenders = new ArrayList<>();
                    while (resultCursor.moveToNext()) {
                        topSenders.add(resultCursor.getString(0));
                    }
                    return topSenders;
                } finally {
                    if (resultCursor != null) {
                        resultCursor.close();
                    }
                    if (db != null) {
                        db.close();
                    }
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
                Cursor resultCursor = null;
                try {
                    resultCursor = getReadableDatabase()
                            .query(MessageStoreEntity.TABLE_NAME, new String[]{MessageStoreEntity._ID,
                                            MessageStoreEntity.COLUMN_SENDER,
                                            MessageStoreEntity.COLUMN_MESSAGE_BODY,
                                            MessageStoreEntity.COLUMN_CREATED_AT
                                    },
                                    "sender=?",
                                    new String[]{from}, null, null, null);
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
                            resultArray.add(new MessageRecordDto(id, sender, messageBody, createdAt,
                                    dateTimeAsText, ItemType.ITEM));
                            dateTimeText = dateTimeAsText;
                        }

                    }

                    return resultArray;
                } finally {
                    if (resultCursor != null) {
                        resultCursor.close();
                    }
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

    @SuppressLint("Range")
    public void loadSessions(SekretessSignalProtocolStore signalProtocolStore) {
        executorService.submit(() -> {
            Cursor result = null;
            try {
                result = getReadableDatabase()
                        .query(SessionStoreEntity.TABLE_NAME, new String[]{SessionStoreEntity.COLUMN_SESSION,
                                        SessionStoreEntity.COLUMN_SERVICE_ID, SessionStoreEntity.COLUMN_ADDRESS_NAME,
                                        SessionStoreEntity.COLUMN_ADDRESS_DEVICE_ID}, null, null,
                                null, null, null);
                while (result.moveToNext()) {
                    String name = result.getString(result.getColumnIndex(SessionStoreEntity.COLUMN_ADDRESS_NAME));
                    int deviceId = result.getInt(result.getColumnIndex(SessionStoreEntity.COLUMN_ADDRESS_DEVICE_ID));

                    byte[] sessionRecord = base64Decoder
                            .decode(result.getString(result.getColumnIndex(SessionStoreEntity.COLUMN_SESSION)));
                    try {
                        signalProtocolStore.loadSession(new SignalProtocolAddress(name, deviceId),
                                new SessionRecord(sessionRecord));
                    } catch (Exception e) {
                        Log.e("DbHelper", "Error occurred during load session. " +
                                "DeviceId = " + deviceId + " DeviceName = " + name, e);
                    }
                }
            } finally {
                if (result != null) {
                    result.close();
                }
            }
        });
    }

    public void removeSession(SignalProtocolAddress address) {
        executorService.submit(() -> {
            getWritableDatabase().delete(SessionStoreEntity.TABLE_NAME,
                    SessionStoreEntity.COLUMN_ADDRESS_NAME + "=? AND"
                            + SessionStoreEntity.COLUMN_ADDRESS_DEVICE_ID + " = ?",
                    new String[]{address.getName(), String.valueOf(address.getDeviceId())});

        });

    }

    private String p() {

        SharedPreferences encryptedSharedPreferences =
                mContext.getSharedPreferences("secret_shared_prefs", Context.MODE_PRIVATE);

        return encryptedSharedPreferences
                .getString("801d0837-c9c3-4a4c-bfcc-67197551d030", "");
//        Log.i("DBHelper", "Get database passwords " + p);
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
        db.execSQL(SessionStoreEntity.SQL_CREATE);
        db.execSQL(AuthStateStoreEntity.SQL_CREATE);
        db.execSQL(KyberPreKeyRecordsEntity.SQL_CREATE_TABLE);
        db.execSQL(SenderKeyEntity.SQL_CREATE_TABLE);
        db.execSQL(GroupChatEntity.SQL_CREATE_TABLE);
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

        Log.i("DbHelper", "OnUpgrade called. Creating tables");
        db.execSQL(MessageStoreEntity.SQL_CREATE_TABLE);
        db.execSQL(IdentityKeyPairStoreEntity.SQL_CREATE_TABLE);
        db.execSQL(RegistrationIdStoreEntity.SQL_CREATE_TABLE);
        db.execSQL(SignedPreKeyRecordStoreEntity.SQL_CREATE_TABLE);
        db.execSQL(PreKeyRecordStoreEntity.SQL_CREATE_TABLE);
        db.execSQL(JwtStoreEntity.SQL_CREATE_TABLE);
        db.execSQL(SessionStoreEntity.SQL_CREATE);
        db.execSQL(AuthStateStoreEntity.SQL_CREATE);
        db.execSQL(KyberPreKeyRecordsEntity.SQL_CREATE_TABLE);
        db.execSQL(SenderKeyEntity.SQL_CREATE_TABLE);
        db.execSQL(GroupChatEntity.SQL_CREATE_TABLE);
    }

    public String getUserNameFromJwt() {
        return new JWT(getAuthState().getIdToken()).getClaim(Constants.USERNAME_CLAIM).asString();
    }

    public void deleteMessage(Long messageId) {
        executorService.submit(() -> {
            getWritableDatabase().delete(MessageStoreEntity.TABLE_NAME,
                    MessageStoreEntity._ID + "=?", new String[]{String.valueOf(messageId)});

        });

    }
}
