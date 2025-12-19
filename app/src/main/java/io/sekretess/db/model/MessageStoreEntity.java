package io.sekretess.db.model;

import android.provider.BaseColumns;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sekretes_message_store")
public class MessageStoreEntity implements BaseColumns {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String username;
    private String sender;
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    private String createdAt;
    private String messageBody;

    public MessageStoreEntity(String username, String sender, String messageBody) {
        this.username = username;
        this.sender = sender;
        this.messageBody = messageBody;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getId() {
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

    public String getCreatedAt() {
        return createdAt;
    }
}
