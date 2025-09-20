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

public class RefreshTokenService extends SekretessAbstractBackgroundService {
    public static final int REFRESH_TOKEN_SERVICE_NOTIFICATION = 3;

    private boolean running;
    private boolean refreshAfterLogin = false;
    private final CountDownTimer countDownTimer = new CountDownTimer(Long.MAX_VALUE, 10000) {
        @Override
        public void onTick(long millisUntilFinished) {
            Log.i("RefreshTokenService", "On tick...");


            DbHelper dbHelper = new DbHelper(getApplicationContext());
            try {
                AuthState authState = dbHelper.getAuthState();
                if (authState != null) {
                    if (authState.getNeedsTokenRefresh() || refreshAfterLogin) {
                        refreshAfterLogin = false;
                        Log.i("RefreshTokenService", "Refreshing token");
                        authState.performActionWithFreshTokens(new AuthorizationService(getApplicationContext()), (accessToken, idToken, ex) -> {
                            if (ex != null) {
                                Log.e("RefreshTokenService", "Error occurred during refresh token.", ex);
                                dbHelper.removeAuthState();
                                cancelTimer();
                                sendBroadcast(new Intent(Constants.EVENT_TOKEN_ISSUE));
                            } else {
                                Log.i("RefreshTokenService", "Token refreshed");
                                dbHelper.storeAuthState(authState.jsonSerializeString());
                            }
                        });
                        authState.setNeedsTokenRefresh(true);
                    } else {
                        Log.i("RefreshTokenService", "Token refresh is not requiring:" + authState.getAccessToken());
                    }
                    running = true;
                } else {
                    Log.e("RefreshTokenService", "Error occurred during refresh token. AuthState is null");
                    cancelTimer();
                    sendBroadcast(new Intent(Constants.EVENT_TOKEN_ISSUE));
                }
            } finally {

            }
        }

        private void cancelTimer() {
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            running = false;
        }


        @Override
        public void onFinish() {

        }
    };

    @Override
    public void started(Intent intent) {
        Log.i("RefreshTokenService", "Service Started");
        refreshAfterLogin = true;
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("RefreshTokenService", "Login event received");
                if (!running) countDownTimer.start();
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
