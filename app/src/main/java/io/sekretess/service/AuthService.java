package io.sekretess.service;


import android.util.Log;

import com.auth0.android.jwt.JWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

import io.sekretess.Constants;
import io.sekretess.SekretessApplication;
import io.sekretess.dto.AuthRequest;
import io.sekretess.dto.AuthResponse;
import io.sekretess.dto.RefreshTokenRequestDto;
import io.sekretess.exception.IncorrectTokenSyntaxException;
import io.sekretess.exception.TokenExpiredException;
import io.sekretess.repository.AuthRepository;
import io.sekretess.utils.ApiClient;

public class AuthService {
    private final String TAG = AuthService.class.getName();
    private final ApiClient apiClient;
    private final AuthRepository authRepository;
    private final ObjectMapper objectMapper;
    private String username;


    public AuthService(ApiClient apiClient, AuthRepository authRepository) {
        this.apiClient = apiClient;
        this.authRepository = authRepository;
        this.objectMapper = new ObjectMapper();
    }

    public Optional<AuthResponse> authorizeUser(AuthRequest authRequest) {
        try {
            AuthResponse login = apiClient.login(authRequest.username(),
                    authRequest.password());
            authRepository.storeAuthState(objectMapper.writeValueAsString(login));
            return Optional.of(login);
        } catch (Exception e) {
            Log.e(TAG, "Error occurred during authorize user.", e);
            return Optional.empty();
        }
    }

    public boolean isAuthorized() {
        try {
            JWT jwt = getAccessToken();
            this.username = jwt.getClaim(Constants.USERNAME_CLAIM).asString();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void logout() {
        authRepository.clearUserData();
        apiClient.logout();
    }

    private Optional<AuthResponse> refreshAccessToken() throws TokenExpiredException, JsonProcessingException {
        JWT refreshToken = getRefreshToken();
        if (refreshToken.isExpired(0)) {
            throw new TokenExpiredException("Refresh token expired at " + refreshToken.getExpiresAt());
        }
        RefreshTokenRequestDto refreshTokenRequestDto = new RefreshTokenRequestDto(refreshToken.toString());
        return apiClient.refresh(refreshTokenRequestDto);
    }


    public JWT getAccessToken() throws TokenExpiredException, IncorrectTokenSyntaxException {
        try {
            AuthResponse authResponse = objectMapper.readValue(authRepository.getAuthState(), AuthResponse.class);
            String token = authResponse.accessToken();
            JWT jwt = new JWT(token);
            if (jwt.isExpired(0)) {
                authResponse = refreshAccessToken().orElseThrow(() -> new TokenExpiredException("Invalid access token"));
                authRepository.storeAuthState(objectMapper.writeValueAsString(authResponse));
                return new JWT(authResponse.accessToken());
            }
            return jwt;
        } catch (JsonProcessingException e) {
            throw new IncorrectTokenSyntaxException("Incorrect token syntax.");
        }
    }

    private JWT getRefreshToken() throws JsonProcessingException {
        AuthResponse authResponse = objectMapper.readValue(authRepository.getAuthState(), AuthResponse.class);
        String token = authResponse.refreshToken();
        return new JWT(token);
    }

    public boolean clearUserData() {
        return authRepository.clearUserData();
    }

    public String getUsername() {
        return username;
    }
}
