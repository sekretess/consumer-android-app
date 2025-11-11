package io.sekretess.service;


import com.auth0.android.jwt.JWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

import io.sekretess.Constants;
import io.sekretess.SekretessApplication;
import io.sekretess.dto.AuthRequest;
import io.sekretess.dto.AuthResponse;
import io.sekretess.dto.RefreshTokenRequestDto;
import io.sekretess.exception.TokenExpiredException;
import io.sekretess.repository.AuthRepository;

public class AuthService {
    private final SekretessApplication sekretessApplication;
    private final AuthRepository authRepository;
    private final ObjectMapper objectMapper;

    public AuthService(SekretessApplication sekretessApplication, AuthRepository authRepository) {
        this.sekretessApplication = sekretessApplication;
        this.authRepository = authRepository;
        this.objectMapper = new ObjectMapper();
    }

    public Optional<AuthResponse> authorizeUser(AuthRequest authRequest) {
        try {
            AuthResponse login = sekretessApplication.getApiClient().login(authRequest.username(),
                    authRequest.password());
            authRepository.storeAuthState(objectMapper.writeValueAsString(login));
            return Optional.of(login);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public boolean isAuthorized() {
        try {
            return getAccessToken() != null;
        } catch (Exception e) {
            return false;
        }
    }


    private Optional<AuthResponse> refreshAccessToken() throws TokenExpiredException {
        JWT refreshToken = getRefreshToken();
        if (refreshToken.isExpired(0)) {
            throw new TokenExpiredException("Refresh token expired at " + refreshToken.getExpiresAt());
        }
        RefreshTokenRequestDto refreshTokenRequestDto = new RefreshTokenRequestDto(refreshToken.toString());
        return sekretessApplication.getApiClient().refresh(refreshTokenRequestDto);
    }

    public String getUserNameFromJwt() throws Exception {
        JWT jwt = getAccessToken();
        return jwt.getClaim(Constants.USERNAME_CLAIM).asString();
    }


    public JWT getAccessToken() throws TokenExpiredException {
        AuthResponse authResponse = objectMapper.readValue(authRepository.getAuthState(), AuthResponse.class);
        String token = authResponse.accessToken();
        JWT jwt = new JWT(token);
        if (jwt.isExpired(0)) {
            authResponse = refreshAccessToken().orElseThrow(() -> new TokenExpiredException("Invalid access token"));
            authRepository.storeAuthState(objectMapper.writeValueAsString(authResponse));
            return new JWT(authResponse.accessToken());
        }
        return jwt;
    }

    private JWT getRefreshToken() throws Exception {
        AuthResponse authResponse = objectMapper.readValue(authRepository.getAuthState(), AuthResponse.class);
        String token = authResponse.refreshToken();
        return new JWT(token);
    }

    public boolean clearUserData() {
        return authRepository.clearUserData();
    }
}
