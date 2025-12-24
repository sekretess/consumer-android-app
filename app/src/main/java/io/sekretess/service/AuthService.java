package io.sekretess.service;


import android.util.Log;

import com.auth0.android.jwt.JWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

import io.sekretess.Constants;
import io.sekretess.dependency.SekretessDependencyProvider;
import io.sekretess.dto.AuthRequest;
import io.sekretess.dto.AuthResponse;
import io.sekretess.dto.RefreshTokenRequestDto;
import io.sekretess.exception.IncorrectTokenSyntaxException;
import io.sekretess.exception.RefreshTokenExpiredException;
import io.sekretess.exception.TokenExpiredException;
import io.sekretess.db.repository.AuthRepository;
import io.sekretess.exception.UnAuhthorizedException;

public class AuthService {
    private final String TAG = AuthService.class.getName();
    private final AuthRepository authRepository;
    private final ObjectMapper objectMapper;
    private String username;


    public AuthService(AuthRepository authRepository) {
        this.authRepository = authRepository;
        this.objectMapper = new ObjectMapper();
    }

    public Optional<AuthResponse> authorizeUser(AuthRequest authRequest) {
        try {
            AuthResponse login = SekretessDependencyProvider.apiClient().login(authRequest.username(),
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
            Log.e(TAG, "Error occurred during check authorization", e);
            return false;
        }
    }

    public void logout() {
        authRepository.removeAuthState();
        SekretessDependencyProvider.apiClient().logout();
    }

    private Optional<AuthResponse> refreshAccessToken() throws TokenExpiredException, JsonProcessingException,
            RefreshTokenExpiredException {
        JWT refreshToken = getRefreshToken();
        if (refreshToken.isExpired(0)) {
            authRepository.removeAuthState();
            throw new RefreshTokenExpiredException("Refresh token expired at " + refreshToken.getExpiresAt());
        }
        RefreshTokenRequestDto refreshTokenRequestDto = new RefreshTokenRequestDto(refreshToken.toString());
        return SekretessDependencyProvider.apiClient().refresh(refreshTokenRequestDto);
    }


    public JWT getAccessToken() throws TokenExpiredException, IncorrectTokenSyntaxException,
            UnAuhthorizedException, RefreshTokenExpiredException {
        try {
            String authState = authRepository.getAuthState()
                    .orElseThrow(() -> new UnAuhthorizedException("Token not found"));

            AuthResponse authResponse = objectMapper.readValue(authState, AuthResponse.class);
            String token = authResponse.accessToken();
            JWT jwt = new JWT(token);
            if (jwt.isExpired(5)) {
                authResponse = refreshAccessToken()
                        .orElseThrow(() -> new TokenExpiredException("Invalid access token"));
                authRepository.storeAuthState(objectMapper.writeValueAsString(authResponse));
                return new JWT(authResponse.accessToken());
            }
            return jwt;
        } catch (JsonProcessingException e) {
            throw new IncorrectTokenSyntaxException("Incorrect token syntax.");
        }
    }

    private JWT getRefreshToken() throws JsonProcessingException, TokenExpiredException {
        String authState = authRepository.getAuthState()
                .orElseThrow(() -> new TokenExpiredException("Token not found"));
        AuthResponse authResponse = objectMapper.readValue(authState, AuthResponse.class);
        String token = authResponse.refreshToken();
        return new JWT(token);
    }

    public void clearUserData() {
        authRepository.removeAuthState();
    }

    public String getUsername() {
        return username;
    }
}
