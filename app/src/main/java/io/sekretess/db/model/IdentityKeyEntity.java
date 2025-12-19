package io.sekretess.db.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "identity_key_store")
public class IdentityKeyEntity  {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int deviceId;
    private String name;
    private String identityKey;
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    private String createdAt;

    public IdentityKeyEntity(int deviceId, String name, String identityKey) {
        this.deviceId = deviceId;
        this.name = name;
        this.identityKey = identityKey;
    }

    public String getCreatedAt() {
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

    public int getId() {
        return id;
    }
}
