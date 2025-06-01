package io.sekretess.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import io.sekretess.Constants;
import io.sekretess.repository.DbHelper;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationService;

import java.util.concurrent.atomic.AtomicInteger;

public class RefreshTokenService extends SekretessBackgroundService {
    public static final int REFRESH_TOKEN_SERVICE_NOTIFICATION = 3;

    private boolean running;
    private CountDownTimer countDownTimer = new CountDownTimer(Long.MAX_VALUE, 10000) {
        @Override
        public void onTick(long millisUntilFinished) {
            Log.i("RefreshTokenService", "On tick...");


            DbHelper dbHelper = DbHelper.getInstance(getApplicationContext());
            try {
                AuthState authState = dbHelper.getAuthState();
                if (authState != null) {
                    if (authState.getNeedsTokenRefresh()) {
                        Log.i("RefreshTokenService", "Refreshing token");
                        authState.performActionWithFreshTokens(new AuthorizationService(getApplicationContext()),
                                (accessToken, idToken, ex) -> {
                                    if (ex != null) {
                                        Log.e("RefreshTokenService", "Error occurred during refresh token.", ex);
                                        dbHelper.removeAuthState();
                                        sendBroadcast(new Intent(Constants.EVENT_REFRESH_TOKEN_FAILED));
                                    } else {
                                        Log.i("RefreshTokenService", "Token refreshed");
                                        dbHelper.storeAuthState(authState.jsonSerializeString());
                                    }
                                });
                    } else {
                        Log.i("RefreshTokenService", "Token refresh is not requiring:" + authState.getAccessToken());
                    }
                    running = true;
                } else {
                    Log.e("RefreshTokenService", "Error occurred during refresh token. AuthState is null");
                    running = false;
                    sendBroadcast(new Intent(Constants.EVENT_REFRESH_TOKEN_FAILED));
                    countDownTimer.cancel();
                }
            } finally {
                dbHelper.close();
            }
        }


        @Override
        public void onFinish() {

        }
    };

    @Override
    public void started(Intent intent) {
        Log.i("RefreshTokenService", "Service Started");
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("RefreshTokenService", "Login event received");
                if (!running)
                    countDownTimer.start();
            }
        }, new IntentFilter(Constants.EVENT_LOGIN), RECEIVER_EXPORTED);
    }


    @Override
    public void destroyed() {
        countDownTimer.cancel();
    }

    @Override
    public int getNotificationId() {
        return REFRESH_TOKEN_SERVICE_NOTIFICATION + 10;
    }

    @Override
    public String getChannelId() {
        return "sekretess:refresh-token-service-channel-1";
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
