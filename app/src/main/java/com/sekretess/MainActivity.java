package com.sekretess;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import androidx.appcompat.app.AppCompatActivity;

import com.auth0.android.jwt.JWT;
import com.sekretess.repository.DbHelper;
import com.sekretess.service.RefreshTokenService;
import com.sekretess.service.SekretessRabbitMqService;
import com.sekretess.service.SignalProtocolService;
import com.sekretess.ui.ChatsActivity;
import com.sekretess.ui.LoginActivity;

import net.openid.appauth.AuthState;

import java.util.Optional;

public class MainActivity extends AppCompatActivity {
    public MainActivity() {

    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isServiceRunning(SignalProtocolService.class)) {
            startForegroundService(new Intent(this, SignalProtocolService.class));
        }
        if (!isServiceRunning(SekretessRabbitMqService.class)) {
            startForegroundService(new Intent(this, SekretessRabbitMqService.class));
        }

        if (!isServiceRunning(RefreshTokenService.class)) {
            startForegroundService(new Intent(this, RefreshTokenService.class));
        }

        Optional<AuthState> authState = restoreState();

        authState.ifPresentOrElse(state -> {
            startActivity(new Intent(this, ChatsActivity.class));
            String username = new JWT(state.getAccessToken()).getClaim(Constants.USERNAME_CLAIM).asString();
            broadcastSuccessfulLogin(username);
        }, () -> startActivity(new Intent(this, LoginActivity.class)));
    }

    private Optional<AuthState> restoreState() {
        DbHelper dbHelper = new DbHelper(getApplicationContext());
        AuthState authState = dbHelper.getAuthState();
        if (authState == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(authState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    private void broadcastSuccessfulLogin(String queueName) {
        Intent intent = new Intent(Constants.EVENT_LOGIN);
        intent.putExtra("queueName", queueName);
        sendBroadcast(intent);
    }
}