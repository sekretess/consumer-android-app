package com.sekretess.dto;


public class RegistrationAndDeviceId {
    private int registrationId;
    private int deviceId;

    public RegistrationAndDeviceId() {
    }

    public RegistrationAndDeviceId(int registrationId, int deviceId) {
        this.registrationId = registrationId;
        this.deviceId = deviceId;
    }

    public int getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(int registrationId) {
        this.registrationId = registrationId;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }
}
