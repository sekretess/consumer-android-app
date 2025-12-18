package io.sekretess.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import io.sekretess.db.model.IdentityKeyPairEntity;

@Dao
public interface IdentityKeyPairDao {
    @Query("SELECT * FROM identity_key_pair_store LIMIT 1")
    IdentityKeyPairEntity getIdentityKeyPair();

    @Insert
    void insert(IdentityKeyPairEntity identityKeyPairEntity);

    @Query("DELETE FROM identity_key_pair_store")
    void delete();
}
