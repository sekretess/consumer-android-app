package io.sekretess.utils;

import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

import java.net.HttpURLConnection;

import io.sekretess.dependency.SekretessDependencyProvider;
import io.sekretess.ui.LoginActivity;
import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class BearerAuthenticator implements Authenticator {


    public BearerAuthenticator() {

    }

    @Override
    public Request authenticate(Route route, Response response) {
        Context applicationContext = SekretessDependencyProvider.applicationContext();
        if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED ||
                response.code() == HttpURLConnection.HTTP_FORBIDDEN) {
            Intent intent = new Intent(applicationContext, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ContextCompat.startActivity(applicationContext, intent,
                    null);
        }
        return null;
    }
}
