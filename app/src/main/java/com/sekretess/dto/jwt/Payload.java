package com.sekretess.dto.jwt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Payload {
    @JsonProperty("exp")
    private Long expireTime;
    private String preferredUsername;

    @JsonProperty("sekretess_ipk")
    private String identityKeyPair;
    @JsonProperty("sekretess_registration_id")
    private Integer registrationId;

    @JsonProperty("sekretess_signed_prekey")
    private String signedPrekey;


    @JsonProperty("sekretess_opk")
    private String[] oneTimePrekeys;

    public String[] getOneTimePrekeys() {
        return oneTimePrekeys;
    }

    public void setOneTimePrekeys(String[] oneTimePrekeys) {
        this.oneTimePrekeys = oneTimePrekeys;
    }

    public String getSignedPrekey() {
        return signedPrekey;
    }

    public void setSignedPrekey(String signedPrekey) {
        this.signedPrekey = signedPrekey;
    }

    public Integer getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(Integer registrationId) {
        this.registrationId = registrationId;
    }

    public String getIdentityKeyPair() {
        return identityKeyPair;
    }

    public void setIdentityKeyPair(String identityKeyPair) {
        this.identityKeyPair = identityKeyPair;
    }

    public Long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
    }

    public String getPreferredUsername() {
        return preferredUsername;
    }

    public void setPreferredUsername(String preferredUsername) {
        this.preferredUsername = preferredUsername;
    }


}
