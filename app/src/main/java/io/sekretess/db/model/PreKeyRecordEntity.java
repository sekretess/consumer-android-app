package io.sekretess.db.model;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "prekey_record_store")
public class PreKeyRecordEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int preKeyId;
    private String preKeyRecord;
    private long createdAt;


    public PreKeyRecordEntity(int preKeyId, String preKeyRecord, long createdAt) {
        this.preKeyId = preKeyId;
        this.preKeyRecord = preKeyRecord;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public int getPreKeyId() {
        return preKeyId;
    }

    public String getPreKeyRecord() {
        return preKeyRecord;
    }

    public void setPreKeyId(int preKeyId) {
        this.preKeyId = preKeyId;
    }

    public void setPreKeyRecord(String preKeyRecord) {
        this.preKeyRecord = preKeyRecord;
    }

    public long getCreatedAt() {
        return createdAt;
    }

}
