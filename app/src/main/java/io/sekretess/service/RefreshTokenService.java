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
    public static final AtomicInteger serviceInstances = new AtomicInteger(0);
    private DbHelper dbHelper;

    private final BroadcastReceiver loggedInEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("RefreshTokenService", "Login event received");
        }
    };
    private CountDownTimer countDownTimer = new CountDownTimer(Long.MAX_VALUE, 10000) {
        @Override
        public void onTick(long millisUntilFinished) {
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
                    Log.i("RefreshTokenService", "Token refresh is not requiring");
                }
            } else {
                Log.e("RefreshTokenService", "Error occurred during refresh token. AuthState is null");
                sendBroadcast(new Intent(Constants.EVENT_REFRESH_TOKEN_FAILED));
            }
        }

        @Override
        public void onFinish() {

        }
    };

    @Override
    public void started(Intent intent) {
        serviceInstances.getAndSet(1);
        this.dbHelper = DbHelper.getInstance(getApplicationContext());

        countDownTimer.start();
        Log.i("RefreshTokenService", "Service Started");
    }


    private void startRefreshTokenService() {
        countDownTimer.start();
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
