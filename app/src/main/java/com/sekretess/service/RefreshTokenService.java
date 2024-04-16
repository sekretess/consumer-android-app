package com.sekretess.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.sekretess.Constants;
import com.sekretess.dto.jwt.Jwt;
import com.sekretess.utils.KeycloakManager;

public class RefreshTokenService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startRefreshTokenService();
        return Service.START_STICKY;
    }


    private void startRefreshTokenService() {
        int refreshTokenInterval = 15;
        SharedPreferences.Editor sharedPreferencesEditor =
                getSharedPreferences(Constants.SEKRETESS_PREFERENCES_NAME, MODE_PRIVATE)
                        .edit();

        new CountDownTimer(Long.MAX_VALUE, 10000) {
            @Override
            public void onTick(long millisUntilFinished) {
                SharedPreferences sharedPreferences = getApplicationContext()
                        .getSharedPreferences(Constants.SEKRETESS_PREFERENCES_NAME, Context.MODE_PRIVATE);
                String jwtStr = sharedPreferences.getString(Constants.PREFERENCES_JWT_PROPERTY_NAME,
                        "");

                if (!jwtStr.isEmpty()) {
                    Jwt currentJwt = Jwt.fromString(jwtStr);
                    Jwt newJwt = KeycloakManager.getInstance().refreshJwt(currentJwt);
                    if (newJwt == null) {
                        sharedPreferencesEditor.remove(Constants.PREFERENCES_JWT_PROPERTY_NAME);
                        sharedPreferencesEditor.apply();
                        sharedPreferencesEditor.commit();
                        LocalBroadcastManager
                                .getInstance(RefreshTokenService.this)
                                .sendBroadcast(new Intent("refresh-token-failed"));

                    } else {
                        sharedPreferencesEditor.putString(Constants.PREFERENCES_JWT_PROPERTY_NAME,
                                newJwt.getJwtStr());
                        sharedPreferencesEditor.apply();
                        sharedPreferencesEditor.commit();
                    }
                }
            }

            @Override
            public void onFinish() {

            }
        }.start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
