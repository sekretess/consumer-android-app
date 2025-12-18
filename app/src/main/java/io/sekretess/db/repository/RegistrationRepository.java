package io.sekretess.db.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.time.Instant;

import io.sekretess.db.SekretessDatabase;
import io.sekretess.db.dao.RegistrationIdDao;
import io.sekretess.db.model.RegistrationIdStoreEntity;
import io.sekretess.dependency.SekretessDependencyProvider;

public class RegistrationRepository {

    private final RegistrationIdDao registrationIdDao;
    private final String TAG = RegistrationRepository.class.getName();

    public RegistrationRepository() {
        SekretessDatabase sekretessDatabase = SekretessDatabase
                .getInstance(SekretessDependencyProvider.applicationContext());
        this.registrationIdDao = sekretessDatabase.registrationIdDao();
    }

    public int getRegistrationId() {
        Integer registrationId = registrationIdDao.getRegistrationId();
        if (registrationId == null) {
            return 0;
        }
        return registrationId;
    }

    public void storeRegistrationId(Integer registrationId) {
        registrationIdDao.insert(new RegistrationIdStoreEntity(registrationId));
    }

    public void clearStorage() {
        registrationIdDao.delete();
    }
}
