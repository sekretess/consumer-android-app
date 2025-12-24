package io.sekretess.db.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "identity_key_store")
public class IdentityKeyEntity  {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private int deviceId;
    private String name;
    private String identityKey;
    private long createdAt;
    public IdentityKeyEntity(int deviceId, String name, String identityKey, long createdAt) {
        this.deviceId = deviceId;
        this.name = name;
        this.identityKey = identityKey;
        this.createdAt = createdAt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public String getName() {
        return name;
    }

    public String getIdentityKey() {
        return identityKey;
    }

    public void setIdentityKey(String identityKey) {
        this.identityKey = identityKey;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
