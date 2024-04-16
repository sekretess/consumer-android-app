package com.sekretess.dto.jwt;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Jwt {
    private final static String ACCESS_TOKEN = "access_token";
    private final static String ID_TOKEN = "id_token";
    private final static String EXPIRES_IN = "expires_in";

    private final static String REFRESH_EXPIRES_IN = "refresh_expires_in";
    private final static String REFRESH_TOKEN = "refresh_token";

    private final static String TOKEN_TYPE = "token_type";
    private final static String NO_BEFORE_POLICY = "no_before_policy";
    private final static String SESSION_STATE = "session_state";
    private final static String SCOPE = "scope";

    private Token accessToken;
    private Token idToken;
    private Integer expiresIn;

    private Integer refreshExpiresIn;

    private Token refreshToken;

    @JsonIgnore
    private String jwtStr;

    private Jwt() {

    }

    public String getJwtStr() {
        return jwtStr;
    }

    public Token getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(Token accessToken) {
        this.accessToken = accessToken;
    }

    public Token getIdToken() {
        return idToken;
    }

    public void setIdToken(Token idToken) {
        this.idToken = idToken;
    }

    public Token getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(Token refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setJwtStr(String jwtStr) {
        this.jwtStr = jwtStr;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Integer expiresIn) {
        this.expiresIn = expiresIn;
    }

    public Integer getRefreshExpiresIn() {
        return refreshExpiresIn;
    }

    public void setRefreshExpiresIn(Integer refreshExpiresIn) {
        this.refreshExpiresIn = refreshExpiresIn;
    }


    public static Jwt fromString(String jwtStr) {
        try {
            Map<String, Object> values = new ObjectMapper().readValue(jwtStr, Map.class);
            Jwt jwt = new Jwt();
            jwt.jwtStr = jwtStr;
            jwt.parse(values);
            return jwt;
        } catch (Exception e) {
            return null;
        }
    }

    public void parse(Map<String, Object> values) throws IOException {
        this.accessToken = new Token((String)values.get(ACCESS_TOKEN));
        //
        this.idToken = new Token((String)values.get(ID_TOKEN));
        //
        this.expiresIn = (int)values.get(EXPIRES_IN);
        //
        this.refreshExpiresIn = (int)(values.get(REFRESH_EXPIRES_IN));
        //
        this.refreshToken = new Token((String)values.get(REFRESH_TOKEN));
    }
}
