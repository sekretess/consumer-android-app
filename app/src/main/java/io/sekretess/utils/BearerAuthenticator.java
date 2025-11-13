package io.sekretess.utils;

import android.content.Intent;

import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.net.HttpURLConnection;

import io.sekretess.SekretessApplication;
import io.sekretess.ui.LoginActivity;
import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class BearerAuthenticator implements Authenticator {

    private final SekretessApplication sekretessApplication;

    public BearerAuthenticator(SekretessApplication sekretessApplication) {
        this.sekretessApplication = sekretessApplication;
    }

    @Override
    public Request authenticate(Route route, Response response) {
        if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED ||
                response.code() == HttpURLConnection.HTTP_FORBIDDEN) {
            Intent intent = new Intent(sekretessApplication.getApplicationContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ContextCompat.startActivity(sekretessApplication.getApplicationContext(), intent,
                    null);
        }
        return null;
    }
}
