package io.sekretess.db.model;

import android.provider.BaseColumns;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sekretes_message_store")
public class MessageEntity implements BaseColumns {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String username;
    private String sender;
    private long createdAt;
    private String messageBody;

    public MessageEntity(String username, String sender, String messageBody, long createdAt) {
        this.username = username;
        this.sender = sender;
        this.messageBody = messageBody;
        this.createdAt = createdAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getId() {
        return id;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setId(long id) {
        this.id = id;
    }
}
