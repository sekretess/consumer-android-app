package io.sekretess.db.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "kyber_prekey_record_store")
public class KyberPreKeyEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private int prekeyId;
    private String kpkRecord;
    private boolean used = false;
    private long createdAt;

    public KyberPreKeyEntity(int prekeyId, String kpkRecord, long createdAt) {
        this.prekeyId = prekeyId;
        this.kpkRecord = kpkRecord;
        this.createdAt = createdAt;
    }

    public int getPrekeyId() {
        return prekeyId;
    }

    public void setPrekeyId(int prekeyId) {
        this.prekeyId = prekeyId;
    }

    public String getKpkRecord() {
        return kpkRecord;
    }

    public void setKpkRecord(String kpkRecord) {
        this.kpkRecord = kpkRecord;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
