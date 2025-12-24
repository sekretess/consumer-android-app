package io.sekretess.db.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "identity_key_pair_store")
public class IdentityKeyPairEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String identityKeyPair;

    private long createdAt;

    public IdentityKeyPairEntity(String identityKeyPair, long createdAt) {
        this.identityKeyPair = identityKeyPair;
        this.createdAt = createdAt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getIdentityKeyPair() {
        return identityKeyPair;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

}
