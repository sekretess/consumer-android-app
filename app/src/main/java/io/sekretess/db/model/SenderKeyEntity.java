package io.sekretess.db.model;

import android.provider.BaseColumns;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sender_key_store")
public class SenderKeyEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private int deviceId;
    private String addressName;
    private String senderKeyRecord;
    private String distributionUuid;
    private long createdAt;

    public SenderKeyEntity(int deviceId, String addressName, String senderKeyRecord, String distributionUuid, long createdAt) {
        this.deviceId = deviceId;
        this.addressName = addressName;
        this.senderKeyRecord = senderKeyRecord;
        this.distributionUuid = distributionUuid;
        this.createdAt = createdAt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public String getAddressName() {
        return addressName;
    }

    public String getSenderKeyRecord() {
        return senderKeyRecord;
    }

    public String getDistributionUuid() {
        return distributionUuid;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setSenderKeyRecord(String senderKeyRecord) {
        this.senderKeyRecord = senderKeyRecord;
    }

    public void setDistributionUuid(String distributionUuid) {
        this.distributionUuid = distributionUuid;
    }

    public void setAddressName(String addressName) {
        this.addressName = addressName;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }


}

