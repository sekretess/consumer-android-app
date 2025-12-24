package io.sekretess.db.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "signed_pre_key_record_store")
public class SignedPreKeyRecordEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String spkRecord;
    public int spkId;
    public long createdAt;

    public SignedPreKeyRecordEntity(String spkRecord, int spkId, long createdAt) {
        this.spkRecord = spkRecord;
        this.spkId = spkId;
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

}
