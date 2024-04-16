package com.sekretess.dto.jwt;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Base64;

public class Token {
    private final Header header;
    private final Payload payload;

    @JsonIgnore
    private String token;

    Token(String token) throws IOException {
        this.token = token;
        String[] tokenParts = token.split("\\.");
        Base64.Decoder base64Decoder = Base64.getDecoder();
//
        ObjectMapper objectMapper = new ObjectMapper();
        this.header = objectMapper.readValue(base64Decoder.decode(tokenParts[0]), Header.class);
        this.payload = objectMapper.readValue(base64Decoder.decode(tokenParts[1]), Payload.class);
    }

    public Header getHeader() {
        return header;
    }

    public Payload getPayload() {
        return payload;
    }

    public String getToken() {
        return token;
    }
}
