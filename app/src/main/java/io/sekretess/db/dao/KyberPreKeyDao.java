package io.sekretess.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.sekretess.db.model.KyberPreKeyEntity;

@Dao
public interface KyberPreKeyDao {
    @Query("UPDATE kyber_prekey_record_store SET used = 1 WHERE prekeyId = :kyberPreKeyId")
    void markUsed(int kyberPreKeyId);

    @Insert
    void insert(KyberPreKeyEntity kyberPreKeyEntity);

    @Query("SELECT * FROM kyber_prekey_record_store WHERE prekeyId = :kyberPreKeyId")
    KyberPreKeyEntity loadKyberPreKey(int kyberPreKeyId);

    @Query("SELECT * FROM kyber_prekey_record_store")
    List<KyberPreKeyEntity> loadKyberPreKeys();

    @Query("DELETE FROM kyber_prekey_record_store")
    void clear();

}
