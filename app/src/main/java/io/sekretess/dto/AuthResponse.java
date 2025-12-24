package io.sekretess.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthResponse(
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("id_token")
        String idToken,

        @JsonProperty("expires_in")
        int expiresIn,

        @JsonProperty("refresh_expires_in")
        int refreshExpiresIn,

        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("not-before-policy")
        int notBeforePolicy,

        @JsonProperty("session_state")
        String sessionState,

        @JsonProperty("scope")
        String scope) {
}
