package io.sekretess.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.sekretess.db.model.SignedPreKeyRecordEntity;

@Dao
public interface SignedPreKeyDao {
    @Query("SELECT * FROM signed_pre_key_record_store")
    List<SignedPreKeyRecordEntity> getAll();

    @Query("SELECT * FROM signed_pre_key_record_store WHERE spkId = :signedPreKeyId and used = 0")
    SignedPreKeyRecordEntity getSignedPreKeyRecord(int signedPreKeyId);

    @Query("UPDATE signed_pre_key_record_store SET used = 1 WHERE spkId = :signedPreKeyId")
    void removeSignedPreKey(int signedPreKeyId);

    @Insert
    void insert(SignedPreKeyRecordEntity signedPreKeyRecordEntity);

}
