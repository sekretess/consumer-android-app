package io.sekretess.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.sekretess.db.model.SessionEntity;

@Dao
public interface SessionDao {
    @Query("DELETE FROM session_store WHERE addressName = :name AND deviceId = :deviceId")
    void removeSession(String name, int deviceId);

    @Query("DELETE FROM session_store WHERE addressName = :name")
    void removeSession(String name);

    @Query("SELECT * FROM session_store WHERE addressName = :name AND deviceId = :deviceId")
    SessionEntity findSession(int deviceId, String name);

    @Query("SELECT deviceId FROM session_store WHERE addressName = :name and deviceId > 1")
    List<Integer> getSubDeviceSessions(String name);

    @Query("DELETE FROM session_store")
    void clear();

    @Insert
    void insert(SessionEntity sessionEntity);
}
