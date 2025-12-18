package io.sekretess.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import io.sekretess.db.model.RegistrationIdStoreEntity;

@Dao
public interface RegistrationIdDao {

    @Insert
    void insert(RegistrationIdStoreEntity registrationIdStoreEntity);

    @Query("SELECT registrationId FROM registration_id_store ORDER BY created_at DESC LIMIT 1")
    Integer getRegistrationId();

    @Query("DELETE FROM registration_id_store")
    void delete();
}
