package io.sekretess.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.sekretess.dto.AuthResponse;
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

public class AuthRepository {

    private final DbHelper dbHelper;
    private final ObjectMapper objectMapper;

    public AuthRepository(DbHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.objectMapper = new ObjectMapper();
    }


    public void storeAuthState(String authState) {
        ContentValues values = new ContentValues();
        values.put(AuthStateStoreEntity.COLUMN_AUTH_STATE, authState);
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();
        sqLiteDatabase.delete(AuthStateStoreEntity.TABLE_NAME, null, null);
        sqLiteDatabase.insert(AuthStateStoreEntity.TABLE_NAME, null, values);
    }

    public void removeAuthState() {
        dbHelper.getWritableDatabase().delete(AuthStateStoreEntity.TABLE_NAME, null, null);
    }

    public String getAuthState() {
        try {
            Cursor result = null;
            try {
                result = dbHelper.getReadableDatabase().query(AuthStateStoreEntity.TABLE_NAME,
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

            SQLiteDatabase db = dbHelper.getWritableDatabase();
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

    }

    public void logout() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        db.delete(AuthStateStoreEntity.TABLE_NAME, null, null);
        db.setTransactionSuccessful();
        db.endTransaction();
    }
}
