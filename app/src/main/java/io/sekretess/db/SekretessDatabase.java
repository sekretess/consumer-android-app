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
import io.sekretess.db.dao.KyberPreKeyDao;
import io.sekretess.db.dao.MessageDao;
import io.sekretess.db.dao.PreKeyDao;
import io.sekretess.db.dao.RegistrationIdDao;
import io.sekretess.db.dao.SenderKeyDao;
import io.sekretess.db.dao.SessionDao;
import io.sekretess.db.dao.SignedPreKeyDao;
import io.sekretess.db.model.AuthStateStoreEntity;
import io.sekretess.db.model.IdentityKeyEntity;
import io.sekretess.db.model.IdentityKeyPairEntity;
import io.sekretess.db.model.KyberPreKeyEntity;
import io.sekretess.db.model.MessageEntity;
import io.sekretess.db.model.PreKeyRecordEntity;
import io.sekretess.db.model.RegistrationIdEntity;
import io.sekretess.db.model.SenderKeyEntity;
import io.sekretess.db.model.SessionEntity;
import io.sekretess.db.model.SignedPreKeyRecordEntity;

@Database(entities = {AuthStateStoreEntity.class, IdentityKeyEntity.class, RegistrationIdEntity.class,
        IdentityKeyPairEntity.class, KyberPreKeyEntity.class, MessageEntity.class,
        SenderKeyEntity.class, SessionEntity.class, PreKeyRecordEntity.class,
        SignedPreKeyRecordEntity.class},
        version = 1, exportSchema = false)
public abstract class SekretessDatabase extends RoomDatabase {

    private static volatile SekretessDatabase INSTANCE;

    public abstract AuthDao authDao();

    public abstract IdentityKeyDao identityKeyDao();

    public abstract RegistrationIdDao registrationIdDao();

    public abstract IdentityKeyPairDao identityKeyPairDao();

    public abstract KyberPreKeyDao kyberPreKeyRecordDao();

    public abstract MessageDao messageStoreDao();

    public abstract SenderKeyDao senderKeyDao();

    public abstract SessionDao sessionDao();

    public abstract PreKeyDao preKeyDao();

    public abstract SignedPreKeyDao signedPreKeyDao();


    public static SekretessDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SekretessDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            SekretessDatabase.class,
                            "sekretess_database"
                    )
                            .allowMainThreadQueries()
                            .setJournalMode(JournalMode.TRUNCATE)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static final DateTimeFormatter dateTimeFormatter
            = DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.systemDefault());

}
