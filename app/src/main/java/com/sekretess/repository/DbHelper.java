package com.sekretess.repository;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;


import com.sekretess.Constants;
import com.sekretess.dto.MessageBriefDto;
import com.sekretess.dto.jwt.Jwt;
import com.sekretess.model.IdentityKeyPairStoreEntity;
import com.sekretess.model.JwtStoreEntity;
import com.sekretess.model.MessageStoreEntity;
import com.sekretess.model.PreKeyRecordStoreEntity;
import com.sekretess.model.RegistrationIdStoreEntity;
import com.sekretess.model.SessionStoreEntity;
import com.sekretess.model.SignedPreKeyRecordStoreEntity;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import org.signal.libsignal.protocol.IdentityKeyPair;
import org.signal.libsignal.protocol.InvalidMessageException;
import org.signal.libsignal.protocol.SignalProtocolAddress;
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

public class DbHelper extends SQLiteOpenHelper {
    private final DateTimeFormatter dateTimeFormatter
            = DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.systemDefault());

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "sekretess_enc.db";


    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase.loadLibs(context);
    }

    @SuppressLint("Range")
    public IdentityKeyPair getIdentityKeyPair() {
        try (Cursor cursor = getReadableDatabase(Constants.password).query(IdentityKeyPairStoreEntity.TABLE_NAME,
                new String[]{IdentityKeyPairStoreEntity._ID, IdentityKeyPairStoreEntity.COLUMN_IKP},
                null, null, null, null, null)) {
            if (cursor.moveToNext()) {
                String ikp = cursor.getString(cursor.getColumnIndex(IdentityKeyPairStoreEntity.COLUMN_IKP));
                return new IdentityKeyPair(Base64.getDecoder().decode(ikp));
            }
        }
        return null;
    }

    public void storeIdentityKeyPair(IdentityKeyPair identityKeyPair) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(IdentityKeyPairStoreEntity.COLUMN_IKP,
                Base64.getEncoder().encodeToString(identityKeyPair.serialize()));
        contentValues.put(IdentityKeyPairStoreEntity.COLUMN_CREATED_AT, dateTimeFormatter.format(Instant.now()));
        getWritableDatabase(Constants.password)
                .insert(IdentityKeyPairStoreEntity.TABLE_NAME, null, contentValues);
    }

    @SuppressLint("Range")
    public Integer getRegistrationId() {
        try (Cursor cursor = getReadableDatabase(Constants.password).query(RegistrationIdStoreEntity.TABLE_NAME,
                new String[]{RegistrationIdStoreEntity._ID, RegistrationIdStoreEntity.COLUMN_REG_ID},
                null, null, null, null, null)) {
            if (cursor.moveToNext()) {
                return cursor.getInt(cursor.getColumnIndex(RegistrationIdStoreEntity.COLUMN_REG_ID));
            }
        }
        return null;
    }

    public void storeRegistrationId(Integer registrationId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(RegistrationIdStoreEntity.COLUMN_REG_ID, registrationId);
        contentValues.put(RegistrationIdStoreEntity.COLUMN_CREATED_AT, dateTimeFormatter.format(Instant.now()));
        getWritableDatabase(Constants.password)
                .insert(RegistrationIdStoreEntity.TABLE_NAME, null, contentValues);
    }

    @SuppressLint("Range")
    public SignedPreKeyRecord getSignedPreKeyRecord() {
        try (Cursor cursor = getReadableDatabase(Constants.password).query(SignedPreKeyRecordStoreEntity.TABLE_NAME,
                new String[]{SignedPreKeyRecordStoreEntity._ID, SignedPreKeyRecordStoreEntity.COLUMN_SPK_RECORD},
                null, null, null, null, null)) {
            if (cursor.moveToNext()) {
                try {
                    return new SignedPreKeyRecord(Base64.getDecoder().decode(cursor.getString(cursor
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
        contentValues.put(SignedPreKeyRecordStoreEntity.COLUMN_SPK_RECORD, Base64.getEncoder()
                .encodeToString(signedPreKeyRecord.serialize()));
        contentValues.put(SignedPreKeyRecordStoreEntity.COLUMN_CREATED_AT, dateTimeFormatter.format(Instant.now()));
        getWritableDatabase(Constants.password)
                .insert(SignedPreKeyRecordStoreEntity.TABLE_NAME, null, contentValues);
    }

    @SuppressLint("Range")
    public PreKeyRecord[] getPreKeyRecords() throws InvalidMessageException {
        PreKeyRecord[] preKeyRecords;
        try (Cursor cursor = getReadableDatabase(Constants.password).query(PreKeyRecordStoreEntity.TABLE_NAME,
                new String[]{PreKeyRecordStoreEntity._ID, PreKeyRecordStoreEntity.COLUMN_PREKEY_RECORD},
                null, null, null, null, null)) {
            int opkCount = cursor.getCount();
            preKeyRecords = new PreKeyRecord[opkCount];
            int idx = 0;
            while (cursor.moveToNext()) {
                preKeyRecords[idx++] = new PreKeyRecord(Base64.getDecoder().decode(cursor
                        .getString(cursor.getColumnIndex(PreKeyRecordStoreEntity.COLUMN_PREKEY_RECORD))));
            }
        }
        return preKeyRecords;
    }

    public void removePreKeyRecord(int prekeyId) {
        getWritableDatabase(Constants.password).delete(PreKeyRecordStoreEntity.TABLE_NAME,
                PreKeyRecordStoreEntity.COLUMN_PREKEY_ID + "=?", new String[]{String.valueOf(prekeyId)});

    }

    public void storePreKeyRecords(PreKeyRecord[] preKeyRecords) {
        for (PreKeyRecord preKeyRecord : preKeyRecords) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(PreKeyRecordStoreEntity.COLUMN_PREKEY_ID, preKeyRecord.getId());
            contentValues.put(PreKeyRecordStoreEntity.COLUMN_PREKEY_RECORD,
                    Base64.getEncoder().encodeToString(preKeyRecord.serialize()));
            contentValues.put(PreKeyRecordStoreEntity.COLUMN_CREATED_AT,
                    dateTimeFormatter.format(Instant.now()));
            getWritableDatabase(Constants.password)
                    .insert(PreKeyRecordStoreEntity.TABLE_NAME, null, contentValues);
        }
    }

    public void storeJwt(String jwt) {
        ContentValues values = new ContentValues();
        values.put(JwtStoreEntity.COLUMN_JWT, jwt);
        values.put(JwtStoreEntity.COLUMN_CREATED_AT, dateTimeFormatter.format(Instant.now()));
        getWritableDatabase(Constants.password)
                .insert(JwtStoreEntity.TABLE_NAME, null, values);
    }

    public void removeJwt() {
        getWritableDatabase(Constants.password).delete(JwtStoreEntity.TABLE_NAME, null, null);
    }

    @SuppressLint("Range")
    public Jwt getJwt() {
        try (Cursor result = getReadableDatabase(Constants.password).
                query(JwtStoreEntity.TABLE_NAME,
                        new String[]{JwtStoreEntity.COLUMN_JWT},
                        null, null, null, null, null)) {
            if (result.moveToNext()) {
                return Jwt.fromString(result.getString(result.getColumnIndex(JwtStoreEntity.COLUMN_JWT)));
            }
        }
        return null;
    }


    public void storeDecryptedMessage(String sender, String message) {
        ContentValues values = new ContentValues();
        values.put(MessageStoreEntity.COLUMN_SENDER, sender);
        values.put(MessageStoreEntity.COLUMN_MESSAGE_BODY, message);
        values.put(MessageStoreEntity.COLUMN_CREATED_AT,
                dateTimeFormatter.format(Instant.now()));
        getWritableDatabase(Constants.password).insert(MessageStoreEntity.TABLE_NAME,
                null, values);
    }

    public List<MessageBriefDto> getMessageBriefs() {
        List<MessageBriefDto> resultArray;
        try (Cursor resultCursor = getReadableDatabase(Constants.password)
                .query(MessageStoreEntity.TABLE_NAME,
                        new String[]{MessageStoreEntity.COLUMN_SENDER,
                                "COUNT(" + MessageStoreEntity.COLUMN_SENDER + ") AS count"},
                        null,
                        null,
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


    public void storeSession(SignalProtocolAddress address, SessionRecord sessionRecord) {
        Base64.Encoder encoder = Base64.getEncoder();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SessionStoreEntity.COLUMN_ADDRESS_NAME, address.getName());
        contentValues.put(SessionStoreEntity.COLUMN_ADDRESS_DEVICE_ID, address.getDeviceId());
        if (address.getServiceId() != null) {
            contentValues.put(SessionStoreEntity.COLUMN_SERVICE_ID,
                    encoder.encodeToString(address.getServiceId().toServiceIdBinary()));
        }
        contentValues.put(SessionStoreEntity.COLUMN_SESSION, encoder.encodeToString(sessionRecord.serialize()));
        getWritableDatabase(Constants.password)
                .insert(SessionStoreEntity.TABLE_NAME, null, contentValues);
    }

    @SuppressLint("Range")
    public void loadSessions(SignalProtocolStore signalProtocolStore) {
        Base64.Decoder base64Decoder = Base64.getDecoder();
        try (Cursor result = getReadableDatabase(Constants.password)
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
                    signalProtocolStore.storeSession(new SignalProtocolAddress(name, deviceId),
                            new SessionRecord(sessionRecord));
                } catch (Exception e) {
                    Log.e("DbHelper", "Error occurred during load session. " +
                            "DeviceId = " + deviceId + " DeviceName = " + name, e);
                }
            }
        }
    }

    public void removeSession(SignalProtocolAddress address) {
        getWritableDatabase(Constants.password)
                .delete(SessionStoreEntity.TABLE_NAME,
                        SessionStoreEntity.COLUMN_ADDRESS_NAME + "=? AND" + SessionStoreEntity.COLUMN_ADDRESS_DEVICE_ID + " = ?",
                        new Object[]{address.getName(), address.getDeviceId()});
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(MessageStoreEntity.SQL_CREATE_TABLE);
        db.execSQL(IdentityKeyPairStoreEntity.SQL_CREATE_TABLE);
        db.execSQL(RegistrationIdStoreEntity.SQL_CREATE_TABLE);
        db.execSQL(SignedPreKeyRecordStoreEntity.SQL_CREATE_TABLE);
        db.execSQL(PreKeyRecordStoreEntity.SQL_CREATE_TABLE);
        db.execSQL(JwtStoreEntity.SQL_CREATE_TABLE);
        db.execSQL(SessionStoreEntity.SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(MessageStoreEntity.SQL_DROP_TABLE);
        db.execSQL(IdentityKeyPairStoreEntity.SQL_DROP_TABLE);
        db.execSQL(RegistrationIdStoreEntity.SQL_DROP_TABLE);
        db.execSQL(SignedPreKeyRecordStoreEntity.SQL_DROP_TABLE);
        db.execSQL(PreKeyRecordStoreEntity.SQL_DROP_TABLE);
        db.execSQL(JwtStoreEntity.SQL_DROP_TABLE);
        db.execSQL(SessionStoreEntity.SQL_DROP);
    }
}
