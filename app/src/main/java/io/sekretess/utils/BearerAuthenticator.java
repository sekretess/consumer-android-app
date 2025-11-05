package io.sekretess.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.TokenRequest;

import java.io.IOException;
import java.net.HttpURLConnection;

import io.sekretess.Constants;
import io.sekretess.repository.DbHelper;
import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class BearerAuthenticator implements Authenticator {

    private final AuthState authState;
    private final Context context;

    public BearerAuthenticator(AuthState authState, Context context) {
        this.authState = authState;
        this.context = context;
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED ||
                response.code() == HttpURLConnection.HTTP_FORBIDDEN) {
            if (authState != null) {
                authState.performActionWithFreshTokens(new AuthorizationService(context), this::action);
            }
        }
        return null;
    }

    private void action(String accessToken, String idToken, AuthorizationException ex) {
        try (DbHelper dbHelper = new DbHelper(context)) {
            if (ex != null) {
                Log.e("RefreshTokenService", "Token refresh failed. Removing auth state", ex);
                dbHelper.removeAuthState();
                context.sendBroadcast(new Intent(Constants.EVENT_TOKEN_ISSUE));
            } else {
                Log.i("RefreshTokenService", "Token refreshed. Storing auth state");
                dbHelper.storeAuthState(authState.jsonSerializeString());
            }
        }
    }
}
