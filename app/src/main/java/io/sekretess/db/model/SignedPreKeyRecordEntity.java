package io.sekretess.db.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "signed_pre_key_record_store")
public class SignedPreKeyRecordEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String spkRecord;
    private int spkId;
    private boolean used;
    private long createdAt;

    public SignedPreKeyRecordEntity(String spkRecord, int spkId, boolean used, long createdAt) {
        this.spkRecord = spkRecord;
        this.spkId = spkId;
        this.used = used;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSpkRecord() {
        return spkRecord;
    }

    public void setSpkRecord(String spkRecord) {
        this.spkRecord = spkRecord;
    }

    public int getSpkId() {
        return spkId;
    }

    public void setSpkId(int spkId) {
        this.spkId = spkId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}
