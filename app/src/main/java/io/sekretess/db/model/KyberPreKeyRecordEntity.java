package io.sekretess.db.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "kyber_prekey_record_store")
public class KyberPreKeyRecordEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int prekeyId;
    private String kpkRecord;
    private boolean used;
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    private String createdAt;

    public KyberPreKeyRecordEntity(int prekeyId, String kpkRecord) {
        this.prekeyId = prekeyId;
        this.kpkRecord = kpkRecord;
        this.used = false;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
