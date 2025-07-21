package io.sekretess.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeyBundleDto {
    private Integer regId;
    private String ik;
    private String spk;
    private String[] opk;
    private String SPKSignature;
    private Channel[] channels;
    private String spkID;

    @JsonProperty("PQSPK")
    private String pqspk;

    @JsonProperty("PQSPKID")
    private String pqspkid;

    @JsonProperty("PQSPKSignature")
    private String pqspkSignature;

    @JsonProperty("OPQK")
    private String[] opqk;

    public String getPqspk() {
        return pqspk;
    }

    public void setOpqk(String[] opqk) {
        this.opqk = opqk;
    }

    public String getPqspkid() {
        return pqspkid;
    }

    public void setPqspk(String pqspk) {
        this.pqspk = pqspk;
    }

    public String getPqspkSignature() {
        return pqspkSignature;
    }

    public void setPqspkid(String pqspkid) {
        this.pqspkid = pqspkid;
    }

    public String[] getOpqk() {
        return opqk;
    }

    public void setPqspkSignature(String pqspkSignature) {
        this.pqspkSignature = pqspkSignature;
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
