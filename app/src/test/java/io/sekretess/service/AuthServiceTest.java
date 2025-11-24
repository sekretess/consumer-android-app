package io.sekretess.service;

import com.auth0.android.jwt.JWT;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import io.sekretess.dto.AuthRequest;
import io.sekretess.dto.AuthResponse;
import io.sekretess.exception.IncorrectTokenSyntaxException;
import io.sekretess.exception.TokenExpiredException;
import io.sekretess.repository.AuthRepository;
import io.sekretess.utils.ApiClient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    @Mock
    private ApiClient mockApiClient;

    @Mock
    private AuthRepository mockAuthRepository;

    private AuthService authService;

    // A valid token that expires in the future
    private static final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwidXNlcm5hbWUiOiJqb2huX2RvZSIsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNzM1Njg5NjAwfQ.DR_t_3-p_3-t_3-p_3-t_3-p_3-t_3-p_3-t_3-p_3-t_3-p_3-t_3-p_3-t_3-p_3";
    // An expired token
    private static final String EXPIRED_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwidXNlcm5hbWUiOiJqb2huX2RvZSIsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNTE2MjM5MDIyfQ.v_3-p_3-t_3-p_3-t_3-p_3-t_3-p_3-t_3-p_3-t_3-p_3-t_3-p_3-t_3-p_3";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        authService = new AuthService(mockApiClient, mockAuthRepository);
    }

    @Test
    public void testAuthorizeUser_success() throws Exception {
        AuthRequest authRequest = new AuthRequest("user", "pass");
        AuthResponse authResponse = new AuthResponse(VALID_TOKEN, VALID_TOKEN, 3600, 3600, VALID_TOKEN, "bearer", 0, "session_state", "scope");
        when(mockApiClient.login(anyString(), anyString())).thenReturn(authResponse);

        Optional<AuthResponse> result = authService.authorizeUser(authRequest);

        assertTrue(result.isPresent());
        assertEquals(authResponse, result.get());
        verify(mockAuthRepository).storeAuthState(anyString());
    }

    @Test
    public void testAuthorizeUser_failure() throws Exception {
        AuthRequest authRequest = new AuthRequest("user", "pass");
        when(mockApiClient.login(anyString(), anyString())).thenThrow(new RuntimeException());

        Optional<AuthResponse> result = authService.authorizeUser(authRequest);

        assertFalse(result.isPresent());
    }

    @Test
    public void testIsAuthorized_validToken() throws Exception {
        when(mockAuthRepository.getAuthState()).thenReturn("{\"access_token\":\"" + VALID_TOKEN + "\"}");

        assertTrue(authService.isAuthorized());
    }

    @Test
    public void testIsAuthorized_expiredToken() throws Exception {
        when(mockAuthRepository.getAuthState()).thenReturn("{\"access_token\":\"" + EXPIRED_TOKEN + "\",\"refresh_token\":\""+VALID_TOKEN+"\"}");
        AuthResponse newAuthResponse = new AuthResponse(VALID_TOKEN, VALID_TOKEN, 0, 0, VALID_TOKEN, null, 0, null, null);
        when(mockApiClient.refresh(any())).thenReturn(Optional.of(newAuthResponse));

        assertTrue(authService.isAuthorized());
    }

    @Test
    public void testLogout() {
        authService.logout();
        verify(mockAuthRepository).clearUserData();
        verify(mockApiClient).logout();
    }

    @Test
    public void testGetAccessToken_valid() throws TokenExpiredException, IncorrectTokenSyntaxException, com.fasterxml.jackson.core.JsonProcessingException {
        when(mockAuthRepository.getAuthState()).thenReturn("{\"access_token\":\"" + VALID_TOKEN + "\"}");

        JWT accessToken = authService.getAccessToken();
        assertNotNull(accessToken);
    }

    @Test(expected = TokenExpiredException.class)
    public void testGetAccessToken_expired() throws TokenExpiredException, IncorrectTokenSyntaxException, com.fasterxml.jackson.core.JsonProcessingException {
        when(mockAuthRepository.getAuthState()).thenReturn("{\"access_token\":\"" + EXPIRED_TOKEN + "\",\"refresh_token\":\""+EXPIRED_TOKEN+"\"}");
        when(mockApiClient.refresh(any())).thenReturn(Optional.empty());

        authService.getAccessToken();
    }

    @Test
    public void testClearUserData() {
        authService.clearUserData();
        verify(mockAuthRepository).clearUserData();
    }
}
