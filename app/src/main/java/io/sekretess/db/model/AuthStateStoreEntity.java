package io.sekretess.db.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "auth_state_store")
public class AuthStateStoreEntity  {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String authState;
    private long createdAt;

    public AuthStateStoreEntity(String authState, long createdAt){
        this.authState = authState;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public String getAuthState() {
        return authState;
    }

    public void setAuthState(String authState) {
        this.authState = authState;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
