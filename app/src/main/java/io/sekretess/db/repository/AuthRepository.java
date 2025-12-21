package io.sekretess.db.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

import io.sekretess.db.SekretessDatabase;
import io.sekretess.db.dao.AuthDao;
import io.sekretess.db.model.AuthStateStoreEntity;
import io.sekretess.dependency.SekretessDependencyProvider;

public class AuthRepository {

    private final AuthDao authDao;

    public AuthRepository() {
        SekretessDatabase db = SekretessDatabase.getInstance(SekretessDependencyProvider.applicationContext());
        this.authDao = db.authDao();
    }


    public void storeAuthState(String authState) {
        authDao.insert(new AuthStateStoreEntity(authState, System.currentTimeMillis()));
    }

    public void removeAuthState() {
        authDao.delete();
    }

    public Optional<String> getAuthState() {
        AuthStateStoreEntity authStateEntity = authDao.getAuthState();
        if (authStateEntity != null) {
            return Optional.of(authStateEntity.getAuthState());
        } else {
            return Optional.empty();
        }
    }

    public void logout() {
        removeAuthState();
    }
}
