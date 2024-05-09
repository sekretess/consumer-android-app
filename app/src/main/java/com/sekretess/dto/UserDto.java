package com.sekretess.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {
    private Integer regId;
    private String username;
    private String password;
    private String email;
    private String ik;
    private String spk;
    private String[] opk;
    private String SPKSignature;
    private Channel[] channels;
    private String spkID;

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


    public Channel[] getChannels() {
        return channels;
    }

    public void setChannels(Channel[] channels) {
        this.channels = channels;
    }

    public Integer getRegId() {
        return regId;
    }

    public void setRegId(Integer regId) {
        this.regId = regId;
    }


    public String getSPKSignature() {
        return SPKSignature;
    }

    public void setSPKSignature(String SPKSignature) {
        this.SPKSignature = SPKSignature;
    }

    public String getIk() {
        return ik;
    }

    public void setIk(String ik) {
        this.ik = ik;
    }

    public String getSpkID() {
        return spkID;
    }

    public void setSpkID(String spkID) {
        this.spkID = spkID;
    }
}
