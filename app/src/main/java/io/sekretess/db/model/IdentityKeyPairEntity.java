package io.sekretess.db.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "identity_key_pair_store")
public class IdentityKeyPairEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String identityKeyPair;
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    private Date createdAt;

    public IdentityKeyPairEntity(String identityKeyPair) {
        this.identityKeyPair = identityKeyPair;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getIdentityKeyPair() {
        return identityKeyPair;
    }

    public int getId() {
        return id;
    }


}
