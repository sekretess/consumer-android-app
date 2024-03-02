package com.sekretess.dto;

import lombok.Data;


public class UserDto {
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIpk() {
        return ipk;
    }

    public void setIpk(String ipk) {
        this.ipk = ipk;
    }

    public String getSpk() {
        return spk;
    }

    public void setSpk(String spk) {
        this.spk = spk;
    }

    public String[] getOpk() {
        return opk;
    }

    public void setOpk(String[] opk) {
        this.opk = opk;
    }

    public String getSigPrekey() {
        return sigPrekey;
    }

    public void setSigPrekey(String sigPrekey) {
        this.sigPrekey = sigPrekey;
    }

    public Channel[] getChannels() {
        return channels;
    }

    public void setChannels(Channel[] channels) {
        this.channels = channels;
    }

    private String username;
    private String password;
    private String email;
    private String ipk;
    private String spk;
    private String[] opk;
    private String sigPrekey;
    private Channel[] channels;
}
