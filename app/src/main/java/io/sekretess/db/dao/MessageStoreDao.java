package io.sekretess.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.sekretess.db.model.MessageStoreEntity;

@Dao
public interface MessageStoreDao {
    @Insert
    void insert(MessageStoreEntity messageStoreEntity);

    @Query("SELECT * FROM sekretes_message_store WHERE username=:username ORDER BY created_at DESC LIMIT 1")
    List<MessageStoreEntity> getMessages(String username);

    @Query("SELECT sender FROM sekretes_message_store ORDER BY created_at DESC LIMIT 4")
    List<String> getTopSenders();
}
