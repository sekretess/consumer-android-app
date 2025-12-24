package io.sekretess.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.sekretess.db.model.PreKeyRecordEntity;

@Dao
public interface PreKeyDao {
    @Query("SELECT * FROM prekey_record_store")
    List<PreKeyRecordEntity> getAll();

    @Query("SELECT COUNT(*) FROM prekey_record_store")
    int getCount();

    @Query("DELETE FROM prekey_record_store WHERE prekeyId = :prekeyId")
    void removePreKeyRecord(int prekeyId);

    @Insert
    void insert(PreKeyRecordEntity preKeyRecordEntity);

    @Query("SELECT * FROM prekey_record_store WHERE prekeyId = :preKeyId")
    PreKeyRecordEntity getPreKey(int preKeyId);

    @Query("DELETE FROM prekey_record_store")
    void clear();
}
