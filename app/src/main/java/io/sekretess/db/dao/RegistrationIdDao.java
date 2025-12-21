package io.sekretess.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import io.sekretess.db.model.RegistrationIdEntity;

@Dao
public interface RegistrationIdDao {

    @Insert
    void insert(RegistrationIdEntity registrationIdEntity);

    @Query("SELECT registrationId FROM registration_id_store ORDER BY createdAt DESC LIMIT 1")
    Integer getRegistrationId();

    @Query("DELETE FROM registration_id_store")
    void delete();
}
