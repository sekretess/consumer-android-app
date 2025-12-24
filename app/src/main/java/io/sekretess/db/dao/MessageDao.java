package io.sekretess.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.sekretess.db.model.MessageEntity;

@Dao
public interface MessageDao {
    @Insert
    void insert(MessageEntity messageEntity);

    @Query("SELECT * FROM sekretes_message_store WHERE username=:username ORDER BY createdAt DESC")
    List<MessageEntity> getMessages(String username);

    @Query("SELECT * FROM sekretes_message_store WHERE username=:username AND sender=:sender ORDER BY createdAt DESC")
    List<MessageEntity> getMessages(String username, String sender);

    @Query("SELECT sender FROM sekretes_message_store ORDER BY createdAt DESC LIMIT 4")
    List<String> getTopSenders();

    @Query("DELETE FROM sekretes_message_store WHERE id=:messageId")
    void deleteMessage(Long messageId);
}
