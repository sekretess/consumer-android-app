package io.sekretess.db.model;

import android.provider.BaseColumns;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "registration_id_store")
public class RegistrationIdStoreEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private Integer registrationId;

    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    private Date createdAt;

    public RegistrationIdStoreEntity(Integer registrationId) {
        this.registrationId = registrationId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Integer getRegistrationId() {
        return registrationId;
    }

    public int getId() {
        return id;
    }
}
