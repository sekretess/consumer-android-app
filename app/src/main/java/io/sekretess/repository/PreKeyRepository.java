package io.sekretess.repository;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import org.signal.libsignal.protocol.InvalidMessageException;
import org.signal.libsignal.protocol.state.PreKeyRecord;
import org.signal.libsignal.protocol.state.SignalProtocolStore;
import org.signal.libsignal.protocol.state.SignedPreKeyRecord;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import io.sekretess.model.PreKeyRecordStoreEntity;
import io.sekretess.model.SignedPreKeyRecordStoreEntity;

public class PreKeyRepository {
    private final DbHelper dbHelper;
    private final String TAG = PreKeyRepository.class.getName();

    public PreKeyRepository(DbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<SignedPreKeyRecord> loadSignedPreKeys() {
        List<SignedPreKeyRecord> signedPreKeyRecords = new ArrayList<>();
        try (Cursor cursor = dbHelper.getReadableDatabase().query(SignedPreKeyRecordStoreEntity.TABLE_NAME,
                new String[]{SignedPreKeyRecordStoreEntity.COLUMN_SPK_RECORD}, null, null,
                null, null, null)) {
            while (cursor.moveToNext()) {
                SignedPreKeyRecord signedPreKeyRecord = new SignedPreKeyRecord(DbHelper
                        .base64Decoder.decode(cursor.getString(cursor
                                .getColumnIndexOrThrow(SignedPreKeyRecordStoreEntity.COLUMN_SPK_RECORD))));
                signedPreKeyRecords.add(signedPreKeyRecord);
            }
        } catch (InvalidMessageException e) {
            Log.e(TAG, "Error occurred during get spk from database", e);
        }
        return signedPreKeyRecords;
    }

    public SignedPreKeyRecord getSignedPreKeyRecord(int signedPreKeyId) {
        try (Cursor cursor = dbHelper.getReadableDatabase().query(SignedPreKeyRecordStoreEntity.TABLE_NAME,
                new String[]{SignedPreKeyRecordStoreEntity._ID, SignedPreKeyRecordStoreEntity.COLUMN_SPK_RECORD},
                SignedPreKeyRecordStoreEntity.COLUMN_SPK_ID + "=?", new String[]{String.valueOf(signedPreKeyId)},
                null, null, null, null)) {
            while (cursor.moveToNext()) {
                try {
                    return new SignedPreKeyRecord(DbHelper.base64Decoder
                            .decode(cursor
                                    .getString(cursor
                                            .getColumnIndexOrThrow(SignedPreKeyRecordStoreEntity
                                                    .COLUMN_SPK_RECORD))));
                } catch (Exception e) {
                    Log.e(TAG, "Error occurred during get spk from database");
                    return null;
                }
            }
        }
        return null;
    }

    public void removeSignedPreKey(int signedPreKeyId) {
        dbHelper.getWritableDatabase().delete(SignedPreKeyRecordStoreEntity.TABLE_NAME, SignedPreKeyRecordStoreEntity
                .COLUMN_SPK_ID + "=?", new String[]{String.valueOf(signedPreKeyId)});
    }

    public void storeSignedPreKeyRecord(SignedPreKeyRecord signedPreKeyRecord) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SignedPreKeyRecordStoreEntity.COLUMN_SPK_RECORD, DbHelper.base64Encoder
                .encodeToString(signedPreKeyRecord.serialize()));
        contentValues.put(SignedPreKeyRecordStoreEntity.COLUMN_SPK_ID, signedPreKeyRecord.getId());
        contentValues.put(SignedPreKeyRecordStoreEntity.COLUMN_CREATED_AT, DbHelper.dateTimeFormatter.format(Instant.now()));
        dbHelper.getWritableDatabase().insert(SignedPreKeyRecordStoreEntity.TABLE_NAME, null, contentValues);
    }

    public int count() {
        try (Cursor cursor = dbHelper.getReadableDatabase().query(PreKeyRecordStoreEntity.TABLE_NAME,
                new String[]{PreKeyRecordStoreEntity._ID, PreKeyRecordStoreEntity.COLUMN_PREKEY_RECORD},
                null, null, null, null, null);) {

            return cursor.getCount();
        }
    }

    public void removePreKeyRecord(int prekeyId) {
        dbHelper.getWritableDatabase().delete(PreKeyRecordStoreEntity.TABLE_NAME,
                PreKeyRecordStoreEntity.COLUMN_PREKEY_ID + "=?", new String[]{String.valueOf(prekeyId)});
    }

    public void storePreKeyRecord(PreKeyRecord preKeyRecord) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(PreKeyRecordStoreEntity.COLUMN_PREKEY_ID, preKeyRecord.getId());
        contentValues.put(PreKeyRecordStoreEntity.COLUMN_PREKEY_RECORD,
                DbHelper.base64Encoder.encodeToString(preKeyRecord.serialize()));
        contentValues.put(PreKeyRecordStoreEntity.COLUMN_CREATED_AT,
                DbHelper.dateTimeFormatter.format(Instant.now()));
        dbHelper.getWritableDatabase().insert(PreKeyRecordStoreEntity.TABLE_NAME, null, contentValues);
    }

    public PreKeyRecord loadPreKey(int preKeyId) {
        try (Cursor cursor = dbHelper.getReadableDatabase().query(PreKeyRecordStoreEntity.TABLE_NAME,
                new String[]{PreKeyRecordStoreEntity._ID, PreKeyRecordStoreEntity.COLUMN_PREKEY_RECORD},
                PreKeyRecordStoreEntity.COLUMN_PREKEY_ID + "=?", new String[]{String.valueOf(preKeyId)},
                null, null, null)) {
            while (cursor.moveToNext()) {
                String preKeyRecordBase64Str = cursor.getString(cursor
                        .getColumnIndexOrThrow(PreKeyRecordStoreEntity.COLUMN_PREKEY_RECORD));
                return new PreKeyRecord(DbHelper.base64Decoder.decode(preKeyRecordBase64Str));
            }
        } catch (InvalidMessageException e) {
            Log.i(TAG, "Error occurred during get prekey from database", e);
        }

        return null;
    }

    public void clearStorage() {
        dbHelper.getWritableDatabase().delete(PreKeyRecordStoreEntity.TABLE_NAME, null, null);

    }
}
