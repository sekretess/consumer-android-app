package io.sekretess.db.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "auth_state_store")
public class AuthStateStoreEntity  {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String authState;

    public AuthStateStoreEntity(String authState){
        this.authState = authState;
    }

    public int getId() {
        return id;
    }

    public String getAuthState() {
        return authState;
    }

    public void setAuthState(String authState) {
        this.authState = authState;
    }
}
