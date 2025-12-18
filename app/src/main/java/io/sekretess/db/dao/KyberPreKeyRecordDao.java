package io.sekretess.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.sekretess.db.model.KyberPreKeyRecordEntity;

@Dao
public interface KyberPreKeyRecordDao {
    @Query("UPDATE kyber_prekey_record_store SET used = 1 WHERE prekeyId = :kyberPreKeyId")
    void markUsed(int kyberPreKeyId);

    @Insert
    void insert(KyberPreKeyRecordEntity kyberPreKeyRecordEntity);

    @Query("SELECT * FROM kyber_prekey_record_store WHERE prekeyId = :kyberPreKeyId")
    KyberPreKeyRecordEntity loadKyberPreKey(int kyberPreKeyId);

    @Query("SELECT * FROM kyber_prekey_record_store")
    List<KyberPreKeyRecordEntity> loadKyberPreKeys();

    @Query("DELETE FROM kyber_prekey_record_store")
    void clear();

}
