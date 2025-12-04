package io.sekretess.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.sekretess.model.AuthStateStoreEntity;

public class AuthRepository {

    private final SekretessDatabase sekretessDatabase;
    private final ObjectMapper objectMapper;

    public AuthRepository(SekretessDatabase sekretessDatabase) {
        this.sekretessDatabase = sekretessDatabase;
        this.objectMapper = new ObjectMapper();
    }


    public void storeAuthState(String authState) {
        ContentValues values = new ContentValues();
        values.put(AuthStateStoreEntity.COLUMN_AUTH_STATE, authState);
        SQLiteDatabase sqLiteDatabase = sekretessDatabase.getWritableDatabase();
        sqLiteDatabase.delete(AuthStateStoreEntity.TABLE_NAME, null, null);
        sqLiteDatabase.insert(AuthStateStoreEntity.TABLE_NAME, null, values);
    }

    public void removeAuthState() {
        sekretessDatabase.getWritableDatabase().delete(AuthStateStoreEntity.TABLE_NAME, null, null);
    }

    public String getAuthState() {
        try {
            Cursor result = null;
            try {
                result = sekretessDatabase.getReadableDatabase().query(AuthStateStoreEntity.TABLE_NAME,
                        null, null, null, null, null,
                        null);
                if (result.moveToNext()) {
                    return result.getString(result
                            .getColumnIndexOrThrow(AuthStateStoreEntity.COLUMN_AUTH_STATE));
                }
            } catch (Throwable e) {
                Log.e("DbHelper", "Getting AuthState failed", e);
                return null;
            }
            return null;
        } catch (Exception e) {
            Log.i("DbHelper", "Getting AuthState failed", e);
            return null;
        }
    }


    public boolean clearUserData() {
        try {
            SQLiteDatabase db = sekretessDatabase.getWritableDatabase();
            db.beginTransaction();
            db.delete(AuthStateStoreEntity.TABLE_NAME, null, null);
            db.setTransactionSuccessful();
            db.endTransaction();
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    public void logout() {
        SQLiteDatabase db = sekretessDatabase.getWritableDatabase();
        db.beginTransaction();
        db.delete(AuthStateStoreEntity.TABLE_NAME, null, null);
        db.setTransactionSuccessful();
        db.endTransaction();
    }
}
