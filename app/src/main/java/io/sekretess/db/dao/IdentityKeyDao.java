package io.sekretess.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import io.sekretess.db.model.IdentityKeyEntity;

@Dao
public interface IdentityKeyDao {
    @Insert
    void insert(IdentityKeyEntity identityKeyEntity);

    @Query("SELECT * FROM identity_key_store LIMIT 1")
    IdentityKeyEntity getIdentityKey();

    @Query("SELECT * FROM identity_key_store WHERE deviceId = :deviceId AND name = :name LIMIT 1")
    IdentityKeyEntity getIdentityKey(int deviceId, String name);


    @Update
    void update(IdentityKeyEntity identityKeyEntity);

    @Query("DELETE FROM identity_key_store")
    void delete();
}
