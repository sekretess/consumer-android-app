package io.sekretess.repository;

import android.content.ContentValues;
import android.database.Cursor;

import java.time.Instant;

import io.sekretess.model.RegistrationIdStoreEntity;

public class RegistrationRepository {

    private final SekretessDatabase sekretessDatabase;
    private final String TAG = RegistrationRepository.class.getName();

    public RegistrationRepository(SekretessDatabase sekretessDatabase) {
        this.sekretessDatabase = sekretessDatabase;
    }

    public int getRegistrationId() {
        try (Cursor cursor = sekretessDatabase.getReadableDatabase().query(RegistrationIdStoreEntity.TABLE_NAME,
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
        contentValues.put(RegistrationIdStoreEntity.COLUMN_CREATED_AT, SekretessDatabase.dateTimeFormatter.format(Instant.now()));
        sekretessDatabase.getWritableDatabase().insert(RegistrationIdStoreEntity.TABLE_NAME, null, contentValues);
    }

    public void clearStorage() {
        sekretessDatabase.getWritableDatabase().delete(RegistrationIdStoreEntity.TABLE_NAME, null, null);
    }
}
