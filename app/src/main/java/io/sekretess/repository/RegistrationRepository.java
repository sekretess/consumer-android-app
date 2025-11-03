package io.sekretess.repository;

import android.content.ContentValues;
import android.database.Cursor;

import org.signal.libsignal.protocol.util.KeyHelper;

import java.time.Instant;

import io.sekretess.model.RegistrationIdStoreEntity;

public class RegistrationRepository {

    private final DbHelper dbHelper;
    private final String TAG = RegistrationRepository.class.getName();

    public RegistrationRepository(DbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public int getRegistrationId() {
        try (Cursor cursor = dbHelper.getReadableDatabase().query(RegistrationIdStoreEntity.TABLE_NAME,
                new String[]{RegistrationIdStoreEntity._ID, RegistrationIdStoreEntity.COLUMN_REG_ID},
                null, null, null, null, null)) {
            if (cursor.moveToNext()) {
                return cursor.getInt(cursor.getColumnIndexOrThrow(RegistrationIdStoreEntity.COLUMN_REG_ID));
            } else {
                return 0;
            }
        }
    }

    public void storeRegistrationId(Integer registrationId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(RegistrationIdStoreEntity.COLUMN_REG_ID, registrationId);
        contentValues.put(RegistrationIdStoreEntity.COLUMN_CREATED_AT, DbHelper.dateTimeFormatter.format(Instant.now()));
        dbHelper.getWritableDatabase().insert(RegistrationIdStoreEntity.TABLE_NAME, null, contentValues);
    }
}
