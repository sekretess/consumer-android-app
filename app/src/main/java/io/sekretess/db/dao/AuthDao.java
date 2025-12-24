package io.sekretess.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import io.sekretess.db.model.AuthStateStoreEntity;

@Dao
public interface AuthDao {
    @Insert
    void insert(AuthStateStoreEntity authEntity);

    @Query("DELETE FROM auth_state_store")
    void delete();

    @Query("SELECT * FROM auth_state_store LIMIT 1")
    AuthStateStoreEntity getAuthState();
}
