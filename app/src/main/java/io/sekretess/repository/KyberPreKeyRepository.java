package io.sekretess.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.signal.libsignal.protocol.InvalidMessageException;
import org.signal.libsignal.protocol.state.KyberPreKeyRecord;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import io.sekretess.model.KyberPreKeyRecordsEntity;

public class KyberPreKeyRepository {
    private final SekretessDatabase sekretessDatabase;
    private final String TAG = KyberPreKeyRepository.class.getName();

    public KyberPreKeyRepository(SekretessDatabase sekretessDatabase) {
        this.sekretessDatabase = sekretessDatabase;
    }


    public void markKyberPreKeyUsed(int kyberPreKeyId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KyberPreKeyRecordsEntity.COLUMN_USED, 1);
        sekretessDatabase.getWritableDatabase().updateWithOnConflict(KyberPreKeyRecordsEntity.TABLE_NAME,
                contentValues, KyberPreKeyRecordsEntity.COLUMN_PREKEY_ID + "=?",
                new String[]{String.valueOf(kyberPreKeyId)}, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void storeKyberPreKey(KyberPreKeyRecord kyberPreKeyRecord) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KyberPreKeyRecordsEntity.COLUMN_PREKEY_ID, kyberPreKeyRecord.getId());
        contentValues.put(KyberPreKeyRecordsEntity.COLUMN_KPK_RECORD,
                SekretessDatabase.base64Encoder.encodeToString(kyberPreKeyRecord.serialize()));
        contentValues.put(KyberPreKeyRecordsEntity.COLUMN_USED, 0);
        contentValues.put(KyberPreKeyRecordsEntity.COLUMN_CREATED_AT,
                SekretessDatabase.dateTimeFormatter.format(Instant.now()));
        sekretessDatabase.getWritableDatabase().insert(KyberPreKeyRecordsEntity.TABLE_NAME, null, contentValues);
    }

    public KyberPreKeyRecord loadKyberPreKey(int kyberPreKeyId) {
        try (Cursor cursor = sekretessDatabase.getReadableDatabase().query(KyberPreKeyRecordsEntity.TABLE_NAME,
                new String[]{KyberPreKeyRecordsEntity.COLUMN_KPK_RECORD}, KyberPreKeyRecordsEntity.COLUMN_PREKEY_ID + "=?",
                new String[]{String.valueOf(kyberPreKeyId)}, null, null, null)) {
            while (cursor.moveToNext()) {
                return new KyberPreKeyRecord(SekretessDatabase.base64Decoder.decode(cursor.getString(0)));
            }
        } catch (InvalidMessageException e) {
            Log.e(TAG, "Error loading KyberPreKeyRecord", e);
        }
        return null;
    }

    public List<KyberPreKeyRecord> loadKyberPreKeys() {
        List<KyberPreKeyRecord> kyberPreKeyRecords = new ArrayList<>();

        try (Cursor cursor = sekretessDatabase.getReadableDatabase().query(KyberPreKeyRecordsEntity.TABLE_NAME,
                new String[]{KyberPreKeyRecordsEntity.COLUMN_KPK_RECORD}, null,
                null, null, null, null)) {
            while (cursor.moveToNext()) {
                kyberPreKeyRecords.add(new KyberPreKeyRecord(SekretessDatabase.base64Decoder.decode(cursor.getString(0))));
            }
        } catch (InvalidMessageException e) {
            Log.e(TAG, "Error loading KyberPreKeyRecord", e);
        }
        return kyberPreKeyRecords;
    }

    public void clearStorage() {
        sekretessDatabase.getWritableDatabase().delete(KyberPreKeyRecordsEntity.TABLE_NAME, null, null);
    }
}
