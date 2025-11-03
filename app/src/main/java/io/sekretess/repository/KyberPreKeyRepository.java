package io.sekretess.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.signal.libsignal.protocol.InvalidMessageException;
import org.signal.libsignal.protocol.state.KyberPreKeyRecord;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import io.sekretess.cryptography.storage.SekretessSignalProtocolStore;
import io.sekretess.model.KyberPreKeyRecordsEntity;

public class KyberPreKeyRepository {
    private final DbHelper dbHelper;
    private final String TAG = KyberPreKeyRepository.class.getName();

    public KyberPreKeyRepository(DbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }


    public void markKyberPreKeyUsed(int kyberPreKeyId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KyberPreKeyRecordsEntity.COLUMN_USED, 1);
        dbHelper.getWritableDatabase().updateWithOnConflict(KyberPreKeyRecordsEntity.TABLE_NAME,
                contentValues, KyberPreKeyRecordsEntity.COLUMN_PREKEY_ID + "=?",
                new String[]{String.valueOf(kyberPreKeyId)}, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void storeKyberPreKey(KyberPreKeyRecord kyberPreKeyRecord) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KyberPreKeyRecordsEntity.COLUMN_PREKEY_ID, kyberPreKeyRecord.getId());
        contentValues.put(KyberPreKeyRecordsEntity.COLUMN_KPK_RECORD,
                DbHelper.base64Encoder.encodeToString(kyberPreKeyRecord.serialize()));
        contentValues.put(KyberPreKeyRecordsEntity.COLUMN_USED, 0);
        contentValues.put(KyberPreKeyRecordsEntity.COLUMN_CREATED_AT,
                DbHelper.dateTimeFormatter.format(Instant.now()));
        dbHelper.getWritableDatabase().insert(KyberPreKeyRecordsEntity.TABLE_NAME, null, contentValues);
    }

    public KyberPreKeyRecord loadKyberPreKey(int kyberPreKeyId) {
        try (Cursor cursor = dbHelper.getReadableDatabase().query(KyberPreKeyRecordsEntity.TABLE_NAME,
                new String[]{KyberPreKeyRecordsEntity.COLUMN_KPK_RECORD}, KyberPreKeyRecordsEntity.COLUMN_PREKEY_ID + "=?",
                new String[]{String.valueOf(kyberPreKeyId)}, null, null, null)) {
            while (cursor.moveToNext()) {
                return new KyberPreKeyRecord(DbHelper.base64Decoder.decode(cursor.getString(0)));
            }
        } catch (InvalidMessageException e) {
            Log.e(TAG, "Error loading KyberPreKeyRecord", e);
        }
        return null;
    }

    public List<KyberPreKeyRecord> loadKyberPreKeys() {
        List<KyberPreKeyRecord> kyberPreKeyRecords = new ArrayList<>();

        try (Cursor cursor = dbHelper.getReadableDatabase().query(KyberPreKeyRecordsEntity.TABLE_NAME,
                new String[]{KyberPreKeyRecordsEntity.COLUMN_KPK_RECORD}, null,
                null, null, null, null)) {
            while (cursor.moveToNext()) {
                kyberPreKeyRecords.add(new KyberPreKeyRecord(DbHelper.base64Decoder.decode(cursor.getString(0))));
            }
        } catch (InvalidMessageException e) {
            Log.e(TAG, "Error loading KyberPreKeyRecord", e);
        }
        return kyberPreKeyRecords;
    }
}
