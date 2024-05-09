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
import com.sekretess.repository.DbHelper;
import com.sekretess.utils.KeycloakManager;

import java.util.concurrent.atomic.AtomicInteger;

public class RefreshTokenService extends SekretessBackgroundService {
    public static final int REFRESH_TOKEN_SERVICE_NOTIFICATION = 3;
    public static final AtomicInteger serviceInstances = new AtomicInteger(0);
    private DbHelper dbHelper;

    @Override
    public void started(Intent intent) {
        serviceInstances.getAndSet(1);
        this.dbHelper = new DbHelper(getApplicationContext());
        startRefreshTokenService();
    }


    private void startRefreshTokenService() {
        new CountDownTimer(Long.MAX_VALUE, 10000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Jwt currentJwt = dbHelper.getJwt();

                if (currentJwt != null) {
                    Jwt newJwt = KeycloakManager.getInstance().refreshJwt(currentJwt);
                    if (newJwt == null) {
                        dbHelper.removeJwt();
                        LocalBroadcastManager
                                .getInstance(RefreshTokenService.this)
                                .sendBroadcast(new Intent(Constants.EVENT_REFRESH_TOKEN_FAILED));

                    } else {
                        dbHelper.storeJwt(newJwt.getJwtStr());
                    }
                }
            }

            @Override
            public void onFinish() {

            }
        }.start();
    }

    @Override
    public void destroyed() {
        serviceInstances.getAndSet(0);
    }

    @Override
    public int getNotificationId() {
        return REFRESH_TOKEN_SERVICE_NOTIFICATION;
    }

    @Override
    public String getChannelId() {
        return "sekretess:refresh-token-service-channel";
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
