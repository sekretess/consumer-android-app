package io.sekretess.db.dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import io.sekretess.db.model.SenderKeyEntity;

@Dao
public interface SenderKeyDao {

    @Insert
    void insert(SenderKeyEntity senderKeyEntity);

    @Query("SELECT * FROM sender_key_store WHERE addressName = :name AND deviceId = :deviceId " +
            "AND distributionUuid = :distributionId")
    SenderKeyEntity getSenderKeyRecord(int deviceId, String name, String distributionId);

    @Query("DELETE FROM sender_key_store")
    void clear();
}
