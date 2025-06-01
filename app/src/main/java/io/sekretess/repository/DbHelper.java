package io.sekretess.repository;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;


import com.auth0.android.jwt.JWT;

import io.sekretess.Constants;
import io.sekretess.dto.MessageBriefDto;
import io.sekretess.dto.MessageRecordDto;
import io.sekretess.dto.RegistrationAndDeviceId;
import io.sekretess.model.AuthStateStoreEntity;
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
import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

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

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class DbHelper extends SQLiteOpenHelper {
    private final DateTimeFormatter dateTimeFormatter
            = DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.systemDefault());

    public static final int DATABASE_VERSION = 13;
    public static final String DATABASE_NAME = "io.sekretess_enc_db.db";
    private static final Base64.Encoder base64Encoder = Base64.getEncoder();
    private static final Base64.Decoder base64Decoder = Base64.getDecoder();
    private Context mContext;

    private static DbHelper mInstance;

    private DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
        SQLiteDatabase.loadLibs(this.mContext);
        try {
            String p = p();
            if (p == null || p.isBlank() || p.isEmpty()) {
                p = cp();
            }
            this.getWritableDatabase(p);
        } catch (Exception e) {
            Log.e("DBHelper", "Db initialization failed. ", e);
        }
    }

    public static synchronized DbHelper getInstance(Context context) {
        synchronized (DbHelper.class) {
            if (mInstance == null) {
                mInstance = new DbHelper(context.getApplicationContext());
            }
            return mInstance;
        }
    }

    @SuppressLint("Range")
    public IdentityKeyPair getIdentityKeyPair() {
        try (Cursor cursor = getReadableDatabase(p()).query(IdentityKeyPairStoreEntity.TABLE_NAME,
                new String[]{IdentityKeyPairStoreEntity._ID, IdentityKeyPairStoreEntity.COLUMN_IKP},
                null, null, null, null, null)) {
            if (cursor.moveToNext()) {
                String ikp = cursor.getString(cursor.getColumnIndex(IdentityKeyPairStoreEntity.COLUMN_IKP));
                return new IdentityKeyPair(base64Decoder.decode(ikp));
            }
        }
        return null;
    }

    public void storeIdentityKeyPair(IdentityKeyPair identityKeyPair) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(IdentityKeyPairStoreEntity.COLUMN_IKP,
                base64Encoder.encodeToString(identityKeyPair.serialize()));
        contentValues.put(IdentityKeyPairStoreEntity.COLUMN_CREATED_AT, dateTimeFormatter.format(Instant.now()));
        try (SQLiteDatabase db = getWritableDatabase(p())) {
            db.insert(IdentityKeyPairStoreEntity.TABLE_NAME, null, contentValues);
        }
    }

    @SuppressLint("Range")
    public RegistrationAndDeviceId getRegistrationId() {
        try (Cursor cursor = getReadableDatabase(p()).query(RegistrationIdStoreEntity.TABLE_NAME,
                new String[]{RegistrationIdStoreEntity._ID, RegistrationIdStoreEntity.COLUMN_REG_ID, RegistrationIdStoreEntity.COLUMN_DEVICE_ID},
                null, null, null, null, null)) {
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
        }
        return null;
    }

    public void storeRegistrationId(Integer registrationId, int deviceId) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(RegistrationIdStoreEntity.COLUMN_REG_ID, registrationId);
        contentValues.put(RegistrationIdStoreEntity.COLUMN_DEVICE_ID, deviceId);
        contentValues.put(RegistrationIdStoreEntity.COLUMN_CREATED_AT, dateTimeFormatter.format(Instant.now()));
        try (SQLiteDatabase db = getWritableDatabase(p())) {
            db.insert(RegistrationIdStoreEntity.TABLE_NAME, null, contentValues);
        }
    }

    @SuppressLint("Range")
    public SignedPreKeyRecord getSignedPreKeyRecord() {
        try (Cursor cursor = getReadableDatabase(p()).query(SignedPreKeyRecordStoreEntity.TABLE_NAME,
                new String[]{SignedPreKeyRecordStoreEntity._ID, SignedPreKeyRecordStoreEntity.COLUMN_SPK_RECORD},
                null, null, null, null, null)) {
            if (cursor.moveToNext()) {
                try {
                    return new SignedPreKeyRecord(base64Decoder.decode(cursor.getString(cursor
                            .getColumnIndex(SignedPreKeyRecordStoreEntity.COLUMN_SPK_RECORD))));
                } catch (Exception e) {
                    Log.e("DbHelper", "Error occurred during get spk from database");
                    return null;
                }
            }
        }
        return null;
    }

    public void storeSignedPreKeyRecord(SignedPreKeyRecord signedPreKeyRecord) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SignedPreKeyRecordStoreEntity.COLUMN_SPK_RECORD, base64Encoder
                .encodeToString(signedPreKeyRecord.serialize()));
        contentValues.put(SignedPreKeyRecordStoreEntity.COLUMN_CREATED_AT, dateTimeFormatter.format(Instant.now()));
        try (SQLiteDatabase db = getWritableDatabase(p())) {
            db.insert(SignedPreKeyRecordStoreEntity.TABLE_NAME, null, contentValues);
        }
    }

    public boolean clearKeyData() {
        SQLiteDatabase db = getWritableDatabase(p());
        try {
            db.beginTransaction();
            db.delete(IdentityKeyPairStoreEntity.TABLE_NAME, null, null);
            db.delete(RegistrationIdStoreEntity.TABLE_NAME, null, null);
            db.delete(SignedPreKeyRecordStoreEntity.TABLE_NAME, null, null);
            db.delete(PreKeyRecordStoreEntity.TABLE_NAME, null, null);
            db.delete(KyberPreKeyRecordsEntity.TABLE_NAME, null, null);
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public boolean clearUserData() {
        SQLiteDatabase db = getWritableDatabase(p());
        try {
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
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            db.endTransaction();
            db.close();
        }

    }

    @SuppressLint("Range")
    public void loadPreKeyRecords(SignalProtocolStore signalProtocolStore) throws InvalidMessageException {
        try (Cursor cursor = getReadableDatabase(p()).query(PreKeyRecordStoreEntity.TABLE_NAME,
                new String[]{PreKeyRecordStoreEntity._ID, PreKeyRecordStoreEntity.COLUMN_PREKEY_RECORD},
                null, null, null, null, null)) {
            while (cursor.moveToNext()) {
                PreKeyRecord preKeyRecord = new PreKeyRecord(base64Decoder.decode(cursor
                        .getString(cursor.getColumnIndex(PreKeyRecordStoreEntity.COLUMN_PREKEY_RECORD))));
                signalProtocolStore.storePreKey(preKeyRecord.getId(), preKeyRecord);
            }
        }

    }

    public void removePreKeyRecord(int prekeyId) {
        try (SQLiteDatabase db = getWritableDatabase(p())) {
            db.delete(PreKeyRecordStoreEntity.TABLE_NAME,
                    PreKeyRecordStoreEntity.COLUMN_PREKEY_ID + "=?", new String[]{String.valueOf(prekeyId)});
        }
    }

    public void storePreKeyRecords(PreKeyRecord[] preKeyRecords) {
        try (SQLiteDatabase db = getWritableDatabase(p())) {
            for (PreKeyRecord preKeyRecord : preKeyRecords) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(PreKeyRecordStoreEntity.COLUMN_PREKEY_ID, preKeyRecord.getId());
                contentValues.put(PreKeyRecordStoreEntity.COLUMN_PREKEY_RECORD,
                        base64Encoder.encodeToString(preKeyRecord.serialize()));
                contentValues.put(PreKeyRecordStoreEntity.COLUMN_CREATED_AT,
                        dateTimeFormatter.format(Instant.now()));
                db.insert(PreKeyRecordStoreEntity.TABLE_NAME, null, contentValues);
            }
        }
    }

    public void markKyberPreKeyUsed(int kyberPreKeyId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KyberPreKeyRecordsEntity.COLUMN_USED, 1);
        try (SQLiteDatabase db = getWritableDatabase(p())) {
            db.update(KyberPreKeyRecordsEntity.TABLE_NAME, SQLiteDatabase.CONFLICT_REPLACE,
                    contentValues, KyberPreKeyRecordsEntity.COLUMN_PREKEY_ID + "=?", new Object[]{kyberPreKeyId});
        }
    }

    public void storeKyberPreKey(KyberPreKeyRecord kyberPreKeyRecord) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KyberPreKeyRecordsEntity.COLUMN_PREKEY_ID, kyberPreKeyRecord.getId());
        contentValues.put(KyberPreKeyRecordsEntity.COLUMN_KPK_RECORD,
                base64Encoder.encodeToString(kyberPreKeyRecord.serialize()));
        contentValues.put(KyberPreKeyRecordsEntity.COLUMN_USED, 0);
        contentValues.put(KyberPreKeyRecordsEntity.COLUMN_CREATED_AT,
                dateTimeFormatter.format(Instant.now()));
        try (SQLiteDatabase db = getWritableDatabase(p())) {
            db.insert(KyberPreKeyRecordsEntity.TABLE_NAME, null, contentValues);
        }
    }

    public void storeAuthState(String authState) {
        ContentValues values = new ContentValues();
        values.put(AuthStateStoreEntity.COLUMN_AUTH_STATE, authState);
        try (SQLiteDatabase db = getWritableDatabase(p())) {
            db.delete(AuthStateStoreEntity.TABLE_NAME, null, null);
            db.insert(AuthStateStoreEntity.TABLE_NAME, null, values);
        }
    }

    public void removeAuthState() {
        try (SQLiteDatabase db = getWritableDatabase(p())) {
            db.delete(AuthStateStoreEntity.TABLE_NAME, null, null);
        }
    }

    @SuppressLint("Range")
    public AuthState getAuthState() {
        try (Cursor result = getReadableDatabase(p())
                .query(AuthStateStoreEntity.TABLE_NAME,
                        null,
                        null, null, null, null, null)) {
            if (result.moveToNext()) {
                return AuthState.jsonDeserialize(result
                        .getString(result.getColumnIndex(AuthStateStoreEntity.COLUMN_AUTH_STATE)));
            }
        } catch (JSONException e) {
            Log.e("DbHelper", "Getting AuthState failed", e);
            return null;
        }
        return null;
    }


    public void storeDecryptedMessage(String sender, String message) {
        String username = getUserNameFromJwt();
        ContentValues values = new ContentValues();
        values.put(MessageStoreEntity.COLUMN_SENDER, sender);
        values.put(MessageStoreEntity.COLUMN_MESSAGE_BODY, message);
        values.put(MessageStoreEntity.COLUMN_USERNAME, username);
        values.put(MessageStoreEntity.COLUMN_CREATED_AT,
                dateTimeFormatter.format(Instant.now()));
        try (SQLiteDatabase db = getWritableDatabase(p())) {
            db.insert(MessageStoreEntity.TABLE_NAME,
                    null, values);
        }
    }

    public List<MessageBriefDto> getMessageBriefs() {
        List<MessageBriefDto> resultArray;

        try (Cursor resultCursor = getReadableDatabase(p())
                .query(MessageStoreEntity.TABLE_NAME,
                        new String[]{MessageStoreEntity.COLUMN_SENDER,
                                "COUNT(" + MessageStoreEntity.COLUMN_SENDER + ") AS count"},
                        MessageStoreEntity.COLUMN_USERNAME + "=?",
                        new String[]{getUserNameFromJwt()},
                        MessageStoreEntity.COLUMN_SENDER,
                        null,
                        null
                )) {

            resultArray = new ArrayList<>();

            while (resultCursor.moveToNext()) {
                String senderName = resultCursor.getString(0);
                int messageCount = resultCursor.getInt(1);
                resultArray.add(new MessageBriefDto(senderName, messageCount));

            }
        }
        return resultArray;
    }

    public void storeSenderKey(SignalProtocolAddress sender, UUID distributionId, SenderKeyRecord record) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SenderKeyEntity.COLUMN_ADDRESS_NAME, sender.getName());
        contentValues.put(SenderKeyEntity.COLUMN_ADDRESS_DEVICE_ID, sender.getDeviceId());
        contentValues.put(SenderKeyEntity.COLUMN_DISTRIBUTION_UUID, distributionId.toString());
        contentValues.put(SenderKeyEntity.COLUMN_SENDER_KEY_RECORD, base64Encoder.encodeToString(record.serialize()));
        try (SQLiteDatabase db = getWritableDatabase(p())) {
            db.insert(SenderKeyEntity.TABLE_NAME, null, contentValues);
        }
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
        try (SQLiteDatabase db = getWritableDatabase(p())) {
            db.insert(SessionStoreEntity.TABLE_NAME, null, contentValues);
        }
    }

    @SuppressLint("Range")
    public void loadKyberPreKeys(SekretessSignalProtocolStore signalProtocolStore) throws InvalidMessageException {
        try (Cursor result = getReadableDatabase(p())
                .query(KyberPreKeyRecordsEntity.TABLE_NAME, new String[]{
                                KyberPreKeyRecordsEntity.COLUMN_PREKEY_ID, KyberPreKeyRecordsEntity.COLUMN_KPK_RECORD,
                                KyberPreKeyRecordsEntity.COLUMN_USED},
                        null, null, null, null, null)) {
            while (result.moveToNext()) {
                int prekeyId = result.getInt(result.getColumnIndex(KyberPreKeyRecordsEntity.COLUMN_PREKEY_ID));
                String kpkRecordBase64 = result.getString(result.getColumnIndex(KyberPreKeyRecordsEntity.COLUMN_KPK_RECORD));
                int used = result.getInt(result.getColumnIndex(KyberPreKeyRecordsEntity.COLUMN_USED));

                signalProtocolStore.loadKyberPreKey(prekeyId, new KyberPreKeyRecord(base64Decoder.decode(kpkRecordBase64)));
                if (used == 1) {
                    signalProtocolStore.markKyberPreKeyUsed(prekeyId);
                }
            }
        }
    }

    public List<MessageRecordDto> loadMessages(String from) {
        try (Cursor resultCursor = getReadableDatabase(p())
                .query(MessageStoreEntity.TABLE_NAME, new String[]{MessageStoreEntity.COLUMN_SENDER,
                                MessageStoreEntity.COLUMN_MESSAGE_BODY,
                                MessageStoreEntity.COLUMN_CREATED_AT
                        },
                        "sender=?",
                        new String[]{from}, null, null, null)) {
            List<MessageRecordDto> resultArray = new ArrayList<>();

            while (resultCursor.moveToNext()) {
                String sender = resultCursor.getString(0);
                String messageBody = resultCursor.getString(1);
                String createdAt = resultCursor.getString(2);

                resultArray.add(new MessageRecordDto(sender, messageBody, createdAt));
            }

            return resultArray;
        }
    }

    @SuppressLint("Range")
    public void loadSessions(SekretessSignalProtocolStore signalProtocolStore) {

        try (Cursor result = getReadableDatabase(p())
                .query(SessionStoreEntity.TABLE_NAME, new String[]{SessionStoreEntity.COLUMN_SESSION,
                                SessionStoreEntity.COLUMN_SERVICE_ID, SessionStoreEntity.COLUMN_ADDRESS_NAME,
                                SessionStoreEntity.COLUMN_ADDRESS_DEVICE_ID}, null, null,
                        null, null, null)) {
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
        }
    }

    public void removeSession(SignalProtocolAddress address) {
        try (SQLiteDatabase db = getWritableDatabase(p())) {
            db.delete(SessionStoreEntity.TABLE_NAME,
                    SessionStoreEntity.COLUMN_ADDRESS_NAME + "=? AND"
                            + SessionStoreEntity.COLUMN_ADDRESS_DEVICE_ID + " = ?",
                    new Object[]{address.getName(), address.getDeviceId()});
        }
    }

    private String cp() {
        SharedPreferences encryptedSharedPreferences =
                mContext.getSharedPreferences("secret_shared_prefs", Context.MODE_PRIVATE);

        String p = RandomStringUtils.randomAlphanumeric(15);
        encryptedSharedPreferences.edit().putString("801d0837-c9c3-4a4c-bfcc-67197551d030", p)
                .apply();
        Log.i("DBHelper", "Create password " + p);
        return p;
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
    }

    public String getUserNameFromJwt() {
        return new JWT(getAuthState().getIdToken()).getClaim(Constants.USERNAME_CLAIM).asString();
    }

}
