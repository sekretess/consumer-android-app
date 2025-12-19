package io.sekretess.db;

import android.content.Context;


import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import io.sekretess.db.dao.AuthDao;
import io.sekretess.db.dao.IdentityKeyDao;
import io.sekretess.db.dao.IdentityKeyPairDao;
import io.sekretess.db.dao.KyberPreKeyRecordDao;
import io.sekretess.db.dao.MessageStoreDao;
import io.sekretess.db.dao.RegistrationIdDao;
import io.sekretess.db.model.AuthStateStoreEntity;
import io.sekretess.db.model.IdentityKeyEntity;
import io.sekretess.db.model.IdentityKeyPairEntity;
import io.sekretess.db.model.KyberPreKeyRecordEntity;
import io.sekretess.db.model.MessageStoreEntity;
import io.sekretess.db.model.RegistrationIdStoreEntity;

@Database(entities = {AuthStateStoreEntity.class, IdentityKeyEntity.class,
        RegistrationIdStoreEntity.class, IdentityKeyPairEntity.class,
        KyberPreKeyRecordEntity.class, MessageStoreEntity.class}, version = 1, exportSchema = false)
public abstract class SekretessDatabase extends RoomDatabase {

    private static volatile SekretessDatabase INSTANCE;

    public abstract AuthDao authDao();

    public abstract IdentityKeyDao identityKeyDao();

    public abstract RegistrationIdDao registrationIdDao();

    public abstract IdentityKeyPairDao identityKeyPairDao();

    public abstract KyberPreKeyRecordDao kyberPreKeyRecordDao();

    public abstract MessageStoreDao messageStoreDao();

    public static SekretessDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SekretessDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            SekretessDatabase.class,
                            "sekretess_database"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }

    public static final DateTimeFormatter dateTimeFormatter
            = DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.systemDefault());


}
