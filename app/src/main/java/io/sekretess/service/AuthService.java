package io.sekretess.service;


import android.util.Log;

import com.auth0.android.jwt.JWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Date;
import java.util.Optional;

import io.sekretess.Constants;
import io.sekretess.dependency.SekretessDependencyProvider;
import io.sekretess.dto.AuthRequest;
import io.sekretess.dto.AuthResponse;
import io.sekretess.dto.RefreshTokenRequestDto;
import io.sekretess.enums.SekretessEvent;
import io.sekretess.exception.IncorrectTokenSyntaxException;
import io.sekretess.exception.RefreshTokenExpiredException;
import io.sekretess.exception.TokenExpiredException;
import io.sekretess.db.repository.AuthRepository;
import io.sekretess.exception.TokenNotFoundException;
import io.sekretess.exception.UnAuhthorizedException;
import io.sekretess.utils.ApiClient;

public class AuthService {

    private static final String TAG = AuthService.class.getSimpleName();

    private final AuthRepository authRepository;
    private final ObjectMapper objectMapper;
    private final ApiClient apiClient;

    public AuthService(AuthRepository authRepository, ApiClient apiClient) {
        this.authRepository = authRepository;
        this.apiClient = apiClient;
        this.objectMapper = new ObjectMapper();
    }

    public void authorize(AuthRequest request) throws UnAuhthorizedException {
        try {
            AuthResponse response = apiClient.login(request.username(), request.password());
            persistAuthState(response);
        } catch (Exception e) {
            Log.e(TAG, "Authorization failed", e);
            throw new UnAuhthorizedException("Authorization failed", e);
        }
    }

    public JWT getAccessToken() throws TokenExpiredException {
        try {
            AuthResponse auth = readAuthState();
            JWT accessToken = new JWT(auth.accessToken());

            if (!isTokenExpired(accessToken)) {
                return accessToken;
            }

            AuthResponse refreshed = refreshAccessToken(auth);
            persistAuthState(refreshed);
            return new JWT(refreshed.accessToken());

        } catch (Exception e) {
            Log.e(TAG, "Access token retrieval failed", e);
            SekretessDependencyProvider.getSekretessEventMutableLiveData().postValue(SekretessEvent.AUTH_FAILED);
            throw new TokenExpiredException("Session expired", e);
        }
    }

    public boolean isAuthorized() {
        try {
            getAccessToken();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void logout() {
        authRepository.removeAuthState();
        apiClient.logout();
    }

    public void clearUserData() {
        authRepository.removeAuthState();
    }

    public String getUsername() throws TokenExpiredException {
        return getAccessToken()
                .getClaim(Constants.USERNAME_CLAIM)
                .asString();
    }

    // ----------------- Helpers -----------------

    private AuthResponse refreshAccessToken(AuthResponse auth) throws RefreshTokenExpiredException, UnAuhthorizedException {
        JWT refreshToken = new JWT(auth.refreshToken());

        if (isTokenExpired(refreshToken)) {
            clearUserData();
            throw new RefreshTokenExpiredException("Refresh token expired");
        }

        return apiClient.refresh(new RefreshTokenRequestDto(refreshToken.toString()))
                .orElseThrow(() -> new UnAuhthorizedException("Refresh failed"));
    }

    private boolean isTokenExpired(JWT jwt) {
        Date expiresAt = jwt.getExpiresAt();
        if (expiresAt == null) return true;

        long leewayMillis = 5_000;
        return expiresAt.before(new Date(System.currentTimeMillis() + leewayMillis));
    }

    private AuthResponse readAuthState()
            throws JsonProcessingException, TokenNotFoundException {

        String authState = authRepository.getAuthState()
                .orElseThrow(() -> new TokenNotFoundException("Auth state not found"));

        return objectMapper.readValue(authState, AuthResponse.class);
    }

    private void persistAuthState(AuthResponse response)
            throws JsonProcessingException {
        authRepository.storeAuthState(objectMapper.writeValueAsString(response));
    }
}

