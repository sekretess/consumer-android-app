package io.sekretess.db.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "session_store")
public class SessionEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String session;
    private String addressName;
    private String serviceId;
    private int deviceId;
    private long createdAt;

    public SessionEntity(String session, String addressName, String serviceId, int deviceId, long createdAt) {
        this.session = session;
        this.addressName = addressName;
        this.serviceId = serviceId;
        this.deviceId = deviceId;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getAddressName() {
        return addressName;
    }

    public void setAddressName(String addressName) {
        this.addressName = addressName;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
