package io.sekretess.db.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "registration_id_store")
public class RegistrationIdEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private Integer registrationId;

    private long createdAt;

    public RegistrationIdEntity(Integer registrationId, long createdAt) {
        this.registrationId = registrationId;
        this.createdAt = createdAt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public Integer getRegistrationId() {
        return registrationId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
